package com.sentrysoftware.matrix.common.helpers.state;


import java.util.Map;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum LinkStatus implements IState {

	PLUGGED(1),
	UNPLUGGED(0);

	@Getter
	private int numericValue;

	/**
	 * Map each state value to a {@link LinkStatus}
	 */
	private static final Map<String, LinkStatus> LINK_STATUS_MAP = Map.of(
		"0", PLUGGED,
		"ok", PLUGGED,
		"1", UNPLUGGED,
		"degraded", UNPLUGGED,
		"2", UNPLUGGED,
		"failed", UNPLUGGED
	);

	/**
	 * Interpret the specified state value:
	 *  <ul>
	 *  	<li>{0, ok, OK, plugged, PLUGGED} as Plugged</li>
	 *  	<li>{1, degraded, DEGRADED, failed, FAILED, 2, unplugged, UNPLUGGED} as Unplugged</li>
	 *  </ul>
	 * @param state String to be interpreted
	 * @return {@link Optional} of {@link LinkStatus}
	 */
	public static Optional<LinkStatus> interpret(final String state) {
		return IState.interpret(state, LINK_STATUS_MAP, LinkStatus.class);
	}

}