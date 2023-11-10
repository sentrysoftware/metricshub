package com.sentrysoftware.metricshub.cli.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConsoleService {

	private static final boolean HAS_CONSOLE = System.console() != null;

	/**
	 * @return whether we have a Console, and thus we should print messages to the user
	 */
	public static boolean hasConsole() {
		return HAS_CONSOLE;
	}
}
