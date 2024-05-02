package org.sentrysoftware.metricshub.engine.strategy.collect;

/*-
 * ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲
 * MetricsHub Engine
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
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.sentrysoftware.metricshub.engine.client.ClientsExecutor;
import org.sentrysoftware.metricshub.engine.configuration.WinRmConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.WmiConfiguration;
import org.sentrysoftware.metricshub.engine.extension.ExtensionManager;
import org.sentrysoftware.metricshub.engine.extension.IProtocolExtension;
import org.sentrysoftware.metricshub.engine.strategy.AbstractStrategy;
import org.sentrysoftware.metricshub.engine.strategy.utils.WqlDetectionHelper;
import org.sentrysoftware.metricshub.engine.telemetry.MetricFactory;
import org.sentrysoftware.metricshub.engine.telemetry.Monitor;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;

/**
 * A strategy that aims to perform health check over a hostname on each protocol
 * (HTTP, SNMP, IPMI, ...).
 *
 * <p>
 * This aims to report the responsiveness of configured protocols on a resource.
 * The health check is performed at the beginning of each data collection cycle,
 * and a metric is generated for each protocol indicating whether it is
 * responding or not.
 * </p>
 */
@Slf4j
public class ProtocolHealthCheckStrategy extends AbstractStrategy {

	/**
	 * Protocol up status value '1.0'
	 */
	public static final Double UP = 1.0;

	/**
	 * Protocol down status value '0.0'
	 */
	public static final Double DOWN = 0.0;

	/**
	 * Up metric name format that will be saved by the metric factory
	 */
	private static final String UP_METRIC_FORMAT = "metricshub.host.up{protocol=\"%s\"}";

	/**
	 * WBEM Up metric
	 */
	public static final String WBEM_UP_METRIC = String.format(UP_METRIC_FORMAT, "wbem");

	/**
	 * WMI Up metric
	 */
	public static final String WMI_UP_METRIC = String.format(UP_METRIC_FORMAT, "wmi");

	/**
	 * WINRM Up metric
	 */
	public static final String WINRM_UP_METRIC = String.format(UP_METRIC_FORMAT, "winrm");

	/**
	 * WQL Query to test WMI and WinRM protocols health check
	 */
	public static final String WMI_AND_WINRM_TEST_QUERY = "Select Name FROM Win32_ComputerSystem";

	/**
	 * WMI and WinRM protocol health check test Namespace
	 */
	public static final String WMI_AND_WINRM_TEST_NAMESPACE = "root\\cimv2";

	/**
	 * Constructs a new {@code HealthCheckStrategy} using the provided telemetry
	 * manager, strategy time, and clients executor.
	 *
	 * @param telemetryManager The telemetry manager responsible for managing
	 *                         telemetry-related operations.
	 * @param strategyTime     The time when the strategy is executed.
	 * @param clientsExecutor  The executor for managing clients used in the
	 *                         strategy.
	 * @param extensionManager The extension manager where all the required extensions are handled.
	 */
	@Builder
	public ProtocolHealthCheckStrategy(
		@NonNull final TelemetryManager telemetryManager,
		@NonNull final Long strategyTime,
		@NonNull final ClientsExecutor clientsExecutor,
		@NonNull final ExtensionManager extensionManager
	) {
		super(telemetryManager, strategyTime, clientsExecutor, extensionManager);
	}

	@Override
	public void run() {
		// Call the extensions to check the protocol health
		final List<IProtocolExtension> protocolExtensions = extensionManager.findProtocolCheckExtensions(telemetryManager);
		protocolExtensions.forEach(protocolExtension -> protocolExtension.checkProtocol(telemetryManager));

		// Retrieve the hostname
		final String hostname = telemetryManager.getHostConfiguration().getHostname();

		// Retrieve the host endpoint monitor
		final Monitor hostMonitor = telemetryManager.getEndpointHostMonitor();

		// If the host monitor does not exist, the strategy is aborted
		if (hostMonitor == null) {
			return;
		}

		log.info("Hostname {} - Performing protocol health check.", hostname);

		// Create a metric factory
		final MetricFactory metricFactory = new MetricFactory(hostname);

		// Check the hostname protocols health
		checkWmiHealth(hostname, hostMonitor, metricFactory);
		checkWinRmHealth(hostname, hostMonitor, metricFactory);
	}

	@Override
	public long getStrategyTimeout() {
		return telemetryManager.getHostConfiguration().getStrategyTimeout();
	}

	@Override
	public Long getStrategyTime() {
		return telemetryManager.getStrategyTime();
	}

	/**
	 * Check WMI protocol health on the hostname for the host monitor.
	 *
	 * <ul>
	 * 	<li>Criteria: The query must not return an error for at least one of the root\cimv2 namespace.</li>
	 * 	<li>Query: SELECT Name FROM Win32_ComputerSystem.</li>
	 * 	<li>Success Conditions: No errors in the query result, indicating that the protocol is responding.</li>
	 * </ul>
	 *
	 * @param hostname      The hostname on which we perform health check
	 * @param hostMonitor   An endpoint host monitor
	 * @param metricFactory The metric factory used to collect the health check metric
	 */
	public void checkWmiHealth(String hostname, Monitor hostMonitor, MetricFactory metricFactory) {
		// Create and set the WMI result to null
		List<List<String>> wmiResult = null;

		// Retrieve WMI Configuration from the telemetry manager host configuration
		final WmiConfiguration wmiConfiguration = (WmiConfiguration) telemetryManager
			.getHostConfiguration()
			.getConfigurations()
			.get(WmiConfiguration.class);

		// Stop the health check if there is not an WMI configuration
		if (wmiConfiguration == null) {
			return;
		}

		log.info(
			"Hostname {} - Checking WMI protocol status. Sending a WQL SELECT request on {} namespace.",
			hostname,
			WMI_AND_WINRM_TEST_NAMESPACE
		);

		try {
			wmiResult =
				clientsExecutor.executeWmi(hostname, wmiConfiguration, WMI_AND_WINRM_TEST_QUERY, WMI_AND_WINRM_TEST_NAMESPACE);
		} catch (Exception e) {
			if (WqlDetectionHelper.isAcceptableException(e)) {
				// Generate a metric from the WMI result
				metricFactory.collectNumberMetric(hostMonitor, WMI_UP_METRIC, UP, strategyTime);
				return;
			}
			log.debug(
				"Hostname {} - Checking WMI protocol status. WMI exception when performing a WQL SELECT request on {} namespace: ",
				hostname,
				WMI_AND_WINRM_TEST_NAMESPACE,
				e
			);
		}

		// Generate a metric from the WMI result
		metricFactory.collectNumberMetric(hostMonitor, WMI_UP_METRIC, wmiResult != null ? UP : DOWN, strategyTime);
	}

	/**
	 * Check WINRM protocol health on the hostname for the host monitor.
	 *
	 * <ul>
	 * 	<li>Criteria: The query must not return an error for at least one of the root\cimv2 namespace.</li>
	 * 	<li>Query: SELECT Name FROM Win32_ComputerSystem.</li>
	 * 	<li>Success Conditions: No errors in the query result, indicating that the protocol is responding.</li>
	 * </ul>
	 *
	 * @param hostname      The hostname on which we perform health check
	 * @param hostMonitor   An endpoint host monitor
	 * @param metricFactory The metric factory used to collect the health check metric
	 */
	public void checkWinRmHealth(String hostname, Monitor hostMonitor, MetricFactory metricFactory) {
		// Create and set the WinRM result to null
		List<List<String>> winRmResult = null;

		// Retrieve WinRM Configuration from the telemetry manager host configuration
		final WinRmConfiguration winRmConfiguration = (WinRmConfiguration) telemetryManager
			.getHostConfiguration()
			.getConfigurations()
			.get(WinRmConfiguration.class);

		// Stop the health check if there is not an WinRM configuration
		if (winRmConfiguration == null) {
			return;
		}

		log.info(
			"Hostname {} - Checking WinRM protocol status. Sending a WQL SELECT request on {} namespace.",
			hostname,
			WMI_AND_WINRM_TEST_NAMESPACE
		);

		try {
			winRmResult =
				clientsExecutor.executeWqlThroughWinRm(
					hostname,
					winRmConfiguration,
					WMI_AND_WINRM_TEST_QUERY,
					WMI_AND_WINRM_TEST_NAMESPACE
				);
		} catch (Exception e) {
			if (WqlDetectionHelper.isAcceptableException(e)) {
				// Generate a metric from the WinRM result
				metricFactory.collectNumberMetric(hostMonitor, WINRM_UP_METRIC, UP, strategyTime);
				return;
			}
			log.debug(
				"Hostname {} - Checking WinRM protocol status. WinRM exception when performing a WQL SELECT request on {} namespace: ",
				hostname,
				WMI_AND_WINRM_TEST_NAMESPACE,
				e
			);
		}

		// Generate a metric from the WINRM result
		metricFactory.collectNumberMetric(hostMonitor, WINRM_UP_METRIC, winRmResult != null ? UP : DOWN, strategyTime);
	}
}
