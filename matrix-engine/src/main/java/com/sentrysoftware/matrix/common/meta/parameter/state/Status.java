package com.sentrysoftware.matrix.common.meta.parameter.state;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.STATUS_PARAMETER_UNIT;

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
public enum Status implements IState {

	OK("OK", 0, Severity.INFO),
	DEGRADED("Degraded", 1, Severity.WARN),
	FAILED("Failed", 2, Severity.ALARM);

	@Getter
	private String displayName;
	@Getter
	private int numericValue;
	@Getter
	private Severity severity;

	/**
	 * Map each state value to a {@link Status}
	 */
	private static final Map<String, Status> STATUS_MAP = Map.of(
			"0", OK,
			"ok", OK,
			"1", DEGRADED,
			"warn", DEGRADED,
			"warning", DEGRADED,
			"2", FAILED,
			"alarm", FAILED);

	/**
	 * {@link Status} simple class name as Enum type. This is mandatory for the Serialization so that the 
	 * Deserialization knows which concrete Enum should be built for the interface {@link IState}
	 */
	private static final String STATUS_TYPE = Status.class.getSimpleName();

	/**
	 * Name to state map used by the Jackson deserialization. Once the Enum is selected by the derserializer,
	 * this map is invoked to create the exact fixed enum constant based on its name
	 */
	private static final Map<String, Status> NAME_TO_STATE_MAP = Stream
			.of(values())
			.collect(Collectors.toMap(Status::name, Function.identity()));

	/**
	 * Interpret the specified state value:
	 *  <ul>
	 *  	<li>{0, ok, OK} as OK</li>
	 *  	<li>{1, warn, WARN, warning, WARNING, degraded, DEGRADED} as DEGRADED</li>
	 *  	<li>{2, alarm, ALARM, failed, FAILED} as FAILED</li>
	 *  </ul>
	 * @param state String to be interpreted
	 * @return  {@link Optional} of {@link Status}
	 */
	public static Optional<Status> interpret(final String state) {

		if (state == null || state.isBlank()) {
			return Optional.empty();
		}

		final Status status = STATUS_MAP.get(
				NumberHelper.formatIntegerState(
						state
						.trim()
						.toLowerCase()
				)
		);

		return Optional.ofNullable(status);
	}

	@Override
	public String getUnit() {
		return STATUS_PARAMETER_UNIT;
	}

	@Override
	public String getType() {
		return STATUS_TYPE;
	}

	@Override
	public String getName() {
		return name();
	}

	@JsonCreator
	public static Status fromMap(final Map<String, String> stateObj) {
		return IState.fromMap(stateObj, NAME_TO_STATE_MAP, Status.class);
	}

}
