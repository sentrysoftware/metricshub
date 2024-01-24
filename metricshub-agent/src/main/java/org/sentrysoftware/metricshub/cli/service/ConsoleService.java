package org.sentrysoftware.metricshub.cli.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Service class providing utility methods related to console interactions.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConsoleService {

	private static final boolean HAS_CONSOLE = System.console() != null;

	/**
	 * Checks whether the application has access to a console.
	 *
	 * @return whether we have a Console, and thus we should print messages to the user
	 */
	public static boolean hasConsole() {
		return HAS_CONSOLE;
	}
}
