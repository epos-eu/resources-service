package org.epos.api.beans;

import java.io.Serializable;
import java.util.*;

import org.epos.api.facets.Node;

public class DataProduct implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String errorMessage;
	
	private transient String productid;
	private transient String distributionid;
	private String operationid;
	
	private String href;

	private Node categories;
	
	private String id;
	private String uid;
	private String type;
	private String title;
	private String description;
	private String license;
	private String version;
	private String downloadURL;
	private List<String> keywords;
	private List<DataServiceProvider> dataProvider;
	private String frequencyUpdate;
	private List<Map<String,String>> identifiers;
	private SpatialInfo spatial;
	private List<TemporalCoverage> temporalCoverage;	
	private List<String> scienceDomain;
	private String hasQualityAnnotation;
	private String accessRights;
	private List<Map<String,String>> provenance;
	private List<Map<String,Object>> contactPoints;
	
	public DataProduct() {}
	
	public DataProduct(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}

	public String getProductid() {
		return productid;
	}

	public Node getCategories() {
		return categories;
	}

	public void setCategories(Node categories) {
		this.categories = categories;
	}

	public void setProductid(String productid) {
		this.productid = productid;
	}

	public String getDistributionid() {
		return distributionid;
	}

	public void setDistributionid(String distributionid) {
		this.distributionid = distributionid;
	}

	public String getOperationid() {
		return operationid;
	}

	public void setOperationid(String operationid) {
		this.operationid = operationid;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getLicense() {
		return license;
	}

	public void setLicense(String license) {
		this.license = license;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getDownloadURL() {
		return downloadURL;
	}

	public void setDownloadURL(String downloadURL) {
		this.downloadURL = downloadURL;
	}

	public List<String> getKeywords() {
		if(keywords==null) keywords = new ArrayList<String>();
		return keywords;
	}

	public void setKeywords(List<String> keywords) {
		this.keywords = keywords;
	}

	public List<DataServiceProvider> getDataProvider() {
		if(dataProvider==null) dataProvider = new ArrayList<DataServiceProvider>();
		return dataProvider;
	}

	public void setDataProvider(List<DataServiceProvider> dataServiceProvider) {
		this.dataProvider = dataServiceProvider;
	}

	public String getFrequencyUpdate() {
		return frequencyUpdate;
	}

	public void setFrequencyUpdate(String frequencyUpdate) {
		this.frequencyUpdate = frequencyUpdate;
	}

	public List<Map<String,String>> getIdentifiers() {
		if(identifiers==null) identifiers = new ArrayList<Map<String,String>>();
		return identifiers;
	}

	public void setIdentifiers(List<Map<String,String>> identifiers) {
		this.identifiers = identifiers;
	}

	public SpatialInfo getSpatial() {
		if(spatial ==  null) spatial = new SpatialInfo();
		return spatial;
	}

	public void setSpatial(SpatialInfo spatial) {
		this.spatial = spatial;
	}

	public List<TemporalCoverage> getTemporalCoverage() {
		if(temporalCoverage ==  null) temporalCoverage = new ArrayList<TemporalCoverage>();
		return temporalCoverage;
	}

	public void setTemporalCoverage(List<TemporalCoverage> temporalCoverage) {
		this.temporalCoverage = temporalCoverage;
	}

	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}
	
	public List<String> getScienceDomain() {
		if(scienceDomain==null) scienceDomain = new ArrayList<String>();
		return scienceDomain;
	}

	public void setScienceDomain(List<String> scienceDomain) {
		this.scienceDomain = scienceDomain;
	}


	public String getHasQualityAnnotation() {
		return hasQualityAnnotation;
	}

	public void setHasQualityAnnotation(String hasQualityAnnotation) {
		this.hasQualityAnnotation = hasQualityAnnotation;
	}

	public String getAccessRights() {
		return accessRights;
	}

	public void setAccessRights(String accessRights) {
		this.accessRights = accessRights;
	}

	public List<Map<String,String>> getProvenance() {
		if(provenance==null) provenance = new ArrayList<Map<String,String>>();
		return provenance;
	}

	public void setProvenance(List<Map<String,String>> provenance) {
		this.provenance = provenance;
	}

	public List<Map<String,Object>> getContactPoints() {
		if(contactPoints==null) contactPoints = new ArrayList<Map<String,Object>>();
		return contactPoints;
	}

	public void setContactPoints(List<Map<String,Object>> contactPoints) {
		this.contactPoints = contactPoints;
	}
}
