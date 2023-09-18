package com.sentrysoftware.matrix.strategy.simple;

import com.sentrysoftware.matrix.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.matrix.strategy.common.DiscoveryOrSimpleStrategy;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SimpleStrategy extends DiscoveryOrSimpleStrategy {

	public SimpleStrategy(
		@NonNull final TelemetryManager telemetryManager,
		final long strategyTime,
		@NonNull final MatsyaClientsExecutor matsyaClientsExecutor
	) {
		super(telemetryManager, strategyTime, matsyaClientsExecutor, true);
	}
}
