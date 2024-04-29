package org.sentrysoftware.metricshub.extension.winrm;

import static com.fasterxml.jackson.annotation.Nulls.SKIP;

import java.util.List;

import org.sentrysoftware.metricshub.engine.common.exception.InvalidConfigurationException;
import org.sentrysoftware.metricshub.engine.common.helpers.StringHelper;
import org.sentrysoftware.metricshub.engine.configuration.TransportProtocols;
import org.sentrysoftware.metricshub.engine.deserialization.TimeDeserializer;
import org.sentrysoftware.metricshub.extension.win.IWinConfiguration;
import org.sentrysoftware.winrm.service.client.auth.AuthenticationEnum;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder.Default;

/**
 * The WinRmConfiguration interface represents the configuration for the Windows Management Instrumentation protocol in the MetricsHub engine.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WinRmConfiguration implements IWinConfiguration {

	private String username;

	private char[] password;

	private String namespace;

	@Default
	private Integer port = 5985;

	@Default
	@JsonSetter(nulls = SKIP)
	private TransportProtocols protocol = TransportProtocols.HTTP;

	private List<AuthenticationEnum> authentications;

	@Default
	@JsonDeserialize(using = TimeDeserializer.class)
	private Long timeout = 120L;	

	@Override
	public void validateConfiguration(String resourceKey) throws InvalidConfigurationException {
		StringHelper.validateConfigurationAttribute(
			port,
			attr -> attr == null || attr < 1 || attr > 65535,
			() -> String.format(
				"Resource %s - Invalid port configured for protocol %s. Port value returned: %s." +
				" This resource will not be monitored. Please verify the configured port value.", 
				resourceKey, 
				"WinRm", 
				port
			)
		);

		StringHelper.validateConfigurationAttribute(
			timeout,
			attr -> attr == null || attr < 0L,
			() -> String.format(
				"Resource %s - Timeout value is invalid for protocol %s." +
				" Timeout value returned: %s. This resource will not be monitored. Please verify the configured timeout value.", 
				resourceKey, 
				"WinRM", 
				timeout
			)
		);

		StringHelper.validateConfigurationAttribute(
			username,
			attr -> attr == null || attr.isBlank(),
			() -> String.format(
				"Resource %s - No username configured for protocol %s." +
				" This resource will not be monitored. Please verify the configured username.", 
				resourceKey, 
				"WinRm"
			)
		);
	}

	@Override
	public String toString() {
		String desc = "WinRM";
		if (username != null) {
			desc = desc + " as " + username;
		}
		return desc;
	}
}
