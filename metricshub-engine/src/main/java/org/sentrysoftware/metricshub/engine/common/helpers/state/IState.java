package org.sentrysoftware.metricshub.engine.common.helpers.state;

import java.util.Map;
import java.util.Optional;
import lombok.NonNull;
import org.sentrysoftware.metricshub.engine.common.helpers.NumberHelper;

/**
 * Interface representing a generic state.
 */
public interface IState {
	/**
	 * Get the numeric value associated with the state.
	 *
	 * @return The numeric value as int
	 */
	int getNumericValue();

	/**
	 * Interpret the given {@code state} value based on the {@code stateTranslations} lookup.
	 *
	 * @param <T>               Type extending {@link IState}
	 * @param state             The state value to interpret
	 * @param stateTranslations The translation lookup from which we get {@link IState} by key
	 * @param type              The type used to cast the result
	 * @return {@link Optional} of {@code T extends IState}
	 */
	static <T extends IState> Optional<T> interpret(
		final String state,
		final @NonNull Map<String, T> stateTranslations,
		final @NonNull Class<T> type
	) {
		if (state == null || state.isBlank()) {
			return Optional.empty();
		}

		final IState status = stateTranslations.get(NumberHelper.cleanUpEnumInput(state));

		return Optional.ofNullable(status).map(type::cast);
	}
}
