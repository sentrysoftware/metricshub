package com.sentrysoftware.metricshub.hardware.strategy;

import static com.sentrysoftware.metricshub.hardware.util.HwConstants.PRESENT_STATUS;

import com.sentrysoftware.metricshub.engine.ClientsExecutor;
import com.sentrysoftware.metricshub.engine.common.helpers.KnownMonitorType;
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
		@NonNull final ClientsExecutor clientsExecutor
	) {
		super(telemetryManager, strategyTime, clientsExecutor);
	}

	/**
	 * Sets the current monitor as missing
	 * @param monitor A given monitor
	 * @param hostname The host's name
	 * @param metricName The collected metric name
	 */
	public void setAsMissing(final Monitor monitor, final String hostname, final String metricName) {
		new MetricFactory(hostname).collectNumberMetric(monitor, metricName, 0.0, strategyTime);
	}

	/**
	 * Sets the current monitor as present
	 * @param monitor A given monitor
	 * @param hostname The host's name
	 * @param metricName The collected metric name
	 */
	public void setAsPresent(final Monitor monitor, final String hostname, final String metricName) {
		new MetricFactory(hostname).collectNumberMetric(monitor, metricName, 1.0, strategyTime);
	}

	/**
	 * Checks whether a monitor has a {@link KnownMonitorType}
	 * @param monitorType A given monitor's type
	 * @return boolean whether a monitor has a {@link KnownMonitorType}
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
		// Loop over each known monitor from the telemetry manager and
		// set the monitor as missing if strategy time is not equal to monitor's discovery time
		// otherwise set the monitor as present.
		telemetryManager
			.getMonitors()
			.values()
			.stream()
			.map(Map::values)
			.flatMap(Collection::stream)
			.filter(monitor -> monitorHasKnownType(monitor.getType()))
			.forEach(monitor -> {
				if (!strategyTime.equals(monitor.getDiscoveryTime())) {
					setAsMissing(monitor, telemetryManager.getHostname(), String.format(PRESENT_STATUS, monitor.getType()));
				} else {
					setAsPresent(monitor, telemetryManager.getHostname(), String.format(PRESENT_STATUS, monitor.getType()));
				}
			});
	}
}
