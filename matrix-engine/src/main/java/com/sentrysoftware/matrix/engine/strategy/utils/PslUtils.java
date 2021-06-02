package com.sentrysoftware.matrix.engine.strategy.utils;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.stream.Collectors;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.CLOSING_SQUARE_BRACKET;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.COMMA;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DOUBLE_BACKSLASH;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.EMPTY;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.OPENING_SQUARE_BRACKET;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.WHITE_SPACE;

@Slf4j
public class PslUtils {

	private static final String SPECIAL_CHARACTERS = "^$.*+?[]\\";
	private static final String BACKSLASH_B = "\\b";
	private static final String SEPARATORS_SPECIAL_CHARACTERS = "([()\\[\\]{}\\\\^\\-$|?*+.])";
	private static final String ESCAPED_MATCHING_SPECIAL_CHARACTER = "\\\\$1";
	private static final String DEFAULT_SEPARATOR_CHARACTERS = "[ ]";
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
	private static final char DASH_CHAR = '-';

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
			return EMPTY;
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

			} else if (c == OPENING_PARENTHESIS_CHAR || c == CLOSING_PARENTHESIS_CHAR || c == PIPE_CHAR
				|| c == OPENING_CURLY_BRACKET_CHAR || c == CLOSING_CURLY_BRACKET_CHAR) {

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
	 * @param i			The index of the backslash in the regular expression.
	 * @param javaRegex	The {@link StringBuilder} holding the resulting Java regular expression.
	 *
	 * @return			The new value of the index
	 */
	private static int handleBackSlash(String pslRegex, boolean inRange, int i, StringBuilder javaRegex) {

		int result = i;

		if (inRange) {

			// Escape works differently in [] ranges
			// We simply need to double backslashes
			javaRegex.append(DOUBLE_BACKSLASH);

		} else {

			// Backslashes in PSL regex are utterly broken
			// We need to handle all cases here to convert to proper regex in Java
			char nextChar = pslRegex.charAt(i + 1);
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
	 * @param text				The text that should be parsed.
	 * @param selectColumns		The list/range(s) of columns that should be extracted from the text.
	 * @param separators		The set of characters used to split the given text.
	 * @param resultSeparator	The separator used to join the resulting elements.
	 *
	 * @return					The nth group in the given text,
	 * 							as formatted according to the given separators and column numbers.
	 */
	public static String nthArgf(String text, String selectColumns, String separators, String resultSeparator) {

		// If any arg is null, then return empty String
		if (text == null || selectColumns == null || separators == null
			|| text.isEmpty() || selectColumns.isEmpty() || separators.isEmpty()) {

			return null;
		}

		// Replace special chars with their literal equivalents
		String separatorsRegExp = OPENING_SQUARE_BRACKET
			+ separators.replaceAll(SEPARATORS_SPECIAL_CHARACTERS, ESCAPED_MATCHING_SPECIAL_CHARACTER)
			+ CLOSING_SQUARE_BRACKET;

		// Call nthArg with the input
		return nthArg(text, selectColumns, separatorsRegExp, resultSeparator);
	}

	/**
	 * @param text				The text that should be parsed.
	 * @param selectColumns		The list/range(s) of columns that should be extracted from the text.
	 * @param separatorsRegExp	A transformed set of characters used to split the given text.
	 * @param resultSeparator	The separator used to join the resulting elements.
	 *
	 * @return					The nth group in the given text,
	 * 							as formatted according to the given separators and column numbers.
	 */
	private static String nthArg(String text, String selectColumns, String separatorsRegExp, String resultSeparator) {

		// Check the result separator
		if (resultSeparator == null) {
			resultSeparator = WHITE_SPACE;
		}

		// Split the input text into a String array thanks to the separatorsRegExp
		String[] splitText = text.split(separatorsRegExp, -1);

		// The user can specify several columns.
		// Split the selectColumns into an array too.
		String[] columnsArray = selectColumns.split(COMMA);

		// So, for each columns group requested
		String result = null;
		int[] columnsRange;
		int fromColumnNumber;
		int toColumnNumber;
		for (String columns : columnsArray) {

			// Get the columns range
			columnsRange = getColumnsRange(columns, splitText.length);
			fromColumnNumber = columnsRange[0];
			toColumnNumber = columnsRange[1];

			// If we have valid fromColumnNumber and toColumnNumber, then retrieve these columns
			// which are actually items in the splitText array
			if (fromColumnNumber > 0 && fromColumnNumber <= toColumnNumber) {

				result = Arrays
					.stream(splitText, fromColumnNumber - 1, toColumnNumber)
					.collect(Collectors.joining(resultSeparator));
			}
		}

		// Alright, we did it!
		return result;
	}

	/**
	 * @param columns       The {@link String} denoting the range of columns, in one of the following forms:
	 *                      "m-n", "m-" or "-n".
	 * @param columnCount	The total number of columns.
	 *
	 * @return				A 2-element array A with:
	 * 						<ul>
	 * 							<li>A[0] being the start of the range, inclusive</li>
	 * 							<li>A[1] being the end of the range, inclusive</li>
	 * 						</ul>
	 */
	private static int[] getColumnsRange(String columns, int columnCount) {

		int fromColumnNumber;
		int toColumnNumber;

		try {

			int dashIndex = columns.indexOf(DASH_CHAR);
			int columnsLength = columns.length();

			// If it is a simple number, we'll retrieve only that column number
			if (dashIndex == -1) {

				fromColumnNumber = Integer.parseInt(columns);
				toColumnNumber = fromColumnNumber;
			}

			// If it is "-n", then we'll retrieve all columns til number n
			else if (dashIndex == 0) {

				fromColumnNumber = 1;
				toColumnNumber = Integer.parseInt(columns.substring(1));
			}

			// If it is "n-", then we'll retrieve all columns starting from n
			else if (dashIndex == columnsLength - 1) {

				fromColumnNumber = Integer.parseInt(columns.substring(0, columnsLength - 1));
				toColumnNumber = columnCount;
			}

			// Else, if it is "m-n", we'll retrieve all columns starting from m til n
			else {

				fromColumnNumber = Integer.parseInt(columns.substring(0, dashIndex));
				toColumnNumber = Integer.parseInt(columns.substring(dashIndex + 1));
			}

			if (fromColumnNumber > columnCount || toColumnNumber > columnCount) {

				log.warn("getColumnRange: Invalid range for a {}-length array: [{}-{}]",
					columnCount, fromColumnNumber, toColumnNumber);

				fromColumnNumber = 0;
				toColumnNumber = 0;
			}

		} catch (NumberFormatException e) {

			log.warn("getColumnRange: Could not determine the range denoted by {}: {}", columns, e.getMessage());

			fromColumnNumber = 0;
			toColumnNumber = 0;
		}

		return new int[]{fromColumnNumber, toColumnNumber};
	}
}
