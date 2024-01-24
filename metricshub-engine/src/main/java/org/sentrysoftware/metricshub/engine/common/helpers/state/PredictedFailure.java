package org.sentrysoftware.metricshub.engine.common.helpers.state;

import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enumeration representing states related to predicted failures.
 */
@AllArgsConstructor
public enum PredictedFailure implements IState {
	/**
	 * OK state with numeric value 0.
	 */
	OK(0),
	/**
	 * FAILURE_PREDICTED state with numeric value 1.
	 */
	FAILURE_PREDICTED(1);

	@Getter
	private int numericValue;

	/**
	 * Map each state value to a {@link PredictedFailure}
	 */
	private static final Map<String, PredictedFailure> PREDICTED_FAILURE_MAP = Map.of(
		"0",
		OK,
		"ok",
		OK,
		"false",
		OK,
		"1",
		FAILURE_PREDICTED,
		"degraded",
		FAILURE_PREDICTED,
		"2",
		FAILURE_PREDICTED,
		"failed",
		FAILURE_PREDICTED,
		"true",
		FAILURE_PREDICTED
	);

	/**
	 * Interpret the specified state value:
	 *  <ul>
	 *  	<li>{0, ok, false} as OK</li>
	 *  	<li>{1, degraded, 2, failed, true} as FAILURE_PREDICTED</li>
	 *  </ul>
	 * @param state String to be interpreted
	 * @return {@link Optional} of {@link PredictedFailure}
	 */
	public static Optional<PredictedFailure> interpret(final String state) {
		return IState.interpret(state, PREDICTED_FAILURE_MAP, PredictedFailure.class);
	}
}
