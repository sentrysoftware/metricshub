package com.sentrysoftware.matrix.strategy.utils;

import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.EMPTY;

import com.sentrysoftware.matrix.strategy.source.SourceTable;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class PslUtils {

	private static final String SPECIAL_CHARACTERS = "^$.*+?[]\\";
	private static final String BACKSLASH_B = "\\b";
	private static final String DOT_PLUS = ".+";
	private static final String DOT = ".";
	private static final char BACKSLASH_CHAR = '\\';
	private static final char OPENING_PARENTHESIS_CHAR = '(';
	private static final char CLOSING_PARENTHESIS_CHAR = ')';
	private static final char PIPE_CHAR = '|';
	private static final char OPENING_CURLY_BRACKET_CHAR = '{';
	private static final char CLOSING_CURLY_BRACKET_CHAR = '}';
	private static final char OPENING_SQUARE_BRACKET_CHAR = '[';
	private static final char CLOSING_SQUARE_BRACKET_CHAR = ']';
	private static final char LOWER_THAN_CHAR = '<';
	private static final char GREATER_THAN_CHAR = '>';

	// Private constructor to prevent instantiation of PslUtils
	private PslUtils() {}

	/**
	 * Converts a PSL regex into its Java equivalent.
	 * Method shamelessly taken from somewhere else.
	 * <p>
	 * @param pslRegex Regular expression as used in PSL's grep() function.
	 * @return Regular expression that can be used in Java's Pattern.compile.
	 */
	public static String psl2JavaRegex(final String pslRegex) {
		if (pslRegex == null || pslRegex.isEmpty()) {
			return "";
		}

		if (DOT.equals(pslRegex)) {
			return DOT_PLUS;
		}

		// We 're going to build the regex char by char
		StringBuilder javaRegex = new StringBuilder();

		// Parse the PSL regex char by char (the very last char will be added unconditionally)
		int i;
		boolean inRange = false;
		for (i = 0; i < pslRegex.length(); i++) {
			char c = pslRegex.charAt(i);

			if (c == BACKSLASH_CHAR && i < pslRegex.length() - 1) {
				i = handleBackSlash(pslRegex, inRange, i, javaRegex);
			} else if (
				c == OPENING_PARENTHESIS_CHAR ||
				c == CLOSING_PARENTHESIS_CHAR ||
				c == PIPE_CHAR ||
				c == OPENING_CURLY_BRACKET_CHAR ||
				c == CLOSING_CURLY_BRACKET_CHAR
			) {
				javaRegex.append(BACKSLASH_CHAR).append(c);
			} else if (c == OPENING_SQUARE_BRACKET_CHAR) {
				// Regex ranges have a different escaping system, so we need to
				// know when we're inside a [a-z] range or not
				javaRegex.append(c);
				inRange = true;
			} else if (c == CLOSING_SQUARE_BRACKET_CHAR) {
				// Getting out of a [] range
				javaRegex.append(c);
				inRange = false;
			} else {
				// Other cases
				javaRegex.append(c);
			}
		}

		return javaRegex.toString();
	}

	/**
	 * Properly converts a backslash (present in the given PSL regular expression)
	 * to its Java regular expression version,
	 * and appends the result to the given Java regular expression {@link StringBuilder}.
	 *
	 * @param pslRegex	Regular expression as used in PSL's grep() function.
	 * @param inRange	Indicate whether the backslash is within a range (i.e. between '[' and ']').
	 * @param index		The index of the backslash in the regular expression.
	 * @param javaRegex	The {@link StringBuilder} holding the resulting Java regular expression.
	 *
	 * @return			The new value of the index
	 */
	private static int handleBackSlash(
		final String pslRegex,
		final boolean inRange,
		final int index,
		final StringBuilder javaRegex
	) {
		int result = index;

		if (inRange) {
			// Escape works differently in [] ranges
			// We simply need to double backslashes
			javaRegex.append("\\\\");
		} else {
			// Backslashes in PSL regex are utterly broken
			// We need to handle all cases here to convert to proper regex in Java
			char nextChar = pslRegex.charAt(index + 1);
			if (nextChar == LOWER_THAN_CHAR || nextChar == GREATER_THAN_CHAR) {
				// Replace \< and \> with \b
				javaRegex.append(BACKSLASH_B);
				result++;
			} else if (SPECIAL_CHARACTERS.indexOf(nextChar) > -1) {
				// Append the backslash and what it's protecting, as is
				javaRegex.append(BACKSLASH_CHAR).append(nextChar);
				result++;
			} else {
				// Append the next character
				javaRegex.append(nextChar);
				result++;
			}
		}

		return result;
	}

	/**
	 * Converts an entry and its result into an extended JSON format:
	 * 	{
	 * 		"Entry":{
	 * 			"Full":"<entry>",
	 * 			"Column(1)":"<1st field value>",
	 * 	    	"Column(2)":"<2nd field value>",
	 * 			"Column(3)":"<3rd field value>",
	 * 			"Value":<result> <- Result must be properly formatted (either "result" or {"property":"value"}
	 * 		}
	 * 	}
	 *
	 * @param entry The row of values.
	 * @param tableResult The output returned by the SourceVisitor.
	 * @return String value
	 */
	public static String formatExtendedJSON(@NonNull String row, @NonNull SourceTable tableResult)
		throws IllegalArgumentException {
		if (row.isEmpty()) {
			log.error("formatExtendedJSON received Empty row of values. Returning empty string.");
			return EMPTY;
		}

		String rawData = tableResult.getRawData();
		if (rawData == null || rawData.isEmpty()) {
			log.error("formatExtendedJSON received Empty SourceTable data {}. Returning empty string.", tableResult);
			return EMPTY;
		}

		StringBuilder jsonContent = new StringBuilder();
		jsonContent.append("{\n\"Entry\":{\n\"Full\":\"").append(row).append("\",\n");

		int i = 1;

		for (String value : row.split(",")) {
			jsonContent.append("\"Column(").append(i).append(")\":\"").append(value).append("\",\n");
			i++;
		}

		jsonContent.append("\"Value\":").append(rawData).append("\n}\n}");

		return jsonContent.toString();
	}
}
