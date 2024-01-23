package org.sentrysoftware.metricshub.cli.service.protocol;

/**
 * AbstractTransportProtocolCli is the base class for implementing command-line interface (CLI) transport protocols
 * in the MetricsHub command-line interface.
 * It provides common functionality for deducing port numbers based on transport protocols and checking whether HTTPS is configured.
 */
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
	 * Check whether HTTPS is configured or not
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
