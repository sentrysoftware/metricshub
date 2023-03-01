package com.sentrysoftware.matrix.connector.update;

import com.sentrysoftware.matrix.connector.model.Connector;

public class MonitorTaskSourceTreeUpdate extends AbstractConnectorUpdateChain {

	@Override
	void doUpdate(Connector connector) {
		// TODO set sourceTree for each AbstractMonitorTask (Discovery, MonoCollect, MultiCollect, AllAtOnce)
	}

}
