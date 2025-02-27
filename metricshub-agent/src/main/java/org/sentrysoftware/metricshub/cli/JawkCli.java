package org.sentrysoftware.metricshub.cli;

/*-
 * ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲
 * MetricsHub Agent
 * ჻჻჻჻჻჻
 * Copyright 2023 - 2024 Sentry Software
 * ჻჻჻჻჻჻
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱
 */

import java.io.OutputStream;
import java.io.PrintStream;
import org.sentrysoftware.jawk.Main;

/**
 * Command-line interface for executing AWK scripts with validation.
 */
public class JawkCli {

	static PrintStream printStream;

	/**
	 * Entry point for executing AWK scripts.
	 *
	 * @param args Command line arguments.
	 * @throws Exception if an error occurs during execution.
	 */
	public static void main(String[] args) throws Exception {
		System.setProperty("log4j2.configurationFile", "log4j2-cli.xml");
		System.setOut(printStream == null ? new ReplacingPrintStream(System.out) : printStream);
		new Main(args, System.in, System.out, System.err);
	}

	/**
	 * Sets the {@link PrintStream} for standard output.
	 *
	 * @param ps the {@link PrintStream} to set as the new standard output
	 */
	public static void setPrintStream(final PrintStream ps) {
		printStream = ps;
		System.setOut(ps);
	}

	/**
	 * PrintStream wrapper that replaces Java execution command references in
	 * output.
	 */
	public static class ReplacingPrintStream extends PrintStream {

		/**
		 * Constructs a new ReplacingPrintStream.
		 *
		 * @param out The output stream to wrap.
		 */
		public ReplacingPrintStream(OutputStream out) {
			super(out);
		}

		@Override
		public void println(String x) {
			super.println(replace(x));
		}

		@Override
		public void print(String x) {
			super.print(replace(x));
		}

		/**
		 * Replaces Java execution command patterns with "jawk".
		 *
		 * @param input The original string.
		 * @return The modified string.
		 */
		private String replace(String input) {
			return (input != null) ? input.replaceAll("java\\s-jar\\s.+\\.jar", "jawk") : null;
		}
	}
}
