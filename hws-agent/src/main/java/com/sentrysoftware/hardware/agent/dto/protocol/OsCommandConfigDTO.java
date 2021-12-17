package com.sentrysoftware.hardware.agent.dto.protocol;

import java.util.HashSet;
import java.util.Set;

import com.sentrysoftware.matrix.engine.protocol.IProtocolConfiguration;
import com.sentrysoftware.matrix.engine.protocol.OSCommandConfig;

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
public class OsCommandConfigDTO extends AbstractProtocolDTO {

	private boolean useSudo;
	
	@Default
	private Set<String> useSudoCommands = new HashSet<>();
	
	@Default
	private String sudoCommand = "sudo";
	
	@Default
	private Long timeout = 120L;

	/**
	 * Create a new {@link OSCommandConfig} instance based on the current members
	 *
	 * @return The {@link OSCommandConfig} instance
	 */
	@Override
	public IProtocolConfiguration toProtocol() {
		return OSCommandConfig
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
