package org.sentrysoftware.metricshub.engine.common.helpers.state;

import java.util.Map;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Enumeration representing the link status states.
 * <p>
 * The states are:
 * <ul>
 *     <li>{@link #PLUGGED} with numeric value 1</li>
 *     <li>{@link #UNPLUGGED} with numeric value 0</li>
 * </ul>
 */
@AllArgsConstructor
public enum LinkStatus implements IState {
	/**
	 * Represents the plugged state with a numeric value of 1.
	 */
	PLUGGED(1),
	/**
	 * Represents the unplugged state with a numeric value of 0.
	 */
	UNPLUGGED(0);

	/**
	 * The numeric value associated with each state.
	 */
	@Getter
	private int numericValue;

	/**
	 * Map each state value to a {@link LinkStatus}
	 */
	private static final Map<String, LinkStatus> LINK_STATUS_MAP = Map.of(
		"0",
		PLUGGED,
		"ok",
		PLUGGED,
		"plugged",
		PLUGGED,
		"1",
		UNPLUGGED,
		"degraded",
		UNPLUGGED,
		"2",
		UNPLUGGED,
		"failed",
		UNPLUGGED,
		"unplugged",
		UNPLUGGED
	);

	/**
	 * Interpret the specified state value:
	 *  <ul>
	 *  	<li>{0, ok, plugged} as Plugged</li>
	 *  	<li>{1, degraded, failed, 2, unplugged} as Unplugged</li>
	 *  </ul>
	 * @param state String to be interpreted
	 * @return {@link Optional} of {@link LinkStatus}
	 */
	public static Optional<LinkStatus> interpret(final String state) {
		return IState.interpret(state, LINK_STATUS_MAP, LinkStatus.class);
	}
}
