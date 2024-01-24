package org.sentrysoftware.metricshub.engine.strategy.collect;

import java.util.Collection;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.sentrysoftware.metricshub.engine.client.ClientsExecutor;
import org.sentrysoftware.metricshub.engine.strategy.AbstractStrategy;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;

/**
 * The {@code PrepareCollectStrategy} class represents a strategy for preparing the collection of metrics
 * from monitors in a monitoring system.
 *
 * <p>
 * This class is part of a strategy design pattern and is responsible for preparing the collection by
 * saving metric values and updating collect times before the actual collection operation.
 * </p>
 *
 * <p>
 * It iterates through all monitors and metrics managed by the telemetry manager, saving metric values,
 * and updating collect times for discovered metrics.
 * </p>
 */
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PrepareCollectStrategy extends AbstractStrategy {

	/**
	 * Constructs a new {@code PrepareCollectStrategy} using the provided telemetry manager, strategy time, and
	 * clients executor.
	 *
	 * @param telemetryManager The telemetry manager responsible for managing telemetry-related operations.
	 * @param strategyTime     The time when the strategy is executed.
	 * @param clientsExecutor  The executor for managing clients used in the strategy.
	 */
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
