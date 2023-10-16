package com.sentrysoftware.matrix.strategy.collect;

import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.PRESENT_STATUS;

import com.sentrysoftware.matrix.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.matrix.strategy.AbstractStrategy;
import com.sentrysoftware.matrix.telemetry.Monitor;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;
import com.sentrysoftware.matrix.telemetry.metric.NumberMetric;
import java.util.Collection;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PostCollectStrategy extends AbstractStrategy {

	public PostCollectStrategy(
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
			.forEach(this::refreshPresentCollectTime);
	}

	/**
	 * Refresh the collect time of the {@link Monitor}'s
	 * hw.status{hw.type="<monitor-type>", state="present"} metric
	 * and set it to the current strategy time.
	 *
	 * @param monitor The {@link Monitor} to refresh
	 */
	private void refreshPresentCollectTime(final Monitor monitor) {
		final String presentMetricName = String.format(PRESENT_STATUS, monitor.getType());

		final NumberMetric presentMetric = monitor.getMetric(presentMetricName, NumberMetric.class);

		if (presentMetric != null) {
			presentMetric.setCollectTime(strategyTime);
		}
	}
}
