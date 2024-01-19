package com.sentrysoftware.metricshub.engine.strategy.collect;

import com.sentrysoftware.metricshub.engine.client.ClientsExecutor;
import com.sentrysoftware.metricshub.engine.strategy.AbstractStrategy;
import com.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import java.util.Collection;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PrepareCollectStrategy extends AbstractStrategy {

	public PrepareCollectStrategy(
		@NonNull final TelemetryManager telemetryManager,
		@NonNull final Long strategyTime,
		@NonNull final ClientsExecutor clientsExecutor
	) {
		super(telemetryManager, strategyTime, clientsExecutor);
	}

	@Override
	public void run() {
		telemetryManager
			.getMonitors()
			.values()
			.stream()
			.map(Map::values)
			.flatMap(Collection::stream)
			.forEach(monitor ->
				monitor
					.getMetrics()
					.values()
					.stream()
					.forEach(metric -> {
						// Save metric, push current value to previous and current collect time to previous
						// Why ? Before the next collect we save the metric previous values
						// in order to compute delta and rates
						metric.save();

						// Discovered metrics should be refreshed in the collect
						// so that they are considered collected
						if (metric.isResetMetricTime()) {
							metric.setCollectTime(strategyTime);
						}
					})
			);
	}
}
