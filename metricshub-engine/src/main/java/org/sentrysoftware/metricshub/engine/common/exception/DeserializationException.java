package org.sentrysoftware.metricshub.engine.common.exception;

/**
 * Exception thrown to indicate a failure in deserialization.
 */
public class DeserializationException extends RuntimeException {

	private static final long serialVersionUID = -5293423254348627190L;

	/**
	 * Constructs a new {@code DeserializationException} with the specified detail message and cause.
	 *
	 * @param message The detail message.
	 * @param cause   The cause (which is saved for later retrieval by the {@link #getCause()} method).
	 */
	public DeserializationException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * Constructs a new {@code DeserializationException} with the specified detail message.
	 *
	 * @param message The detail message.
	 */
	public DeserializationException(final String message) {
		super(message);
	}
}
