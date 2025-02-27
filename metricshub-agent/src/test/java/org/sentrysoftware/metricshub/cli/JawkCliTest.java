package org.sentrysoftware.metricshub.cli;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

class JawkCliTest {

	private final String SCRIPT_FILE_PATH = "./src/test/resources/cli/script.awk";
	private final String INPUT_FILE_PATH = "./src/test/resources/cli/input.txt";
	private final String INPUT_FILE_PATH2 = "./src/test/resources/cli/input2.txt";
	private final String RESULT_FILE_PATH = "./src/test/resources/cli/result.txt";
	private final String SCRIPT = "BEGIN { FS=\";\" } \n { print $2 }";
	private final String RESULT = "Brand\r\nIntel\r\nNvidia\r\nASUS\r\nCorsair\r\nSamsung";

	static PrintStream originalOutputStream = System.out;
	static PrintStream printStream;
	static CustomOutputStream customOutputStream;

	@BeforeAll
	static void setupOutputStream() {
		customOutputStream = new CustomOutputStream();
		printStream = new PrintStream(customOutputStream);
		JawkCli.setPrintStream(printStream);
	}

	@Test
	@EnabledOnOs(OS.WINDOWS)
	void testMainWithFiles() throws Exception {
		String[] args = { "-f", SCRIPT_FILE_PATH, INPUT_FILE_PATH };
		JawkCli.main(args);
		final String expectedResult = Files.readString(Path.of(RESULT_FILE_PATH));
		customOutputStream.flush();
		printStream.flush();
		assertTrue(customOutputStream.toString().contains(expectedResult));
	}

	@Test
	@EnabledOnOs(OS.WINDOWS)
	void testMainWithInlineScript() throws Exception {
		String[] args = { SCRIPT, INPUT_FILE_PATH2 };
		JawkCli.main(args);
		customOutputStream.flush();
		printStream.flush();
		assertTrue(customOutputStream.toString().trim().contains(RESULT));
	}

	static class CustomOutputStream extends OutputStream {

		StringBuilder stringBuilder;

		public CustomOutputStream() {
			stringBuilder = new StringBuilder();
		}

		@Override
		public void write(int i) {
			stringBuilder.append((char) i);
		}

		@Override
		public String toString() {
			return stringBuilder.toString();
		}
	}
}
