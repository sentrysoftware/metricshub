package com.sentrysoftware.matrix.agent.config.protocols;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sentrysoftware.matrix.agent.deserialization.SnmpPrivacyDeserializer;
import com.sentrysoftware.matrix.agent.deserialization.SnmpVersionDeserializer;
import com.sentrysoftware.matrix.agent.deserialization.TimeDeserializer;
import com.sentrysoftware.matrix.configuration.IConfiguration;
import com.sentrysoftware.matrix.configuration.SnmpConfiguration;
import com.sentrysoftware.matrix.configuration.SnmpConfiguration.Privacy;
import com.sentrysoftware.matrix.configuration.SnmpConfiguration.SnmpVersion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SnmpProtocolConfig extends AbstractProtocolConfig {

	@Default
	@JsonDeserialize(using = SnmpVersionDeserializer.class)
	private SnmpVersion version = SnmpVersion.V1;

	@Default
	private char[] community = new char[] { 'p', 'u', 'b', 'l', 'i', 'c' };

	@Default
	private Integer port = 161;

	@Default
	@JsonDeserialize(using = TimeDeserializer.class)
	private Long timeout = 120L;

	@JsonDeserialize(using = SnmpPrivacyDeserializer.class)
	private Privacy privacy;

	private char[] privacyPassword;

	private String username;

	private char[] password;

	/**
	 * Create a new {@link SnmpConfiguration} instance based on the current members
	 * 
	 * @return The {@link SnmpConfiguration} instance
	 */
	@Override
	public IConfiguration toProtocol() {
		return SnmpConfiguration
			.builder()
			.version(version)
			.community(String.valueOf(decrypt(community)))
			.username(username)
			.password(super.decrypt(password))
			.privacy(privacy)
			.privacyPassword(super.decrypt(privacyPassword))
			.port(port)
			.timeout(timeout)
			.build();
	}

	@Override
	public String toString() {
		String desc = version.getDisplayName();
		if (version == SnmpVersion.V1 || version == SnmpVersion.V2C) {
			desc = desc + " (" + String.valueOf(community) + ")";
		} else {
			if (username != null) {
				desc = desc + " as " + username;
			}
			if (privacy != null && privacy != Privacy.NO_ENCRYPTION) {
				desc = desc + " (" + privacy + "-encrypted)";
			}
		}
		return desc;
	}

}
