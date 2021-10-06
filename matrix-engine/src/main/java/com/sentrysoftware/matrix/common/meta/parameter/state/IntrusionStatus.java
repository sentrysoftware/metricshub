package com.sentrysoftware.matrix.common.meta.parameter.state;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.INTRUSION_STATUS_PARAMETER_UNIT;

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
public enum IntrusionStatus implements IState {

	CLOSED("Closed", 0, Severity.INFO),
	OPEN("Open", 1, Severity.INFO);

	@Getter
	private String displayName;
	@Getter
	private int numericValue;
	@Getter
	private Severity severity;

	/**
	 * Map each state value to a {@link IntrusionStatus}
	 */
	private static final Map<String, IntrusionStatus> INTRUSION_STATUS_MAP = Map.of(
			"0", CLOSED,
			"ok", CLOSED,
			"1", OPEN,
			"warn", OPEN,
			"warning", OPEN,
			"2", OPEN,
			"alarm", OPEN);

	/**
	 * {@link IntrusionStatus} simple class name as Enum type. This is mandatory for the Serialization so that the 
	 * Deserialization knows which concrete Enum should be built for the interface {@link IState}
	 */
	public static final String INTRUSION_STATUS_TYPE = IntrusionStatus.class.getSimpleName();

	/**
	 * Name to state map used by the Jackson deserialization. Once the Enum is selected by the derserializer,
	 * this map is invoked to create the exact fixed enum constant based on its name
	 */
	private static final Map<String, IntrusionStatus> NAME_TO_STATE_MAP = Stream
			.of(values())
			.collect(Collectors.toMap(IntrusionStatus::name, Function.identity()));

	/**
	 * Interpret the specified state value:
	 *  <ul>
	 *  	<li>{0, ok, OK, closed, CLOSED} as CLOSED</li>
	 *  	<li>{1, warn, WARN, warning, WARNING, alarm, ALARM, 2} as OPEN</li>
	 *  </ul>
	 * @param state String to be interpreted
	 * @return {@link Optional} of {@link IntrusionStatus}
	 */
	public static Optional<IntrusionStatus> interpret(final String state) {

		if (state == null || state.isBlank()) {
			return Optional.empty();
		}

		final IntrusionStatus intrusionStatus = INTRUSION_STATUS_MAP.get(
				NumberHelper.formatIntegerState(
						state
						.trim()
						.toLowerCase()
				)
		);

		return Optional.ofNullable(intrusionStatus);
	}


	@Override
	public String getUnit() {
		return INTRUSION_STATUS_PARAMETER_UNIT;
	}

	@Override
	public String getType() {
		return INTRUSION_STATUS_TYPE;
	}

	@Override
	public String getName() {
		return name();
	}

	@JsonCreator
	public static IntrusionStatus fromMap(final Map<String, String> stateObj) {
		return IState.fromMap(stateObj, NAME_TO_STATE_MAP, IntrusionStatus.class);
	}
}
