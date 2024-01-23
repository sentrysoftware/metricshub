package org.sentrysoftware.metricshub.engine.common.helpers;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class FunctionArgumentsExtractor {

	private static final Pattern TRAILING_AND_LEADING_DOUBLE_QUOTES_PATTERN = Pattern.compile("^\"(.*)\"$");

	/**
	 * Extracts the arguments from a function like<br>
	 * lookup("disk_controller", "id", "controller_number", $2)
	 *
	 * @param functionCode function code definition
	 * @return captured arguments
	 */
	public static List<String> extractArguments(String functionCode) {
		final List<String> arguments = new ArrayList<>();
		final LinkedList<Character> parenthesesStack = new LinkedList<>();
		StringBuilder argumentBuilder = new StringBuilder();
		boolean insideDoubleQuotes = false;
		var input = functionCode.substring(functionCode.indexOf('(') + 1, functionCode.lastIndexOf(')')).trim();

		for (char c : input.toCharArray()) {
			if (c == '(' && !insideDoubleQuotes) {
				parenthesesStack.push(c);
			} else if (c == ')' && !insideDoubleQuotes) {
				parenthesesStack.pop();
			} else if (c == '"') {
				insideDoubleQuotes = !insideDoubleQuotes;
			}

			if (c == ',' && parenthesesStack.isEmpty() && !insideDoubleQuotes) {
				arguments.add(normalizeArgument(argumentBuilder));
				argumentBuilder = new StringBuilder();
			} else {
				argumentBuilder.append(c);
			}
		}

		arguments.add(normalizeArgument(argumentBuilder));

		return arguments;
	}

	/**
	 * Transforms the argument builder to a string value, trims white spaces
	 * from the string and removes trailing and leading double quotes
	 *
	 * @param argumentBuilder {@link StringBuilder} instance wrapping one
	 * function argument
	 * @return normalized argument
	 */
	private static String normalizeArgument(final StringBuilder argumentBuilder) {
		final String value = argumentBuilder.toString().trim();

		final Matcher matcher = TRAILING_AND_LEADING_DOUBLE_QUOTES_PATTERN.matcher(value);
		if (matcher.find()) {
			return matcher.group(1);
		}

		return value;
	}
}
