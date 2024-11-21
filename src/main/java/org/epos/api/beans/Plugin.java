package org.epos.api.beans;

public class Plugin {
	private String id;
	private String softwareSourceCodeId;
	private String softwareApplicationId;
	private String version;
	private String proxyType;
	private String runtime;
	private String execution;
	private String installed;
	private String enabled;

	public Plugin(String id, String softwareSourceCodeId, String softwareApplicationId, String version,
			String proxyType, String runtime, String execution, String installed, String enabled) {
		this.id = id;
		this.softwareSourceCodeId = softwareSourceCodeId;
		this.softwareApplicationId = softwareApplicationId;
		this.version = version;
		this.proxyType = proxyType;
		this.runtime = runtime;
		this.execution = execution;
		this.installed = installed;
		this.enabled = enabled;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSoftwareSourceCodeId() {
		return softwareSourceCodeId;
	}

	public void setSoftwareSourceCodeId(String softwareSourceCodeId) {
		this.softwareSourceCodeId = softwareSourceCodeId;
	}

	public String getSoftwareApplicationId() {
		return softwareApplicationId;
	}

	public void setSoftwareApplicationId(String softwareApplicationId) {
		this.softwareApplicationId = softwareApplicationId;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getProxyType() {
		return proxyType;
	}

	public void setProxyType(String proxyType) {
		this.proxyType = proxyType;
	}

	public String getRuntime() {
		return runtime;
	}

	public void setRuntime(String runtime) {
		this.runtime = runtime;
	}

	public String getExecution() {
		return execution;
	}

	public void setExecution(String execution) {
		this.execution = execution;
	}

	public String getInstalled() {
		return installed;
	}

	public void setInstalled(String installed) {
		this.installed = installed;
	}

	public String getEnabled() {
		return enabled;
	}

	public void setEnabled(String enabled) {
		this.enabled = enabled;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		Plugin other = (Plugin) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
