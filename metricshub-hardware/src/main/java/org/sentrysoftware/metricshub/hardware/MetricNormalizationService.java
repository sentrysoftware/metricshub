package org.sentrysoftware.metricshub.hardware;

/*-
 * ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲
 * MetricsHub Hardware Energy and Sustainability Module
 * ჻჻჻჻჻჻
 * Copyright 2023 - 2024 Sentry Software
 * ჻჻჻჻჻჻
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱
 */

import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sentrysoftware.metricshub.engine.common.helpers.KnownMonitorType;
import org.sentrysoftware.metricshub.engine.delegate.IPostExecutionService;
import org.sentrysoftware.metricshub.engine.telemetry.Monitor;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import org.sentrysoftware.metricshub.hardware.threshold.CpuMetricNormalizer;
import org.sentrysoftware.metricshub.hardware.threshold.FanMetricNormalizer;
import org.sentrysoftware.metricshub.hardware.threshold.GpuMetricNormalizer;
import org.sentrysoftware.metricshub.hardware.threshold.LogicalDiskMetricNormalizer;
import org.sentrysoftware.metricshub.hardware.threshold.MemoryMetricNormalizer;
import org.sentrysoftware.metricshub.hardware.threshold.NetworkMetricNormalizer;
import org.sentrysoftware.metricshub.hardware.threshold.OtherDeviceMetricNormalizer;

/**
 * Service class for normalizing hardware monitor metrics.
 * This service retrieves all monitors from the {@link TelemetryManager}, filters the hardware monitors,
 * and normalizes their metrics based on their type.
 * Implements the {@link IPostExecutionService} interface to define the post-execution metric normalization logic.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MetricNormalizationService implements IPostExecutionService {

	private TelemetryManager telemetryManager;

	/**
	 * The connector and hosts are generic kinds, so they are excluded from the physical types
	 */
	private static final Set<String> EXCLUDED_MONITOR_TYPES = Set.of(
		KnownMonitorType.CONNECTOR.name(),
		KnownMonitorType.HOST.name()
	);

	/**
	 * Checks if the given monitor is a hardware monitor.
	 * This method determines if a given monitor is a hardware monitor by
	 * checking if its type is not included in the {@code EXCLUDED_MONITOR_TYPES} set.
	 * @param monitor the monitor to check
	 * @return {@code true} if the monitor is a hardware monitor, {@code false} otherwise
	 */
	private boolean isHardwareMonitor(final Monitor monitor) {
		return !EXCLUDED_MONITOR_TYPES.contains(monitor.getType());
	}

	/**
	 * Normalizes hardware monitors metrics.
	 * Retrieves all monitors from the {@code telemetryManager}, filters for hardware monitors,
	 * and normalizes the metrics of each monitor based on its type.
	 */
	@Override
	public void run() {
		// Retrieve hardware monitors from the TelemetryManager
		telemetryManager
			.getMonitors()
			.values()
			.stream()
			.flatMap(monitors -> monitors.values().stream())
			.filter(this::isHardwareMonitor)
			.forEach(monitor -> {
				switch (monitor.getType()) {
					case "cpu":
						new CpuMetricNormalizer(telemetryManager.getStrategyTime(), telemetryManager.getHostname())
							.normalize(monitor);
						break;
					case "fan":
						new FanMetricNormalizer(telemetryManager.getStrategyTime(), telemetryManager.getHostname())
							.normalize(monitor);
						break;
					case "gpu":
						new GpuMetricNormalizer(telemetryManager.getStrategyTime(), telemetryManager.getHostname())
							.normalize(monitor);
						break;
					case "logical_disk":
						new LogicalDiskMetricNormalizer(telemetryManager.getStrategyTime(), telemetryManager.getHostname())
							.normalize(monitor);
						break;
					case "lun":
					//TODO
					case "memory":
						new MemoryMetricNormalizer(telemetryManager.getStrategyTime(), telemetryManager.getHostname())
							.normalize(monitor);
						break;
					case "network":
						new NetworkMetricNormalizer(telemetryManager.getStrategyTime(), telemetryManager.getHostname())
							.normalize(monitor);
						break;
					case "other_device":
						new OtherDeviceMetricNormalizer(telemetryManager.getStrategyTime(), telemetryManager.getHostname())
							.normalize(monitor);
						break;
					case "physical_disk":
					//TODO
					case "robotics":
					//TODO
					case "tape_drive":
					//TODO
					case "temperature":
					//TODO
					case "voltage":
					//TODO
					default:
					//TODO other hardware monitor types
				}
			});
	}
}
