package com.sentrysoftware.hardware.prometheus.deserialization;

import java.io.IOException;
import java.util.Optional;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.sentrysoftware.matrix.model.parameter.ParameterState;

import lombok.NonNull;

public class UnknownStatusDeserializer extends JsonDeserializer<Optional<ParameterState>> {
	@Override
	public Optional<ParameterState> deserialize(JsonParser parser, DeserializationContext ctxt)
			throws IOException {
		if (parser == null) {
			return Optional.empty();
		}

		try {
			return interpretValueOf(parser.getValueAsString());
		} catch (IllegalArgumentException e) {
			throw new IOException(e.getMessage());
		}
	}

	/**
	 * Interpret the specified unkonwStatus OK {0, ok, OK}, WARN {1, warn, WARN, unknown},
	 * ALARM {2, alarm, ALARM}.
	 * 
	 * @param status String to be interpreted
	 * @return {@link Optional} of {@link ParameterState}
	 */
	public static Optional<ParameterState> interpretValueOf(@NonNull final String status) {

		final String lCaseStatus = status.toLowerCase();

		if (lCaseStatus.isBlank()) {
			return Optional.empty();
		}

		if ("0".equals(lCaseStatus) || "ok".equals(lCaseStatus)) {
			return Optional.of(ParameterState.OK);
		}

		if ("1".equals(lCaseStatus) || "warn".equals(lCaseStatus) || "unknown".equals(lCaseStatus)) {
			return Optional.of(ParameterState.WARN);
		}

		if ("2".equals(lCaseStatus) || "alarm".equals(lCaseStatus)) {
			return Optional.of(ParameterState.ALARM);
		}

		throw new IllegalArgumentException("Invalid Parameter State for UnkownStatus: " + status);

	}
}
