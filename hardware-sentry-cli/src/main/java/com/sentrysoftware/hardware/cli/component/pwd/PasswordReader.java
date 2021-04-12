package com.sentrysoftware.hardware.cli.component.pwd;

import org.jline.reader.LineReader;

public class PasswordReader {

	private static final String COLON_SPACE = ": ";

	private static final Character DEFAULT_MASK = '*';

	private Character mask;
	private LineReader lineReader;

	public PasswordReader(LineReader lineReader, Character mask) {
		this.lineReader = lineReader;
		this.mask = mask != null ? mask : DEFAULT_MASK;
	}

	public PasswordReader(LineReader lineReader) {
		this(lineReader, DEFAULT_MASK);
	}

	public String prompt(String prompt) {
		return prompt(prompt, true);
	}

	public String prompt(String prompt, boolean echo) {
		String answer;
		if (echo) {
			answer = lineReader.readLine(prompt + COLON_SPACE);
		} else {
			answer = lineReader.readLine(prompt + COLON_SPACE, mask);
		}
		return answer;
	}
}
