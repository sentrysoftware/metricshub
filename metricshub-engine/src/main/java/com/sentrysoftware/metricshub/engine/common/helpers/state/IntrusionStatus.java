package com.sentrysoftware.metricshub.engine.common.helpers.state;

import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum IntrusionStatus implements IState {
	CLOSED(0),
	OPEN(1);

	@Getter
	private int numericValue;

	/**
	 * Map each state value to a {@link IntrusionStatus}
	 */
	private static final Map<String, IntrusionStatus> INTRUSION_STATUS_MAP = Map.of(
		"0",
		CLOSED,
		"ok",
		CLOSED,
		"closed",
		CLOSED,
		"1",
		OPEN,
		"degraded",
		OPEN,
		"2",
		OPEN,
		"failed",
		OPEN,
		"open",
		OPEN
	);

	/**
	 * Interpret the specified state value:
	 *  <ul>
	 *  	<li>{0, ok, closed} as CLOSED</li>
	 *  	<li>{1, degraded, 2, failed, open} as OPEN</li>
	 *  </ul>
	 * @param state String to be interpreted
	 * @return {@link Optional} of {@link IntrusionStatus}
	 */
	public static Optional<IntrusionStatus> interpret(final String state) {
		return IState.interpret(state, INTRUSION_STATUS_MAP, IntrusionStatus.class);
	}
}
