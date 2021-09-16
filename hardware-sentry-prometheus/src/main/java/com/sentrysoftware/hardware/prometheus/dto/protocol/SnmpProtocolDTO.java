package com.sentrysoftware.hardware.prometheus.dto.protocol;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sentrysoftware.hardware.prometheus.deserialization.SnmpPrivacyDeserializer;
import com.sentrysoftware.hardware.prometheus.deserialization.SnmpVersionDeserializer;
import com.sentrysoftware.matrix.engine.protocol.IProtocolConfiguration;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol.Privacy;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol.SNMPVersion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SnmpProtocolDTO {

	@Default
	@JsonDeserialize(using = SnmpVersionDeserializer.class)
	private SNMPVersion version = SNMPVersion.V1;
	@Default
	private String community = "public";
	@Default
	private Integer port = 161;
	@Default
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
	public IProtocolConfiguration toProtocol() {
		return SNMPProtocol
				.builder()
				.version(version)
				.community(community)
				.username(username)
				.password(password)
				.privacy(privacy)
				.privacyPassword(privacyPassword)
				.port(port)
				.timeout(timeout)
				.build();
	}


}
