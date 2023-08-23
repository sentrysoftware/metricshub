package com.sentrysoftware.matrix.strategy;

import com.sentrysoftware.matrix.telemetry.TelemetryManager;

import lombok.Data;

@Data
public abstract class AbstractStrategy implements IStrategy {

	protected TelemetryManager telemetryManager;
	protected long strategyTime;
}
