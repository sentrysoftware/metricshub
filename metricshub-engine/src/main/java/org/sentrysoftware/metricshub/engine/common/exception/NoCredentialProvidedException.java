package org.sentrysoftware.metricshub.engine.common.exception;

/**
 * This exception is thrown when no credentials are provided.
 *
 * <p>{@code NoCredentialProvidedException} is a checked exception that indicates a scenario
 * where the required credentials are not provided.</p>
 */
public class NoCredentialProvidedException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new {@code NoCredentialProvidedException} with a default message.
	 * The default message is "No credentials provided."
	 */
	public NoCredentialProvidedException() {
		super("No credentials provided.");
	}
}
