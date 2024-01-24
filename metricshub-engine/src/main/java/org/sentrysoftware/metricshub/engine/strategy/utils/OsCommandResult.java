package org.sentrysoftware.metricshub.engine.strategy.utils;

import lombok.Data;

/**
 * The {@code OsCommandResult} class represents the result of an operating system command execution.
 */
@Data
public class OsCommandResult {

	private final String result;
	private final String noPasswordCommand;
}
