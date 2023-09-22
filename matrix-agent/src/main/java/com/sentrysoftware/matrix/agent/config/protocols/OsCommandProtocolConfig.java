package com.sentrysoftware.matrix.agent.config.protocols;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sentrysoftware.matrix.agent.deserialization.TimeDeserializer;
import com.sentrysoftware.matrix.configuration.IConfiguration;
import com.sentrysoftware.matrix.configuration.OsCommandConfiguration;
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
public class OsCommandProtocolConfig extends AbstractProtocolConfig {

	private boolean useSudo;

	@Default
	private Set<String> useSudoCommands = new HashSet<>();

	@Default
	private String sudoCommand = "sudo";

	@Default
	@JsonDeserialize(using = TimeDeserializer.class)
	private Long timeout = 120L;

	/**
	 * Create a new {@link OsCommandConfiguration} instance based on the current members
	 *
	 * @return The {@link OsCommandConfiguration} instance
	 */
	@Override
	public IConfiguration toConfigurartion() {
		return OsCommandConfiguration
			.builder()
			.useSudo(useSudo)
			.useSudoCommands(useSudoCommands)
			.sudoCommand(sudoCommand)
			.timeout(timeout)
			.build();
	}

	@Override
	public String toString() {
		return "Local Commands";
	}
}
