package org.sentrysoftware.metricshub.engine.connector.update;

import org.sentrysoftware.metricshub.engine.connector.model.Connector;

/**
 * Update chain interface used to update connector data at the POST processing
 *
 */
public interface ConnectorUpdateChain {
	/**
	 * Sets the next update chain in the sequence.
	 *
	 * @param nextChain The next update chain in the sequence.
	 */
	void setNextUpdateChain(ConnectorUpdateChain nextChain);

	/**
	 * Updates the given {@link Connector} object using the defined logic.
	 *
	 * @param connector The connector to be updated.
	 */
	void update(Connector connector);
}
