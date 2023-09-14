package com.sentrysoftware.matrix.common.helpers.state;

import java.util.Map;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum NeedsCleaning implements IState {

	OK(0),
	NEEDED(1),
	NEEDED_IMMEDIATELY(2);

	@Getter
	private int numericValue;

	/**
	 * Map each state value to a {@link NeedsCleaning}
	 */
	private static final Map<String, NeedsCleaning> NEEDS_CLEANING_MAP = Map.of(
		"0", OK,
		"1", NEEDED,
		"2", NEEDED_IMMEDIATELY
	);

	/**
	 * Interpret the specified state value:
	 *  <ul>
	 *  	<li>{ 0 } as OK</li>
	 *  	<li>{ 1 } as NEEDED</li>
	 *  	<li>{ 2 } as NEEDED_IMMEDIATELY</li>
	 *  </ul>
	 * @param state String to be interpreted
	 * @return {@link Optional} of {@link NeedsCleaning}
	 */
	public static Optional<NeedsCleaning> interpret(final String state) {
		return IState.interpret(state, NEEDS_CLEANING_MAP, NeedsCleaning.class);
	}
}
