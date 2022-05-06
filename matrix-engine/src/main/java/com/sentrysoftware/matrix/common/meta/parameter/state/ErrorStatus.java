package com.sentrysoftware.matrix.common.meta.parameter.state;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ERROR_STATUS_PARAMETER_UNIT;

import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.sentrysoftware.matrix.model.alert.Severity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum ErrorStatus implements IState {

	NONE("No Errors", 0, Severity.INFO),
	DETECTED("Detected Errors", 1, Severity.WARN),
	TOO_MANY("Too Many Errors", 2, Severity.ALARM);

	@Getter
	private String displayName;
	@Getter
	private int numericValue;
	@Getter
	private Severity severity;

	/**
	 * Map each state value to a {@link ErrorStatus}
	 */
	private static final Map<String, ErrorStatus> ERROR_STATUS_MAP = Map.of(
			"0", NONE,
			"ok", NONE,
			"1", DETECTED,
			"warn", DETECTED,
			"warning", DETECTED,
			"2", TOO_MANY,
			"alarm", TOO_MANY);

	/**
	 * {@link ErrorStatus} simple class name as Enum type. This is mandatory for the Serialization so that the 
	 * Deserialization knows which concrete Enum should be built for the interface {@link IState}
	 */
	public static final String ERROR_STATUS_TYPE = ErrorStatus.class.getSimpleName();

	/**
	 * Name to state map used by the Jackson deserialization. Once the Enum is selected by the derserializer,
	 * this map is invoked to create the exact fixed enum constant based on its name
	 */
	private static final Map<String, ErrorStatus> NAME_TO_STATE_MAP = Stream
			.of(values())
			.collect(Collectors.toMap(ErrorStatus::name, Function.identity()));

	/**
	 * Interpret the specified state value:
	 *  <ul>
	 *  	<li>{0, ok, OK} as OK</li>
	 *  	<li>{1, warn, WARN, warning, WARNING} as DETECTED</li>
	 *  	<li>{2, alarm, ALARM} as TOO_MANY</li>
	 *  </ul>
	 * @param state String to be interpreted
	 * @return {@link Optional} of {@link ErrorStatus}
	 */
	public static Optional<ErrorStatus> interpret(final String state) {
		return IState.interpret(state, ERROR_STATUS_MAP, ErrorStatus.class);
	}

	@Override
	public String getUnit() {
		return ERROR_STATUS_PARAMETER_UNIT;
	}

	@Override
	public String getType() {
		return ERROR_STATUS_TYPE;
	}

	@Override
	public String getName() {
		return name();
	}

	@JsonCreator
	public static ErrorStatus fromMap(final Map<String, String> stateObj) {
		return IState.fromMap(stateObj, NAME_TO_STATE_MAP, ErrorStatus.class);
	}
}
