package com.sentrysoftware.matrix.strategy.collect;

import com.sentrysoftware.matrix.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.matrix.strategy.AbstractStrategy;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class CollectStrategy extends AbstractStrategy {

	public CollectStrategy(
		@NonNull final TelemetryManager telemetryManager,
		final long strategyTime,
		@NonNull final MatsyaClientsExecutor matsyaClientsExecutor
	) {
		super(telemetryManager, strategyTime, matsyaClientsExecutor);

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
