package org.epos.api.beans;

import java.util.List;
import java.util.Objects;

public class DataServiceProvider {

	private String dataProviderLegalName;
	private String dataProviderUrl;
	private String country;
	private String uid;
	private String metaid;
	private String instanceid;
	
	private List<DataServiceProvider> relatedDataServiceProvider;

	public String getDataProviderLegalName() {
		return dataProviderLegalName;
	}

	public void setDataProviderLegalName(String dataProviderLegalName) {
		this.dataProviderLegalName = dataProviderLegalName;
	}

	public List<DataServiceProvider> getRelatedDataProvider() {
		return relatedDataServiceProvider;
	}

	public void setRelatedDataProvider(List<DataServiceProvider> relatedDataServiceProvider) {
		this.relatedDataServiceProvider = relatedDataServiceProvider;
	}

	public String getDataProviderUrl() {
		return dataProviderUrl;
	}

	public void setDataProviderUrl(String dataProviderUrl) {
		this.dataProviderUrl = dataProviderUrl;
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getMetaid() {
		return metaid;
	}

	public void setMetaid(String metaid) {
		this.metaid = metaid;
	}

	public String getInstanceid() {
		return instanceid;
	}

	public void setInstanceid(String instanceid) {
		this.instanceid = instanceid;
	}

	public List<DataServiceProvider> getRelatedDataServiceProvider() {
		return relatedDataServiceProvider;
	}

	public void setRelatedDataServiceProvider(List<DataServiceProvider> relatedDataServiceProvider) {
		this.relatedDataServiceProvider = relatedDataServiceProvider;
	}

	@Override
	public int hashCode() {
		return Objects.hash(country, dataProviderLegalName, dataProviderUrl, instanceid, metaid,
				relatedDataServiceProvider, uid);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DataServiceProvider other = (DataServiceProvider) obj;
		return Objects.equals(country, other.country)
				&& Objects.equals(dataProviderLegalName, other.dataProviderLegalName)
				&& Objects.equals(dataProviderUrl, other.dataProviderUrl)
				&& Objects.equals(instanceid, other.instanceid) && Objects.equals(metaid, other.metaid)
				&& Objects.equals(relatedDataServiceProvider, other.relatedDataServiceProvider)
				&& Objects.equals(uid, other.uid);
	}

	@Override
	public String toString() {
		return "DataServiceProvider [dataProviderLegalName=" + dataProviderLegalName + ", dataProviderUrl="
				+ dataProviderUrl + ", country=" + country + ", uid=" + uid + ", metaid=" + metaid + ", instanceid="
				+ instanceid + ", relatedDataServiceProvider=" + relatedDataServiceProvider + "]";
	}

}
