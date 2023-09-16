package com.sentrysoftware.matrix.agent.deserialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.sentrysoftware.matrix.common.helpers.NumberHelper;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.NonNull;

public class TimeDeserializer extends JsonDeserializer<Long> {

	@Override
	public Long deserialize(JsonParser parser, DeserializationContext ctxt) throws IOException {
		if (parser == null) {
			return null;
		}

		try {
			return interpretValueOf(parser.getValueAsString());
		} catch (IllegalArgumentException e) {
			throw new IOException(e.getMessage());
		}
	}

	/**
	 * Interpret the timeout value to have it in seconds.
	 * The timeout is expected to be ordered from the biggest unit to the smallest : y > w > d > h > m > s > ms.
	 *
	 * @param value The value we wish to parse and interpret
	 * @return long value
	 */
	public static long interpretValueOf(@NonNull final String value) {
		final long longValue = NumberHelper.parseInt(value, -1);
		if (longValue != -1) {
			return longValue;
		}

		final Matcher matcher = Pattern
			.compile(
				"\\s*(?:(\\d+)\\s*(?:years?|yrs?|y))?" +
				"\\s*(?:(\\d+)\\s*(?:weeks?|wks?|w))?" +
				"\\s*(?:(\\d+)\\s*(?:days?|d))?" +
				"\\s*(?:(\\d+)\\s*(?:hours?|hrs?|h))?" +
				"\\s*(?:(\\d+)\\s*(?:minutes?|mins?|m))?" +
				"\\s*(?:(\\d+)\\s*(?:seconds?|secs?|s))?" +
				"\\s*(?:(\\d+)\\s*(?:milliseconds?|millisecs?|ms))?" +
				"\\s*",
				Pattern.CASE_INSENSITIVE
			)
			.matcher(value);

		if (!matcher.matches()) {
			throw new IllegalArgumentException("Not valid duration: " + value);
		}

		final long years = (matcher.start(1) == -1 ? 0 : Integer.parseInt(matcher.group(1)));
		final long weeks = (matcher.start(2) == -1 ? 0 : Integer.parseInt(matcher.group(2)));
		final long days = (matcher.start(3) == -1 ? 0 : Integer.parseInt(matcher.group(3)));
		final long hours = (matcher.start(4) == -1 ? 0 : Integer.parseInt(matcher.group(4)));
		final long minutes = (matcher.start(5) == -1 ? 0 : Integer.parseInt(matcher.group(5)));
		final long seconds = (matcher.start(6) == -1 ? 0 : Integer.parseInt(matcher.group(6)));
		final long milliseconds = (matcher.start(7) == -1 ? 0 : Integer.parseInt(matcher.group(7)));

		// CHECKSTYLE:OFF
		return (
			years * 60 * 60 * 24 * 365 +
			weeks * 60 * 60 * 24 * 7 +
			days * 60 * 60 * 24 +
			hours * 60 * 60 +
			minutes * 60 +
			seconds +
			milliseconds / 1000
		);
		// CHECKSTYLE:ON
	}
}
