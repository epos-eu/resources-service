package org.epos.api.beans;


import java.io.Serializable;
import java.util.*;

public class Webservice implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String href;

	private HashMap<String, ArrayList<String>> categories;
	
	private String id;
	private String metaid;
	private String uid;
	private String name;
	private String description;
	private DataServiceProvider provider;
	private SpatialInfo spatial;
	private String documentation;
	private List<TemporalCoverage> temporalCoverage;
	private List<String> type;
	private List<HashMap<String,Object>> contactPoints;
	
	private List<Operation> operations;
	
	public Webservice() {}

	/**
	 * @return the href
	 */
	public String getHref() {
		return href;
	}

	/**
	 * @param href the href to set
	 */
	public void setHref(String href) {
		this.href = href;
	}

	/**
	 * @return the categories
	 */
	public HashMap<String, ArrayList<String>> getCategories() {
		return categories;
	}

	/**
	 * @param categories the categories to set
	 */
	public void setCategories(HashMap<String, ArrayList<String>> categories) {
		this.categories = categories;
	}

	/**
	 * @return the id
	 */
	public String getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id) {
		this.id = id;
	}

	/**
	 * @return the metaid
	 */
	public String getMetaid() {
		return metaid;
	}

	/**
	 * @param metaid the metaid to set
	 */
	public void setMetaid(String metaid) {
		this.metaid = metaid;
	}

	/**
	 * @return the uid
	 */
	public String getUid() {
		return uid;
	}

	/**
	 * @param uid the uid to set
	 */
	public void setUid(String uid) {
		this.uid = uid;
	}

	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the provider
	 */
	public DataServiceProvider getProvider() {
		return provider;
	}

	/**
	 * @param provider the provider to set
	 */
	public void setProvider(DataServiceProvider provider) {
		this.provider = provider;
	}

	/**
	 * @return the spatial
	 */
	public SpatialInfo getSpatial() {
		if(spatial==null) spatial = new SpatialInfo();
		return spatial;
	}

	/**
	 * @param spatial the spatial to set
	 */
	public void setSpatial(SpatialInfo spatial) {
		this.spatial = spatial;
	}

	/**
	 * @return the documentation
	 */
	public String getDocumentation() {
		return documentation;
	}

	/**
	 * @param documentation the documentation to set
	 */
	public void setDocumentation(String documentation) {
		this.documentation = documentation;
	}

	/**
	 * @return the temporalCoverage
	 */
	public List<TemporalCoverage> getTemporalCoverage() {
		if(temporalCoverage==null) temporalCoverage = new ArrayList<TemporalCoverage>();
		return temporalCoverage;
	}

	/**
	 * @param temporalCoverage the temporalCoverage to set
	 */
	public void setTemporalCoverage(List<TemporalCoverage> temporalCoverage) {
		this.temporalCoverage = temporalCoverage;
	}

	/**
	 * @return the type
	 */
	public List<String> getType() {
		if(type==null) type = new ArrayList<String>();
		return type;
	}

	/**
	 * @param type the type to set
	 */
	public void setType(List<String> type) {
		this.type = type;
	}

	public List<HashMap<String,Object>> getContactPoints() {
		if(contactPoints==null) contactPoints = new ArrayList<HashMap<String,Object>>();
		return contactPoints;
	}

	public void setContactPoints(List<HashMap<String,Object>> contactPoints) {
		this.contactPoints = contactPoints;
	}

	/**
	 * @return the operations
	 */
	public List<Operation> getOperations() {
		if(operations==null) operations = new ArrayList<Operation>();
		return operations;
	}

	/**
	 * @param operations the operations to set
	 */
	public void setOperations(List<Operation> operations) {
		this.operations = operations;
	}

}
