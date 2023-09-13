package com.sentrysoftware.matrix.common.helpers.state;

import java.util.Map;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum PredictedFailure implements IState {

	OK(0),
	FAILURE_PREDICTED(1);

	@Getter
	private int numericValue;

	/**
	 * Map each state value to a {@link PredictedFailure}
	 */
	private static final Map<String, PredictedFailure> PREDICTED_FAILURE_MAP = Map.of(
			"0", OK,
			"ok", OK,
			"false", OK,
			"1", FAILURE_PREDICTED,
			"warn", FAILURE_PREDICTED,
			"warning", FAILURE_PREDICTED,
			"2", FAILURE_PREDICTED,
			"alarm", FAILURE_PREDICTED,
			"true", FAILURE_PREDICTED);

	/**
	 * Interpret the specified state value:
	 *  <ul>
	 *  	<li>{0, ok, OK, false, FALSE} as OK</li>
	 *  	<li>{1, warn, WARN, warning, WARNING, 2, alarm, ALARM, true, TRUE} as FAILURE_PREDICTED</li>
	 *  	<li>{2, alarm, ALARM} as TOO_MANY</li>
	 *  </ul>
	 * @param state String to be interpreted
	 * @return {@link Optional} of {@link PredictedFailure}
	 */
	public static Optional<PredictedFailure> interpret(final String state) {
		return IState.interpret(state, PREDICTED_FAILURE_MAP, PredictedFailure.class);
	}
}