package com.sentrysoftware.metricshub.engine.strategy.discovery;

import com.sentrysoftware.metricshub.engine.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.metricshub.engine.strategy.AbstractStrategy;
import com.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import java.util.Collection;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PostDiscoveryStrategy extends AbstractStrategy {

	public PostDiscoveryStrategy(
		@NonNull final TelemetryManager telemetryManager,
		@NonNull final Long strategyTime,
		@NonNull final MatsyaClientsExecutor matsyaClientsExecutor
	) {
		super(telemetryManager, strategyTime, matsyaClientsExecutor);
	}

	@Override
	public void run() {
		telemetryManager
			.getMonitors()
			.values()
			.stream()
			.map(Map::values)
			.flatMap(Collection::stream)
			.filter(monitor -> !strategyTime.equals(monitor.getDiscoveryTime()))
			.forEach(monitor -> monitor.setAsMissing(telemetryManager.getHostname()));
	}
}
