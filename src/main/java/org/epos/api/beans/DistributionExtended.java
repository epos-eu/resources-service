package org.epos.api.beans;

import java.io.Serializable;
import java.util.*;

import org.epos.api.facets.Node;

public class DistributionExtended implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String errorMessage;
	
	private String href;
	private String hrefExtended;

	private Node categories;
	
	private String id;
	private String uid;
	private String type;
	private String title;
	private String description;
	private String license;
	private String downloadURL;
	private List<String> keywords;
	private String frequencyUpdate;
	private List<String> scienceDomain;
	private String hasQualityAnnotation;
	private List<AvailableFormat> availableFormats;

	private List<AvailableContactPoints> availableContactPoints;
	
	private List<DataProduct> relatedDataProducts;
	private List<Webservice> relatedWebservice;
	
	public DistributionExtended() {}
	
	public DistributionExtended(String errorMessage) {
		this.errorMessage = errorMessage;
	}


	public String getErrorMessage() {
		return errorMessage;
	}

	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
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

	public String getLicense() {
		return license;
	}

	public void setLicense(String license) {
		this.license = license;
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
	public String getFrequencyUpdate() {
		return frequencyUpdate;
	}

	public void setFrequencyUpdate(String frequencyUpdate) {
		this.frequencyUpdate = frequencyUpdate;
	}

	public List<AvailableFormat> getAvailableFormats() {
		if(availableFormats==null) availableFormats = new ArrayList<AvailableFormat>();
		return availableFormats;
	}

	public void setAvailableFormats(List<AvailableFormat> availableFormats) {
		this.availableFormats = availableFormats;
	}

	public List<AvailableContactPoints> getAvailableContactPoints() {
		if(availableContactPoints==null) availableContactPoints = new ArrayList<AvailableContactPoints>();
		return availableContactPoints;
	}

	public void setAvailableContactPoints(List<AvailableContactPoints> availableContactPoints) {
		this.availableContactPoints = availableContactPoints;
	}

	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}
	
	public String getHrefExtended() {
		return hrefExtended;
	}

	public void setHrefExtended(String hrefExtended) {
		this.hrefExtended = hrefExtended;
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

	/**
	 * @return the relatedDataProducts
	 */
	public List<DataProduct> getRelatedDataProducts() {
		if(relatedDataProducts==null) relatedDataProducts = new ArrayList<DataProduct>();
		return relatedDataProducts;
	}

	/**
	 * @param relatedDataProducts the relatedDataProducts to set
	 */
	public void setRelatedDataProducts(List<DataProduct> relatedDataProducts) {
		this.relatedDataProducts = relatedDataProducts;
	}

	/**
	 * @return the relatedWebservice
	 */
	public List<Webservice> getRelatedWebservice() {
		if(relatedWebservice==null) relatedWebservice = new ArrayList<Webservice>();
		return relatedWebservice;
	}

	/**
	 * @param relatedWebservice the relatedWebservice to set
	 */
	public void setRelatedWebservice(List<Webservice> relatedWebservice) {
		this.relatedWebservice = relatedWebservice;
	}

}
