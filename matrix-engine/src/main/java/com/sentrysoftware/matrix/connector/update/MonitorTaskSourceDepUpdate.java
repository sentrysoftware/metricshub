package com.sentrysoftware.matrix.connector.update;

import com.sentrysoftware.matrix.connector.model.Connector;

public class MonitorTaskSourceDepUpdate extends AbstractConnectorUpdateChain {

	@Override
	void doUpdate(Connector connector) {
		// TODO set sourceDep for each AbstractMonitorTask (Discovery, MonoCollect, MultiCollect, AllAtOnce)
	}

}
