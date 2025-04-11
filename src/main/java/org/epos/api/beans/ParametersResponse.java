package org.epos.api.beans;

import java.io.Serializable;
import java.util.Set;

/**
 * ParametersResponse
 */
public class ParametersResponse {

	private Set<Parameter> items;

	public ParametersResponse(Set<Parameter> items) {
		this.items = items;
	}

	public Set<Parameter> getItems() {
		return items;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((items == null) ? 0 : items.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ParametersResponse other = (ParametersResponse) obj;
		if (items == null) {
			if (other.items != null)
				return false;
		} else if (!items.equals(other.items))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "LinkedResponse [items=" + items + "]";
	}

}
