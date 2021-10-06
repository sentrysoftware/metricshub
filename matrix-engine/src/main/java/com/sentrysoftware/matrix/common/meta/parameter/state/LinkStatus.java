package com.sentrysoftware.matrix.common.meta.parameter.state;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.LINK_STATUS_PARAMETER_UNIT;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.sentrysoftware.matrix.common.helpers.NumberHelper;
import com.sentrysoftware.matrix.model.alert.Severity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum LinkStatus implements IState {

	PLUGGED("Plugged", 0, Severity.INFO),
	UNPLUGGED("Unplugged", 1,Severity.WARN);

	@Getter
	private String displayName;
	@Getter
	private int numericValue;
	@Getter
	private Severity severity;

	/**
	 * Map each state value to a {@link LinkStatus}
	 */
	private static final Map<String, LinkStatus> LINK_STATUS_MAP = Map.of(
			"0", PLUGGED,
			"ok", PLUGGED,
			"1", UNPLUGGED,
			"warn", UNPLUGGED,
			"warning", UNPLUGGED,
			"2", UNPLUGGED,
			"alarm", UNPLUGGED);

	/**
	 * {@link LinkStatus} simple class name as Enum type. This is mandatory for the Serialization so that the 
	 * Deserialization knows which concrete Enum should be built for the interface {@link IState}
	 */
	public static final String LINK_STATUS_TYPE = LinkStatus.class.getSimpleName();

	/**
	 * Name to state map used by the Jackson deserialization. Once the Enum is selected by the derserializer,
	 * this map is invoked to create the exact fixed enum constant based on its name
	 */
	private static final Map<String, LinkStatus> NAME_TO_STATE_MAP = Stream
			.of(values())
			.collect(Collectors.toMap(LinkStatus::name, Function.identity()));

	/**
	 * Interpret the specified state value:
	 *  <ul>
	 *  	<li>{0, ok, OK, plugged, PLUGGED} as Plugged</li>
	 *  	<li>{1, warn, WARN, warning, WARNING, alarm, ALARM, 2, unplugged, UNPLUGGED} as Unplugged</li>
	 *  </ul>
	 * @param state String to be interpreted
	 * @return {@link Optional} of {@link LinkStatus}
	 */
	public static Optional<LinkStatus> interpret(final String state) {

		if (state == null || state.isBlank()) {
			return Optional.empty();
		}

		final LinkStatus linkStatus = LINK_STATUS_MAP.get(
				NumberHelper.formatIntegerState(
						state
						.trim()
						.toLowerCase()
				)
		);

		return Optional.ofNullable(linkStatus);
	}


	@Override
	public String getUnit() {
		return LINK_STATUS_PARAMETER_UNIT;
	}

	@Override
	public String getType() {
		return LINK_STATUS_TYPE;
	}

	@Override
	public String getName() {
		return name();
	}

	@JsonCreator
	public static LinkStatus fromMap(final Map<String, String> stateObj) {
		return IState.fromMap(stateObj, NAME_TO_STATE_MAP, LinkStatus.class);
	}
}