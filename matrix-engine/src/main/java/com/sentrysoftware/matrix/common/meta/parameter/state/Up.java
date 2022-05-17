package com.sentrysoftware.matrix.common.meta.parameter.state;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.UP_PARAMETER_UNIT;

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
public enum Up implements IState {

	DOWN("Down", 0, Severity.ALARM),
	UP("Up", 1, Severity.INFO);

	@Getter
	private String displayName;
	@Getter
	private int numericValue;
	@Getter
	private Severity severity;

	/**
	 * Map each state value to a {@link Up}
	 */
	private static final Map<String, Up> UP_MAP = Map.of(
			"0", DOWN,
			"1", UP);

	/**
	 * {@link Up} simple class name as Enum type. This is mandatory for the Serialization so that the 
	 * Deserialization knows which concrete Enum should be built for the interface {@link IState}
	 */
	private static final String UP_TYPE = Up.class.getSimpleName();

	/**
	 * Name to state map used by the Jackson deserialization. Once the Enum is selected by the derserializer,
	 * this map is invoked to create the exact fixed enum constant based on its name
	 */
	private static final Map<String, Up> NAME_TO_STATE_MAP = Stream
			.of(values())
			.collect(Collectors.toMap(Up::name, Function.identity()));

	/**
	 * Interpret the specified state value:
	 *  <ul>
	 *  	<li>{ 0 } as DOWN</li>
	 *  	<li>{ 1 } as UP</li>
	 *  </ul>
	 * @param state String to be interpreted
	 * @return {@link Optional} of {@link Present}
	 */
	public static Optional<Up> interpret(final String state) {
		return IState.interpret(state, UP_MAP, Up.class);
	}


	@Override
	public String getUnit() {
		return UP_PARAMETER_UNIT;
	}

	@Override
	public String getType() {
		return UP_TYPE;
	}

	@Override
	public String getName() {
		return name();
	}

	@JsonCreator
	public static Up fromMap(final Map<String, String> stateObj) {
		return IState.fromMap(stateObj, NAME_TO_STATE_MAP, Up.class);
	}
}