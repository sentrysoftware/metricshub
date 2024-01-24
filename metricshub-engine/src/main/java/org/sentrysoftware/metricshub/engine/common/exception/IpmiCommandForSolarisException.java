package org.sentrysoftware.metricshub.engine.common.exception;

/**
 * This exception is thrown for IPMI commands specific to Solaris.
 */
public class IpmiCommandForSolarisException extends Exception {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new IpmiCommandForSolarisException with the specified detail message.
	 *
	 * @param message the detail message.
	 */
	public IpmiCommandForSolarisException(final String message) {
		super(message);
	}
}
