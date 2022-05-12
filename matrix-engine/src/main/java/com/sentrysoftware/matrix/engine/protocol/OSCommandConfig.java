package com.sentrysoftware.matrix.engine.protocol;

import java.util.Set;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class OSCommandConfig extends AbstractCommand {

	@Builder
	public OSCommandConfig(boolean useSudo, Set<String> useSudoCommands, String sudoCommand, Long timeout) {
		super(useSudo, useSudoCommands, sudoCommand, timeout);
	}

	@Override
	public String toString() {
		return "Local Commands";
	}
}
