package com.sentrysoftware.matrix.engine.protocol;

import java.util.HashSet;
import java.util.Set;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public abstract class AbstractCommand implements IProtocolConfiguration {

	private static final String SUDO = "sudo";
	public static final Long DEFAULT_TIMEOUT = 30L;

	/**
	 * @param useSudo
	 * @param useSudoCommands
	 * @param sudoCommand
	 * @param timeout
	 */
	public AbstractCommand(boolean useSudo, Set<String> useSudoCommands, String sudoCommand, Long timeout) {
		this.useSudo = useSudo;
		this.useSudoCommands = useSudoCommands == null ? new HashSet<>() : useSudoCommands;
		this.sudoCommand = sudoCommand == null ? SUDO : sudoCommand;
		this.timeout = timeout == null ? DEFAULT_TIMEOUT : timeout;
	}

	private boolean useSudo;
	private Set<String> useSudoCommands = new HashSet<>();
	private String sudoCommand = SUDO;
	private Long timeout = DEFAULT_TIMEOUT;

}
