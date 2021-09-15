package com.sentrysoftware.matrix.engine.protocol;

import java.io.File;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class SSHProtocol extends OSCommandConfig implements IProtocolConfiguration {

	private String username;
	private char[] password;
	private File privateKey;

	@Builder(builderMethodName = "sshProtocolBuilder")
	public SSHProtocol(boolean useSudo, Set<String> useSudoCommands, String sudoCommand, Long timeout, String username,
			char[] password, File privateKey) {
		super(useSudo, useSudoCommands, sudoCommand, timeout);

		this.username = username;
		this.password = password;
		this.privateKey = privateKey;
	}

}
