package com.sentrysoftware.matrix.model.parameter;

import java.util.Map;
import java.util.Optional;

import lombok.NonNull;

public enum ParameterState {

	OK,
	WARN,
	ALARM;

	/**
	 * Map each state value to a {@link ParameterState}
	 */
	private static final Map<String, ParameterState> PARAMETER_STATE_MAP = Map.of(
			"0", OK,
			"ok", OK,
			"1", WARN,
			"warn", WARN,
			"2", ALARM,
			"alarm", ALARM);

	/**
	 * Interpret the specified state value:
	 *  <ul>
	 *  	<li>{0, ok, OK} as OK</li>
	 *  	<li>{1, warn, WARN} as WARN</li>
	 *  	<li>{2, alarm, ALARM} as ALARM</li>
	 *  <ul>
	 * @param state String to be interpreted
	 * @return {@link Optional} of {@link ParameterState}
	 */
	public static Optional<ParameterState> interpretValueOf(@NonNull final String state) {

		if (state.isBlank()) {
			return Optional.empty();
		}

		final ParameterState parameterState = PARAMETER_STATE_MAP.get(state.toLowerCase());

		if (parameterState != null) {
			return Optional.of(parameterState);
		}

		throw new IllegalArgumentException("Invalid value for parameter state: " + state);

	}
}
