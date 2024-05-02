package org.epos.api.core.filtersearch;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.epos.api.beans.DataServiceProvider;
import org.epos.api.core.DataServiceProviderGeneration;
import org.epos.api.utility.BBoxToPolygon;
import org.epos.eposdatamodel.*;
import org.epos.handler.dbapi.model.EDMCategory;
import org.epos.handler.dbapi.model.EDMDataproduct;
import org.epos.handler.dbapi.model.EDMDataproductCategory;
import org.epos.handler.dbapi.model.EDMDataproductSpatial;
import org.epos.handler.dbapi.model.EDMDistribution;
import org.epos.handler.dbapi.model.EDMDistributionDescription;
import org.epos.handler.dbapi.model.EDMDistributionTitle;
import org.epos.handler.dbapi.model.EDMIsDistribution;
import org.epos.handler.dbapi.model.EDMPublisher;
import org.epos.handler.dbapi.model.EDMWebservice;
import org.epos.handler.dbapi.model.EDMWebserviceCategory;
import org.epos.handler.dbapi.model.EDMWebserviceSpatial;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class DistributionFilterSearch {

	private static final Logger LOGGER = LoggerFactory.getLogger(DistributionFilterSearch.class); 


	private static final String NORTHEN_LAT  = "epos:northernmostLatitude";
	private static final String SOUTHERN_LAT  = "epos:southernmostLatitude";
	private static final String WESTERN_LON  = "epos:westernmostLongitude";
	private static final String EASTERN_LON  = "epos:easternmostLongitude";

	private static final String PARAMETER__SCIENCE_DOMAIN = "sciencedomains";
	private static final String PARAMETER__SERVICE_TYPE = "servicetypes";


	public static List<EDMDataproduct> doFilters(List<EDMDataproduct> datasetList, Map<String,Object> parameters) {

		datasetList = filterByFullText(datasetList, parameters);
		datasetList = filterByKeywords(datasetList, parameters);
		datasetList = filterByOrganizations(datasetList, parameters);
		datasetList = filterByDateRange(datasetList, checkTemporalExtent(parameters));
		datasetList = filterByBoundingBox(datasetList, parameters);
		datasetList = filterByScienceDomain(datasetList, parameters);
		datasetList = filterByServiceType(datasetList, parameters);

		return datasetList;
	}

	private static List<EDMDataproduct> filterByScienceDomain(List<EDMDataproduct> datasetList, Map<String,Object> parameters) {
		if(parameters.containsKey(PARAMETER__SCIENCE_DOMAIN)) {
			ArrayList<EDMDataproduct> tempDatasetList = new ArrayList<>();
			List<String> scienceDomainsParameters = List.of(parameters.get(PARAMETER__SCIENCE_DOMAIN).toString().split(","));
			datasetList.forEach(dataproduct -> {
				if (Objects.nonNull(dataproduct.getDataproductCategoriesByInstanceId()) && !dataproduct.getDataproductCategoriesByInstanceId().isEmpty()){
					List<String> scienceDomainOfDataproduct = dataproduct.getDataproductCategoriesByInstanceId().stream()
							.map(EDMDataproductCategory::getCategoryByCategoryId)
							.filter(Objects::nonNull)
							.map(EDMCategory::getId)
							.collect(Collectors.toList());

					if(!Collections.disjoint(scienceDomainOfDataproduct, scienceDomainsParameters)){
						tempDatasetList.add(dataproduct);
					}
				}
			});
			datasetList = tempDatasetList;
		}
		return datasetList;
	}


	private static List<EDMDataproduct> filterByServiceType(List<EDMDataproduct> datasetList, Map<String,Object> parameters) {
		if (parameters.containsKey(PARAMETER__SERVICE_TYPE)) {
			ArrayList<EDMDataproduct> tempDatasetList = new ArrayList<>();
			List<String> serviceTypesParameters = List.of(parameters.get(PARAMETER__SERVICE_TYPE).toString().split(","));

			datasetList.forEach(dataproduct -> {

				List<EDMWebservice> webservices = dataproduct.getIsDistributionsByInstanceId().stream()
						.map(EDMIsDistribution::getDistributionByInstanceDistributionId)
						.filter(Objects::nonNull)
						.map(EDMDistribution::getWebserviceByAccessService)
						.filter(Objects::nonNull)
						.collect(Collectors.toList());

				for (EDMWebservice webservice : webservices) {
					if (Objects.nonNull(webservice.getWebserviceCategoriesByInstanceId()) && !webservice.getWebserviceCategoriesByInstanceId().isEmpty()) {
						List<String> serviceTypesOfWebservice = webservice.getWebserviceCategoriesByInstanceId().stream()
								.map(EDMWebserviceCategory::getCategoryByCategoryId)
								.filter(Objects::nonNull)
								.map(EDMCategory::getId)
								.collect(Collectors.toList());

						if (!Collections.disjoint(serviceTypesOfWebservice, serviceTypesParameters)) {
							tempDatasetList.add(dataproduct);
							break;
						}
					}
				}

			});
			datasetList = tempDatasetList;
		}
		return datasetList;
	}


	private static List<EDMDataproduct> filterByBoundingBox(List<EDMDataproduct> datasetList, Map<String,Object> parameters) {
		//check if the bbox passed inside the parameters is complete, if not exit and return the whole list of dataproduct
		if (!parameters.containsKey(NORTHEN_LAT)
				|| !parameters.containsKey(SOUTHERN_LAT)
				|| !parameters.containsKey(WESTERN_LON)
				|| !parameters.containsKey(EASTERN_LON))
			return datasetList;
		GeometryFactory geometryFactory = new GeometryFactory();
		WKTReader reader = new WKTReader( geometryFactory );
		try {
			//try to parse the bbox
			final Geometry inputGeometry = reader.read(BBoxToPolygon.transform(parameters));
			//check if the bbox is parsed
			if(inputGeometry!=null) {
				//temporary dataproduct list, it will collect the dataproduct which are, even in part, contained inside the bbox
				ArrayList<EDMDataproduct> tempDatasetList = new ArrayList<>();
				//utility set to contain the all the uid of the selected dataproduct to avoid duplicates
				Set<String> uidSet = new HashSet<>();
				//iterate over every dataproduct
				for (EDMDataproduct ds : datasetList) {
					//if the uid belong to an already selected dataproduct just skip the iteration
					if(uidSet.contains(ds.getMetaId())) continue;
					//iterate over every distribution related to the dataproduct taken into account
					ds.getIsDistributionsByInstanceId().stream()
					.map(EDMIsDistribution::getDistributionByInstanceDistributionId)
					.map(EDMDistribution::getWebserviceByAccessService)
					.filter(Objects::nonNull)
					//iterate over the webservice related to the distribution
					.forEach(ws -> {
						if(Objects.nonNull(ws.getWebserviceSpatialsByInstanceId())){
							//loop on every spatial of the webservice
							for (EDMWebserviceSpatial wsSpatial : ws.getWebserviceSpatialsByInstanceId()) {
								try {
									//parse the spatial of the webservice
									Geometry dsGeometry = reader.read(wsSpatial.getLocation());
									//if the dataproduct hasn't been selected yet and the spatial of the webservice
									//intersect with the bbox, the dataproduct is selected.
									if (!uidSet.contains(ds.getMetaId()) && inputGeometry.intersects(dsGeometry)) {
										tempDatasetList.add(ds);
										uidSet.add(ds.getMetaId());
									}
								} catch (ParseException e) {
									LOGGER.error("Error occurs during BBOX dataproduct parsing", e);
								}
							}
						}
					});
					//if the uid belong to an already selected dataproduct just skip the iteration
					if(uidSet.contains(ds.getMetaId())) continue;
					//same operation done before for the webservice's spatial but now for the dataprodut spatial
					if(ds.getDataproductSpatialsByInstanceId() != null){
						for(EDMDataproductSpatial dsSpatial : ds.getDataproductSpatialsByInstanceId()){
							Geometry dsGeometry = reader.read(dsSpatial.getLocation());
							if (!uidSet.contains(ds.getMetaId()) && inputGeometry.intersects(dsGeometry)) {
								tempDatasetList.add(ds);
								uidSet.add(ds.getMetaId());
							}
						}
					}
					//if the uid belong to an already selected dataproduct just skip the iteration
					if(uidSet.contains(ds.getMetaId())) continue;
					//if the dataproduct doesn't have any spatial information it is taken
					if ( ds.getDataproductSpatialsByInstanceId() == null || ds.getDataproductSpatialsByInstanceId().isEmpty() ) {
						tempDatasetList.add(ds);
						uidSet.add(ds.getMetaId());
					}
				}
				//replace the old dataset list with the new temporary filtered dataproduct list
				datasetList = tempDatasetList;
			}
		} catch (org.locationtech.jts.io.ParseException e) {
			LOGGER.error("Error occurs during BBOX input parsing ",e);
		}
		return datasetList;
	}

	private static List<EDMDataproduct> filterByDateRange(List<EDMDataproduct> datasetList, PeriodOfTime temporal) {
		if(temporal.getStartDate()!=null && temporal.getEndDate()!=null) {
			ArrayList<EDMDataproduct> tempDatasetList = new ArrayList<>();
			datasetList.forEach(ds -> {
				List<PeriodOfTime> dsTemporalList = ds.getDataproductTemporalsByInstanceId() != null ?
						ds.getDataproductTemporalsByInstanceId().stream()
						.map(elem -> {
							PeriodOfTime periodOfTime = new PeriodOfTime();
							periodOfTime.setStartDate(
									elem.getStartdate() != null ?
											elem.getStartdate().toLocalDateTime() : null
									);
							periodOfTime.setEndDate(
									elem.getEnddate() != null ?
											elem.getEnddate().toLocalDateTime() : null
									);
							return periodOfTime;
						}).collect(Collectors.toList())
						: new ArrayList<>();
				if( dsTemporalList.size() > 0) {
					if((dsTemporalList.get(0).getStartDate() == null || temporal.getEndDate() == null || dsTemporalList.get(0).getStartDate().isBefore(temporal.getEndDate()))
							&& (temporal.getStartDate() == null || dsTemporalList.get(0).getEndDate() == null || temporal.getStartDate().isBefore(dsTemporalList.get(0).getEndDate()))
							&& (dsTemporalList.get(0).getStartDate() == null || dsTemporalList.get(0).getEndDate() == null || dsTemporalList.get(0).getStartDate().isBefore(dsTemporalList.get(0).getEndDate()))
							&& (temporal.getStartDate() == null || temporal.getEndDate() == null || temporal.getStartDate().isBefore(temporal.getEndDate())))
						tempDatasetList.add(ds);
				}
			});
			datasetList = tempDatasetList;
		}
		return datasetList;
	}

	private static List<EDMDataproduct> filterByOrganizations(List<EDMDataproduct> datasetList, Map<String,Object> parameters) {
		if(parameters.containsKey("organisations")) {
			List<String> organisations = Arrays.asList(parameters.get("organisations").toString().split(","));

			HashSet<EDMDataproduct> tempDatasetList = new HashSet<>();
			datasetList.forEach(ds -> {

				List<DataServiceProvider> providers = new ArrayList<DataServiceProvider>();
				providers.addAll(DataServiceProviderGeneration.getProviders(ds.getPublishersByInstanceId().stream().map(EDMPublisher::getEdmEntityIdByMetaOrganizationId).collect(Collectors.toList())));
				providers.addAll(DataServiceProviderGeneration.getProviders(ds.getIsDistributionsByInstanceId().stream()
						.map(EDMIsDistribution::getDistributionByInstanceDistributionId)
						.filter(Objects::nonNull)
						.map(EDMDistribution::getWebserviceByAccessService)
						.filter(Objects::nonNull)
						.map(EDMWebservice::getEdmEntityIdByProvider)
						.filter(Objects::nonNull).collect(Collectors.toList())));



				List<String> value = new ArrayList<>();


				providers.forEach(resource->{
					value.add(resource.getInstanceid());
					//LOGGER.info("Number of related retrieved "+resource.getRelatedDataProvider().size());
					resource.getRelatedDataProvider().forEach(related ->{
						value.add(related.getInstanceid());
					});
					//LOGGER.info("Number of related retrieved "+resource.getRelatedDataServiceProvider().size());
					resource.getRelatedDataServiceProvider().forEach(related ->{
						value.add(related.getInstanceid());
					});
				});

				if(!Collections.disjoint(organisations, value)){
					tempDatasetList.add(ds);
				}

			});
			datasetList = new ArrayList<>(tempDatasetList);
		}
		return datasetList;
	}

	private static List<EDMDataproduct> filterByKeywords(List<EDMDataproduct> datasetList, Map<String,Object> parameters) {
		if(parameters.containsKey("keywords")) {
			ArrayList<EDMDataproduct> tempDatasetList = new ArrayList<>();
			String[] keywords = parameters.get("keywords").toString().split(",");
			datasetList.forEach(ds -> {
				if(!Collections.disjoint(Arrays.stream(ds.getKeywords().split(",")).map(String::toLowerCase).map(String::trim).collect(Collectors.toList()), Arrays.asList(keywords))){
					tempDatasetList.add(ds);
				}
			});
			datasetList = tempDatasetList;
		}
		return datasetList;
	}

	private static List<EDMDataproduct> filterByFullText(List<EDMDataproduct> datasetList, Map<String,Object> parameters) {
		if(parameters.containsKey("q")) {
			HashSet<EDMDataproduct> tempDatasetList = new HashSet<>();
			String[] qs = parameters.get("q").toString().toLowerCase().split(",");
			
			System.out.println(Arrays.toString(qs));

			for (EDMDataproduct edmDataproduct : datasetList) {
				Map<String, Boolean> qSMap = Arrays.stream(qs)
						.collect(Collectors.toMap(
								key -> key, value -> Boolean.FALSE
								));

				if(Objects.nonNull(edmDataproduct.getKeywords())){
					List<String> dataproductKeywords = Arrays.stream(edmDataproduct.getKeywords().split(","))
							.map(String::toLowerCase)
							.map(String::trim)
							.collect(Collectors.toList());

					for (String q : qSMap.keySet()) {
						if (dataproductKeywords.contains(q)) qSMap.put(q, Boolean.TRUE);
					}
				}
				if(Objects.nonNull(edmDataproduct.getUid())){
					for (String q : qSMap.keySet()) {
						if (edmDataproduct.getUid().toLowerCase().contains(q)) qSMap.put(q, Boolean.TRUE);
					}
				}


				if (edmDataproduct.getIsDistributionsByInstanceId() != null && !edmDataproduct.getIsDistributionsByInstanceId().isEmpty()) {
					for (EDMDistribution edmDistribution : edmDataproduct.getIsDistributionsByInstanceId().stream().map(EDMIsDistribution::getDistributionByInstanceDistributionId).collect(Collectors.toList())) {

						//distribution title
						if (edmDistribution.getDistributionTitlesByInstanceId() != null) {
							edmDistribution.getDistributionTitlesByInstanceId()
							.stream().map(EDMDistributionTitle::getTitle)
							.forEach(title ->
							{
								for (String q : qSMap.keySet()) {
									if (title.toLowerCase().contains(q)) {
										qSMap.put(q, Boolean.TRUE);
									}
								}
							}
									);
						}
						
						if(Objects.nonNull(edmDistribution.getUid())){
							for (String q : qSMap.keySet()) {
								if (edmDistribution.getUid().toLowerCase().contains(q)) qSMap.put(q, Boolean.TRUE);
							}
						}

						//distribution description
						if (edmDistribution.getDistributionDescriptionsByInstanceId() != null) {
							edmDistribution.getDistributionDescriptionsByInstanceId()
							.stream().map(EDMDistributionDescription::getDescription)
							.forEach(description ->
							{
								for (String q : qSMap.keySet()) {
									if (description.toLowerCase().contains(q)) {
										qSMap.put(q, Boolean.TRUE);
									}
								}
							}
									);
						}

						//webservice
						if (edmDistribution.getWebserviceByAccessService() != null) {
							EDMWebservice edmWebservice = edmDistribution.getWebserviceByAccessService();

							if(Objects.nonNull(edmWebservice.getUid())){
								for (String q : qSMap.keySet()) {
									if (edmWebservice.getUid().toLowerCase().contains(q)) qSMap.put(q, Boolean.TRUE);
								}
							}
							
							//webservice title
							if (edmWebservice.getName() != null) {
								for (String q : qSMap.keySet()) {
									if (edmWebservice.getName().toLowerCase().contains(q)) {
										qSMap.put(q, Boolean.TRUE);
									}
								}
							}

							//webservice description
							if (edmWebservice.getDescription() != null) {
								for (String q : qSMap.keySet()) {
									if (edmWebservice.getDescription().toLowerCase().contains(q)) {
										qSMap.put(q, Boolean.TRUE);
									}
								}
							}

							if(Objects.nonNull(edmWebservice.getKeywords())){
								List<String> webserviceKeywords = Arrays.stream(edmWebservice.getKeywords().split(","))
										.map(String::toLowerCase)
										.map(String::trim)
										.collect(Collectors.toList());

								for (String q : qSMap.keySet()) {
									if (webserviceKeywords.contains(q)) qSMap.put(q, Boolean.TRUE);
								}
							}

						}

					}
				}

				//take a dataproduct onlly if every element of the freetext search is satisfied
				if (qSMap.values().stream().allMatch(b -> b)) tempDatasetList.add(edmDataproduct);
			}

			datasetList = new ArrayList<>(tempDatasetList);
		}

		return datasetList;

	}


	public static PeriodOfTime checkTemporalExtent(Map<String,Object> parameters) {
		PeriodOfTime temporal = new PeriodOfTime();
		try {
			if(parameters.containsKey("schema:startDate")) temporal.setStartDate(convertToLocalDateTimeViaSqlTimestamp(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(parameters.get("schema:startDate").toString().replace("T", " ").replace("Z", ""))));
			if(parameters.containsKey("schema:endDate")) temporal.setEndDate(convertToLocalDateTimeViaSqlTimestamp(new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(parameters.get("schema:endDate").toString().replace("T", " ").replace("Z", ""))));
		} catch (java.text.ParseException e) {
			LOGGER.error("Error occurs during search caused by Date parsing {}",e);
		}
		return temporal;
	}

	public static LocalDateTime convertToLocalDateTimeViaSqlTimestamp(Date dateToConvert) {
		return new java.sql.Timestamp(
				dateToConvert.getTime()).toLocalDateTime();
	}

}
