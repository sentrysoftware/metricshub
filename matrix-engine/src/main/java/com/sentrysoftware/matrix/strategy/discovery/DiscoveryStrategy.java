package com.sentrysoftware.matrix.strategy.discovery;

import com.sentrysoftware.matrix.strategy.AbstractStrategy;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class DiscoveryStrategy extends AbstractStrategy {

	public DiscoveryStrategy(final TelemetryManager telemetryManager) {
		this.telemetryManager = telemetryManager;
	}

	@Override
	public void prepare() {
		// TODO Auto-generated method stub
	}

	@Override
	public void post() {
		// TODO Auto-generated method stub
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
	}
}
