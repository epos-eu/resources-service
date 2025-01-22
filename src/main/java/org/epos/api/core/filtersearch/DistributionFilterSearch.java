package org.epos.api.core.filtersearch;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.epos.api.beans.DataServiceProvider;
import org.epos.api.core.DataServiceProviderGeneration;
import org.epos.api.routines.DatabaseConnections;
import org.epos.api.utility.BBoxToPolygon;
import org.epos.eposdatamodel.*;
import org.epos.eposdatamodel.DataProduct;
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


	public static List<DataProduct> doFilters(List<DataProduct> datasetList, Map<String,Object> parameters) {

		datasetList = filterByFullText(datasetList, parameters);
		datasetList = filterByKeywords(datasetList, parameters);
		datasetList = filterByOrganizations(datasetList, parameters);
		datasetList = filterByDateRange(datasetList, checkTemporalExtent(parameters));
		datasetList = filterByBoundingBox(datasetList, parameters);
		datasetList = filterByScienceDomain(datasetList, parameters);
		datasetList = filterByServiceType(datasetList, parameters);

		return datasetList;
	}

	private static List<DataProduct> filterByScienceDomain(List<DataProduct> datasetList, Map<String,Object> parameters) {
		if(parameters.containsKey(PARAMETER__SCIENCE_DOMAIN)) {
			ArrayList<DataProduct> tempDatasetList = new ArrayList<>();
			List<String> scienceDomainsParameters = List.of(parameters.get(PARAMETER__SCIENCE_DOMAIN).toString().split(","));
			datasetList.forEach(dataproduct -> {
				if (Objects.nonNull(dataproduct.getCategory()) && !dataproduct.getCategory().isEmpty()){
					List<String> scienceDomainOfDataproduct = new ArrayList<>();
					for(LinkedEntity item : dataproduct.getCategory()){
						Optional<Category> category = DatabaseConnections.getInstance().getCategoryList().stream().filter(obj -> obj.getInstanceId().equals(item.getInstanceId())).findFirst();
                        category.ifPresent(value -> scienceDomainOfDataproduct.add(value.getName()));
					}

					if(!Collections.disjoint(scienceDomainOfDataproduct, scienceDomainsParameters)){
						tempDatasetList.add(dataproduct);
					}
				}
			});
			datasetList = tempDatasetList;
		}
		return datasetList;
	}


	private static List<DataProduct> filterByServiceType(List<DataProduct> datasetList, Map<String,Object> parameters) {
		if (parameters.containsKey(PARAMETER__SERVICE_TYPE)) {
			ArrayList<DataProduct> tempDatasetList = new ArrayList<>();
			List<String> serviceTypesParameters = List.of(parameters.get(PARAMETER__SERVICE_TYPE).toString().split(","));

			datasetList.forEach(dataproduct -> {
				for (LinkedEntity distribution : dataproduct.getDistribution()) {
					Optional<Distribution> distribution1 = DatabaseConnections.getInstance().getDistributionList().stream().filter(obj -> obj.getInstanceId().equals(distribution.getInstanceId())).findFirst();
					if(distribution1.isPresent()){
						for (LinkedEntity accessService : distribution1.get().getAccessService()) {
							Optional<WebService> webservice = DatabaseConnections.getInstance().getWebServiceList().stream().filter(obj -> obj.getInstanceId().equals(accessService.getInstanceId())).findFirst();
							if(webservice.isPresent()){
								if (Objects.nonNull(webservice.get().getCategory()) && !webservice.get().getCategory().isEmpty()){
									List<String> serviceTypesOfWebservice = new ArrayList<>();
									for(LinkedEntity item : dataproduct.getCategory()){
										Optional<Category> category = DatabaseConnections.getInstance().getCategoryList().stream().filter(obj -> obj.getInstanceId().equals(item.getInstanceId())).findFirst();
										category.ifPresent(value -> serviceTypesOfWebservice.add(value.getName()));
									}
									if(!Collections.disjoint(serviceTypesOfWebservice, serviceTypesParameters)){
										tempDatasetList.add(dataproduct);
									}
								}
							}
						}
					}
				}
			});
			datasetList = tempDatasetList;
		}
		return datasetList;
	}


	private static List<DataProduct> filterByBoundingBox(List<DataProduct> datasetList, Map<String,Object> parameters) {
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
				ArrayList<DataProduct> tempDatasetList = new ArrayList<>();
				//utility set to contain the all the uid of the selected dataproduct to avoid duplicates
				Set<String> uidSet = new HashSet<>();
				//iterate over every dataproduct
				for (DataProduct ds : datasetList) {
					//if the uid belong to an already selected dataproduct just skip the iteration
					if(uidSet.contains(ds.getMetaId())) continue;
					//iterate over every distribution related to the dataproduct taken into account
					for (LinkedEntity distribution : ds.getDistribution()) {
						Optional<Distribution> distribution1 = DatabaseConnections.getInstance().getDistributionList().stream().filter(obj -> obj.getInstanceId().equals(distribution.getInstanceId())).findFirst();
						if(distribution1.isPresent()){
							for (LinkedEntity accessService : distribution1.get().getAccessService()) {
								Optional<WebService> ws = DatabaseConnections.getInstance().getWebServiceList().stream().filter(obj -> obj.getInstanceId().equals(accessService.getInstanceId())).findFirst();
								if(ws.isPresent()){
									for (LinkedEntity wsSpatialLe : ws.get().getSpatialExtent()) {
										Optional<Location> wsSpatial = DatabaseConnections.getInstance().getLocationList().stream().filter(obj -> obj.getInstanceId().equals(wsSpatialLe.getInstanceId())).findFirst();
										if(wsSpatial.isPresent()){
											try {
												//parse the spatial of the webservice
												Geometry dsGeometry = reader.read(wsSpatial.get().getLocation());
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
								}
							}
						}
					}
					//if the uid belong to an already selected dataproduct just skip the iteration
					if(uidSet.contains(ds.getMetaId())) continue;
					//same operation done before for the webservice's spatial but now for the dataprodut spatial
					if(ds.getSpatialExtent() != null){
						for (LinkedEntity wsSpatialLe : ds.getSpatialExtent()) {
							Optional<Location> wsSpatial = DatabaseConnections.getInstance().getLocationList().stream().filter(obj -> obj.getInstanceId().equals(wsSpatialLe.getInstanceId())).findFirst();
							if(wsSpatial.isPresent()){
								try {
									//parse the spatial of the webservice
									Geometry dsGeometry = reader.read(wsSpatial.get().getLocation());
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
					}
					//if the uid belong to an already selected dataproduct just skip the iteration
					if(uidSet.contains(ds.getMetaId())) continue;
					//if the dataproduct doesn't have any spatial information it is taken
					if ( ds.getSpatialExtent() == null || ds.getSpatialExtent().isEmpty() ) {
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

	private static List<DataProduct> filterByDateRange(List<DataProduct> datasetList, PeriodOfTime temporal) {
		if(temporal.getStartDate()!=null && temporal.getEndDate()!=null) {
			ArrayList<DataProduct> tempDatasetList = new ArrayList<>();
			datasetList.forEach(ds -> {
				if(ds.getTemporalExtent()!=null){
					for(LinkedEntity linkedEntity : ds.getTemporalExtent()){
						Optional<PeriodOfTime> dsTemporalItem = DatabaseConnections.getInstance().getPeriodOfTimeList().stream().filter(obj -> obj.getInstanceId().equals(linkedEntity.getInstanceId())).findFirst();
						if(dsTemporalItem.isPresent()){
							if((dsTemporalItem.get().getStartDate() == null || temporal.getEndDate() == null || dsTemporalItem.get().getStartDate().isBefore(temporal.getEndDate()))
									&& (temporal.getStartDate() == null || dsTemporalItem.get().getEndDate() == null || temporal.getStartDate().isBefore(dsTemporalItem.get().getEndDate()))
									&& (dsTemporalItem.get().getStartDate() == null || dsTemporalItem.get().getEndDate() == null || dsTemporalItem.get().getStartDate().isBefore(dsTemporalItem.get().getEndDate()))
									&& (temporal.getStartDate() == null || temporal.getEndDate() == null || temporal.getStartDate().isBefore(temporal.getEndDate())))
								tempDatasetList.add(ds);
						}
					}
				}
			});
			datasetList = tempDatasetList;
		}
		return datasetList;
	}

	private static List<DataProduct> filterByOrganizations(List<DataProduct> datasetList, Map<String,Object> parameters) {
		if(parameters.containsKey("organisations")) {
			List<String> organisations = Arrays.asList(parameters.get("organisations").toString().split(","));

			HashSet<DataProduct> tempDatasetList = new HashSet<>();
			datasetList.forEach(ds -> {
				List<Organization> organizations = new ArrayList<>();
				for(LinkedEntity linkedEntity : ds.getPublisher()){
					Optional<Organization> provider = DatabaseConnections.getInstance().getOrganizationList().stream().filter(organization -> organization.getInstanceId().equals(linkedEntity.getInstanceId())).findFirst();
                    provider.ifPresent(organizations::add);
				}
				List<DataServiceProvider> providers = new ArrayList<DataServiceProvider>(DataServiceProviderGeneration.getProviders(organizations));

				List<Organization> organisationList = new ArrayList<>();
				for (LinkedEntity distribution : ds.getDistribution()) {
					Optional<Distribution> distribution1 = DatabaseConnections.getInstance().getDistributionList().stream().filter(distribution2 -> distribution2.getInstanceId().equals(distribution.getInstanceId())).findFirst();
					if(distribution1.isPresent()){
						for (LinkedEntity accessService : distribution1.get().getAccessService()) {
							Optional<WebService> ws = DatabaseConnections.getInstance().getWebServiceList().stream().filter(webService1 -> webService1.getInstanceId().equals(accessService.getInstanceId())).findFirst();
							if(ws.isPresent()){
								Optional<Organization> organization = DatabaseConnections.getInstance().getOrganizationList().stream().filter(organization1 -> organization1.getInstanceId().equals(ws.get().getProvider().getInstanceId())).findFirst();
                                organization.ifPresent(organisationList::add);
							}
						}
					}
				}
				providers.addAll(DataServiceProviderGeneration.getProviders(organisationList));

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

	private static List<DataProduct> filterByKeywords(List<DataProduct> datasetList, Map<String,Object> parameters) {
		if(parameters.containsKey("keywords")) {
			ArrayList<DataProduct> tempDatasetList = new ArrayList<>();
			String[] keywords = parameters.get("keywords").toString().split(",");
			Set<String> keywordsSet = new HashSet<String>(Arrays.asList(keywords));
			datasetList.forEach(ds -> {
				if(!Collections.disjoint(Arrays.stream(ds.getKeywords().split(",")).map(String::toLowerCase).map(String::trim).collect(Collectors.toList()), keywordsSet)){
					tempDatasetList.add(ds);
				}
			});
			datasetList = tempDatasetList;
		}
		return datasetList;
	}

	private static List<DataProduct> filterByFullText(List<DataProduct> datasetList, Map<String,Object> parameters) {
		if(parameters.containsKey("q")) {
			HashSet<DataProduct> tempDatasetList = new HashSet<>();
			String[] qs = parameters.get("q").toString().toLowerCase().split(",");

			Set<String> qsSet = new HashSet<String>(Arrays.asList(qs));

			for (DataProduct edmDataproduct : datasetList) {
				Map<String, Boolean> qSMap = qsSet.stream()
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
				
				//identifier
				if (edmDataproduct.getIdentifier()!=null && !edmDataproduct.getIdentifier().isEmpty()) {
					for (LinkedEntity linkedEntity : edmDataproduct.getIdentifier()) {
						Optional<Identifier> edmIdentifier = DatabaseConnections.getInstance().getIdentifierList().stream().filter(identifier -> identifier.getInstanceId().equals(linkedEntity.getInstanceId())).findFirst();
						if(edmIdentifier.isPresent()){
							for (String q : qSMap.keySet()) {
								if (edmIdentifier.get().getIdentifier().toLowerCase().contains(q)) qSMap.put(q, Boolean.TRUE);
								if (edmIdentifier.get().getType().toLowerCase().contains(q)) qSMap.put(q, Boolean.TRUE);
								if ((edmIdentifier.get().getType().toLowerCase()+edmIdentifier.get().getIdentifier().toLowerCase()).contains(q)) qSMap.put(q, Boolean.TRUE);
							}
						}
					}
				}


				if (edmDataproduct.getDistribution() != null && !edmDataproduct.getDistribution().isEmpty()) {
					for (LinkedEntity edmDistributionLe : edmDataproduct.getDistribution()) {
						Optional<Distribution> edmDistribution = DatabaseConnections.getInstance().getDistributionList().stream().filter(distribution2 -> distribution2.getInstanceId().equals(edmDistributionLe.getInstanceId())).findFirst();
						if(edmDistribution.isPresent()){
							//distribution title
							if (edmDistribution.get().getTitle() != null) {
								edmDistribution.get().getTitle()
										.forEach(title ->
										{
											for (String q : qSMap.keySet()) {
												if (title.toLowerCase().contains(q)) {
													qSMap.put(q, Boolean.TRUE);
												}
											}
										});
							}

							if(Objects.nonNull(edmDistribution.get().getUid())){
								for (String q : qSMap.keySet()) {
									if (edmDistribution.get().getUid().toLowerCase().contains(q)) qSMap.put(q, Boolean.TRUE);
								}
							}

							//distribution description
							if (edmDistribution.get().getDescription() != null) {
								edmDistribution.get().getDescription()
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
							if (edmDistribution.get().getAccessService() != null) {
								for(LinkedEntity accessService : edmDistribution.get().getAccessService()){
									Optional<WebService> edmWebservice = DatabaseConnections.getInstance().getWebServiceList().stream().filter(webService1 -> webService1.getInstanceId().equals(accessService.getInstanceId())).findFirst();
									if(edmWebservice.isPresent()){

										if(Objects.nonNull(edmWebservice.get().getUid())){
											for (String q : qSMap.keySet()) {
												if (edmWebservice.get().getUid().toLowerCase().contains(q)) qSMap.put(q, Boolean.TRUE);
											}
										}

										//webservice title
										if (edmWebservice.get().getName() != null) {
											for (String q : qSMap.keySet()) {
												if (edmWebservice.get().getName().toLowerCase().contains(q)) {
													qSMap.put(q, Boolean.TRUE);
												}
											}
										}

										//webservice description
										if (edmWebservice.get().getDescription() != null) {
											for (String q : qSMap.keySet()) {
												if (edmWebservice.get().getDescription().toLowerCase().contains(q)) {
													qSMap.put(q, Boolean.TRUE);
												}
											}
										}

										if(Objects.nonNull(edmWebservice.get().getKeywords())){
											List<String> webserviceKeywords = Arrays.stream(edmWebservice.get().getKeywords().split(","))
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
