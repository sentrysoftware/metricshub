package com.sentrysoftware.metricshub.hardware.strategy;

import static com.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.PRESENT_STATUS;

import com.sentrysoftware.metricshub.engine.common.helpers.KnownMonitorType;
import com.sentrysoftware.metricshub.engine.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.metricshub.engine.strategy.AbstractStrategy;
import com.sentrysoftware.metricshub.engine.telemetry.MetricFactory;
import com.sentrysoftware.metricshub.engine.telemetry.Monitor;
import com.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import java.util.Collection;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class HardwarePostDiscoveryStrategy extends AbstractStrategy {

	public HardwarePostDiscoveryStrategy(
		@NonNull final TelemetryManager telemetryManager,
		@NonNull final Long strategyTime,
		@NonNull final MatsyaClientsExecutor matsyaClientsExecutor
	) {
		super(telemetryManager, strategyTime, matsyaClientsExecutor);
	}

	/**
	 * Set the current monitor as missing
	 * @param hostname The host's name
	 * @param metricName The metric's name
	 * @param monitor A given monitor
	 */
	public void setAsMissing(final Monitor monitor, final String hostname, final String metricName) {
		new MetricFactory(hostname).collectNumberMetric(monitor, metricName, 0.0, strategyTime);
	}

	/**
	 * Checks whether a monitor has a {@link KnownMonitorType}
	 * @param monitorType A given monitor's type
	 * @return boolean
	 */
	private boolean monitorHasKnownType(final String monitorType) {
		for (KnownMonitorType type : KnownMonitorType.values()) {
			if (type.getKey().equals(monitorType)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void run() {
		telemetryManager
			.getMonitors()
			.values()
			.stream()
			.map(Map::values)
			.flatMap(Collection::stream)
			.filter(monitor -> monitorHasKnownType(monitor.getType()))
			.filter(monitor -> !strategyTime.equals(monitor.getDiscoveryTime()))
			.forEach(monitor ->
				setAsMissing(monitor, telemetryManager.getHostname(), String.format(PRESENT_STATUS, monitor.getType()))
			);
	}
}
