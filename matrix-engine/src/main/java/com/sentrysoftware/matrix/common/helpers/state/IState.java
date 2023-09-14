package com.sentrysoftware.matrix.common.helpers.state;

import java.util.Map;
import java.util.Optional;

import com.sentrysoftware.matrix.common.helpers.NumberHelper;

import lombok.NonNull;

public interface IState {


	/**
	 * @return The numeric value as int
	 */
	int getNumericValue();


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
	static <T extends IState> Optional<T> interpret(
			final String state,
			final @NonNull Map<String, T> stateTranslations,
			final @NonNull Class<T> type) {
				if (state == null || state.isBlank()) {
					return Optional.empty();
			}

		final IState status = stateTranslations.get(NumberHelper.cleanUpEnumInput(state));

		return Optional.ofNullable(status).map(type::cast);
	}
}
