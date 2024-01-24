package org.sentrysoftware.metricshub.engine.common.exception;

/**
 * This class is used to represent clients-related exceptions.
 * Clients include among others: IPMI, SNMP, SSH, WMI, WinRm, WBEM.
 */
public class ClientException extends Exception {

	/**
	 * Exception class for representing client-related exceptions.
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new ClientException.
	 */
	public ClientException() {
		super();
	}

	/**
	 * Constructs a new ClientException with the specified cause.
	 *
	 * @param cause The cause of the exception.
	 */
	public ClientException(Exception cause) {
		super(cause);
	}

	/**
	 * Constructs a new ClientException with the specified detail message.
	 *
	 * @param message The detail message.
	 */
	public ClientException(String message) {
		super(message);
	}

	/**
	 * Constructs a new ClientException with the specified detail message and cause.
	 *
	 * @param message The detail message.
	 * @param cause   The cause of the exception.
	 */
	public ClientException(String message, Exception cause) {
		super(message, cause);
	}
}
