package com.sentrysoftware.metricshub.engine.configuration;

import java.util.HashSet;
import java.util.Set;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class OsCommandConfiguration implements IConfiguration {

	private static final String SUDO = "sudo";
	public static final Long DEFAULT_TIMEOUT = 30L;
	private boolean useSudo;
	private Set<String> useSudoCommands = new HashSet<>();
	private String sudoCommand = SUDO;
	private Long timeout = DEFAULT_TIMEOUT;

	@Builder
	public OsCommandConfiguration(
		final boolean useSudo,
		final Set<String> useSudoCommands,
		final String sudoCommand,
		final Long timeout
	) {
		this.useSudo = useSudo;
		this.useSudoCommands = useSudoCommands == null ? new HashSet<>() : useSudoCommands;
		this.sudoCommand = sudoCommand == null ? SUDO : sudoCommand;
		this.timeout = timeout == null ? DEFAULT_TIMEOUT : timeout;
	}

	@Override
	public String toString() {
		return "Local Commands";
	}
}
