package org.sentrysoftware.metricshub.engine.configuration;

/**
 * The IWinConfiguration interface represents the configuration for Windows protocols in the MetricsHub engine.
 */
public interface IWinConfiguration extends IConfiguration {
	/**
	 * Gets the namespace for the Windows protocol.
	 *
	 * @return The namespace as a string.
	 */
	String getNamespace();

	/**
	 * Gets the username for the Windows protocol.
	 *
	 * @return The username as a string.
	 */
	String getUsername();

	/**
	 * Gets the timeout for the Windows protocol.
	 *
	 * @return The timeout as a Long value.
	 */
	Long getTimeout();

	/**
	 * Gets the password for the Windows protocol.
	 *
	 * @return The password as a character array.
	 */
	char[] getPassword();
}
