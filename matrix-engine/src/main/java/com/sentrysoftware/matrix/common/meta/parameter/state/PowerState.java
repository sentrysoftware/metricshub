package com.sentrysoftware.matrix.common.meta.parameter.state;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.POWER_STATE_PARAMETER_UNIT;

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
public enum PowerState implements IState {

	OFF("Off", 0, Severity.INFO),
	SUSPENDED("Suspended", 1, Severity.INFO),
	ON("On", 2, Severity.INFO);

	@Getter
	private String displayName;
	@Getter
	private int numericValue;
	@Getter
	private Severity severity;


	/**
	 * Map each state value to a {@link PowerState}
	 */
	private static final Map<String, PowerState> POWER_STATE_MAP = Map.of(
			"off", OFF,
			"0", OFF,
			"suspended", SUSPENDED,
			"1", SUSPENDED,
			"on", ON,
			"2", ON);

	/**
	 * {@link PowerState} simple class name as Enum type. This is mandatory for the Serialization so that the 
	 * Deserialization knows which concrete Enum should be built for the interface {@link IState}
	 */
	private static final String POWER_STATE_TYPE = PowerState.class.getSimpleName();

	/**
	 * Name to state map used by the Jackson deserialization. Once the Enum is selected by the derserializer,
	 * this map is invoked to create the exact fixed enum constant based on its name
	 */
	private static final Map<String, PowerState> NAME_TO_STATE_MAP = Stream.of(values())
			.collect(Collectors.toMap(PowerState::name, Function.identity()));

	/**
	 * Interpret the specified state value:
	 *  <ul>
	 *  	<li>{0, off, OFF} as OFF</li>
	 *  	<li>{1, suspended, SUSPENDED} as SUSPENDED</li>
	 *  	<li>{2, on, ON} as ON</li>
	 *  </ul>
	 * @param state String to be interpreted
	 * @return {@link Optional} of {@link PowerState} 
	 */
	public static Optional<PowerState> interpret(final String state) {
		return IState.interpret(state, POWER_STATE_MAP, PowerState.class);
	}

	@Override
	public String getUnit() {
		return POWER_STATE_PARAMETER_UNIT;
	}

	@Override
	public String getType() {
		return POWER_STATE_TYPE;
	}

	@Override
	public String getName() {
		return name();
	}

	@JsonCreator
	public static PowerState fromMap(final Map<String, String> stateObj) {
		return IState.fromMap(stateObj, NAME_TO_STATE_MAP, PowerState.class);
	}
}
