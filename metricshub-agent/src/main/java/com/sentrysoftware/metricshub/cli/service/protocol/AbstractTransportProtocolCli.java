package com.sentrysoftware.metricshub.cli.service.protocol;

public abstract class AbstractTransportProtocolCli implements IProtocolConfigCli {

	/**
	 * Get or deduce the port number based on the transport protocol
	 *
	 * @return int value
	 */
	protected int getOrDeducePortNumber() {
		final Integer port = getPort();
		if (port != null) {
			return port;
		} else if (isHttps()) {
			return defaultHttpsPortNumber();
		}
		return defaultHttpPortNumber();
	}

	/**
	 * Whether HTTPS is configured or not
	 *
	 * @return boolean value
	 */
	protected abstract boolean isHttps();

	/**
	 * @return Default HTTPS port number
	 */
	protected abstract int defaultHttpsPortNumber();

	/**
	 * @return Default HTTP port number
	 */
	protected abstract int defaultHttpPortNumber();

	/**
	 * @return Configured port number
	 */
	protected abstract Integer getPort();
}
