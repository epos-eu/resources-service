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

		if(parameters.containsKey("equipmenttypes")) {
			List<String> scienceDomainsParameters = List.of(parameters.get("equipmenttypes").toString().split(","));
			List<Equipment> tempEquipmentList = new ArrayList<Equipment>();
			for(Equipment item : equipmentList) {
				List<String> facilityTypes = new ArrayList<String>();
				categoriesFromDB
				.stream()
				.filter(cat -> cat.getUid().equals(item.getType()))
				.map(EDMCategory::getId)
				.forEach(facilityTypes::add);
				if(!Collections.disjoint(facilityTypes, scienceDomainsParameters)){
					tempEquipmentList.add(item);
				}
			}
			equipmentList = tempEquipmentList;
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