package org.epos.api.core.facilities;

import org.epos.eposdatamodel.Equipment;
import org.epos.eposdatamodel.Facility;
import org.epos.eposdatamodel.LinkedEntity;
import org.epos.eposdatamodel.Location;
import org.epos.eposdatamodel.State;
import org.epos.handler.dbapi.dbapiimplementation.EquipmentDBAPI;
import org.epos.handler.dbapi.model.*;
import org.epos.handler.dbapi.service.DBService;
import org.epos.library.feature.Feature;
import org.epos.library.feature.FeaturesCollection;
import org.epos.library.geometries.Geometry;
import org.epos.library.geometries.Point;
import org.epos.library.geometries.PointCoordinates;
import org.epos.library.geometries.Polygon;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;

import java.util.*;
import java.util.stream.Collectors;

import static org.epos.handler.dbapi.util.DBUtil.getFromDB;

public class EquipmentsDetailsItemGenerationJPA {

	private static final Logger LOGGER = LoggerFactory.getLogger(EquipmentsDetailsItemGenerationJPA.class);

	public static Object generate(Map<String,Object> parameters) {

		LOGGER.info("Parameters {}", parameters);

		EntityManager em = new DBService().getEntityManager();

		List<EDMFacility> facilitySelectedList = getFromDB(em, EDMFacility.class,
				"facility.findAllByMetaId",
				"METAID", parameters.get("id"));

		List<EDMCategory> categoriesFromDB = getFromDB(em, EDMCategory.class, "EDMCategory.findAll");


		System.out.println(facilitySelectedList.toString());

		if (facilitySelectedList.stream().noneMatch(facSelected -> facSelected.getState().equals("PUBLISHED")))
			return null;

		System.out.println(facilitySelectedList.toString());

		List<Equipment> equipmentList = new EquipmentDBAPI().getAllByState(State.PUBLISHED);
		List<Equipment> returnList = new ArrayList<Equipment>();

		for(Equipment equipment : equipmentList) {
			for(LinkedEntity le : equipment.getIsPartOf()) {
				if(le.getMetaId().contains(facilitySelectedList.get(0).getMetaId())){
					returnList.add(equipment);
				}
			}
		}

		System.out.println(returnList.toString());

		if(parameters.containsKey("format") && parameters.get("format").toString().equals("application/epos.geo+json"))
			return generateAsGeoJson(facilitySelectedList.get(0),categoriesFromDB, returnList);

		em.close();

		return returnList;
	}

	public static FeaturesCollection generateAsGeoJson(EDMFacility facilitySelected, List<EDMCategory> categoriesFromDB, List<Equipment> equipmentList) {

		FeaturesCollection geojson = new FeaturesCollection();


		for(Equipment equipment : equipmentList) {

			Feature feature = new Feature();

			//COMMON FOR FACILITY
			feature.addSimpleProperty("Facility name", Optional.ofNullable(facilitySelected.getTitle()).orElse(""));
			feature.addSimpleProperty("Facility description", Optional.ofNullable(facilitySelected.getDescription()).orElse(""));
			feature.addSimpleProperty("Facility type", Optional.ofNullable(categoriesFromDB
					.stream()
					.filter(cat -> cat.getUid().equals(facilitySelected.getType())).map(EDMCategory::getName).collect(Collectors.toList())).get().toString());

			for(EDMFacilitySpatial loc : facilitySelected.getFacilitySpatialsByInstanceId()) {
				String location = loc.getLocation();
				boolean isPoint = location.contains("POINT");
				location = location.replaceAll("POLYGON", "").replaceAll("POINT", "").replaceAll("\\(", "").replaceAll("\\)", "");
				String[] coordinates = location.split("\\,");
				Geometry geometry = null;

				if(isPoint) {
					geometry = new Point();
					for(String coo : coordinates) {
						String[] cooz = coo.split(" ");
						if(cooz.length==2) {
							((Point) geometry).setCoordinates(new PointCoordinates(Double.parseDouble(cooz[0]), Double.parseDouble(cooz[1])));
						}else {
							((Point) geometry).setCoordinates(new PointCoordinates(Double.parseDouble(cooz[1]), Double.parseDouble(cooz[2])));
						}
					}
				}else {
					geometry = new Polygon();
					ArrayList<PointCoordinates> deep = new ArrayList<PointCoordinates>();
					for(String coo : coordinates) {
						String[] cooz = coo.split(" ");
						if(cooz.length==2) {
							deep.add(new PointCoordinates(Double.parseDouble(cooz[0]), Double.parseDouble(cooz[1])));
						}else {
							deep.add(new PointCoordinates(Double.parseDouble(cooz[1]), Double.parseDouble(cooz[2])));
						}
					}
					((Polygon) geometry).setStartingPoint(deep.get(0));
					for(int i = 1; i<deep.size();i++)
						((Polygon) geometry).addAdditionalPoint(deep.get(i));	
				}
				feature.setGeometry(geometry);
			}


			feature.addSimpleProperty("Name", Optional.ofNullable(equipment.getName()).orElse(""));
			feature.addSimpleProperty("Description", Optional.ofNullable(equipment.getDescription()).orElse(""));
			feature.addSimpleProperty("Type", Optional.ofNullable(Optional.ofNullable(categoriesFromDB
					.stream()
					.filter(cat -> cat.getUid().equals(equipment.getType())).map(EDMCategory::getName).collect(Collectors.toList())).get().toString()).orElse(""));
			feature.addSimpleProperty("Category", Optional.ofNullable(equipment.getCategory().toString()).orElse(""));
			feature.addSimpleProperty("Dynamic range", Optional.ofNullable(equipment.getDynamicRange()).orElse(""));
			feature.addSimpleProperty("Filter", Optional.ofNullable(equipment.getFilter()).orElse(""));
			feature.addSimpleProperty("Manufacturer", Optional.ofNullable(equipment.getManufacturer() != null ? equipment.getManufacturer().getUid() : "").orElse(""));
			feature.addSimpleProperty("Orientation", Optional.ofNullable(equipment.getOrientation()).orElse(""));
			feature.addSimpleProperty("Page url", Optional.ofNullable(equipment.getPageURL()).orElse(""));
			feature.addSimpleProperty("Resolution", Optional.ofNullable(equipment.getResolution()).orElse(""));
			feature.addSimpleProperty("Sample period", Optional.ofNullable(equipment.getSamplePeriod()).orElse(""));
			feature.addSimpleProperty("Serial number", Optional.ofNullable(equipment.getSerialNumber()).orElse(""));

			geojson.addFeature(feature);
		}

		return geojson;
	}

}