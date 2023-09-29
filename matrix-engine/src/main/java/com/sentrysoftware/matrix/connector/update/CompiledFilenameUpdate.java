package com.sentrysoftware.matrix.connector.update;

import com.sentrysoftware.matrix.connector.model.Connector;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

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
