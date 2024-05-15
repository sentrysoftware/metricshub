package org.sentrysoftware.metricshub.agent.extension;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sentrysoftware.metricshub.engine.common.exception.InvalidConfigurationException;
import org.sentrysoftware.metricshub.engine.common.helpers.StringHelper;
import org.sentrysoftware.metricshub.engine.configuration.IConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.TransportProtocols;

/**
 * The WbemConfiguration interface represents the configuration for the Web-Based Enterprise Management protocol in the MetricsHub engine.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WbemTestConfiguration implements IConfiguration {

	@Builder.Default
	private final TransportProtocols protocol = TransportProtocols.HTTPS;

	@Builder.Default
	private final Integer port = 5989;

	private String namespace;

	@Builder.Default
	private final Long timeout = 120L;

	String username;
	char[] password;
	String vCenter;

	@Override
	public String toString() {
		String description = protocol + "/" + port;
		if (username != null) {
			description = description + " as " + username;
		}
		return description;
	}

	/**
	 * Validate the given WBEM information (username and timeout)
	 *
	 * @param resourceKey Resource unique identifier
	 * @throws InvalidConfigurationException
	 */
	@Override
	public void validateConfiguration(String resourceKey) throws InvalidConfigurationException {
		StringHelper.validateConfigurationAttribute(
			timeout,
			attr -> attr == null || attr < 0L,
			() ->
				String.format(
					"Resource %s - Timeout value is invalid for protocol %s." +
					" Timeout value returned: %s. This resource will not be monitored. Please verify the configured timeout value.",
					resourceKey,
					"WBEM",
					timeout
				)
		);

		StringHelper.validateConfigurationAttribute(
			port,
			attr -> attr == null || attr < 1 || attr > 65535,
			() ->
				String.format(
					"Resource %s - Invalid port configured for protocol %s. Port value returned: %s." +
					" Port value returned: %s. This resource will not be monitored. Please verify the configured port value.",
					resourceKey,
					"WBEM",
					port
				)
		);

		StringHelper.validateConfigurationAttribute(
			username,
			attr -> attr != null && attr.isBlank(),
			() ->
				String.format(
					"Resource %s - No username configured for protocol %s." +
					" This resource will not be monitored. Please verify the configured username.",
					resourceKey,
					"WBEM"
				)
		);

		StringHelper.validateConfigurationAttribute(
			vCenter,
			attr -> attr != null && attr.isBlank(),
			() ->
				String.format(
					"Resource %s - vCenter value is invalid for protocol %s." +
					" This resource will not be monitored. Please verify the configured vCenter value.",
					resourceKey,
					"WBEM"
				)
		);
	}
}
