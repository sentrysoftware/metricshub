package com.sentrysoftware.matrix.common.meta.parameter.state;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PREDICTED_FAILURE_PARAMETER_UNIT;

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
public enum PredictedFailure implements IState {

	OK("OK", 0, Severity.INFO),
	FAILURE_PREDICTED("Failure Predicted", 1, Severity.WARN);

	@Getter
	private String displayName;
	@Getter
	private int numericValue;
	@Getter
	private Severity severity;

	/**
	 * Map each state value to a {@link PredictedFailure}
	 */
	private static final Map<String, PredictedFailure> PREDICTED_FAILURE_MAP = Map.of(
			"0", OK,
			"ok", OK,
			"false", OK,
			"1", FAILURE_PREDICTED,
			"warn", FAILURE_PREDICTED,
			"warning", FAILURE_PREDICTED,
			"2", FAILURE_PREDICTED,
			"alarm", FAILURE_PREDICTED,
			"true", FAILURE_PREDICTED);

	/**
	 * {@link PredictedFailure} simple class name as Enum type. This is mandatory for the Serialization so that the 
	 * Deserialization knows which concrete Enum should be built for the interface {@link IState}
	 */
	private static final String PREDICTED_FAILURE_TYPE = PredictedFailure.class.getSimpleName();

	/**
	 * Name to state map used by the Jackson deserialization. Once the Enum is selected by the derserializer,
	 * this map is invoked to create the exact fixed enum constant based on its name
	 */
	private static final Map<String, PredictedFailure> NAME_TO_STATE_MAP = Stream
			.of(values())
			.collect(Collectors.toMap(PredictedFailure::name, Function.identity()));

	/**
	 * Interpret the specified state value:
	 *  <ul>
	 *  	<li>{0, ok, OK, false, FALSE} as OK</li>
	 *  	<li>{1, warn, WARN, warning, WARNING, 2, alarm, ALARM, true, TRUE} as FAILURE_PREDICTED</li>
	 *  	<li>{2, alarm, ALARM} as TOO_MANY</li>
	 *  </ul>
	 * @param state String to be interpreted
	 * @return {@link Optional} of {@link PredictedFailure}
	 */
	public static Optional<PredictedFailure> interpret(final String state) {

		if (state == null || state.isBlank()) {
			return Optional.empty();
		}

		final PredictedFailure predictedFailure = PREDICTED_FAILURE_MAP.get(
				NumberHelper.formatIntegerState(
						state
						.trim()
						.toLowerCase()
				)
		);

		return  Optional.ofNullable(predictedFailure);
	}

	@Override
	public String getUnit() {
		return PREDICTED_FAILURE_PARAMETER_UNIT;
	}

	@Override
	public String getType() {
		return PREDICTED_FAILURE_TYPE;
	}

	@Override
	public String getName() {
		return name();
	}

	@JsonCreator
	public static PredictedFailure fromMap(final Map<String, String> stateObj) {
		return IState.fromMap(stateObj, NAME_TO_STATE_MAP, PredictedFailure.class);
	}
}