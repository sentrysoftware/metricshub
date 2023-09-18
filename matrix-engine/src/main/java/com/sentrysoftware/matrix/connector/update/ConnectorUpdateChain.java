package com.sentrysoftware.matrix.connector.update;

import com.sentrysoftware.matrix.connector.model.Connector;

/**
 * Update chain interface used to update connector data at the POST processing
 *
 */
public interface ConnectorUpdateChain {
	/**
	 * Set the next update chain
	 *
	 * @param nextChain
	 */
	void setNextUpdateChain(ConnectorUpdateChain nextChain);

	/**
	 * Update the given connector
	 *
	 * @param connector
	 */
	void update(Connector connector);
}
