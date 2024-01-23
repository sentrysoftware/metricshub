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
import org.sentrysoftware.metricshub.engine.configuration.IpmiConfiguration;

/**
 * Configuration class for the IPMI protocol.
 * Extends {@link AbstractProtocolConfig}.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class IpmiProtocolConfig extends AbstractProtocolConfig {

	@Default
	@JsonDeserialize(using = TimeDeserializer.class)
	private Long timeout = 120L;

	private String username;

	private char[] password;

	private byte[] bmcKey;

	private boolean skipAuth;

	/**
	 * Create a new {@link IpmiConfiguration} instance based on the current members
	 *
	 * @return The {@link IpmiConfiguration} instance
	 */
	@Override
	public IConfiguration toConfiguration() {
		return IpmiConfiguration
			.builder()
			.username(username)
			.password(super.decrypt(password))
			.bmcKey(bmcKey)
			.skipAuth(skipAuth)
			.timeout(timeout)
			.build();
	}

	@Override
	public String toString() {
		String desc = "IPMI";
		if (username != null) {
			desc = desc + " as " + username;
		}
		return desc;
	}
}
