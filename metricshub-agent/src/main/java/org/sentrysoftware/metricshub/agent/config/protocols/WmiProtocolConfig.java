package org.sentrysoftware.metricshub.agent.config.protocols;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.sentrysoftware.metricshub.agent.deserialization.TimeDeserializer;
import org.sentrysoftware.metricshub.engine.configuration.IConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.WmiConfiguration;

/**
 * Configuration class for the WMI (Windows Management Instrumentation) protocol.
 * Extends {@link AbstractProtocolConfig}.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class WmiProtocolConfig extends AbstractProtocolConfig {

	private String username;

	private char[] password;

	private String namespace;

	@Default
	@JsonDeserialize(using = TimeDeserializer.class)
	private Long timeout = 120L;

	/**
	 * Create a new {@link WmiConfiguration} instance based on the current members
	 *
	 * @return The {@link WmiConfiguration} instance
	 */
	@Override
	public IConfiguration toConfiguration() {
		return WmiConfiguration
			.builder()
			.namespace(namespace)
			.username(username)
			.password(super.decrypt(password))
			.timeout(timeout)
			.build();
	}

	@Override
	public String toString() {
		String desc = "WMI";
		if (username != null) {
			desc = desc + " as " + username;
		}
		return desc;
	}
}
