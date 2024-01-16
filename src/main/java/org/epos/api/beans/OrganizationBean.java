package org.epos.api.beans;

import java.io.Serializable;

public class OrganizationBean implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String uid;
    private String logo;
    private String url;
    private String name;
    private String country;
    
	public OrganizationBean(String uid, String logo, String url, String name, String country) {
		super();
		this.uid = uid;
		this.logo = logo;
		this.url = url;
		this.name = name;
		this.country = country;
	}
	public String getUid() {
		return uid;
	}
	public void setUid(String uid) {
		this.uid = uid;
	}
	public String getLogo() {
		return logo;
	}
	public void setLogo(String logo) {
		this.logo = logo;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
    
    
}
