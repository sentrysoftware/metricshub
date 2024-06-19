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
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sentrysoftware.metricshub.engine.common.helpers.KnownMonitorType;
import org.sentrysoftware.metricshub.engine.delegate.IPostExecutionService;
import org.sentrysoftware.metricshub.engine.telemetry.Monitor;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;

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

	private boolean isHardwareMonitor(final Monitor monitor) {
		return !EXCLUDED_MONITOR_TYPES.contains(monitor.getType());
	}

	@Override
	public void run() {
		final List<Monitor> hardwareMonitors = telemetryManager
			.getMonitors()
			.values()
			.stream()
			.flatMap(monitors -> monitors.values().stream())
			.filter(this::isHardwareMonitor)
			.toList();

		hardwareMonitors.forEach(monitor -> {
			switch (monitor.getType()) {
				case "cpu":
				//TODO
				case "fan":
				//TODO
				case "gpu":
				//TODO
				case "logical_disk":
				//TODO
				case "lun":
				//TODO
				case "memory":
				//TODO
				case "network":
				//TODO
				case "other_device":
				//TODO
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
