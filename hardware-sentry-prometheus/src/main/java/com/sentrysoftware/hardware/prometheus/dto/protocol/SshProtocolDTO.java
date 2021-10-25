package com.sentrysoftware.hardware.prometheus.dto.protocol;

import java.io.File;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sentrysoftware.hardware.prometheus.deserialization.TimeDeserializer;
import com.sentrysoftware.matrix.engine.protocol.IProtocolConfiguration;
import com.sentrysoftware.matrix.engine.protocol.SSHProtocol;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.extern.slf4j.Slf4j;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class SshProtocolDTO implements IProtocolConfigDTO {

	@Default
	@JsonDeserialize(using = TimeDeserializer.class)
	private Long timeout = 120L;

	private String username;
	private char[] password;
	private File privateKey;
	private boolean useSudo;

	@Default
	private Set<String> useSudoCommands = new HashSet<>();

	@Default
	private String sudoCommand = "sudo";

	/**
	 * Create a new {@link SSHProtocol} instance based on the current members
	 *
	 * @return The {@link SSHProtocol} instance
	 */
	@Override
	public IProtocolConfiguration toProtocol() {
		return SSHProtocol
				.builder()
				.username(username)
				.password(IProtocolConfigDTO.decrypt(password, log))
				.privateKey(privateKey)
				.timeout(timeout)
				.useSudo(useSudo)
				.useSudoCommands(useSudoCommands)
				.sudoCommand(sudoCommand)
				.build();
	}

	@Override
	public String toString() {
		String desc = "SSH";
		if (username != null) {
			desc = desc + " as " + username;
		}
		return desc;
	}
}
