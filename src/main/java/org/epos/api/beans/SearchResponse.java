package org.epos.api.beans;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

import org.epos.api.facets.Node;

public class SearchResponse {
	
	private Node results;
	private ArrayList<NodeFilters> filters;

	public SearchResponse(Node results, ArrayList<NodeFilters> filters) {
		super();
		this.results = results.getChildren().get(0);
		this.filters = filters;
	}

	public Node getResults() {
		return results;
	}

	public void setResults(Node results) {
		this.results = results;
	}

	public ArrayList<NodeFilters> getFilters() {
		return filters;
	}

	public void setFilters(ArrayList<NodeFilters> filters) {
		this.filters = filters;
	}

	@Override
	public int hashCode() {
		return Objects.hash(filters, results);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SearchResponse other = (SearchResponse) obj;
		return Objects.equals(filters, other.filters) && Objects.equals(results, other.results);
	}

	@Override
	public String toString() {
		return "SearchResponse [results=" + results + ", filters=" + filters + "]";
	}
	
	
		
}
