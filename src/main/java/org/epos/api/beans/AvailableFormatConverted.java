package org.epos.api.beans;

/**
 * Extends {@link AvailableFormat}adding the attributes needed to represent
 * converted formats:
 * 
 * <ul>
 * <li>{@code inputFormat}: the input format that the plugin requires to
 * convert to this format</li>
 * <li>{@code pluginId}: the ID of the plugin to convert to this format</li>
 * 
 **/
public class AvailableFormatConverted extends AvailableFormat {

	private String inputFormat;
	private String pluginId;

	public AvailableFormatConverted(AvailableFormatConvertedBuilder builder) {
		super(builder);
		this.inputFormat = builder.inputFormat;
		this.pluginId = builder.pluginId;
	}

	public String getInputFormat() {
		return inputFormat;
	}

	public String getPluginId() {
		return pluginId;
	}

	public static class AvailableFormatConvertedBuilder extends AvailableFormatBuilder {
		private String inputFormat;
		private String pluginId;

		public AvailableFormatConvertedBuilder inputFormat(String inputFormat) {
			this.inputFormat = inputFormat;
			return this;
		}

		public AvailableFormatConvertedBuilder pluginId(String pluginId) {
			this.pluginId = pluginId;
			return this;
		}

		public AvailableFormatConverted build() {
			return new AvailableFormatConverted(this);
		}
	}
}
