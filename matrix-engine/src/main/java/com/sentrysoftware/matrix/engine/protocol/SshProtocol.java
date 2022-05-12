package com.sentrysoftware.matrix.engine.protocol;

import java.io.File;
import java.util.Set;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SshProtocol extends AbstractCommand {

	private String username;
	private char[] password;
	private File privateKey;

	@Builder()
	public SshProtocol(boolean useSudo, Set<String> useSudoCommands, String sudoCommand, Long timeout, String username,
			char[] password, File privateKey) {
		super(useSudo, useSudoCommands, sudoCommand, timeout);

		this.username = username;
		this.password = password;
		this.privateKey = privateKey;
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
