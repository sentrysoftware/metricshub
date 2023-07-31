package com.sentrysoftware.matrix.strategy.detection;

import java.util.List;

public abstract class AbstractConnectorProcessor {

	/**
	 * Run the Detection job and returns the detected {@link ConnectorTestResult}
	 * @return The {@link List} of {@link ConnectorTestResult}
	 */
	public abstract List<ConnectorTestResult> run();
}
