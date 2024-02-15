package org.epos.api.beans;


import java.io.Serializable;
import java.util.*;

public class Operation implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	private String href;

	
	private String id;
	private String uid;
	private String method;
	
	private String endpoint;
	private List<ServiceParameter> serviceParameters;
	
	public Operation() {
		serviceParameters = new ArrayList<ServiceParameter>();
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

	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}

	/**
	 * @return the method
	 */
	public String getMethod() {
		return method;
	}

	/**
	 * @param method the method to set
	 */
	public void setMethod(String method) {
		this.method = method;
	}

	/**
	 * @return the endpoint
	 */
	public String getEndpoint() {
		return endpoint;
	}

	/**
	 * @param endpoint the endpoint to set
	 */
	public void setEndpoint(String endpoint) {
		this.endpoint = endpoint;
	}

	/**
	 * @return the serviceParameters
	 */
	public List<ServiceParameter> getServiceParameters() {
		return serviceParameters;
	}

	/**
	 * @param serviceParameters the serviceParameters to set
	 */
	public void setServiceParameters(List<ServiceParameter> serviceParameters) {
		this.serviceParameters = serviceParameters;
	}
	

}
