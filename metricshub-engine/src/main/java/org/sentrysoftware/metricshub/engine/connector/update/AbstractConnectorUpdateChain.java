package org.sentrysoftware.metricshub.engine.connector.update;

import lombok.Data;
import lombok.NonNull;
import org.sentrysoftware.metricshub.engine.connector.model.Connector;

/**
 * An abstract class implementing the {@link ConnectorUpdateChain} interface.
 * It provides the structure for handling the update chain and ensures that the next chain in the sequence is called.
 */
@Data
public abstract class AbstractConnectorUpdateChain implements ConnectorUpdateChain {

	protected ConnectorUpdateChain nextChain;

	@Override
	public void setNextUpdateChain(ConnectorUpdateChain nextChain) {
		this.nextChain = nextChain;
	}

	@Override
	public void update(@NonNull Connector connector) {
		doUpdate(connector);

		// Call next update chain
		if (nextChain != null) {
			nextChain.update(connector);
		}
	}

	abstract void doUpdate(Connector connector);
}
