package org.epos.api.clienthelpers.responses.model;

import java.io.Serializable;
import java.util.ArrayList;

import org.epos.api.clienthelpers.model.DiscoveryItem;

public class SearchResponse implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private ArrayList<DiscoveryItem> response;
	

	public SearchResponse(ArrayList<DiscoveryItem> response) {
		super();
		this.response = response;
	}

	public ArrayList<DiscoveryItem> getResponse() {
		return response;
	}

	public void setResponse(ArrayList<DiscoveryItem> response) {
		this.response = response;
	}
	
	
}
