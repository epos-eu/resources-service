/*******************************************************************************
 * Copyright 2021 EPOS ERIC
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package org.epos.api.clienthelpers.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;

import org.springframework.validation.annotation.Validated;

import com.fasterxml.jackson.annotation.JsonProperty;

@Validated
@javax.annotation.Generated(value = "io.swagger.codegen.v3.generators.java.SpringCodegen", date = "2021-10-22T14:05:21.395Z[GMT]")

public class Dataproduct implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private transient List<String> productid;
	
	@JsonProperty("id")
	private String id;
	
	@JsonProperty("ddss")
	private String ddss;
	
	@JsonProperty("title")
	private String title;
	
	@JsonProperty("type")
	private String type;
	
	@JsonProperty("startDate")
	private String startDate;
	
	@JsonProperty("endDate")
	private String endDate;
	
	@JsonProperty("distributions")
	@Valid
	private List<DistributionResource> distributions;
	
	@JsonProperty("spatial")
	private SpatialInfo spatial;
	
	@JsonProperty("license")
	private String license;
	
	@JsonProperty("description")
	private String description;
	

	public Dataproduct(){
		distributions = new ArrayList<>();
		productid = new ArrayList<>();
		spatial = new SpatialInfo();
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getDdss() {
		return ddss;
	}

	public void setDdss(String ddss) {
		this.ddss = ddss;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	/**
	 * @return the startDate
	 */
	public String getStartDate() {
		return startDate;
	}

	/**
	 * @param startDate the startDate to set
	 */
	public void setStartDate(String startDate) {
		this.startDate = startDate;
	}

	/**
	 * @return the endDate
	 */
	public String getEndDate() {
		return endDate;
	}

	/**
	 * @param endDate the endDate to set
	 */
	public void setEndDate(String endDate) {
		this.endDate = endDate;
	}

	public List<DistributionResource> getDistributions() {
		return distributions;
	}

	public void setDistributions(List<DistributionResource> distributions) {
		this.distributions = distributions;
	}

	public List<String> getProductid() {
		return productid;
	}

	public void setProductid(List<String> productid) {
		this.productid = productid;
	}

	

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "DDSS [productid=" + productid + ", id=" + id + ", ddss=" + ddss + ", title=" + title + ", type=" + type
				+ ", startDate=" + startDate + ", endDate=" + endDate + ", distributions=" + distributions
				+ ", getId()=" + getId() + ", getDdss()=" + getDdss() + ", getTitle()=" + getTitle() + ", getType()="
				+ getType() + ", getStartDate()=" + getStartDate() + ", getEndDate()=" + getEndDate()
				+ ", getDistributions()=" + getDistributions() + ", getProductid()=" + getProductid() + ", getClass()="
				+ getClass() + ", hashCode()=" + hashCode() + ", toString()=" + super.toString() + "]";
	}

	public void addProductid(String asString) {
		if(!this.productid.contains(asString)) this.productid.add(asString);
	}

	public SpatialInfo getSpatial() {
		return spatial;
	}

	public void setSpatial(SpatialInfo spatial) {
		this.spatial = spatial;
	}

	/**
	 * @return the license
	 */
	public String getLicense() {
		return license;
	}

	/**
	 * @param license the license to set
	 */
	public void setLicense(String license) {
		this.license = license;
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

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ddss == null) ? 0 : ddss.hashCode());
		result = prime * result + ((description == null) ? 0 : description.hashCode());
		result = prime * result + ((distributions == null) ? 0 : distributions.hashCode());
		result = prime * result + ((endDate == null) ? 0 : endDate.hashCode());
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		result = prime * result + ((license == null) ? 0 : license.hashCode());
		result = prime * result + ((productid == null) ? 0 : productid.hashCode());
		result = prime * result + ((spatial == null) ? 0 : spatial.hashCode());
		result = prime * result + ((startDate == null) ? 0 : startDate.hashCode());
		result = prime * result + ((title == null) ? 0 : title.hashCode());
		result = prime * result + ((type == null) ? 0 : type.hashCode());
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
		Dataproduct other = (Dataproduct) obj;
		if (ddss == null) {
			if (other.ddss != null)
				return false;
		} else if (!ddss.equals(other.ddss))
			return false;
		if (description == null) {
			if (other.description != null)
				return false;
		} else if (!description.equals(other.description))
			return false;
		if (distributions == null) {
			if (other.distributions != null)
				return false;
		} else if (!distributions.equals(other.distributions))
			return false;
		if (endDate == null) {
			if (other.endDate != null)
				return false;
		} else if (!endDate.equals(other.endDate))
			return false;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		if (license == null) {
			if (other.license != null)
				return false;
		} else if (!license.equals(other.license))
			return false;
		if (productid == null) {
			if (other.productid != null)
				return false;
		} else if (!productid.equals(other.productid))
			return false;
		if (spatial == null) {
			if (other.spatial != null)
				return false;
		} else if (!spatial.equals(other.spatial))
			return false;
		if (startDate == null) {
			if (other.startDate != null)
				return false;
		} else if (!startDate.equals(other.startDate))
			return false;
		if (title == null) {
			if (other.title != null)
				return false;
		} else if (!title.equals(other.title))
			return false;
		if (type == null) {
			if (other.type != null)
				return false;
		} else if (!type.equals(other.type))
			return false;
		return true;
	}
	
	
	
}
