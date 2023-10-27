package com.sentrysoftware.metricshub.agent.config.protocols;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sentrysoftware.metricshub.agent.deserialization.TimeDeserializer;
import com.sentrysoftware.metricshub.engine.configuration.IConfiguration;
import com.sentrysoftware.metricshub.engine.configuration.SshConfiguration;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
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
public class SshProtocolConfig extends AbstractProtocolConfig {

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
	 * Create a new {@link SshConfiguration} instance based on the current members
	 *
	 * @return The {@link SshConfiguration} instance
	 */
	@Override
	public IConfiguration toConfiguration() {
		return SshConfiguration
			.sshConfigurationBuilder()
			.username(username)
			.password(super.decrypt(password))
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