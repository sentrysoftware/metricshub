package org.sentrysoftware.metricshub.engine.strategy.detection;

/*-
 * ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲
 * MetricsHub Engine
 * ჻჻჻჻჻჻
 * Copyright 2023 - 2024 Sentry Software
 * ჻჻჻჻჻჻
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱
 */

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.Criterion;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CriterionTestResult {

	private String result;

	private boolean success;

	private String message;

	private Throwable exception;

	public static CriterionTestResult empty() {
		return CriterionTestResult.builder().build();
	}

	/**
	 * Create a detection failure report.
	 * <br>
	 *
	 * @param criterion The failed criterion
	 * @param result    Its result (that doesn't match with the criterion)
	 * @return a new {@link CriterionTestResult} instance
	 */
	public static CriterionTestResult failure(final Criterion criterion, final String result) {
		final String message = String.format(
			"%s test ran but failed:\n%s\n\nActual result:\n%s",
			criterion.getClass().getSimpleName(),
			criterion.toString(),
			result
		);
		return CriterionTestResult.builder().success(false).message(message).result(result).build();
	}

	/**
	 * Create a detection error report.
	 * <br>
	 *
	 * @param criterion The failed criterion
	 * @param reason    The reason why it failed
	 * @param t         the Exception that made the test fail
	 * @return a new {@link CriterionTestResult} instance
	 */
	public static CriterionTestResult error(Criterion criterion, String reason, Throwable t) {
		String message;
		if (criterion == null) {
			message = "Error with a <null> Criterion: " + reason;
		} else {
			message =
				String.format(
					"Error in %s test:\n%s\n\n%s",
					criterion.getClass().getSimpleName(),
					criterion.toString(),
					reason
				);
		}
		return CriterionTestResult.builder().success(false).message(message).exception(t).build();
	}

	/**
	 * Create a detection error report.
	 * <br>
	 *
	 * @param criterion The failed criterion
	 * @param reason    The reason why it failed
	 * @return a new {@link CriterionTestResult} instance
	 */
	public static CriterionTestResult error(final Criterion criterion, final String reason) {
		return error(criterion, reason, null);
	}

	/**
	 * Create a detection error report.
	 * <br>
	 *
	 * @param criterion The failed criterion
	 * @param t         the Exception that made the test fail
	 * @return a new {@link CriterionTestResult} instance
	 */
	public static CriterionTestResult error(final Criterion criterion, final Throwable t) {
		final StringBuilder messageBuilder = new StringBuilder();
		messageBuilder.append(t.getClass().getSimpleName()).append(": ").append(t.getMessage());
		Throwable cause = t.getCause();
		if (cause != null) {
			messageBuilder
				.append("\nCaused by ")
				.append(cause.getClass().getSimpleName())
				.append(": ")
				.append(cause.getMessage());
		} else {
			cause = t;
		}
		return error(criterion, messageBuilder.toString(), cause);
	}

	/**
	 * Create a successful detection report.
	 * <br>
	 *
	 * @param criterion The criterion that was fulfilled
	 * @param result    Its result
	 * @return a new {@link CriterionTestResult} instance
	 */
	public static CriterionTestResult success(final Criterion criterion, final String result) {
		final String message = String.format(
			"%s test succeeded:\n%s\n\nResult: %s",
			criterion.getClass().getSimpleName(),
			criterion.toString(),
			result
		);

		return CriterionTestResult.builder().success(true).message(message).result(result).build();
	}
}
