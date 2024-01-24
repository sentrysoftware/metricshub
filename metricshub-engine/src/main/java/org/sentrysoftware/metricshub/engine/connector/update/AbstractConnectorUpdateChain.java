package org.sentrysoftware.metricshub.engine.connector.update;

import lombok.Data;
import lombok.NonNull;
import org.sentrysoftware.metricshub.engine.connector.model.Connector;

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
