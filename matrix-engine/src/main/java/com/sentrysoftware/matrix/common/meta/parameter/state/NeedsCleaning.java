package com.sentrysoftware.matrix.common.meta.parameter.state;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.NEEDS_CLEANING_PARAMETER_UNIT;

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
public enum NeedsCleaning implements IState {

	OK("OK", 0, Severity.INFO),
	NEEDED("Cleaning Needed", 1, Severity.WARN),
	NEEDED_IMMEDIATELY("Cleaning Needed Immediately", 2, Severity.ALARM);

	@Getter
	private String displayName;
	@Getter
	private int numericValue;
	@Getter
	private Severity severity;

	/**
	 * Map each state value to a {@link NeedsCleaning}
	 */
	private static final Map<String, NeedsCleaning> NEEDS_CLEANING_MAP = Map.of(
			"0", OK,
			"1", NEEDED,
			"2", NEEDED_IMMEDIATELY);

	/**
	 * {@link NeedsCleaning} simple class name as Enum type. This is mandatory for the Serialization so that the 
	 * Deserialization knows which concrete Enum should be built for the interface {@link IState}
	 */
	private static final String NEEDS_CLEANING_TYPE = NeedsCleaning.class.getSimpleName();

	/**
	 * Name to state map used by the Jackson deserialization. Once the Enum is selected by the derserializer,
	 * this map is invoked to create the exact fixed enum constant based on its name
	 */
	private static final Map<String, NeedsCleaning> NAME_TO_STATE_MAP = Stream
			.of(values())
			.collect(Collectors.toMap(NeedsCleaning::name, Function.identity()));

	/**
	 * Interpret the specified state value:
	 *  <ul>
	 *  	<li>{ 0 } as OK</li>
	 *  	<li>{ 1 } as NEEDED</li>
	 *  	<li>{ 2 } as NEEDED_IMMEDIATELY</li>
	 *  </ul>
	 * @param state String to be interpreted
	 * @return {@link Optional} of {@link NeedsCleaning}
	 */
	public static Optional<NeedsCleaning> interpret(final String state) {

		if (state == null || state.isBlank()) {
			return Optional.empty();
		}

		final NeedsCleaning needsCleaning = NEEDS_CLEANING_MAP.get(
				NumberHelper.formatIntegerState(
						state
						.trim()
						.toLowerCase()
				)
		);

		return Optional.ofNullable(needsCleaning);
	}

	@Override
	public String getUnit() {
		return NEEDS_CLEANING_PARAMETER_UNIT;
	}

	@Override
	public String getType() {
		return NEEDS_CLEANING_TYPE;
	}

	@Override
	public String getName() {
		return name();
	}

	@JsonCreator
	public static NeedsCleaning fromMap(final Map<String, String> stateObj) {
		return IState.fromMap(stateObj, NAME_TO_STATE_MAP, NeedsCleaning.class);
	}
}
