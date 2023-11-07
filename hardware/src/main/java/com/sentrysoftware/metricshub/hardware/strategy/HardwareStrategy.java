package com.sentrysoftware.metricshub.hardware.strategy;

import com.sentrysoftware.metricshub.engine.common.helpers.KnownMonitorType;
import com.sentrysoftware.metricshub.engine.strategy.IStrategy;
import com.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import com.sentrysoftware.metricshub.hardware.HardwareEnergyPostExecutionService;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Data
public class HardwareStrategy implements IStrategy {

	@NonNull
	private TelemetryManager telemetryManager;

	@NonNull
	private Long strategyTime;

	/**
	 * The connector and hosts are generic kinds so they are excluded from the physical types
	 */
	private static final Set<KnownMonitorType> EXCLUDED_MONITOR_TYPES = Set.of(
		KnownMonitorType.CONNECTOR,
		KnownMonitorType.HOST
	);

	/**
	 * Set of all the hardware monitor types as strings
	 */
	private static final Set<String> HARDWARE_MONITOR_TYPES = Stream
		.of(KnownMonitorType.values())
		.filter(monitorType -> !EXCLUDED_MONITOR_TYPES.contains(monitorType))
		.map(KnownMonitorType::getKey)
		.collect(Collectors.toSet());

	@Override
	public void run() {
		if (hasHardwareMonitors(telemetryManager)) {
			new HardwareEnergyPostExecutionService(telemetryManager).run();
		}
	}

	/**
	 * Whether the telemetry manager defines hardware monitors or not.
	 *
	 * @param telemetryManager Wraps all the monitors.
	 * @return <code>true</code> if the telemetry manager has hardware monitors otherwise <code>false</code>
	 */
	boolean hasHardwareMonitors(final TelemetryManager telemetryManager) {
		return telemetryManager.getMonitors().keySet().stream().anyMatch(HARDWARE_MONITOR_TYPES::contains);
	}

	@Override
	public long getStrategyTimeout() {
		return telemetryManager.getHostConfiguration().getStrategyTimeout();
	}
}
