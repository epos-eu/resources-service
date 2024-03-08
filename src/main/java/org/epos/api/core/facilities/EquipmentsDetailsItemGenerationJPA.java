package org.epos.api.core.facilities;

import org.epos.api.utility.Utils;
import org.epos.eposdatamodel.Equipment;
import org.epos.eposdatamodel.Facility;
import org.epos.eposdatamodel.LinkedEntity;
import org.epos.eposdatamodel.Location;
import org.epos.eposdatamodel.State;
import org.epos.handler.dbapi.dbapiimplementation.EquipmentDBAPI;
import org.epos.handler.dbapi.model.*;
import org.epos.handler.dbapi.service.DBService;
import org.epos.library.enums.Anchor;
import org.epos.library.feature.Feature;
import org.epos.library.feature.FeaturesCollection;
import org.epos.library.geometries.Geometry;
import org.epos.library.geometries.Point;
import org.epos.library.geometries.PointCoordinates;
import org.epos.library.geometries.Polygon;
import org.epos.library.propertiestypes.PropertyDataKeys;
import org.epos.library.propertiestypes.PropertyMapKeys;
import org.epos.library.style.EposStyleItem;
import org.epos.library.style.EposStyleObject;
import org.epos.library.style.FontAwesomeMarker;
import org.epos.library.style.Marker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import javax.persistence.EntityManager;

import java.util.*;
import java.util.stream.Collectors;

import static org.epos.handler.dbapi.util.DBUtil.getFromDB;

public class EquipmentsDetailsItemGenerationJPA {

	private static final Logger LOGGER = LoggerFactory.getLogger(EquipmentsDetailsItemGenerationJPA.class);

	public static Object generate(Map<String,Object> parameters) {

		LOGGER.info("Parameters {}", parameters);

		EntityManager em = new DBService().getEntityManager();
		List<EDMFacility> facilitySelectedList = null;
		if(parameters.containsKey("facilityid")) {
			facilitySelectedList = getFromDB(em, EDMFacility.class,
					"facility.findAllByMetaId",
					"METAID", parameters.get("facilityid").toString());
		}else {
			facilitySelectedList = getFromDB(em, EDMFacility.class,
					"facility.findAll");
		}

		List<EDMCategory> categoriesFromDB = getFromDB(em, EDMCategory.class, "EDMCategory.findAll");

		if (facilitySelectedList.stream().noneMatch(facSelected -> facSelected.getState().equals("PUBLISHED")))
			return null;

		List<Equipment> equipmentList = null;

		if(parameters.containsKey("id") && !parameters.get("id").equals("all")) {
			equipmentList = List.of(new EquipmentDBAPI().getByInstanceId(parameters.get("id").toString()));
		}else {
			equipmentList = new EquipmentDBAPI().getAllByState(State.PUBLISHED);
		}

		if(parameters.containsKey("params")) {
			try {
			JsonObject params = Utils.gson.fromJson(parameters.get("params").toString(), JsonObject.class);
			if(params.has("equipmenttypes")) {
				String equipmenttypes = params.get("equipmenttypes").getAsString();
				if(!(equipmenttypes.isBlank() || equipmenttypes.isEmpty())){
					System.out.println("Stil there");
					List<String> scienceDomainsParameters = List.of(equipmenttypes.split(","));
					List<Equipment> tempEquipmentList = new ArrayList<Equipment>();
					for(Equipment item : equipmentList) {
						List<String> facilityTypes = new ArrayList<String>();
						categoriesFromDB
						.stream()
						.filter(cat -> cat.getUid().equals(item.getType()))
						.map(EDMCategory::getName)
						.forEach(facilityTypes::add);
						if(!Collections.disjoint(facilityTypes, scienceDomainsParameters)){
							tempEquipmentList.add(item);
						}
					}
					equipmentList = tempEquipmentList;
				}
			}
			}catch(Exception e) {
				System.err.println("Not valid json, skip filter");
			}
		}



		List<Equipment> returnList = new ArrayList<Equipment>();

		for(Equipment equipment : equipmentList) {
			for(LinkedEntity le : equipment.getIsPartOf()) {
				if(le.getMetaId().contains(facilitySelectedList.get(0).getMetaId())){
					returnList.add(equipment);
				}
			}
		}

		if(parameters.containsKey("format") && parameters.get("format").toString().equals("application/epos.geo+json"))
			return generateAsGeoJson(facilitySelectedList.get(0),categoriesFromDB, returnList);

		em.close();

		return returnList;
	}

	public static FeaturesCollection generateAsGeoJson(EDMFacility facilitySelected, List<EDMCategory> categoriesFromDB, List<Equipment> equipmentList) {

		FeaturesCollection geojson = new FeaturesCollection();

		for(Equipment equipment : equipmentList) {

			Feature feature = new Feature();

			feature.addSimpleProperty("Name", Optional.ofNullable(equipment.getName()).orElse(null));
			feature.addSimpleProperty("Description", Optional.ofNullable(equipment.getDescription()).orElse(null));
			feature.addSimpleProperty("Type", Optional.ofNullable(Optional.ofNullable(categoriesFromDB
					.stream()
					.filter(cat -> cat.getUid().equals(equipment.getType())).map(EDMCategory::getName).collect(Collectors.toList())).get()).orElse(null));
			feature.addSimpleProperty("Category", Optional.ofNullable(equipment.getCategory()).orElse(null));
			feature.addSimpleProperty("Dynamic range", Optional.ofNullable(equipment.getDynamicRange()).orElse(null));
			feature.addSimpleProperty("Filter", Optional.ofNullable(equipment.getFilter()).orElse(null));
			feature.addSimpleProperty("Manufacturer", Optional.ofNullable(equipment.getManufacturer() != null ? equipment.getManufacturer().getUid() : null).orElse(null));
			feature.addSimpleProperty("Orientation", Optional.ofNullable(equipment.getOrientation()).orElse(null));
			feature.addSimpleProperty("Page url", Optional.ofNullable(equipment.getPageURL()).orElse(null));
			feature.addSimpleProperty("Resolution", Optional.ofNullable(equipment.getResolution()).orElse(null));
			feature.addSimpleProperty("Sample period", Optional.ofNullable(equipment.getSamplePeriod()).orElse(null));
			feature.addSimpleProperty("Serial number", Optional.ofNullable(equipment.getSerialNumber()).orElse(null));
			
			for(Location loc : equipment.getSpatialExtent()) {
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
			List<Object> values = new ArrayList<Object>();
			values.add("Name");
			values.add("Description");
			values.add("Type");
			values.add("Category");
			values.add("Dynamic range");
			values.add("Filter");
			values.add("Manufacturer");
			values.add("Orientation");
			values.add("Page url");
			values.add("Resolution");
			values.add("Sample period");
			values.add("Serial number");
			PropertyDataKeys pdk = new PropertyDataKeys(values);
			PropertyMapKeys pmk = new PropertyMapKeys(values);
			feature.addPropertyFromPropertyObject(pdk);
			feature.addPropertyFromPropertyObject(pmk);
			geojson.addFeature(feature);
		}

		return geojson;
	}

}