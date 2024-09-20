package org.sentrysoftware.metricshub.hardware.strategy;

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

import static org.sentrysoftware.metricshub.hardware.util.HwCollectHelper.connectorHasHardwareTag;
import static org.sentrysoftware.metricshub.hardware.util.HwConstants.PRESENT_STATUS;

import java.util.Collection;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.sentrysoftware.metricshub.engine.client.ClientsExecutor;
import org.sentrysoftware.metricshub.engine.common.helpers.KnownMonitorType;
import org.sentrysoftware.metricshub.engine.extension.ExtensionManager;
import org.sentrysoftware.metricshub.engine.strategy.AbstractStrategy;
import org.sentrysoftware.metricshub.engine.telemetry.MetricFactory;
import org.sentrysoftware.metricshub.engine.telemetry.Monitor;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;

@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class HardwarePostDiscoveryStrategy extends AbstractStrategy {

	public HardwarePostDiscoveryStrategy(
		@NonNull final TelemetryManager telemetryManager,
		@NonNull final Long strategyTime,
		@NonNull final ClientsExecutor clientsExecutor,
		@NonNull final ExtensionManager extensionManager
	) {
		super(telemetryManager, strategyTime, clientsExecutor, extensionManager);
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

	/**
	 * Checks whether any connector monitor in the current host has the hardware tag
	 * @param telemetryManager The telemetry manager
	 * @return boolean
	 */
	private boolean hostHasConnectorWithHardwareTag(final TelemetryManager telemetryManager) {
		return telemetryManager
			.getMonitors()
			.values()
			.stream()
			.map(Map::values)
			.flatMap(Collection::stream)
			.anyMatch(monitor ->
				monitor.getType().equals(KnownMonitorType.CONNECTOR.getKey()) &&
				connectorHasHardwareTag(monitor, telemetryManager)
			);
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
			.filter(monitor -> {
				// @CHECKSTYLE:OFF
				final boolean isEndpointHost = monitor.isEndpointHost();
				return (
					(!isEndpointHost && connectorHasHardwareTag(monitor, telemetryManager)) ||
					(isEndpointHost && hostHasConnectorWithHardwareTag(telemetryManager))
				);
				// @CHECKSTYLE:ON
			})
			.forEach(monitor -> {
				if (!strategyTime.equals(monitor.getDiscoveryTime())) {
					setAsMissing(monitor, telemetryManager.getHostname(), String.format(PRESENT_STATUS, monitor.getType()));
				} else {
					setAsPresent(monitor, telemetryManager.getHostname(), String.format(PRESENT_STATUS, monitor.getType()));
				}
			});
	}
}
