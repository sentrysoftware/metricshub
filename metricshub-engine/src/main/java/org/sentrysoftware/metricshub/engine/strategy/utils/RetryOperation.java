package org.sentrysoftware.metricshub.engine.strategy.utils;

import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.EMPTY;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.sentrysoftware.metricshub.engine.common.exception.RetryableException;

/**
 * The {@code RetryOperation} class provides a mechanism for retrying a function that may throw a {@link RetryableException}.
 * It allows configuring the number of retries, the wait strategy between retries, and handles the retry logic.
 *
 * @param <T> The type of the result produced by the operation.
 */
@AllArgsConstructor
@Builder(setterPrefix = "with")
@Slf4j
public class RetryOperation<T> {

	/**
	 * Time to wait in seconds before triggering a retry
	 */
	@Builder.Default
	private long waitStrategy = 0;

	/**
	 * Default value to return if the retry fails
	 */
	private T defaultValue;

	/**
	 * Maximum number of retries
	 */
	@Builder.Default
	private int maxRetries = 1;

	/**
	 * The hostname of the device you currently query
	 */
	@NonNull
	private String hostname;

	/**
	 * The operation description used to add more context to the log messages
	 */
	@NonNull
	private String description;

	/**
	 * Execute the function, if it fails with the {@link RetryableException}, retry the function.
	 *
	 * @param function operation to execute that produces a result
	 * @return The value returned by the function or default value if all retries fail
	 */
	public T run(final Supplier<T> function) {
		try {
			return function.get();
		} catch (RetryableException e) {
			if (maxRetries <= 0) {
				log.info("Hostname {} - {} failed and will not be retried.", hostname, description);
				return defaultValue;
			}

			return retry(function);
		}
	}

	/**
	 * Performs retry of the given function.
	 *
	 * @param function
	 * @return The value returned by the function or default value if all retries fail
	 */
	private T retry(final Supplier<T> function) {
		log.info(
			"Hostname {} - {} failed and will be retried {} time{}.",
			hostname,
			description,
			maxRetries,
			maxRetries == 1 ? EMPTY : "s"
		);

		int retryCounter = 0;
		while (retryCounter < maxRetries) {
			try {
				if (waitStrategy > 0) {
					log.info("Hostname {} - {} retry will be performed after {} seconds.", hostname, description, waitStrategy);

					pauseRetry();
				}

				return function.get();
			} catch (RetryableException ex) {
				retryCounter++;

				log.info("Hostname {} - {} failed on retry {} / {}.", hostname, description, retryCounter, maxRetries);

				if (retryCounter >= maxRetries) {
					log.warn("Hostname {} - Max retries exceeded for {}.", hostname, description);
					break;
				}
			}
		}

		return defaultValue;
	}

	/**
	 * Performs a Thread.sleep using the wait strategy value expressed in seconds.
	 */
	private void pauseRetry() {
		try {
			TimeUnit.SECONDS.sleep(waitStrategy);
		} catch (InterruptedException e) {
			log.warn("Hostname {} - {} retry interrupted while sleeping.", hostname, description);

			log.debug(
				String.format("Hostname %s - %s retry interrupted while sleeping. Exception: ", hostname, description),
				e
			);

			Thread.currentThread().interrupt();
		}
	}
}
