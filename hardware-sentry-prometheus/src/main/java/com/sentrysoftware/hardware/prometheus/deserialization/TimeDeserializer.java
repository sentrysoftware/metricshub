package com.sentrysoftware.hardware.prometheus.deserialization;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.sentrysoftware.matrix.common.helpers.NumberHelper;

import lombok.NonNull;

public class TimeDeserializer extends JsonDeserializer<Long> {
	@Override
	public Long deserialize(JsonParser parser, DeserializationContext ctxt)
			throws IOException {
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
	 * @param value
	 * @return
	 */
	public static Long interpretValueOf(@NonNull final String value) {
		long longValue = NumberHelper.parseInt(value, -1);
		if (longValue != -1) {
			return longValue;
		}

		Matcher m = Pattern.compile("\\s*(?:(\\d+)\\s*(?:years?|yrs?|y))?" +
				"\\s*(?:(\\d+)\\s*(?:weeks?|wks?|w))?" +
				"\\s*(?:(\\d+)\\s*(?:days?|d))?" +
				"\\s*(?:(\\d+)\\s*(?:hours?|hrs?|h))?" +
				"\\s*(?:(\\d+)\\s*(?:minutes?|mins?|m))?" +
				"\\s*(?:(\\d+)\\s*(?:seconds?|secs?|s))?" +
				"\\s*(?:(\\d+)\\s*(?:milliseconds?|millisecs?|ms))?" +
				"\\s*", Pattern.CASE_INSENSITIVE)
				.matcher(value);

		if (!m.matches()) {
			throw new IllegalArgumentException("Not valid duration: " + value);
		}

		long years = (m.start(1) == -1 ? 0 : Integer.parseInt(m.group(1)));
		long weeks = (m.start(2) == -1 ? 0 : Integer.parseInt(m.group(2)));
		long days = (m.start(3) == -1 ? 0 : Integer.parseInt(m.group(3)));
		long hours = (m.start(4) == -1 ? 0 : Integer.parseInt(m.group(4)));
		long mins = (m.start(5) == -1 ? 0 : Integer.parseInt(m.group(5)));
		long secs = (m.start(6) == -1 ? 0 : Integer.parseInt(m.group(6)));
		long millisecs = (m.start(7) == -1 ? 0 : Integer.parseInt(m.group(7)));

		return years * 60 * 60 * 24 * 365
				+ weeks * 60 * 60 * 24 * 7
				+ days * 60 * 60 * 24
				+ hours * 60 * 60
				+ mins * 60
				+ secs
				+ millisecs / 1000;
	}
}
