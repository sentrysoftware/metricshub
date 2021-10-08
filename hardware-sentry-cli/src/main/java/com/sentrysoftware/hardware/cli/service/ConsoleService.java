package com.sentrysoftware.hardware.cli.service;

import org.fusesource.jansi.Ansi;

import com.sentrysoftware.matrix.engine.OperationStatus;

public class ConsoleService {

	private static final boolean HAS_CONSOLE = System.console() != null;

	/**
	 * @return whether we have a Console, and thus we should print messages to the user
	 */
	public static boolean hasConsole() {
		return HAS_CONSOLE;
	}

	public static String statusToAnsi(OperationStatus status) {
		if (status == OperationStatus.SUCCESS) {
			return Ansi.ansi().fgGreen().a(status).reset().toString();
		}
		return Ansi.ansi().fgBrightRed().a(status).reset().toString();
	}
}
