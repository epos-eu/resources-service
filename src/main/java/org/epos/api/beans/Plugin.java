package org.epos.api.beans;

import java.util.List;
import java.util.Objects;

public class Plugin {

	private String distributionId;
	private List<Relations> relations;

	public String getDistributionId() {
		return distributionId;
	}

	public void setDistributionId(String distributionId) {
		this.distributionId = distributionId;
	}

	public List<Relations> getRelations() {
		return relations;
	}

	public void setRelations(List<Relations> relations) {
		this.relations = relations;
	}

	@Override
	public boolean equals(Object o) {
		if (!(o instanceof Plugin))
			return false;
		Plugin plugin = (Plugin) o;
		return Objects.equals(distributionId, plugin.distributionId) && Objects.equals(relations, plugin.relations);
	}

	@Override
	public int hashCode() {
		return Objects.hash(distributionId, relations);
	}

	@Override
	public String toString() {
		return "Plugin{" +
				"distributionId='" + distributionId + '\'' +
				", relations=" + relations +
				'}';
	}

	public class Relations {
		private String pluginId;
		private String inputFormat;
		private String outputFormat;

		public String getPluginId() {
			return pluginId;
		}

		public void setPluginId(String pluginId) {
			this.pluginId = pluginId;
		}

		public String getInputFormat() {
			return inputFormat;
		}

		public void setInputFormat(String inputFormat) {
			this.inputFormat = inputFormat;
		}

		public String getOutputFormat() {
			return outputFormat;
		}

		public void setOutputFormat(String outputFormat) {
			this.outputFormat = outputFormat;
		}

		@Override
		public boolean equals(Object o) {
			if (!(o instanceof Relations))
				return false;
			Relations relations = (Relations) o;
			return Objects.equals(pluginId, relations.pluginId) && Objects.equals(inputFormat, relations.inputFormat)
					&& Objects.equals(outputFormat, relations.outputFormat);
		}

		@Override
		public int hashCode() {
			return Objects.hash(pluginId, inputFormat, outputFormat);
		}

		@Override
		public String toString() {
			return "Relations{" +
					"pluginId='" + pluginId + '\'' +
					", inputFormat='" + inputFormat + '\'' +
					", outputFormat='" + outputFormat + '\'' +
					'}';
		}
	}
}
