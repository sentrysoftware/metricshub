package com.sentrysoftware.matrix.common.meta.parameter.state;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PRESENT_PARAMETER_UNIT;

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
public enum Present implements IState {

	MISSING("Missing", 0, Severity.ALARM),
	PRESENT("Present", 1, Severity.INFO);

	@Getter
	private String displayName;
	@Getter
	private int numericValue;
	@Getter
	private Severity severity;

	/**
	 * Map each state value to a {@link Present}
	 */
	private static final Map<String, Present> PRESENT_MAP = Map.of(
			"0", MISSING,
			"1", PRESENT);

	/**
	 * {@link Present} simple class name as Enum type. This is mandatory for the Serialization so that the 
	 * Deserialization knows which concrete Enum should be built for the interface {@link IState}
	 */
	private static final String PRESENT_TYPE = Present.class.getSimpleName();

	/**
	 * Name to state map used by the Jackson deserialization. Once the Enum is selected by the derserializer,
	 * this map is invoked to create the exact fixed enum constant based on its name
	 */
	private static final Map<String, Present> NAME_TO_STATE_MAP = Stream
			.of(values())
			.collect(Collectors.toMap(Present::name, Function.identity()));

	/**
	 * Interpret the specified state value:
	 *  <ul>
	 *  	<li>{ 0 } as MISSING</li>
	 *  	<li>{ 1 } as PRESENT</li>
	 *  </ul>
	 * @param state String to be interpreted
	 * @return {@link Optional} of {@link Present}
	 */
	public static Optional<Present> interpret(final String state) {
		return IState.interpret(state, PRESENT_MAP, Present.class);
	}


	@Override
	public String getUnit() {
		return PRESENT_PARAMETER_UNIT;
	}

	@Override
	public String getType() {
		return PRESENT_TYPE;
	}

	@Override
	public String getName() {
		return name();
	}

	@JsonCreator
	public static Present fromMap(final Map<String, String> stateObj) {
		return IState.fromMap(stateObj, NAME_TO_STATE_MAP, Present.class);
	}
}