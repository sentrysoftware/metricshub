package com.sentrysoftware.matrix.strategy;

import com.sentrysoftware.matrix.telemetry.TelemetryManager;

public abstract class AbstractStrategy implements IStrategy, Runnable {

	protected TelemetryManager telemetryManager;
}
