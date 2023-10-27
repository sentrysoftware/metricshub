package com.sentrysoftware.metricshub.engine.common.helpers.state;

import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum DuplexMode implements IState {
	HALF(0),
	FULL(1);

	@Getter
	private int numericValue;

	/**
	 * Map each state value to a {@link DuplexMode}
	 */
	private static final Map<String, DuplexMode> DUPLEX_MODE_MAP = Map.of(
		"0",
		HALF,
		"no",
		HALF,
		"half",
		HALF,
		"degraded",
		HALF,
		"1",
		FULL,
		"yes",
		FULL,
		"full",
		FULL,
		"ok",
		FULL
	);

	/**
	 * Interpret the specified state value:
	 *  <ul>
	 *  	<li>{0, no, half, degraded} as HALF</li>
	 *  	<li>{1, yes, full, ok} as FULL</li>
	 *  </ul>
	 * @param state String to be interpreted
	 * @return {@link Optional} of {@link DuplexMode}
	 */
	public static Optional<DuplexMode> interpret(final String state) {
		return IState.interpret(state, DUPLEX_MODE_MAP, DuplexMode.class);
	}
}