package com.sentrysoftware.matrix.common.meta.parameter.state;

import java.util.Map;
import java.util.Optional;

import org.springframework.util.Assert;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sentrysoftware.matrix.common.helpers.NumberHelper;
import com.sentrysoftware.matrix.model.alert.Severity;

import lombok.NonNull;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.EXISTING_PROPERTY, property = "type")

@JsonSubTypes({ 
	@JsonSubTypes.Type(value = DuplexMode.class, name = "DuplexMode"),
	@JsonSubTypes.Type(value = ErrorStatus.class, name = "ErrorStatus"),
	@JsonSubTypes.Type(value = IntrusionStatus.class, name = "IntrusionStatus"),
	@JsonSubTypes.Type(value = LedColorStatus.class, name = "LedColorStatus"),
	@JsonSubTypes.Type(value = LedIndicator.class, name = "LedIndicator"),
	@JsonSubTypes.Type(value = LinkStatus.class, name = "LinkStatus"),
	@JsonSubTypes.Type(value = NeedsCleaning.class, name = "NeedsCleaning"),
	@JsonSubTypes.Type(value = PowerState.class, name = "PowerState"),
	@JsonSubTypes.Type(value = PredictedFailure.class, name = "PredictedFailure"),
	@JsonSubTypes.Type(value = Present.class, name = "Present"),
	@JsonSubTypes.Type(value = Status.class, name = "Status")})
public interface IState {

	/**
	 * @return The unit as {@link String}
	 */
	String getUnit();

	/**
	 * @return The numeric value as int
	 */
	int getNumericValue();

	/**
	 * @return Text description as {@link String}
	 */
	String getDisplayName();

	/**
	 * @return type of the state as string
	 */
	String getType();

	/**
	 * @return name of the state as string
	 */
	String getName();

	/**
	 * @return {@link Severity} of the state
	 */
	Severity getSeverity();

	/**
	 * Extract the 'name' value from the <code>stateObj</code> map then get the {@link IState}
	 * instance from <code>knownStates</code> lookup
	 * @param <T>
	 * 
	 * @param stateObj    {@link IState} formatted as a {@link Map}
	 * @param knownStates Known states instances
	 * @param type        The type used to cast the result
	 * @return {@link IState} instance or <code>null</code> if the
	 *         <code>stateObj</code> is null
	 */
	static <T extends IState> T fromMap(final Map<String, String> stateObj,
			final @NonNull Map<String, T> knownStates, @NonNull Class<T> type) {
		if (stateObj == null) {
			return null;
		}

		final String name = stateObj.get("name");
		Assert.state(name != null, "JSON name attribute cannot be null.");
		return Optional
				.ofNullable(knownStates.get(name))
				.map(type::cast)
				.orElseThrow(() -> new IllegalArgumentException(stateObj.toString()));
	}

	/**
	 * Interpret the given <code>state</code> value based on the
	 * <code>stateTranslations</code> lookup
	 * 
	 * @param <T>
	 * @param state             The state value to interpret
	 * @param stateTranslations The translation lookup from which we get {@link IState} by key
	 * @param type              The type used to cast the result
	 * @return {@link Optional} of <code>T extends IState</code>
	 */
	static <T extends IState> Optional<T> interpret(final String state, final @NonNull Map<String, T> stateTranslations,
			final @NonNull Class<T> type) {
		if (state == null || state.isBlank()) {
			return Optional.empty();
		}

		final IState status = stateTranslations.get(NumberHelper.cleanUpEnumInput(state));

		return Optional.ofNullable(status).map(type::cast);
	}
}
