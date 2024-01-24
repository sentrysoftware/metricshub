package org.sentrysoftware.metricshub.engine.common.helpers.state;

import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * The NeedsCleaning enum represents different states related to the need for cleaning.
 * It implements the IState interface and includes methods for interpreting state values.
 */
@AllArgsConstructor
public enum NeedsCleaning implements IState {
	/**
	 * OK state indicating no immediate need for cleaning.
	 */
	OK(0),
	/**
	 * NEEDED state indicating that cleaning is needed.
	 */
	NEEDED(1),
	/**
	 * NEEDED_IMMEDIATELY state indicating an immediate need for cleaning.
	 */
	NEEDED_IMMEDIATELY(1);

	@Getter
	private int numericValue;

	/**
	 * Map each state value to a {@link NeedsCleaning}
	 */
	private static final Map<String, NeedsCleaning> NEEDS_CLEANING_MAP = Map.of(
		"0",
		OK,
		"1",
		NEEDED,
		"2",
		NEEDED_IMMEDIATELY
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
