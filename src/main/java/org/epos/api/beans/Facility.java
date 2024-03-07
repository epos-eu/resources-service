package org.epos.api.beans;

import java.io.Serializable;
import java.util.*;

import org.epos.api.facets.Node;

public class Facility implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String errorMessage;
	
	private transient String serviceid;
	private transient String facilityid;
	private String operationid;
	
	private String href;

	private Node categories;
	
	private String id;
	private String uid;
	private String type;
	private String title;
	private String description;
	private List<String> page;
	//private String license;
	//private String downloadURL;
	private List<String> keywords;
	private List<DataServiceProvider> dataProvider;
	//private String frequencyUpdate;
	private List<String> internalID;
	//private List<String> DOI;
	private SpatialInfo spatial;
	//private TemporalCoverage temporalCoverage;	
	//private List<String> scienceDomain;
	//private String hasQualityAnnotation;
	private List<AvailableContactPoints> availableContactPoints;
	private List<AvailableFormat> availableFormats;
	
	// WEBSERVICE
	
	//private String serviceName;
	//private String serviceDescription;
	//private DataServiceProvider serviceProvider;
	//private SpatialInfo serviceSpatial;
	//private TemporalCoverage serviceTemporalCoverage;
	//private String serviceEndpoint;
	//private String serviceDocumentation;
	private List<String> serviceType;

	private List<ServiceParameter> serviceParameters;
	
	
	//private String endpoint;
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	public String getServiceid() {
		return serviceid;
	}
	public void setServiceid(String serviceid) {
		this.serviceid = serviceid;
	}
	public String getFacilityid() {
		return facilityid;
	}
	public void setFacilityid(String facilityid) {
		this.facilityid = facilityid;
	}
	public String getOperationid() {
		return operationid;
	}
	public void setOperationid(String operationid) {
		this.operationid = operationid;
	}
	public String getHref() {
		return href;
	}
	public void setHref(String href) {
		this.href = href;
	}
	public Node getCategories() {
		return categories;
	}
	public void setCategories(Node categories) {
		this.categories = categories;
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
	public List<String> getPage() {
		if(page==null) page = new ArrayList<String>();
		return page;
	}
	public void setPage(List<String> page) {
		this.page = page;
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
	public void setDataProvider(List<DataServiceProvider> dataProvider) {
		this.dataProvider = dataProvider;
	}
	public List<String> getInternalID() {
		if(internalID==null) internalID = new ArrayList<String>();
		return internalID;
	}
	public void setInternalID(List<String> internalID) {
		this.internalID = internalID;
	}
	public SpatialInfo getSpatial() {
		if(spatial ==  null) spatial = new SpatialInfo();
		return spatial;
	}
	public void setSpatial(SpatialInfo spatial) {
		this.spatial = spatial;
	}
	public List<AvailableContactPoints> getAvailableContactPoints() {
		if(availableContactPoints==null) availableContactPoints = new ArrayList<AvailableContactPoints>();
		return availableContactPoints;
	}
	public void setAvailableContactPoints(List<AvailableContactPoints> availableContactPoints) {
		this.availableContactPoints = availableContactPoints;
	}
	public List<AvailableFormat> getAvailableFormats() {
		if(availableFormats==null) availableFormats = new ArrayList<AvailableFormat>();
		return availableFormats;
	}
	public void setAvailableFormats(List<AvailableFormat> availableFormats) {
		this.availableFormats = availableFormats;
	}
	public List<String> getServiceType() {
		if(serviceType==null) serviceType = new ArrayList<String>();
		return serviceType;
	}
	public void setServiceType(List<String> serviceType) {
		this.serviceType = serviceType;
	}
	public static long getSerialversionuid() {
		return serialVersionUID;
	}
	public List<ServiceParameter> getServiceParameters() {
		return serviceParameters;
	}
	public void setServiceParameters(List<ServiceParameter> serviceParameters) {
		this.serviceParameters = serviceParameters;
	}
	@Override
	public int hashCode() {
		return Objects.hash(availableContactPoints, availableFormats, categories, description, errorMessage,
				dataProvider, href, id, internalID, keywords, operationid, serviceType, spatial, title, type, uid);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Facility other = (Facility) obj;
		return Objects.equals(availableContactPoints, other.availableContactPoints)
				&& Objects.equals(availableFormats, other.availableFormats)
				&& Objects.equals(categories, other.categories) && Objects.equals(description, other.description)
				&& Objects.equals(errorMessage, other.errorMessage)
				&& Objects.equals(dataProvider, other.dataProvider) && Objects.equals(href, other.href)
				&& Objects.equals(id, other.id) && Objects.equals(internalID, other.internalID)
				&& Objects.equals(keywords, other.keywords) && Objects.equals(operationid, other.operationid)
				&& Objects.equals(serviceType, other.serviceType) && Objects.equals(spatial, other.spatial)
				&& Objects.equals(title, other.title) && Objects.equals(type, other.type)
				&& Objects.equals(uid, other.uid);
	}
	@Override
	public String toString() {
		return "Facility [errorMessage=" + errorMessage + ", operationid=" + operationid + ", href=" + href
				+ ", categories=" + categories + ", id=" + id + ", uid=" + uid + ", type=" + type + ", title=" + title
				+ ", description=" + description + ", keywords=" + keywords + ", dataProvider=" + dataProvider
				+ ", internalID=" + internalID + ", spatial=" + spatial + ", availableContactPoints="
				+ availableContactPoints + ", availableFormats=" + availableFormats + ", serviceType=" + serviceType
				+ "]";
	}
	
}
