package com.sentrysoftware.hardware.prometheus.deserialization;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.sentrysoftware.matrix.model.parameter.ParameterState;

import lombok.NonNull;

public class UnkownStatusDeserializer extends JsonDeserializer<ParameterState> {
	@Override
	public ParameterState deserialize(JsonParser parser, DeserializationContext ctxt)
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
	 * Interpret the specified unkonwStatus OK {0, ok, OK}, WARN{1, warn,unkown}, ALARM{alarm, 2}.
	 * @param status
	 * @return
	 */
	public static ParameterState interpretValueOf(@NonNull final String status) {

		final String lCaseStatus = status.toLowerCase();

		if ("0".equals(lCaseStatus) || "ok".equals(lCaseStatus)) {
			return ParameterState.OK;
		}

		if ("1".equals(lCaseStatus) || "warn".equals(lCaseStatus) || "unknown".equals(lCaseStatus)) {
			return ParameterState.WARN;
		}

		if ("2".equals(lCaseStatus) || "alarm".equals(lCaseStatus)) {
			return ParameterState.ALARM;
		}

		throw new IllegalArgumentException("Invalid Parameter State for UnkownStatus: " + status);

	}
}
