package com.sentrysoftware.hardware.agent.dto.protocol;

import java.util.HashSet;
import java.util.Set;

import com.sentrysoftware.matrix.engine.protocol.IProtocolConfiguration;
import com.sentrysoftware.matrix.engine.protocol.OsCommandConfig;

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
public class OsCommandConfigDto extends AbstractProtocolDto {

	private boolean useSudo;
	
	@Default
	private Set<String> useSudoCommands = new HashSet<>();
	
	@Default
	private String sudoCommand = "sudo";
	
	@Default
	private Long timeout = 120L;

	/**
	 * Create a new {@link OsCommandConfig} instance based on the current members
	 *
	 * @return The {@link OsCommandConfig} instance
	 */
	@Override
	public IProtocolConfiguration toProtocol() {
		return OsCommandConfig
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
