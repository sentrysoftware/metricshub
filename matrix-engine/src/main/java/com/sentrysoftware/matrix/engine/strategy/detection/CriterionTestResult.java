package com.sentrysoftware.matrix.engine.strategy.detection;

import com.sentrysoftware.matrix.connector.model.detection.criteria.Criterion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
	 * <p>
	 * @param criterion The failed criterion
	 * @param result Its result (that doesn't match with the criterion)
	 * @return a new {@link CriterionTestresult} instance
	 */
	public static CriterionTestResult failure(Criterion criterion, String result) {
		final String message = String.format( //NOSONAR
				"%s test ran but failed:\n%s\n\nActual result:\n%s",
				criterion.getClass().getSimpleName(),
				criterion.toString(),
				result
		);
		return CriterionTestResult.builder().success(false).message(message).result(result).build();
	}

	/**
	 * Create a detection error report.
	 * <p>
	 * @param criterion The failed criterion
	 * @param reason The reason why it failed
	 * @param t the Exception that made the test fail
	 * @return a new {@link CriterionTestResult} instance
	 */
	public static CriterionTestResult error(Criterion criterion, String reason, Throwable t) {

		String message;
		if (criterion == null) {
			message = "Error with a <null> Criterion: " + reason;
		} else {
			message = String.format( //NOSONAR
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
	 * <p>
	 * @param criterion The failed criterion
	 * @param reason The reason why it failed
	 * @return a new {@link CriterionTestResult} instance
	 */
	public static CriterionTestResult error(Criterion criterion, String reason) {
		return error(criterion, reason, null);
	}

	/**
	 * Create a detection error report.
	 * <p>
	 * @param criterion The failed criterion
	 * @param t the Exception that made the test fail
	 * @return a new {@link CriterionTestResult} instance
	 */
	public static CriterionTestResult error(Criterion criterion, Throwable t) {
		final StringBuilder messageBuilder = new StringBuilder();
		messageBuilder
				.append(t.getClass().getSimpleName())
				.append(": ")
				.append(t.getMessage());
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
	 * <p>
	 * @param criterion The criterion that was fulfilled
	 * @param result Its result
	 * @return a new {@link CriterionTestResult} instance
	 */
	public static CriterionTestResult success(Criterion criterion, String result) {
		final String message = String.format( //NOSONAR
				"%s test succeeded:\n%s\n\nResult: %s",
				criterion.getClass().getSimpleName(),
				criterion.toString(),
				result
		);
		return CriterionTestResult.builder().success(true).message(message).result(result).build();
	}



}
