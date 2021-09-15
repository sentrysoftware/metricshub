package com.sentrysoftware.matrix.engine.protocol;

import java.util.HashSet;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OSCommandConfig implements IProtocolConfiguration {

	private static final String SUDO = "sudo";

	private boolean useSudo;
	@Default
	private Set<String> useSudoCommands = new HashSet<>();
	@Default
	private String sudoCommand = SUDO;

	@Default
	private Long timeout = 120L;
}
