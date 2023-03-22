package com.sentrysoftware.matrix.converter.state;

import java.util.List;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConversionHelper {

	/**
	 * A compiled representation of the HDF source reference regular expression.
	 * We attempt to match input like %Enclosure.Discovery.Source(2)%
	 */
	private static final Pattern SOURCE_REF_PATTERN = Pattern.compile(
		"%\\s*(\\w+)\\.(discovery|collect)\\.(source\\(\\d+\\))\\s*%",
		Pattern.CASE_INSENSITIVE | Pattern.MULTILINE 
	);

	/**
	 * A compiled representation of the HDF entry reference regular expression.
	 * We attempt to match input like %Entry.Column(2)%
	 */
	private static final Pattern SOURCE_ENTRY_PATTERN = Pattern.compile(
		"%\\s*(entry)\\.(column\\(\\d+\\))\\s*%",
		Pattern.CASE_INSENSITIVE | Pattern.MULTILINE 
	);

	/**
	 * List of pattern function converters
	 */
	private static final List<PatternFunctionConverter> PATTERN_FUNCTION_CONVERTERS = List.of(
		new PatternFunctionConverter(SOURCE_REF_PATTERN, ConversionHelper::convertSourceReference),
		new PatternFunctionConverter(SOURCE_ENTRY_PATTERN, ConversionHelper::convertEntryReference)
	);

	/**
	 * Perform value conversions
	 * 
	 * @param input
	 * @return updated string value
	 */
	public static String performValueConversions(String input) {

		// Loop over the pattern functions
		for (final PatternFunctionConverter patternFunction : PATTERN_FUNCTION_CONVERTERS) {
			// Get the defined pattern and creates a matcher that will match the given input against this pattern.
			final Matcher matcher = patternFunction.getPattern().matcher(input);

			// Get the converter function
			final BiFunction<Matcher, String, String> converter = patternFunction.getConverter();

			while (matcher.find()) {
				// Convert the input value
				input = converter.apply(matcher, input);
			}

		}

		return input;
	}

	/**
	 * Convert source reference. E.g.
	 * <b><u>%Enclosure.Discovery.Source(2)%</u></b> becomes
	 * <b><u>$monitors.enclosure.discovery.sources.source(2)$</u></b>
	 * 
	 * @param matcher matcher used to find groups
	 * @param input   input value to be replaced
	 * @return updated string value
	 */
	private static String convertSourceReference(final Matcher matcher, final String input) {
		final String monitor = matcher.group(1).toLowerCase();
		final String job = matcher.group(2).toLowerCase();
		final String source = matcher.group(3).toLowerCase();

		return input.replace(
			matcher.group(),
			String.format("$monitors.%s.%s.sources.%s$", monitor, job, source)
		);
	}

	/**
	 * Convert entry reference. E.g.
	 * <b><u>%Entry.Column(2)%</u></b> becomes
	 * <b><u>$entry.column(2)$</u></b>
	 * 
	 * @param matcher matcher used to find groups
	 * @param input   input value to be replaced
	 * @return updated string value
	 */
	private static String convertEntryReference(final Matcher matcher, final String input) {
		final String entry = matcher.group(1).toLowerCase();
		final String column = matcher.group(2).toLowerCase();
		return input.replace(
			matcher.group(),
			String.format("$%s.%s$", entry, column)
		);
	}

	/**
	 * Build a source key regex
	 * 
	 * @param regex Keyword or regular expression used to build the final regex
	 * @return String value
	 */
	public static String buildSourceKeyRegex(final String regex) {
		return String.format("^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\.%s\\s*$", regex);
	}

	/**
	 * Build a criteria key regex
	 * 
	 * @param regex Keyword or regular expression used to build the final regex
	 * @return String value
	 */
	public static String buildCriteriaKeyRegex(final String regex) {
		return String.format("^\\s*detection\\.criteria\\(([1-9]\\d*)\\)\\.%s\\s*$", regex);
	}

	@AllArgsConstructor
	static class PatternFunctionConverter {
		@Getter
		private Pattern pattern;
		@Getter
		private BiFunction<Matcher, String, String> converter;
	}
}
