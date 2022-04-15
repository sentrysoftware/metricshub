package com.sentrysoftware.hardware.agent.dto.protocol;

import java.nio.file.Path;
import java.util.List;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sentrysoftware.hardware.agent.deserialization.TimeDeserializer;
import com.sentrysoftware.matrix.engine.protocol.IProtocolConfiguration;
import com.sentrysoftware.matrix.engine.protocol.WinRMProtocol;
import com.sentrysoftware.matsya.winrm.service.client.auth.AuthenticationEnum;

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
public class WinRMProtocolDTO extends AbstractProtocolDTO {

	private String username;
	private char[] password;
	private String namespace;
	private String command;
	private String workingDirectory;
	private Integer port;
	private String protocol; // HTTP or HTTPS
	private Path ticketCache;
	private List<AuthenticationEnum> authentications;

	@Default
	@JsonDeserialize(using = TimeDeserializer.class)
	private Long timeout = 120L;

	/**
	 * Create a new {@link WinRMProtocol} instance based on the current members
	 *
	 * @return The {@link WinRMProtocol} instance
	 */
	@Override
	public IProtocolConfiguration toProtocol() {
		return WinRMProtocol
				.builder()
				.username(username)
				.password(super.decrypt(password))
				.namespace(namespace)
				.command(command)
				.workingDirectory(workingDirectory)
				.port(port)
				.protocol(protocol)
				.ticketCache(ticketCache)
				.authentications(authentications)
				.timeout(timeout)
				.build();
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
