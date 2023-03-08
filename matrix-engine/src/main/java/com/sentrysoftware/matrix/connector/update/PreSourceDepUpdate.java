package com.sentrysoftware.matrix.connector.update;

import com.sentrysoftware.matrix.connector.model.Connector;

public class PreSourceDepUpdate extends SourceConnectorUpdateChain {

	@Override
	void doUpdate(Connector connector) {
		connector.setPreSourceDep(updateSourceDependency(connector.getPre(), "$pre."));

		
		
	}

}
