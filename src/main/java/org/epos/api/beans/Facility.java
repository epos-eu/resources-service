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
	//private String license;
	//private String downloadURL;
	private List<String> keywords;
	private List<DataServiceProvider> facilityProvider;
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
	
	private String serviceName;
	private String serviceDescription;
	private DataServiceProvider serviceProvider;
	private SpatialInfo serviceSpatial;
	private TemporalCoverage serviceTemporalCoverage;
	private String serviceEndpoint;
	//private String serviceDocumentation;
	private List<String> serviceType;
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
	public List<String> getKeywords() {
		if(keywords==null) keywords = new ArrayList<String>();
		return keywords;
	}
	public void setKeywords(List<String> keywords) {
		this.keywords = keywords;
	}
	public List<DataServiceProvider> getFacilityProvider() {
		if(facilityProvider==null) facilityProvider = new ArrayList<DataServiceProvider>();
		return facilityProvider;
	}
	public void setFacilityProvider(List<DataServiceProvider> facilityProvider) {
		this.facilityProvider = facilityProvider;
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
	public String getServiceName() {
		return serviceName;
	}
	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}
	public String getServiceDescription() {
		return serviceDescription;
	}
	public void setServiceDescription(String serviceDescription) {
		this.serviceDescription = serviceDescription;
	}
	public DataServiceProvider getServiceProvider() {
		return serviceProvider;
	}
	public void setServiceProvider(DataServiceProvider serviceProvider) {
		this.serviceProvider = serviceProvider;
	}
	public SpatialInfo getServiceSpatial() {
		if(serviceSpatial ==  null) serviceSpatial = new SpatialInfo();
		return serviceSpatial;
	}
	public void setServiceSpatial(SpatialInfo serviceSpatial) {
		this.serviceSpatial = serviceSpatial;
	}
	public TemporalCoverage getServiceTemporalCoverage() {
		if(serviceTemporalCoverage ==  null) serviceTemporalCoverage = new TemporalCoverage();
		return serviceTemporalCoverage;
	}
	public void setServiceTemporalCoverage(TemporalCoverage serviceTemporalCoverage) {
		this.serviceTemporalCoverage = serviceTemporalCoverage;
	}
	public String getServiceEndpoint() {
		return serviceEndpoint;
	}
	public void setServiceEndpoint(String serviceEndpoint) {
		this.serviceEndpoint = serviceEndpoint;
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
	@Override
	public int hashCode() {
		return Objects.hash(availableContactPoints, availableFormats, categories, description, errorMessage,
				facilityProvider, href, id, internalID, keywords, operationid, serviceDescription, serviceEndpoint,
				serviceName, serviceProvider, serviceSpatial, serviceTemporalCoverage, serviceType, spatial, title,
				type, uid);
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
				&& Objects.equals(facilityProvider, other.facilityProvider) && Objects.equals(href, other.href)
				&& Objects.equals(id, other.id) && Objects.equals(internalID, other.internalID)
				&& Objects.equals(keywords, other.keywords) && Objects.equals(operationid, other.operationid)
				&& Objects.equals(serviceDescription, other.serviceDescription)
				&& Objects.equals(serviceEndpoint, other.serviceEndpoint)
				&& Objects.equals(serviceName, other.serviceName)
				&& Objects.equals(serviceProvider, other.serviceProvider)
				&& Objects.equals(serviceSpatial, other.serviceSpatial)
				&& Objects.equals(serviceTemporalCoverage, other.serviceTemporalCoverage)
				&& Objects.equals(serviceType, other.serviceType) && Objects.equals(spatial, other.spatial)
				&& Objects.equals(title, other.title) && Objects.equals(type, other.type)
				&& Objects.equals(uid, other.uid);
	}
	@Override
	public String toString() {
		return "Facility [errorMessage=" + errorMessage + ", operationid=" + operationid + ", href=" + href
				+ ", categories=" + categories + ", id=" + id + ", uid=" + uid + ", type=" + type + ", title=" + title
				+ ", description=" + description + ", keywords=" + keywords + ", facilityProvider=" + facilityProvider
				+ ", internalID=" + internalID + ", spatial=" + spatial + ", availableContactPoints="
				+ availableContactPoints + ", availableFormats=" + availableFormats + ", serviceName=" + serviceName
				+ ", serviceDescription=" + serviceDescription + ", serviceProvider=" + serviceProvider
				+ ", serviceSpatial=" + serviceSpatial + ", serviceTemporalCoverage=" + serviceTemporalCoverage
				+ ", serviceEndpoint=" + serviceEndpoint + ", serviceType=" + serviceType + "]";
	}

	//private List<ServiceParameter> serviceParameters;
	
	

}
