package org.sentrysoftware.metricshub.engine.configuration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * The WmiConfiguration interface represents the configuration for the Windows Management Instrumentation protocol in the MetricsHub engine.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WmiConfiguration implements IWinConfiguration {

	private String username;
	private char[] password;
	private String namespace;

	@Builder.Default
	private Long timeout = 120L;

	@Override
	public String toString() {
		String description = "WMI";
		if (username != null) {
			description = description + " as " + username;
		}
		return description;
	}
}
