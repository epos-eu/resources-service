package org.epos.api.clienthelpers.model;

import java.io.Serializable;
import java.util.List;

public class DiscoveryItem implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String href;
	private String id;
	private transient String sha256id;
	private String ddss;
	private String title;
	private String description;
	private int status = 0;
	private String statusTimestamp;
	private List<AvailableFormat> availableFormats;
	
	public DiscoveryItem(DiscoveryItemBuilder builder) {
		this.href = builder.href;
		this.id = builder.id;
		this.sha256id = builder.sha256id;
		this.ddss = builder.ddss;
		this.title = builder.title;
		this.description = builder.description;
		this.status = builder.status;
		this.statusTimestamp = builder.statusTimestamp;
		this.availableFormats = builder.availableFormats;
	}

	public String getHref() {
		return href;
	}

	public void setHref(String href) {
		this.href = href;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSha256id() {
		return sha256id;
	}

	public void setSha256id(String sha256id) {
		this.sha256id = sha256id;
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

	public static class DiscoveryItemBuilder{
		
		private String href;
		private String id;
		private transient String sha256id;
		private transient String ddss;
		private String title;
		private String description;
		private int status = 0;
		private String statusTimestamp;
		private List<AvailableFormat> availableFormats;
		
		public DiscoveryItemBuilder(String id, String href) {
			this.id = id;
			this.href = href;
		}

		public DiscoveryItemBuilder setSha256id(String sha256id) {
			this.sha256id = sha256id;
			return this;
		}

		public DiscoveryItemBuilder ddss(String ddss) {
			this.ddss = ddss;
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
	
}
