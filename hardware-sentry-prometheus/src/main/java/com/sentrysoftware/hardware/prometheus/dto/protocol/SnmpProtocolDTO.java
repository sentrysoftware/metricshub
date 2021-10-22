package com.sentrysoftware.hardware.prometheus.dto.protocol;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sentrysoftware.hardware.prometheus.deserialization.SnmpPrivacyDeserializer;
import com.sentrysoftware.hardware.prometheus.deserialization.SnmpVersionDeserializer;
import com.sentrysoftware.hardware.prometheus.deserialization.TimeDeserializer;
import com.sentrysoftware.matrix.engine.protocol.IProtocolConfiguration;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol.Privacy;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol.SNMPVersion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class SnmpProtocolDTO implements IProtocolConfigDTO {

	@Default
	@JsonDeserialize(using = SnmpVersionDeserializer.class)
	private SNMPVersion version = SNMPVersion.V1;
	@Default
	private String community = "public";
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
	 * Create a new {@link SNMPProtocol} instance based on the current members
	 * 
	 * @return The {@link SNMPProtocol} instance
	 */
	@Override
	public IProtocolConfiguration toProtocol() {
		return SNMPProtocol
				.builder()
				.version(version)
				.community(community)
				.username(username)
				.password(IProtocolConfigDTO.decrypt(password, log))
				.privacy(privacy)
				.privacyPassword(IProtocolConfigDTO.decrypt(privacyPassword, log))
				.port(port)
				.timeout(timeout)
				.build();
	}

	@Override
	public String toString() {
		String desc = version.getDisplayName();
		if (version == SNMPVersion.V1 || version == SNMPVersion.V2C) {
			desc = desc + " (" + community + ")";
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
