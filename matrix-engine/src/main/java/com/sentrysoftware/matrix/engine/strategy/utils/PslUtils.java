package com.sentrysoftware.matrix.engine.strategy.utils;

public class PslUtils {

	private PslUtils() { }

	/**
	 * Converts a PSL regex into its Java equivalent.
	 * Method shamelessly taken from somewhere else.
	 * <p>
	 * @param pslRegex Regular expression as used in PSL's grep() function.
	 * @return Regular expression that can be used in Java's Pattern.compile.
	 */
	public static String psl2JavaRegex(String pslRegex) {

		if (pslRegex == null || pslRegex.isEmpty()) {
			return "";
		}

		// We 're going to build the regex char by char
		StringBuilder javaRegex = new StringBuilder();

		// Parse the PSL regex char by char (the very last char will be added unconditionally)
		int i;
		boolean inRange = false;
		for (i = 0 ; i < pslRegex.length() ; i++) {

			char c = pslRegex.charAt(i);

			if (c == '\\' && i < pslRegex.length() - 1) {

				if (inRange) {

					// Escape works differently in [] ranges
					// We simply need to double backslashes
					javaRegex.append("\\\\");

				} else {

					// Backslashes in PSL regex are utterly broken
					// We need to handle all cases here to convert to proper regex in Java
					char nextChar = pslRegex.charAt(i + 1);
					if (nextChar == '(' || nextChar == ')' || nextChar == '|') {
						// Replace \( with (
						// Replace \) with )
						// Replace \| with |
						javaRegex.append(nextChar);
						i++;
					} else if (nextChar == '<' || nextChar == '>') {
						// Replace \< and \> with \b
						javaRegex.append("\\b");
						i++;
					} else if ("^$.*+?[]\\".indexOf(nextChar) > -1) {
						// Append the backslash and what it's protecting, as is
						javaRegex.append('\\').append(nextChar);
						i++;
					} else {
						// Invalid escape sequence, skip it
						javaRegex.append(nextChar);
						i++;
					}

				}

			} else if (c == '(' || c == ')' || c == '|' || c == '{' || c == '}') {

				javaRegex.append('\\').append(c);

			} else if (c == '[') {

				// Regex ranges have a different escaping system, so we need to
				// know when we're inside a [a-z] range or not
				javaRegex.append('[');
				inRange = true;

			} else if (c == ']') {

				// Getting out of a [] range
				javaRegex.append(']');
				inRange = false;

			} else {

				// Other cases
				javaRegex.append(c);
			}

		}

		return javaRegex.toString();
	}
}
