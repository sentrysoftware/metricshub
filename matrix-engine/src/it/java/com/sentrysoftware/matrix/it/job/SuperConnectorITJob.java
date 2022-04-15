package com.sentrysoftware.matrix.it.job;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class SuperConnectorITJob extends AbstractITJob {

	@Override
	public ITJob withServerRecordData(String... recordDataPaths) throws Exception {
		return this;
	}

	@Override
	public void stopServer() {
		// There is no server to stop
	}

	@Override
	public boolean isServerStarted() {
		// We don't really have a server but let's say server is simulated as started for the SuperConnector
		// Knowing that it only perform local OS commands and AWK calls
		return true;
	}

}