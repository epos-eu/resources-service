package org.epos.api.beans;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class DiscoveryItem implements Serializable {

	private static final long serialVersionUID = 1L;

	private String href;
	private String hrefExtended;
	private String id;
	private String uid;
	private String metaid;
	private transient String sha256id;
	private transient Set<String> dataprovider;
	private transient Set<String> facilityprovider;
	private transient Set<String> serviceprovider;
	private transient List<String> categories;
	private String title;
	private String description;
	private int status = 0;
	private String versioningStatus;
	private String statusTimestamp;
	private List<AvailableFormat> availableFormats;

	public DiscoveryItem() {
	}

	public DiscoveryItem(DiscoveryItemBuilder builder) {
		this.href = builder.href;
		this.hrefExtended = builder.hrefExtended;
		this.id = builder.id;
		this.uid = builder.uid;
		this.metaid = builder.metaid;
		this.sha256id = builder.sha256id;
		this.dataprovider = builder.dataprovider;
		this.serviceprovider = builder.serviceprovider;
		this.facilityprovider = builder.facilityprovider;
		this.categories = builder.categories;
		this.title = builder.title;
		this.description = builder.description;
		this.status = builder.status;
		this.versioningStatus = builder.versioningStatus;
		this.statusTimestamp = builder.statusTimestamp;
		this.availableFormats = builder.availableFormats;
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

	public String getId() {
		return id;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}

	public String getUid() {
		return uid;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getMetaid() {
		return metaid;
	}

	public void setMetaid(String metaid) {
		this.metaid = metaid;
	}

	public String getSha256id() {
		return sha256id;
	}

	public void setSha256id(String sha256id) {
		this.sha256id = sha256id;
	}

	public Set<String> getDataprovider() {
		return dataprovider;
	}

	public void setDataprovider(Set<String> dataprovider) {
		this.dataprovider = dataprovider;
	}

	public Set<String> getServiceprovider() {
		return serviceprovider;
	}

	public void setServiceprovider(Set<String> serviceprovider) {
		this.serviceprovider = serviceprovider;
	}

	public List<String> getCategories() {
		return categories;
	}

	public void setCategories(List<String> categories) {
		this.categories = categories;
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

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getVersioningStatus() {
		return versioningStatus;
	}

	public void setVersioningStatus(String versioningStatus) {
		this.versioningStatus = versioningStatus;
	}

	public String getStatusTimestamp() {
		return statusTimestamp;
	}

	public void setStatusTimestamp(String statusTimestamp) {
		this.statusTimestamp = statusTimestamp;
	}

	public List<AvailableFormat> getAvailableFormats() {
		return availableFormats;
	}

	public void setAvailableFormats(List<AvailableFormat> availableFormats) {
		this.availableFormats = availableFormats;
	}

	public Set<String> getFacilityprovider() {
		return facilityprovider;
	}

	public void setFacilityprovider(Set<String> facilityprovider) {
		this.facilityprovider = facilityprovider;
	}

	public static class DiscoveryItemBuilder {

		private String href;
		private String hrefExtended;
		private String id;
		private String uid;
		private String metaid;
		private transient String sha256id;
		private transient Set<String> dataprovider;
		private transient Set<String> serviceprovider;
		private transient Set<String> facilityprovider;
		private transient List<String> categories;
		private String title;
		private String description;
		private int status = 0;
		private String versioningStatus;
		private String statusTimestamp;
		private List<AvailableFormat> availableFormats;

		public DiscoveryItemBuilder(String id, String href, String hrefExtended) {
			this.id = id;
			this.href = href;
			this.hrefExtended = hrefExtended;
		}

		public DiscoveryItemBuilder uid(String uid) {
			this.uid = uid;
			return this;
		}

		public DiscoveryItemBuilder metaid(String metaid) {
			this.metaid = metaid;
			return this;
		}

		public DiscoveryItemBuilder setSha256id(String sha256id) {
			this.sha256id = sha256id;
			return this;
		}

		public DiscoveryItemBuilder setDataprovider(Set<String> facetsDataProviders) {
			this.dataprovider = facetsDataProviders;
			return this;
		}

		public DiscoveryItemBuilder setServiceProvider(Set<String> serviceprovider) {
			this.serviceprovider = serviceprovider;
			return this;
		}

		public DiscoveryItemBuilder setFacilityProvider(Set<String> facilityprovider) {
			this.facilityprovider = facilityprovider;
			return this;
		}

		public DiscoveryItemBuilder setCategories(List<String> categories) {
			this.categories = categories;
			return this;
		}

		public DiscoveryItemBuilder title(String title) {
			this.title = title;
			return this;
		}

		public DiscoveryItemBuilder description(String description) {
			this.description = description;
			return this;
		}

		public DiscoveryItemBuilder setStatus(int status) {
			this.status = status;
			return this;
		}

		public DiscoveryItemBuilder setVersioningStatus(String versioningStatus) {
			this.versioningStatus = versioningStatus;
			return this;
		}

		public DiscoveryItemBuilder setStatusTimestamp(String statusTimestamp) {
			this.statusTimestamp = statusTimestamp;
			return this;
		}

		public DiscoveryItemBuilder availableFormats(List<AvailableFormat> availableFormats) {
			this.availableFormats = availableFormats;
			return this;
		}

		public DiscoveryItem build() {
			return new DiscoveryItem(this);
		}
	}

	@Override
	public int hashCode() {
		return Objects.hash(
				availableFormats,
				description,
				href,
				hrefExtended,
				id,
				status,
				statusTimestamp,
				title,
				uid,
				metaid);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DiscoveryItem other = (DiscoveryItem) obj;
		return Objects.equals(availableFormats, other.availableFormats)
				&& Objects.equals(description, other.description)
				&& Objects.equals(href, other.href)
				&& Objects.equals(hrefExtended, other.hrefExtended)
				&& Objects.equals(id, other.id)
				&& status == other.status
				&& Objects.equals(statusTimestamp, other.statusTimestamp)
				&& Objects.equals(versioningStatus, other.versioningStatus)
				&& Objects.equals(title, other.title)
				&& Objects.equals(uid, other.uid)
				&& Objects.equals(metaid, other.metaid);
	}

	@Override
	public String toString() {
		return "DiscoveryItem [href=" + href
				+ ", hrefExtended=" + hrefExtended
				+ ", id=" + id
				+ ", uid=" + uid
				+ ", metaid=" + metaid
				+ ", title=" + title
				+ ", description=" + description
				+ ", status=" + status
				+ ", statusTimestamp=" + statusTimestamp
				+ ", versioningStatus=" + versioningStatus
				+ ", availableFormats=" + availableFormats
				+ "]";
	}
}
