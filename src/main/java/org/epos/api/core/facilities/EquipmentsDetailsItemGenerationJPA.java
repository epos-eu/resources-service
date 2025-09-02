package org.epos.api.core.facilities;

import abstractapis.AbstractAPI;
import commonapis.LinkedEntityAPI;
import metadataapis.EntityNames;
import model.StatusType;
import org.epos.api.routines.DatabaseConnections;
import org.epos.api.utility.Utils;
import org.epos.eposdatamodel.*;
import org.epos.library.feature.Feature;
import org.epos.library.feature.FeaturesCollection;
import org.epos.library.geometries.Geometry;
import org.epos.library.geometries.Point;
import org.epos.library.geometries.PointCoordinates;
import org.epos.library.geometries.Polygon;
import org.epos.library.propertiestypes.PropertyDataKeys;
import org.epos.library.propertiestypes.PropertyMapKeys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;

import java.util.*;
import java.util.stream.Collectors;

public class EquipmentsDetailsItemGenerationJPA {

	private static final Logger LOGGER = LoggerFactory.getLogger(EquipmentsDetailsItemGenerationJPA.class);

	public static Object generate(Map<String,Object> parameters) {

		LOGGER.info("Parameters {}", parameters);

		List<Facility> facilitySelectedList = null;
		if(parameters.containsKey("facilityid")) {
			facilitySelectedList = List.of((Facility)AbstractAPI.retrieveAPI(EntityNames.FACILITY.name()).retrieve(parameters.get("facilityid").toString()));
		}else {
			facilitySelectedList = (List<Facility>) AbstractAPI.retrieveAPI(EntityNames.FACILITY.name()).retrieveAll();
		}

		List<Category> categoriesFromDB = (List<Category>) AbstractAPI.retrieveAPI(EntityNames.CATEGORY.name()).retrieveAll();

		List<Equipment> equipmentList = null;

		if(parameters.containsKey("id") && !parameters.get("id").equals("all")) {
			equipmentList = List.of( (org.epos.eposdatamodel.Equipment) AbstractAPI.retrieveAPI(EntityNames.EQUIPMENT.name()).retrieve(parameters.get("id").toString()));
		}else {
			equipmentList = (List<Equipment>) AbstractAPI.retrieveAPI(EntityNames.EQUIPMENT.name()).retrieveAll();
		}

		if(parameters.containsKey("params")) {
			try {
			JsonObject params = Utils.gson.fromJson(parameters.get("params").toString(), JsonObject.class);
			if(params.has("equipmenttypes")) {
				String equipmenttypes = params.get("equipmenttypes").getAsString();
				if(!equipmenttypes.isBlank()){
					List<String> scienceDomainsParameters = List.of(equipmenttypes.split(","));
					List<Equipment> tempEquipmentList = new ArrayList<Equipment>();
					for(Equipment item : equipmentList) {
						List<String> facilityTypes = new ArrayList<String>();
						categoriesFromDB
						.stream()
						.filter(cat -> cat.getUid().equals(item.getType()))
						.map(Category::getName)
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
			if(equipment.getIsPartOf()!=null)
				for(LinkedEntity le : equipment.getIsPartOf()) {
					if(le.getMetaId().contains(facilitySelectedList.get(0).getMetaId())){
						returnList.add(equipment);
					}
				}
		}

		if(parameters.containsKey("format") && parameters.get("format").toString().equals("application/epos.geo+json"))
			return generateAsGeoJson(facilitySelectedList.get(0),categoriesFromDB, returnList);

		return returnList;
	}

	public static FeaturesCollection generateAsGeoJson(Facility facilitySelected, List<Category> categoriesFromDB, List<Equipment> equipmentList) {

		FeaturesCollection geojson = new FeaturesCollection();

		for(Equipment equipment : equipmentList) {

			Feature feature = new Feature();

			feature.addSimpleProperty("Equipment name", equipment.getName());
			feature.addSimpleProperty("Description", equipment.getDescription());
			feature.addSimpleProperty("Type", Optional.of(Optional.of(categoriesFromDB
					.stream()
					.filter(cat -> cat.getUid().equals(equipment.getType())).map(Category::getName).collect(Collectors.toList())).get()).orElse(null));
			feature.addSimpleProperty("Category", equipment.getCategory());
			feature.addSimpleProperty("Dynamic range", equipment.getDynamicRange());
			feature.addSimpleProperty("Filter", equipment.getFilter());
			feature.addSimpleProperty("Manufacturer", equipment.getManufacturer() != null ? equipment.getManufacturer().getUid() : null);
			feature.addSimpleProperty("Orientation", equipment.getOrientation());
			feature.addSimpleProperty("Page url", equipment.getPageURL());
			feature.addSimpleProperty("Resolution", equipment.getResolution());
			feature.addSimpleProperty("Sample period", equipment.getSamplePeriod());
			feature.addSimpleProperty("Serial number", equipment.getSerialNumber());
			
			for(LinkedEntity locLe : equipment.getSpatialExtent()) {
				Location loc = (Location) LinkedEntityAPI.retrieveFromLinkedEntity(locLe);
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
			values.add("Equipment name");
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
