package com.sentrysoftware.matrix.strategy.detection;

import com.sentrysoftware.matrix.strategy.AbstractStrategy;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Builder
public class DetectionStrategy extends AbstractStrategy {

	public DetectionStrategy(final TelemetryManager telemetryManager) {
		this.telemetryManager = telemetryManager;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

	@Override
	public void prepare() {
		// TODO Auto-generated method stub

	}

	@Override
	public void post() {
		// TODO Auto-generated method stub

	}
}
