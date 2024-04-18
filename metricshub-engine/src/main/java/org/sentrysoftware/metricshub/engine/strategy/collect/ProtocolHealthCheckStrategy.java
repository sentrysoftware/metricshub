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

import static org.sentrysoftware.metricshub.engine.configuration.OsCommandConfiguration.DEFAULT_TIMEOUT;

import java.util.Collections;
import java.util.List;
import lombok.Builder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.sentrysoftware.metricshub.engine.client.ClientsExecutor;
import org.sentrysoftware.metricshub.engine.configuration.SshConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.WbemConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.WinRmConfiguration;
import org.sentrysoftware.metricshub.engine.configuration.WmiConfiguration;
import org.sentrysoftware.metricshub.engine.extension.ExtensionManager;
import org.sentrysoftware.metricshub.engine.extension.IProtocolExtension;
import org.sentrysoftware.metricshub.engine.strategy.AbstractStrategy;
import org.sentrysoftware.metricshub.engine.strategy.utils.OsCommandHelper;
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
	 * SSH Up metric
	 */
	public static final String SSH_UP_METRIC = String.format(UP_METRIC_FORMAT, "ssh");

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
	 * SSH test command to execute
	 */
	public static final String SSH_TEST_COMMAND = "echo test";

	/**
	 * List of WBEM protocol health check test Namespaces
	 */
	public static final List<String> WBEM_UP_TEST_NAMESPACES = Collections.unmodifiableList(
		List.of("root/Interop", "interop", "root/PG_Interop", "PG_Interop")
	);

	/**
	 * WQL Query to test WBEM protocol health check
	 */
	public static final String WBEM_TEST_QUERY = "SELECT Name FROM CIM_NameSpace";

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
		checkSshHealth(hostname, hostMonitor, metricFactory);
		checkWbemHealth(hostname, hostMonitor, metricFactory);
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
	 * Check SSH protocol health on the hostname for the host monitor.
	 * Criteria: The echo command must be working.
	 *
	 * @param hostname      The hostname on which we perform health check
	 * @param hostMonitor   An endpoint host monitor
	 * @param metricFactory The metric factory used to collect the health check metric
	 *                      metric
	 */
	public void checkSshHealth(String hostname, Monitor hostMonitor, MetricFactory metricFactory) {
		// Create and set the SSH result to null
		Double sshResult = UP;

		// Retrieve SSH Configuration
		final SshConfiguration sshConfiguration = (SshConfiguration) telemetryManager
			.getHostConfiguration()
			.getConfigurations()
			.get(SshConfiguration.class);

		// Stop the SSH health check if there is not any SSH configuration
		if (sshConfiguration == null || !telemetryManager.getHostProperties().isMustCheckSshStatus()) {
			return;
		}

		log.info("Hostname {} - Checking SSH protocol status. Sending an SSH 'echo test' command.", hostname);

		// Execute Local test
		if (telemetryManager.getHostProperties().isOsCommandExecutesLocally()) {
			sshResult = localSshTest(hostname);
		}

		if (telemetryManager.getHostProperties().isOsCommandExecutesRemotely()) {
			sshResult = remoteSshTest(hostname, sshResult, sshConfiguration);
		}

		// Generate a metric from the SSH result
		metricFactory.collectNumberMetric(hostMonitor, SSH_UP_METRIC, sshResult, strategyTime);
	}

	/**
	 * Performs a local Os Command test to determine whether the SSH protocol is UP.
	 *
	 * @param hostname  The hostname on which we perform health check
	 * @return The SSH health check result after performing the tests
	 */
	private Double localSshTest(String hostname) {
		try {
			if (OsCommandHelper.runLocalCommand(SSH_TEST_COMMAND, DEFAULT_TIMEOUT, null) == null) {
				log.debug(
					"Hostname {} - Checking SSH protocol status. Local OS command has not returned any results.",
					hostname
				);
				return DOWN;
			}
		} catch (Exception e) {
			log.debug(
				"Hostname {} - Checking SSH protocol status. SSH exception when performing a local OS command test: ",
				hostname,
				e
			);
			return DOWN;
		}

		return UP;
	}

	/**
	 * Performs a remote SSH test to determine whether the SSH protocol is UP in the given host.
	 *
	 * @param hostname           The hostname on which we perform health check
	 * @param previousSshStatus  The results that will be used to create protocol health check metric
	 * @param sshConfiguration   The SSH configuration retrieved from the telemetryManager
	 * @return The updated SSH status after performing the remote SSH test or the previous SSH status if the SSH test succeeds.
	 */
	private Double remoteSshTest(String hostname, Double previousSshStatus, SshConfiguration sshConfiguration) {
		try {
			if (
				OsCommandHelper.runSshCommand(SSH_TEST_COMMAND, hostname, sshConfiguration, DEFAULT_TIMEOUT, null, null) == null
			) {
				log.debug(
					"Hostname {} - Checking SSH protocol status. Remote SSH command has not returned any results.",
					hostname
				);
				return DOWN;
			}
		} catch (Exception e) {
			log.debug(
				"Hostname {} - Checking SSH protocol status. SSH exception when performing a remote SSH command test: ",
				hostname,
				e
			);
			return DOWN;
		}
		return previousSshStatus;
	}

	/**
	 * Check WBEM protocol health on the hostname for the host monitor.
	 *
	 * <ul>
	 * 	<li>Criteria: The query must not return an error for at least one of the following namespaces:
	 * 		"root/Interop", "interop", "root/PG_Interop", "PG_Interop"</li>
	 * 	<li>Query: SELECT Name FROM CIM_NameSpace.</li>
	 * 	<li>Success Conditions: CIM_ERR_INVALID_NAMESPACE and CIM_ERR_NOT_FOUND errors are considered successful, indicating that the protocol is responding.</li>
	 * </ul>
	 *
	 * @param hostname      The hostname on which we perform health check
	 * @param hostMonitor   An endpoint host monitor
	 * @param metricFactory The metric factory used to collect the health check metric
	 */
	public void checkWbemHealth(String hostname, Monitor hostMonitor, MetricFactory metricFactory) {
		// Retrieve WBEM Configuration from the telemetry manager host configuration
		final WbemConfiguration wbemConfiguration = (WbemConfiguration) telemetryManager
			.getHostConfiguration()
			.getConfigurations()
			.get(WbemConfiguration.class);

		// Stop the health check if there is not an WBEM configuration
		if (wbemConfiguration == null) {
			return;
		}

		log.info(
			"Hostname {} - Checking WBEM protocol status. Sending a WQL SELECT request on different namespaces.",
			hostname
		);

		for (final String wbemNamespace : WBEM_UP_TEST_NAMESPACES) {
			try {
				log.info(
					"Hostname {} - Checking WBEM protocol status. Sending a WQL SELECT request on {} namespace.",
					hostname,
					wbemNamespace
				);

				// The query on the WBEM namespace returned a result
				if (clientsExecutor.executeWbem(hostname, wbemConfiguration, WBEM_TEST_QUERY, wbemNamespace) != null) {
					// Collect the metric with a '1.0' value and stop the test
					metricFactory.collectNumberMetric(hostMonitor, WBEM_UP_METRIC, UP, strategyTime);
					return;
				}
			} catch (Exception e) {
				if (WqlDetectionHelper.isAcceptableException(e)) {
					// Collect the WBEM metric with a '1.0' value and stop the test as the thrown exception is acceptable
					metricFactory.collectNumberMetric(hostMonitor, WBEM_UP_METRIC, UP, strategyTime);
					return;
				}
				log.debug(
					"Hostname {} - Checking WBEM protocol status. WBEM exception when performing a WQL SELECT query on '{}' namespace: ",
					hostname,
					wbemNamespace,
					e
				);
			}
		}

		// Collect the WBEM metric with a '0.0' value as the queries response was not positive
		metricFactory.collectNumberMetric(hostMonitor, WBEM_UP_METRIC, DOWN, strategyTime);
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
