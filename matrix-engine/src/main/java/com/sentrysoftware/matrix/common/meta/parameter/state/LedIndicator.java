package com.sentrysoftware.matrix.common.meta.parameter.state;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.LED_INDICATOR_PARAMETER_UNIT;

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
public enum LedIndicator implements IState {

	OFF("Off", 0, Severity.INFO),
	BLINKING("Blinking", 1, Severity.WARN),
	ON("On", 2, Severity.ALARM);

	@Getter
	private String displayName;
	@Getter
	private int numericValue;
	@Getter
	private Severity severity;

	/**
	 * Map each state value to a {@link LedIndicator}
	 */
	private static final Map<String, LedIndicator> LED_INDICATOR_MAP = Map.of(
			"off", OFF,
			"0", OFF,
			"blinking", BLINKING,
			"1", BLINKING,
			"on", ON,
			"2", ON);

	/**
	 * {@link LedIndicator} simple class name as Enum type. This is mandatory for the Serialization so that the 
	 * Deserialization knows which concrete Enum should be built for the interface {@link IState}
	 */
	private static final String LED_INDICATOR_TYPE = LedIndicator.class.getSimpleName();

	/**
	 * Name to state map used by the Jackson deserialization. Once the Enum is selected by the derserializer,
	 * this map is invoked to create the exact fixed enum constant based on its name
	 */
	private static final Map<String, LedIndicator> NAME_TO_STATE_MAP = Stream
			.of(values())
			.collect(Collectors.toMap(LedIndicator::name, Function.identity()));

	/**
	 * Interpret the specified state value:
	 *  <ul>
	 *  	<li>{0, off, OFF} as OFF</li>
	 *  	<li>{1, blinking, BLINKING} as BLINKING</li>
	 *  	<li>{2, on, ON} as ON</li>
	 *  </ul>
	 * @param state String to be interpreted
	 * @return {@link Optional} of {@link LedIndicator}
	 */
	public static Optional<LedIndicator> interpret(final String state) {
		return IState.interpret(state, LED_INDICATOR_MAP, LedIndicator.class);
	}

	@Override
	public String getUnit() {
		return LED_INDICATOR_PARAMETER_UNIT;
	}

	@Override
	public String getType() {
		return LED_INDICATOR_TYPE;
	}

	@Override
	public String getName() {
		return name();
	}

	@JsonCreator
	public static LedIndicator fromMap(final Map<String, String> stateObj) {
		return IState.fromMap(stateObj, NAME_TO_STATE_MAP, LedIndicator.class);
	}
}
