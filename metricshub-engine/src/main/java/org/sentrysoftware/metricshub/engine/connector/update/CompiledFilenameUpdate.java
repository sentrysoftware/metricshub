package org.sentrysoftware.metricshub.engine.connector.update;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.sentrysoftware.metricshub.engine.connector.model.Connector;

/**
 * Implementation of {@link AbstractConnectorUpdateChain} for updating compiled filename in the connector.
 */
@RequiredArgsConstructor
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(callSuper = true)
public class CompiledFilenameUpdate extends AbstractConnectorUpdateChain {

	@NonNull
	private String filename;

	@Override
	void doUpdate(Connector connector) {
		connector.getOrCreateConnectorIdentity().setCompiledFilename(filename.substring(0, filename.lastIndexOf('.')));
	}
}
