package com.sentrysoftware.matrix.common.meta.parameter.state;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DUPLEX_MODE_PARAMETER_UNIT;

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
public enum DuplexMode implements IState {

	HALF("Half-duplex", 0, Severity.INFO),
	FULL("Full-duplex", 1, Severity.INFO);

	@Getter
	private String displayName;
	@Getter
	private int numericValue;
	@Getter
	private Severity severity;

	/**
	 * Map each state value to a {@link DuplexMode}
	 */
	private static final Map<String, DuplexMode> DUPLEX_MODE_MAP = Map.of(
			"0", HALF,
			"no", HALF,
			"half", HALF,
			"warn", HALF,
			"warning", HALF,
			"1", FULL,
			"yes", FULL,
			"full", FULL,
			"ok", FULL);

	/**
	 * {@link DuplexMode} simple class name as Enum type. This is mandatory for the Serialization so that the 
	 * Deserialization knows which concrete Enum should be built for the interface {@link IState}
	 */
	public static final String DUPLEX_MODE_TYPE = DuplexMode.class.getSimpleName();

	/**
	 * Name to state map used by the Jackson deserialization. Once the Enum is selected by the derserializer,
	 * this map is invoked to create the exact fixed enum constant based on its name
	 */
	private static final Map<String, DuplexMode> NAME_TO_STATE_MAP = Stream
			.of(values())
			.collect(Collectors.toMap(DuplexMode::name, Function.identity()));

	/**
	 * Interpret the specified state value:
	 *  <ul>
	 *  	<li>{0, no, half, HALF, warn, WARN, warning, WARNING} as HALF</li>
	 *  	<li>{1, yes, full, FULL} as FULL</li>
	 *  </ul>
	 * @param <T>
	 * @param state String to be interpreted
	 * @return {@link Optional} of {@link DuplexMode}
	 */
	public static Optional<DuplexMode> interpret(final String state) {
		return IState.interpret(state, DUPLEX_MODE_MAP, DuplexMode.class);
	}

	@Override
	public String getUnit() {
		return DUPLEX_MODE_PARAMETER_UNIT;
	}

	@Override
	public String getType() {
		return DUPLEX_MODE_TYPE;
	}

	@Override
	public String getName() {
		return name();
	}

	@JsonCreator
	public static DuplexMode fromMap(final Map<String, String> stateObj) {
		return IState.fromMap(stateObj, NAME_TO_STATE_MAP, DuplexMode.class);
	}
}

