package com.sentrysoftware.matrix.strategy.collect;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;

import lombok.Data;

@Data
public abstract class AbstractCollect {

	protected Connector connector;

	protected String hostname;

	protected TelemetryManager telemetryManager;

	protected AbstractCollect connectorCollect;

	/**
	 * Run the collect for the connector 
	 */
	public abstract void collect();
}
