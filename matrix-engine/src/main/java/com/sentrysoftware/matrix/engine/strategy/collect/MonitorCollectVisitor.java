package com.sentrysoftware.matrix.engine.strategy.collect;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ALARM_ON_COLOR;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ALLOCATED_SPACE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ALLOCATED_SPACE_PERCENT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.AVAILABLE_PATH_COUNT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.AVAILABLE_PATH_WARNING;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.BANDWIDTH_UTILIZATION_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.BLINKING_STATUS;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.BYTES_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.BYTES_RATE_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.CHARGE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.COLOR_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.CORRECTED_ERROR_COUNT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DECODER_USED_TIME_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DECODER_USED_TIME_PERCENT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DUPLEX_MODE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.EMPTY;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENCODER_USED_TIME_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENCODER_USED_TIME_PERCENT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENDURANCE_REMAINING_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENERGY_USAGE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ERROR_COUNT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ERROR_COUNT_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ERROR_PERCENT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.HTTP_UP_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.IPMI_UP_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.LED_INDICATOR_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.LINK_SPEED_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.LINK_STATUS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.MAX_AVAILABLE_PATH_COUNT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.MOUNT_COUNT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.MOUNT_COUNT_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.MOVE_COUNT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.MOVE_COUNT_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.OFF_STATUS;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ON_STATUS;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PACKETS_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PACKETS_RATE_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PERCENT_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.POWER_CONSUMPTION_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.POWER_SUPPLY_POWER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.RECEIVED_BYTES_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.RECEIVED_BYTES_RATE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.RECEIVED_PACKETS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.RECEIVED_PACKETS_RATE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SIZE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SNMP_UP_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SPACE_GB_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SPEED_MBITS_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SPEED_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SPEED_PERCENT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SSH_UP_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.STARTING_CORRECTED_ERROR_COUNT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.STARTING_ERROR_COUNT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.STATUS_INFORMATION_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.STATUS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TEMPERATURE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TEMPERATURE_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TIME_LEFT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TIME_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TOTAL_PACKETS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TRANSMITTED_BYTES_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TRANSMITTED_BYTES_RATE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TRANSMITTED_PACKETS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TRANSMITTED_PACKETS_RATE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.UNALLOCATED_SPACE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.UNALLOCATED_SPACE_PERCENT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.UNMOUNT_COUNT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.UNMOUNT_COUNT_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.USAGE_REPORT_RECEIVED_BYTES_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.USAGE_REPORT_RECEIVED_PACKETS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.USAGE_REPORT_TRANSMITTED_BYTES_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.USAGE_REPORT_TRANSMITTED_PACKETS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.USED_CAPACITY_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.USED_PERCENT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.USED_TIME_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.USED_TIME_PERCENT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.USED_WATTS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.VOLTAGE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.VOLTAGE_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.WARNING_ON_COLOR;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.WBEM_UP_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.WMI_UP_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.WINRM_UP_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ZERO_BUFFER_CREDIT_COUNT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ZERO_BUFFER_CREDIT_COUNT_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ZERO_BUFFER_CREDIT_PERCENT_PARAMETER;

import java.math.RoundingMode;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.util.Assert;

import com.sentrysoftware.matrix.common.helpers.NumberHelper;
import com.sentrysoftware.matrix.common.meta.monitor.Battery;
import com.sentrysoftware.matrix.common.meta.monitor.Blade;
import com.sentrysoftware.matrix.common.meta.monitor.Cpu;
import com.sentrysoftware.matrix.common.meta.monitor.CpuCore;
import com.sentrysoftware.matrix.common.meta.monitor.DiskController;
import com.sentrysoftware.matrix.common.meta.monitor.Enclosure;
import com.sentrysoftware.matrix.common.meta.monitor.Fan;
import com.sentrysoftware.matrix.common.meta.monitor.Gpu;
import com.sentrysoftware.matrix.common.meta.monitor.IMetaMonitor;
import com.sentrysoftware.matrix.common.meta.monitor.Led;
import com.sentrysoftware.matrix.common.meta.monitor.LogicalDisk;
import com.sentrysoftware.matrix.common.meta.monitor.Lun;
import com.sentrysoftware.matrix.common.meta.monitor.Memory;
import com.sentrysoftware.matrix.common.meta.monitor.MetaConnector;
import com.sentrysoftware.matrix.common.meta.monitor.NetworkCard;
import com.sentrysoftware.matrix.common.meta.monitor.OtherDevice;
import com.sentrysoftware.matrix.common.meta.monitor.PhysicalDisk;
import com.sentrysoftware.matrix.common.meta.monitor.PowerSupply;
import com.sentrysoftware.matrix.common.meta.monitor.Robotics;
import com.sentrysoftware.matrix.common.meta.monitor.TapeDrive;
import com.sentrysoftware.matrix.common.meta.monitor.Host;
import com.sentrysoftware.matrix.common.meta.monitor.Temperature;
import com.sentrysoftware.matrix.common.meta.monitor.Vm;
import com.sentrysoftware.matrix.common.meta.monitor.Voltage;
import com.sentrysoftware.matrix.common.meta.parameter.DiscreteParamType;
import com.sentrysoftware.matrix.common.meta.parameter.SimpleParamType;
import com.sentrysoftware.matrix.common.meta.parameter.state.DuplexMode;
import com.sentrysoftware.matrix.common.meta.parameter.state.IState;
import com.sentrysoftware.matrix.common.meta.parameter.state.LedColorStatus;
import com.sentrysoftware.matrix.common.meta.parameter.state.LedIndicator;
import com.sentrysoftware.matrix.common.meta.parameter.state.LinkStatus;
import com.sentrysoftware.matrix.common.meta.parameter.state.Status;
import com.sentrysoftware.matrix.common.meta.parameter.state.Up;
import com.sentrysoftware.matrix.connector.model.common.http.ResultContent;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.engine.protocol.HttpProtocol;
import com.sentrysoftware.matrix.engine.protocol.IpmiOverLanProtocol;
import com.sentrysoftware.matrix.engine.protocol.SnmpProtocol;
import com.sentrysoftware.matrix.engine.protocol.SshProtocol;
import com.sentrysoftware.matrix.engine.protocol.WbemProtocol;
import com.sentrysoftware.matrix.engine.protocol.WinRmProtocol;
import com.sentrysoftware.matrix.engine.protocol.WmiProtocol;
import com.sentrysoftware.matrix.engine.strategy.IMonitorVisitor;
import com.sentrysoftware.matrix.engine.strategy.matsya.HttpRequest;
import com.sentrysoftware.matrix.engine.strategy.utils.OsCommandHelper;
import com.sentrysoftware.matrix.engine.strategy.utils.WqlDetectionHelper;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.parameter.DiscreteParam;
import com.sentrysoftware.matrix.model.parameter.IParameter;
import com.sentrysoftware.matrix.model.parameter.TextParam;

import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MonitorCollectVisitor implements IMonitorVisitor {

	private static final List<String> WBEM_UP_TEST_NAMESPACES = Collections
		.unmodifiableList(
			List.of(
				"root/Interop",
				"interop",
				"root/PG_Interop",
				"PG_Interop"
			)
		);
	static final String WBEM_UP_TEST_WQL = "SELECT Name FROM CIM_NameSpace";
	static final String WINRM_AND_WMI_UP_TEST_NAMESPACE = "root\\cimv2";
	static final String WINRM_AND_WMI_UP_TEST_WQL = "Select Name FROM Win32_ComputerSystem";
	static final String SNMP_UP_TEST_OID = "1.3.6.1";
	private static final String VALUE_TABLE_CANNOT_BE_NULL = "valueTable cannot be null";
	private static final String DATA_CANNOT_BE_NULL = "row cannot be null.";
	private static final String CONNECTOR_NAME_CANNOT_BE_NULL = "connectorName cannot be null.";
	private static final String HOST_MONITORING_CANNOT_BE_NULL = "hostMonitoring cannot be null.";
	private static final String HOSTNAME_CANNOT_BE_NULL = "hostname cannot be null.";
	private static final String MAPPING_CANNOT_BE_NULL = "mapping cannot be null.";
	private static final String MONITOR_CANNOT_BE_NULL = "monitor cannot be null.";
	private static final String COLLECT_TIME_CANNOT_BE_NULL = "collectTime cannot be null.";
	private static final double BYTES_TO_GB_CONV_FACTOR = 1024.0 * 1024.0 * 1024.0;

	private static final Map<String, String> GPU_USED_TIME_PARAMETERS = Map.of(
		DECODER_USED_TIME_PARAMETER, DECODER_USED_TIME_PERCENT_PARAMETER,
		ENCODER_USED_TIME_PARAMETER, ENCODER_USED_TIME_PERCENT_PARAMETER,
		USED_TIME_PARAMETER, USED_TIME_PERCENT_PARAMETER);

	private static final Map<String, String> GPU_BYTES_TRANSFER_PARAMETERS = Map.of(
		TRANSMITTED_BYTES_PARAMETER, TRANSMITTED_BYTES_RATE_PARAMETER,
		RECEIVED_BYTES_PARAMETER, RECEIVED_BYTES_RATE_PARAMETER);

	@Getter
	private final MonitorCollectInfo monitorCollectInfo;

	public MonitorCollectVisitor(@NonNull MonitorCollectInfo monitorCollectInfo) {
		checkCollectInfo(monitorCollectInfo);
		this.monitorCollectInfo = monitorCollectInfo;
	}

	private void checkCollectInfo(MonitorCollectInfo monitorCollectInfo) {

		Assert.notNull(monitorCollectInfo.getMonitor(), MONITOR_CANNOT_BE_NULL);

		Assert.isTrue(monitorCollectInfo.getMonitor().getMonitorType().equals(MonitorType.HOST)
					|| monitorCollectInfo.getConnectorName() != null, CONNECTOR_NAME_CANNOT_BE_NULL);

		Assert.isTrue(monitorCollectInfo.getMonitor().getMonitorType().equals(MonitorType.HOST)
					|| monitorCollectInfo.getRow() != null, DATA_CANNOT_BE_NULL);

		Assert.notNull(monitorCollectInfo.getHostMonitoring(), HOST_MONITORING_CANNOT_BE_NULL);

		Assert.notNull(monitorCollectInfo.getHostname(), HOSTNAME_CANNOT_BE_NULL);

		Assert.isTrue(monitorCollectInfo.getMonitor().getMonitorType().equals(MonitorType.HOST)
					|| monitorCollectInfo.getMapping() != null, MAPPING_CANNOT_BE_NULL);

		Assert.isTrue(monitorCollectInfo.getMonitor().getMonitorType().equals(MonitorType.HOST)
					|| monitorCollectInfo.getValueTable() != null, VALUE_TABLE_CANNOT_BE_NULL);

		Assert.notNull(monitorCollectInfo.getCollectTime(), COLLECT_TIME_CANNOT_BE_NULL);

	}

	@Override
	public void visit(MetaConnector metaConnector) {
		// Not implemented
	}

	@Override
	public void visit(Host host) {

		final String hostname = monitorCollectInfo.getHostname();
		final Monitor monitor = monitorCollectInfo.getMonitor();

		// No engine configuration means no protocols to check if they are up.
		if (monitorCollectInfo.getEngineConfiguration() == null) {
			return;
		}

		updateSnmpUpParameter(hostname, monitor);
		updateWbemUpParameter(hostname, monitor);
		updateSshUpParameter(hostname, monitor);
		updateWmiUpParameter(hostname, monitor);
		updateHttpUpParameter(hostname, monitor);
		updateIpmiUpParameter(hostname, monitor);
		updateWinRmUpParameter(hostname, monitor);

	}

	/**
	 * Update IPMI protocol up parameter
	 * 
	 * @param hostname hostname
	 * @param monitor  monitor
	 */
	private void updateIpmiUpParameter(final String hostname, final Monitor monitor) {
		// Get IPMI Configuration if it exists
		final IpmiOverLanProtocol ipmi = (IpmiOverLanProtocol) monitorCollectInfo.getEngineConfiguration().getProtocolConfigurations()
				.get(IpmiOverLanProtocol.class);

		// Update the IPMI_UP_PARAMETER
		if (ipmi != null) {

			String ipmiResult = null;

			try {
				// Query to test for response.
				ipmiResult = monitorCollectInfo.getMatsyaClientsExecutor().executeIpmiDetection(hostname, ipmi);
			} catch (Exception e) {
				log.debug("Hostname {} - Checking IPMI protocol status. IPMI exception when performing a test IPMI detection: ", hostname, e);
			}

			CollectHelper.updateDiscreteParameter(
					monitor,
					IPMI_UP_PARAMETER,
					monitorCollectInfo.getCollectTime(),
					ipmiResult != null ? Up.UP : Up.DOWN);

		}
	}


	/**
	 * Update HTTP protocol up parameter
	 * 
	 * @param hostname hostname
	 * @param monitor  monitor
	 */
	private void updateHttpUpParameter(final String hostname, final Monitor monitor) {
		// Get HTTP Configuration if it exists
		final HttpProtocol http = (HttpProtocol) monitorCollectInfo.getEngineConfiguration().getProtocolConfigurations()
				.get(HttpProtocol.class);

		// Update the HTTP_UP_PARAMETER
		if (http != null) {

			String httpResult = null;

			try {
				// Query to test for response.
				HttpRequest request = HttpRequest.builder()
						.hostname(hostname)
						.method("GET")
						.url("/")
						.httpProtocol(http)
						.resultContent(ResultContent.ALL)
						.build();

				httpResult = monitorCollectInfo.getMatsyaClientsExecutor().executeHttp(request, true);
			} catch (Exception e) {
				log.debug("Hostname {} - Checking HTTP protocol status. HTTP exception when performing a test HTTP query: ", hostname, e);
			}

			CollectHelper.updateDiscreteParameter(
					monitor,
					HTTP_UP_PARAMETER,
					monitorCollectInfo.getCollectTime(),
					httpResult != null ? Up.UP : Up.DOWN);

		}
	}

	/**
	 * Update WMI protocol up parameter
	 * 
	 * @param hostname hostname
	 * @param monitor  monitor
	 */
	private void updateWmiUpParameter(final String hostname, final Monitor monitor) {
		// Get WMI Configuration if it exists
		final WmiProtocol wmi = (WmiProtocol) monitorCollectInfo.getEngineConfiguration().getProtocolConfigurations()
				.get(WmiProtocol.class);

		// Update the WMI_UP_PARAMETER
		if (wmi != null) {

			List<List<String>> wmiResult = null;

			try {
				// Query to test for response.
				wmiResult = monitorCollectInfo
					.getMatsyaClientsExecutor()
					.executeWmi(
						hostname,
						wmi,
						WINRM_AND_WMI_UP_TEST_WQL,
						WINRM_AND_WMI_UP_TEST_NAMESPACE
					);

			} catch (Exception e) {
				if (WqlDetectionHelper.isAcceptableException(e)) {
					CollectHelper.updateDiscreteParameter(
						monitor,
						WMI_UP_PARAMETER,
						monitorCollectInfo.getCollectTime(),
						Up.UP
					);
					return;
				}
				log.debug("Hostname {} - Checking WMI protocol status. WMI exception when performing a test WMI query: ", hostname, e);
			}

			CollectHelper.updateDiscreteParameter(
				monitor,
				WMI_UP_PARAMETER,
				monitorCollectInfo.getCollectTime(),
				wmiResult != null ? Up.UP : Up.DOWN
			);

		}
	}

	/**
	 * Update WinRm protocol up parameter
	 * 
	 * @param hostname hostname
	 * @param monitor  monitor
	 */
	private void updateWinRmUpParameter(final String hostname, final Monitor monitor) {
		// Get WinRm Configuration if it exists
		final WinRmProtocol winRm = (WinRmProtocol) monitorCollectInfo.getEngineConfiguration().getProtocolConfigurations()
				.get(WinRmProtocol.class);

		// Update the WINRM_UP_PARAMETER
		if (winRm != null) {

			List<List<String>> winRmResult = null;

			try {
				// Query to test for response.
				winRmResult = monitorCollectInfo
					.getMatsyaClientsExecutor()
					.executeWqlThroughWinRm(
						hostname,
						winRm,
						WINRM_AND_WMI_UP_TEST_WQL,
						WINRM_AND_WMI_UP_TEST_NAMESPACE
					);

			} catch (Exception e) {
				if (WqlDetectionHelper.isAcceptableException(e)) {
					CollectHelper.updateDiscreteParameter(
						monitor,
						WINRM_UP_PARAMETER,
						monitorCollectInfo.getCollectTime(),
						Up.UP
					);
					return;
				}
				log.debug("Hostname {} - Checking WinRM protocol status. WinRM exception when performing a test WQL query: ", hostname, e);
			}

			CollectHelper.updateDiscreteParameter(
				monitor,
				WINRM_UP_PARAMETER,
				monitorCollectInfo.getCollectTime(),
				winRmResult != null ? Up.UP : Up.DOWN
			);
		}
	}

	/**
	 * Update SSH Protocol Up Parameter
	 * 
	 * @param hostname hostname
	 * @param monitor  monitor
	 */
	private void updateSshUpParameter(final String hostname, final Monitor monitor) {
		// Get SSH Configuration if it exists
		final SshProtocol ssh = (SshProtocol) monitorCollectInfo.getEngineConfiguration().getProtocolConfigurations()
				.get(SshProtocol.class);

		// Update the SSH_UP_PARAMETER
		if (ssh != null && monitorCollectInfo.getHostMonitoring().isMustCheckSshStatus()) {
			Up state = Up.UP;

			if(monitorCollectInfo.getHostMonitoring().isOsCommandExecutesLocally()) {
				state = sshTestExecutesLocally(hostname, ssh, state);
			}

			if(monitorCollectInfo.getHostMonitoring().isOsCommandExecutesRemotely()) {
				state = sshTestExecutesRemotely(hostname, ssh, state);
			}

			// Set the parameter at the end
			CollectHelper.updateDiscreteParameter(
					monitor,
					SSH_UP_PARAMETER,
					monitorCollectInfo.getCollectTime(),
					state
				);
		}
	}

	/**
	 * Test whether remote ssh is responding.
	 * 
	 * @param hostname
	 * @param ssh
	 * @param state
	 * @return
	 */
	private Up sshTestExecutesRemotely(final String hostname, final SshProtocol ssh, Up state) {
		try {
			if (OsCommandHelper.runSshCommand("echo SSH_UP_TEST", hostname, ssh, Math.toIntExact(ssh.getTimeout()), null, null) == null) {
				log.debug("Hostname {} - Checking SSH protocol status. Remote SSH command has not returned any results. ", hostname);
				state = Up.DOWN;
			}
		} catch (Exception e) {
			log.debug("Hostname {} - Checking SSH protocol status. SSH exception when performing a remote SSH command test: ", hostname, e);
			state = Up.DOWN;
		}
		return state;
	}

	/**
	 * 
	 * 
	 * @param hostname
	 * @param ssh
	 * @param state
	 * @return
	 */
	private Up sshTestExecutesLocally(final String hostname, final SshProtocol ssh, Up state) {
		try {
			if (OsCommandHelper.runLocalCommand("echo SSH_UP_TEST", Math.toIntExact(ssh.getTimeout()), null) == null) {
				log.debug("Hostname {} - Checking SSH protocol status. Local OS command has not returned any results.", hostname);
				state = Up.DOWN;
			}
		} catch (Exception e) {
			log.debug("Hostname {} - Checking SSH protocol status. SSH exception when performing a local OS command test: ", hostname, e);
			state = Up.DOWN;
		}
		return state;
	}

	/**
	 * Update WBEM Protocol Up Parameter
	 * 
	 * @param hostname hostname
	 * @param monitor  monitor
	 */
	private void updateWbemUpParameter(final String hostname, final Monitor monitor) {

		// Get WBEM Configuration if it exists
		final WbemProtocol wbem = (WbemProtocol) monitorCollectInfo.getEngineConfiguration().getProtocolConfigurations()
				.get(WbemProtocol.class);

		// Configuration expects WBEM
		if (wbem != null) {

			List<List<String>> wbemResult = null;

			for (String wbemNamespace : WBEM_UP_TEST_NAMESPACES) {
				try {
					// Execute wbem query
					wbemResult = monitorCollectInfo
						.getMatsyaClientsExecutor()
						.executeWbem(
							hostname,
							wbem,
							WBEM_UP_TEST_WQL,
							wbemNamespace
						);

					// We have got a result?
					if (wbemResult != null) {
						CollectHelper.updateDiscreteParameter(
								monitor, WBEM_UP_PARAMETER,
								monitorCollectInfo.getCollectTime(),
								Up.UP);
						return;
					}
				} catch (Exception e) {
					if (WqlDetectionHelper.isAcceptableException(e)) {
						CollectHelper.updateDiscreteParameter(
								monitor, WBEM_UP_PARAMETER,
								monitorCollectInfo.getCollectTime(),
								Up.UP);
						return;
					}
					log.debug("Hostname {} - Checking WBEM protocol status. WBEM exception when performing a test WBEM query: ", hostname, e);
				}
			}

			CollectHelper.updateDiscreteParameter(
					monitor, WBEM_UP_PARAMETER,
					monitorCollectInfo.getCollectTime(),
					Up.DOWN);
		}
	}

	/**
	 * Update SNMP Protocol Up Parameter
	 * 
	 * @param hostname hostname
	 * @param monitor  monitor
	 */
	private void updateSnmpUpParameter(final String hostname, final Monitor monitor) {
		// Get SNMP Configuration if it exists
		final SnmpProtocol snmp = (SnmpProtocol) monitorCollectInfo.getEngineConfiguration().getProtocolConfigurations()
				.get(SnmpProtocol.class);

		// Update the SNMP_UP_PARAMETER.
		if (snmp != null) {

			String snmpGetNext = null;
			try {
				snmpGetNext = monitorCollectInfo.getMatsyaClientsExecutor()
						.executeSNMPGetNext(SNMP_UP_TEST_OID, snmp, hostname, true);

			} catch (Exception e) {
				log.debug("Hostname {} - Checking SNMP protocol status. SNMP exception when performing a test SNMP Get Next: ", hostname, e);
			} finally {
				CollectHelper.updateDiscreteParameter(monitor, SNMP_UP_PARAMETER,
						monitorCollectInfo.getCollectTime(), snmpGetNext != null ? Up.UP : Up.DOWN);
			}
		}
	}

	@Override
	public void visit(Battery battery) {

		collectBasicParameters(battery);

		collectBatteryCharge();

		collectBatteryTimeLeft();

		collectStatusInformation();

	}

	@Override
	public void visit(Blade blade) {

		collectBasicParameters(blade);

		collectStatusInformation();

	}

	@Override
	public void visit(Cpu cpu) {

		collectBasicParameters(cpu);

		collectStatusInformation();

	}

	@Override
	public void visit(CpuCore cpuCore) {

		collectBasicParameters(cpuCore);

		collectCpuCoreUsedTimePercent();

		collectStatusInformation();

	}

	@Override
	public void visit(DiskController diskController) {

		collectBasicParameters(diskController);

		collectStatusInformation();

	}

	@Override
	public void visit(Enclosure enclosure) {

		collectBasicParameters(enclosure);

		collectPowerConsumption();

		collectStatusInformation();

	}

	@Override
	public void visit(Fan fan) {

		collectBasicParameters(fan);

		estimateFanPowerConsumption();

		collectStatusInformation();

	}

	@Override
	public void visit(Gpu gpu) {

		collectBasicParameters(gpu);

		collectGpuUsedTimeRatioParameters();
		collectGpuTransferredBytesParameters();

		collectPowerConsumption();

		collectErrorCount(ERROR_COUNT_PARAMETER, STARTING_ERROR_COUNT_PARAMETER);
		collectErrorCount(CORRECTED_ERROR_COUNT_PARAMETER, STARTING_CORRECTED_ERROR_COUNT_PARAMETER);

		collectStatusInformation();
	}

	@Override
	public void visit(Led led) {

		collectBasicParameters(led);

		collectLedColor();

		collectLedStatusAndLedIndicatorStatus();

		collectStatusInformation();

	}

	@Override
	public void visit(LogicalDisk logicalDisk) {

		collectBasicParameters(logicalDisk);

		collectErrorCount(ERROR_COUNT_PARAMETER, STARTING_ERROR_COUNT_PARAMETER);

		collectLogicalDiskSpace();

		collectStatusInformation();

	}

	@Override
	public void visit(Lun lun) {

		collectBasicParameters(lun);

		collectStatusInformation();

		collectAvailablePathWarning();
	}

	@Override
	public void visit(Memory memory) {

		collectBasicParameters(memory);

		collectErrorCount(ERROR_COUNT_PARAMETER, STARTING_ERROR_COUNT_PARAMETER);

		collectStatusInformation();

	}

	@Override
	public void visit(NetworkCard networkCard) {

		collectBasicParameters(networkCard);

		final DuplexMode duplexMode = collectNetworkCardDuplexMode();
		final Double linkSpeed = collectNetworkCardLinkSpeed();
		final Double receivedBytesRate = collectNetworkCardBytesRate(
			RECEIVED_BYTES_PARAMETER,
			RECEIVED_BYTES_RATE_PARAMETER,
			USAGE_REPORT_RECEIVED_BYTES_PARAMETER
		);
		final Double transmittedBytesRate = collectNetworkCardBytesRate(
			TRANSMITTED_BYTES_PARAMETER,
			TRANSMITTED_BYTES_RATE_PARAMETER,
			USAGE_REPORT_TRANSMITTED_BYTES_PARAMETER
		);

		collectNetworkCardBandwidthUtilization(duplexMode, linkSpeed, receivedBytesRate,transmittedBytesRate);

		final Double receivedPackets = collectNetworkCardPacketsRate(
			RECEIVED_PACKETS_PARAMETER,
			RECEIVED_PACKETS_RATE_PARAMETER,
			USAGE_REPORT_RECEIVED_PACKETS_PARAMETER
		);
		final Double transmittedPackets = collectNetworkCardPacketsRate(
			TRANSMITTED_PACKETS_PARAMETER,
			TRANSMITTED_PACKETS_RATE_PARAMETER,
			USAGE_REPORT_TRANSMITTED_PACKETS_PARAMETER
		);
		collectNetworkCardErrorPercent(receivedPackets, transmittedPackets);
		collectNetworkCardZeroBufferCreditPercent();

		estimateNetworkCardPowerConsumption();

		collectStatusInformation();

	}

	@Override
	public void visit(OtherDevice otherDevice) {

		collectBasicParameters(otherDevice);

		collectStatusInformation();

	}

	@Override
	public void visit(PhysicalDisk physicalDisk) {

		collectBasicParameters(physicalDisk);

		collectPhysicalDiskParameters();

		collectErrorCount(ERROR_COUNT_PARAMETER, STARTING_ERROR_COUNT_PARAMETER);

		collectStatusInformation();

	}

	@Override
	public void visit(PowerSupply powerSupply) {

		collectBasicParameters(powerSupply);

		collectPowerSupplyUsedCapacity();

		collectStatusInformation();

	}

	@Override
	public void visit(Robotics robotics) {

		collectBasicParameters(robotics);

		collectIncrementCount(MOVE_COUNT_PARAMETER, MOVE_COUNT_PARAMETER_UNIT);

		collectErrorCount(ERROR_COUNT_PARAMETER, STARTING_ERROR_COUNT_PARAMETER);

		estimateRoboticsPowerConsumption();

		collectStatusInformation();

	}

	@Override
	public void visit(TapeDrive tapeDrive) {

		collectBasicParameters(tapeDrive);

		collectIncrementCount(MOUNT_COUNT_PARAMETER, MOUNT_COUNT_PARAMETER_UNIT);

		collectIncrementCount(UNMOUNT_COUNT_PARAMETER, UNMOUNT_COUNT_PARAMETER_UNIT);

		collectErrorCount(ERROR_COUNT_PARAMETER, STARTING_ERROR_COUNT_PARAMETER);

		estimateTapeDrivePowerConsumption();

		collectStatusInformation();

	}

	@Override
	public void visit(Temperature temperature) {

		collectBasicParameters(temperature);

		collectTemperature();

		collectStatusInformation();
	}

	@Override
	public void visit(Vm vm) {

		collectBasicParameters(vm);

		collectStatusInformation();

	}

	@Override
	public void visit(Voltage voltage) {

		collectBasicParameters(voltage);

		collectVoltage();

		collectStatusInformation();

	}

	/**
	 * Collect a discrete parameter of the current {@link Monitor} instance
	 *
	 * @param monitorType       The type of the monitor we currently collect
	 * @param parameterName      The meta information of the parameter we wish to collect
	 * @param discreteParamType The {@link DiscreteParamType} defining the interpret function
	 */
	void collectDiscreteParameter(@NonNull final MonitorType monitorType, @NonNull String parameterName,
			@NonNull final DiscreteParamType discreteParamType) {

		checkCollectInfo(monitorCollectInfo);

		final Monitor monitor = monitorCollectInfo.getMonitor();
		final List<String> row = monitorCollectInfo.getRow();
		final Map<String, String> mapping = monitorCollectInfo.getMapping();
		final String hostname = monitorCollectInfo.getHostname();
		final String valueTable = monitorCollectInfo.getValueTable();
		final Long collectTime = monitorCollectInfo.getCollectTime();

		// Get the parameter raw value
		final String stateValue = CollectHelper.getValueTableColumnValue(
				valueTable,
				parameterName,
				monitorType,
				row,
				mapping.get(parameterName),
				hostname);

		// Translate the state raw value
		final IState state = CollectHelper.translateState(
				stateValue,
				discreteParamType.getInterpreter(),
				parameterName,
				monitor.getId(),
				hostname);

		if (state == null) {
			log.warn("Hostname {} - Could not collect {} for monitor id {}", hostname, parameterName, monitor.getId());
			return;
		}

		CollectHelper.updateDiscreteParameter(
				monitor,
				parameterName,
				collectTime,
				state
		);

	}

	/**
	 * Collect a number parameter
	 *
	 * @param monitorType   The type of the monitor we currently collect
	 * @param parameterName The name of the status parameter to collect
	 * @param unit          The unit to set in the {@link IParameter} instance
	 */
	void collectNumberParameter(@NonNull final MonitorType monitorType, final String parameterName, final String unit) {

		checkCollectInfo(monitorCollectInfo);

		final Monitor monitor = monitorCollectInfo.getMonitor();
		final Long collectTime = monitorCollectInfo.getCollectTime();


		final Double value = extractParameterValue(monitorType, parameterName);
		if (value != null) {
			CollectHelper.updateNumberParameter(
				monitor,
				parameterName,
				unit,
				collectTime,
				value,
				value
			);
		}

	}

	/**
	 * Collect the parameter string from the current value
	 *
	 * @param parameterName The unique name of the parameter
	 * @param value   		The value of the text parameter
	 */
	void collectTextParameter(@NonNull final String parameterName, final String value) {

		if (value == null) {
			return;
		}

		final Monitor monitor = monitorCollectInfo.getMonitor();

		// Create a text parameter and update the value and the collect time
		final TextParam textParam = TextParam.builder()
				.name(parameterName)
				.value(value)
				.collectTime(monitorCollectInfo.getCollectTime())
				.build();

		monitor.collectParameter(textParam);
	}

	/**
	 * Extract the parameter value from the current row
	 *
	 * @param monitorType   The type of the monitor
	 * @param parameterName The unique name of the parameter
	 * @return {@link Double} value
	 */
	Double extractParameterValue(@NonNull final MonitorType monitorType, final String parameterName) {

		checkCollectInfo(monitorCollectInfo);

		final Monitor monitor = monitorCollectInfo.getMonitor();
		final List<String> row = monitorCollectInfo.getRow();
		final Map<String, String> mapping = monitorCollectInfo.getMapping();
		final String hostname = monitorCollectInfo.getHostname();
		final String valueTable = monitorCollectInfo.getValueTable();
		
		// Making sure the parameter is not in the parameterActivation Set
		if (EMPTY.equals(monitor.getMetadata(String.format("ParameterActivation.%s", parameterName)))) {
			return null;
		}

		// Get the number value as string from the current row
		final String stringValue = CollectHelper.getValueTableColumnValue(valueTable,
				parameterName,
				monitorType,
				row,
				mapping.get(parameterName),
				hostname);


		if (stringValue == null) {
			log.debug("Hostname {} - No {} to collect for monitor id {}", hostname, parameterName, monitor.getId());
			return null;
		}

		try {
			return Double.parseDouble(stringValue);
		} catch(NumberFormatException e) {
			log.warn("Hostname {} - Cannot parse the {} value '{}' for monitor id {}. {} won't be collected",
					hostname, parameterName, stringValue, monitor.getId(), parameterName);
		}

		return null;
	}

	/**
	 * Extract the parameter string from the current row
	 *
	 * @param monitorType   The type of the monitor
	 * @param parameterName The unique name of the parameter
	 * @return {@link String} value
	 */
	String extractParameterStringValue(@NonNull final MonitorType monitorType, final String parameterName) {

		checkCollectInfo(monitorCollectInfo);

		// Get the number value as string from the current row
		return CollectHelper.getValueTableColumnValue(
				monitorCollectInfo.getValueTable(),
				parameterName,
				monitorType,
				monitorCollectInfo.getRow(),
				monitorCollectInfo.getMapping().get(parameterName),
				monitorCollectInfo.getHostname());
	}

	/**
	 * Collect the basic parameters as defined by the given {@link IMetaMonitor}
	 *
	 * @param metaMonitor Defines all the meta information of the parameters to collect (name, type, unit and basic or not)
	 */
	private void collectBasicParameters(final IMetaMonitor metaMonitor) {

		// Collect discrete parameters
		metaMonitor.getMetaParameters()
		.values()
		.stream()
		.filter(metaParam -> metaParam.isBasicCollect() && metaParam.getType() instanceof DiscreteParamType)
		.forEach(metaParam -> collectDiscreteParameter(
			metaMonitor.getMonitorType(),
			metaParam.getName(),
			(DiscreteParamType) metaParam.getType())
		);

		// Collect number parameters
		metaMonitor.getMetaParameters()
		.values()
		.stream()
		.filter(metaParam -> metaParam.isBasicCollect() && SimpleParamType.NUMBER.equals(metaParam.getType()))
		.forEach(metaParam -> collectNumberParameter(
			metaMonitor.getMonitorType(),
			metaParam.getName(),
			metaParam.getUnit()
		));

		// Collect text parameters
		metaMonitor.getMetaParameters()
		.values()
		.stream()
		.filter(metaParam -> metaParam.isBasicCollect() && SimpleParamType.TEXT.equals(metaParam.getType()))
		.forEach(metaParam -> collectTextParameter(
			metaParam.getName(),
			extractParameterStringValue(metaMonitor.getMonitorType(), metaParam.getName())
		));
	}

	/**
	 * Collect the power consumption. <br>
	 * <ol>
	 * <li>If the energyUsage is collected by the connector, we compute the delta energyUsage (Joules) and then the powerConsumption (Watts) based on the
	 * collected delta energyUsage and the collect time.</li>
	 * <li>If the connector collects the powerConsumption, we directly collect the power consumption (Watts) then we compute the energy usage based on the
	 * collected power consumption and the delta collect time</li>
	 * </ol>
	 */
	void collectPowerConsumption() {

		checkCollectInfo(monitorCollectInfo);

		final Monitor monitor = monitorCollectInfo.getMonitor();
		final Long collectTime = monitorCollectInfo.getCollectTime();
		final String hostname = monitorCollectInfo.getHostname();

		// When the connector collects the energy usage,
		// the power consumption will be computed based on the collected energy usage value
		final Double energyUsageRaw = extractParameterValue(monitor.getMonitorType(), ENERGY_USAGE_PARAMETER);
		if (energyUsageRaw != null && energyUsageRaw >= 0) {

			CollectHelper.collectPowerFromEnergyUsage(monitor, collectTime, energyUsageRaw, hostname);
			return;
		}

		// based on the power consumption compute the energy usage
		final Double powerConsumption = extractParameterValue(monitor.getMonitorType(),
				POWER_CONSUMPTION_PARAMETER);
		if (powerConsumption != null && powerConsumption >= 0) {
			CollectHelper.collectEnergyUsageFromPower(monitor, collectTime, powerConsumption, hostname);
		}

	}

	/**
	 * Collects the percentage of charge, if the current {@link Monitor} is a {@link Battery}.
	 */
	void collectBatteryCharge() {

		final Monitor monitor = monitorCollectInfo.getMonitor();

		final Double chargeRaw = extractParameterValue(monitor.getMonitorType(), CHARGE_PARAMETER);
		if (chargeRaw != null) {

			CollectHelper.updateNumberParameter(
				monitor,
				CHARGE_PARAMETER,
				PERCENT_PARAMETER_UNIT,
				monitorCollectInfo.getCollectTime(),
				Math.min(chargeRaw, 100.0), // In case the raw value is greater than 100%
				chargeRaw
			);
		}
	}

	/**
	 * Collects the remaining time, in seconds, before the {@link Battery} runs out of power.
	 */
	void collectBatteryTimeLeft() {

		final Monitor monitor = monitorCollectInfo.getMonitor();

		final Double timeLeftRaw = extractParameterValue(monitor.getMonitorType(),
			TIME_LEFT_PARAMETER);

		if (timeLeftRaw != null) {

			CollectHelper.updateNumberParameter(
				monitor,
				TIME_LEFT_PARAMETER,
				TIME_PARAMETER_UNIT,
				monitorCollectInfo.getCollectTime(),
				timeLeftRaw * 60.0, // minutes to seconds
				timeLeftRaw
			);
		}
	}

	/**
	 * Collects the percentage of used time, if the current {@link Monitor} is a {@link CpuCore}.
	 */
	void collectCpuCoreUsedTimePercent() {

		final Monitor monitor = monitorCollectInfo.getMonitor();
		final String hostname = monitorCollectInfo.getHostname();

		// Getting the current value
		final Double usedTimePercentRaw = extractParameterValue(monitor.getMonitorType(),
			USED_TIME_PERCENT_PARAMETER);

		if (usedTimePercentRaw == null) {
			return;
		}

		// Getting the current value's collect time
		Long collectTime = monitorCollectInfo.getCollectTime();

		// Getting the previous value
		Double usedTimePercentPrevious = CollectHelper.getNumberParamRawValue(monitor,
			USED_TIME_PERCENT_PARAMETER, true);

		if (usedTimePercentPrevious == null) {

			// Setting the current raw value so that it becomes the previous raw value when the next collect occurs
			CollectHelper.updateNumberParameter(
				monitor,
				USED_TIME_PERCENT_PARAMETER,
				PERCENT_PARAMETER_UNIT,
				collectTime,
				null,
				usedTimePercentRaw
			);

			return;
		}

		// Getting the previous value's collect time
		final Double collectTimePrevious = CollectHelper.getNumberParamCollectTime(monitor,
			USED_TIME_PERCENT_PARAMETER, true);

		if (collectTimePrevious == null) {

			// This should never happen
			log.warn("Hostname {} - Found previous usedTimePercent value, but could not find previous collect time.", hostname);

			return;
		}

		// Computing the value delta
		final Double usedTimePercentDelta = CollectHelper.subtract(USED_TIME_PERCENT_PARAMETER,
			usedTimePercentRaw, usedTimePercentPrevious, hostname);

		// Computing the time delta
		final double timeDeltaInSeconds = CollectHelper.subtract(USED_TIME_PERCENT_PARAMETER,
			collectTime.doubleValue(), collectTimePrevious, hostname) / 1000.0;

		if (timeDeltaInSeconds == 0.0) {
			return;
		}

		// Setting the parameter
		CollectHelper.updateNumberParameter(monitor,
			USED_TIME_PERCENT_PARAMETER,
			PERCENT_PARAMETER_UNIT,
			collectTime,
			100.0 * usedTimePercentDelta / timeDeltaInSeconds,
			usedTimePercentRaw);
	}

	/**
	 * Collect the voltage value, if the current {@link Monitor} is a {@link Voltage}.
	 */
	void collectVoltage() {
		final Monitor monitor = monitorCollectInfo.getMonitor();

		// Getting the current value
		final Double voltageValue = extractParameterValue(monitor.getMonitorType(),
				VOLTAGE_PARAMETER);

		final Double computedVoltage = (voltageValue != null && voltageValue >= -100000 && voltageValue <= 450000) ? voltageValue : null;

		if (computedVoltage != null ) {
			CollectHelper.updateNumberParameter(
				monitor,
				VOLTAGE_PARAMETER,
				VOLTAGE_PARAMETER_UNIT,
				monitorCollectInfo.getCollectTime(),
				computedVoltage,
				voltageValue
			);
		}
	}

	/**
	 * Collects the error counts
	 * 
	 * @param errorCountParameter           the name of the error count parameter
	 * @param startingErrorCounterParameter the name of the starting error count parameter
	 */
	void collectErrorCount(@NonNull String errorCountParameter, @NonNull String startingErrorCounterParameter) {

		final Monitor monitor = monitorCollectInfo.getMonitor();

		Double rawErrorCount = extractParameterValue(monitor.getMonitorType(),
				errorCountParameter);

		if (rawErrorCount != null) {
			double errorCount = 0.0;
			final Double startingErrorCount = CollectHelper.getNumberParamRawValue(
					monitor, startingErrorCounterParameter, true);

			if (startingErrorCount != null) {
				// Remove existing error count from the current value
				errorCount = rawErrorCount - startingErrorCount;

				// If we obtain a negative number, that's impossible: set everything to 0
				if (errorCount < 0) {
					errorCount = 0.0;

					// Reset the starting error count
					CollectHelper.updateNumberParameter(
							monitor,
							startingErrorCounterParameter,
							ERROR_COUNT_PARAMETER_UNIT,
							monitorCollectInfo.getCollectTime(),
							0.0,
							0.0
					);
				} else {
					// Copy the last startingErrorCount
					CollectHelper.updateNumberParameter(
							monitor,
							startingErrorCounterParameter,
							ERROR_COUNT_PARAMETER_UNIT,
							monitorCollectInfo.getCollectTime(),
							startingErrorCount,
							startingErrorCount
					);
				}
			} else {
				// First polling, we're going to pretend that everything is alright and save the existing number of errors
				if (rawErrorCount < 0) {
					rawErrorCount = 0.0;
				}
				CollectHelper.updateNumberParameter(
						monitor,
						startingErrorCounterParameter,
						ERROR_COUNT_PARAMETER_UNIT,
						monitorCollectInfo.getCollectTime(),
						rawErrorCount,
						rawErrorCount
				);
			}

			CollectHelper.updateNumberParameter(
					monitor,
					errorCountParameter,
					ERROR_COUNT_PARAMETER_UNIT,
					monitorCollectInfo.getCollectTime(),
					errorCount,
					errorCount
			);
		}

	}

	/**
	 * Collects the incremental parameters, namely
	 * {@link TapeDrive} unmount, mount & {@link Robotics} move count.
	 *
	 * @param countParameter		The name of the count parameter, like mountCount
	 * @param countParameterUnit	The unit of the count parameter, like mounts
	 */
	void collectIncrementCount(final String countParameter, final String countParameterUnit) {

		final Monitor monitor = monitorCollectInfo.getMonitor();
		final Double rawCount  = extractParameterValue(monitor.getMonitorType(), countParameter);

		if (rawCount != null) {

			// Getting the previous value
			Double previousRawCount = CollectHelper.getNumberParamRawValue(monitor, countParameter, true);

			CollectHelper.updateNumberParameter(
				monitor,
				countParameter,
				countParameterUnit,
				monitorCollectInfo.getCollectTime(),
				(previousRawCount != null && previousRawCount < rawCount) ?  (rawCount - previousRawCount) : 0,
				rawCount
			);
		}
	}

	/**
	 * Collects the used capacity of {@link PowerSupply}.
	 */
	void collectPowerSupplyUsedCapacity() {

		final Monitor monitor = monitorCollectInfo.getMonitor();

		// Getting the used percent
		Double usedPercent = null;
		final Double usedPercentRaw = extractParameterValue(monitor.getMonitorType(),
			USED_PERCENT_PARAMETER);

		if (usedPercentRaw == null) {

			// Getting the used capacity
			final Double powerSupplyUsedWatts = extractParameterValue(monitor.getMonitorType(),
				USED_WATTS_PARAMETER);

			// Getting the power
			final Double power = NumberHelper.parseDouble(monitor.getMetadata(POWER_SUPPLY_POWER), null);

			if (powerSupplyUsedWatts  != null && power != null && power > 0) {
				usedPercent = 100.0 * powerSupplyUsedWatts / power;
			}

		} else {
			usedPercent = usedPercentRaw;
		}

		// Update the used capacity, if the usedPercent is valid
		if (usedPercent != null && usedPercent >= 0.0 && usedPercent <= 100.0) {
			CollectHelper.updateNumberParameter(
				monitor,
				USED_CAPACITY_PARAMETER,
				PERCENT_PARAMETER_UNIT,
				monitorCollectInfo.getCollectTime(),
				usedPercent,
				usedPercentRaw
			);
		}
	}

	/**
	 * Collects space parameters for {@link LogicalDisk}:<br>
	 * <ul>
	 * <li>Unallocated space.</li>
	 * <li>Allocated space.</li>
	 * <li>Unallocated space percentage.</li>
	 * <li>Allocated space percentage.</li>
	 * </ul>
	 */
	void collectLogicalDiskSpace() {

		final Monitor monitor = monitorCollectInfo.getMonitor();

		final Double unallocatedSpaceRaw = extractParameterValue(monitor.getMonitorType(),
				UNALLOCATED_SPACE_PARAMETER);

		if (unallocatedSpaceRaw != null) {

			final double unallocatedSpaceGb = unallocatedSpaceRaw / BYTES_TO_GB_CONV_FACTOR;

			CollectHelper.updateNumberParameter(
				monitor,
				UNALLOCATED_SPACE_PARAMETER,
				SPACE_GB_PARAMETER_UNIT,
				monitorCollectInfo.getCollectTime(),
				unallocatedSpaceGb, // Bytes to GB
				unallocatedSpaceRaw
			);

			final Double sizeRawBytes = NumberHelper.parseDouble(monitor.getMetadata(SIZE), null);

			if (sizeRawBytes != null) {

				// Convert the size to GB
				double sizeGb = sizeRawBytes / BYTES_TO_GB_CONV_FACTOR;

				// Check if the size is correct to avoid the negative value for the allocated space
				if (sizeGb < unallocatedSpaceGb) {
					log.warn(
						"Hostname {} - The logical disk size ({} GB) is greater than the unallocated space ({} GB). "
							+ "The following parameters will be ignored:\n{}.\n{}.\n{}.",
						monitorCollectInfo.getHostname(),
						sizeGb,
						unallocatedSpaceGb,
						ALLOCATED_SPACE_PARAMETER,
						ALLOCATED_SPACE_PERCENT_PARAMETER,
						UNALLOCATED_SPACE_PERCENT_PARAMETER
					);
					return;
				}

				// Compute the allocated space
				final double allocatedSpaceGb = sizeGb - unallocatedSpaceGb;

				// Collect the allocated space parameter
				CollectHelper.updateNumberParameter(
					monitor,
					ALLOCATED_SPACE_PARAMETER,
					SPACE_GB_PARAMETER_UNIT,
					monitorCollectInfo.getCollectTime(),
					allocatedSpaceGb,
					allocatedSpaceGb
				);

				// sizeGb equals 0? Avoid the division by zero!
				if (sizeGb == 0.0) {
					log.warn(
						"Hostname {} - The logical disk size equals 0. The following parameters will be ignored:\n{}.\n{}.",
						monitorCollectInfo.getHostname(),
						ALLOCATED_SPACE_PERCENT_PARAMETER,
						UNALLOCATED_SPACE_PERCENT_PARAMETER
					);
					return;
				}

				// Collect the allocated space percentage parameter
				CollectHelper.updateNumberParameter(
					monitor,
					ALLOCATED_SPACE_PERCENT_PARAMETER,
					PERCENT_PARAMETER_UNIT,
					monitorCollectInfo.getCollectTime(),
					allocatedSpaceGb / sizeGb * 100,
					allocatedSpaceGb / sizeGb * 100
				);

				// Collect the unallocated space percentage parameter
				CollectHelper.updateNumberParameter(
					monitor,
					UNALLOCATED_SPACE_PERCENT_PARAMETER,
					PERCENT_PARAMETER_UNIT,
					monitorCollectInfo.getCollectTime(),
					unallocatedSpaceGb / sizeGb * 100,
					unallocatedSpaceGb / sizeGb * 100
				);
			}
		}
	}

	/**
	 * Collect the temperature value, if the current {@link Monitor} is a {@link Temperature}.
	 */
	void collectTemperature() {
		final Monitor monitor = monitorCollectInfo.getMonitor();

		// Getting the current value
		final Double temperatureValue = extractParameterValue(monitor.getMonitorType(),
				TEMPERATURE_PARAMETER);

		if (temperatureValue != null && temperatureValue >= -100 && temperatureValue <= 200) {
			CollectHelper.updateNumberParameter(
				monitor,
				TEMPERATURE_PARAMETER,
				TEMPERATURE_PARAMETER_UNIT,
				monitorCollectInfo.getCollectTime(),
				temperatureValue,
				temperatureValue
			);
		}
	}

	/**
	 * Estimates the power dissipation of a network card, based on some characteristics Inspired by:
	 * https://www.cl.cam.ac.uk/~acr31/pubs/sohan-10gbpower.pdf
	 */
	void estimateNetworkCardPowerConsumption() {
		final Monitor monitor = monitorCollectInfo.getMonitor();

		// Network card name
		final String lowerCaseName = monitor.getName().toLowerCase();

		// Link status
		final IState linkStatus = CollectHelper.getParameterState(monitor, LINK_STATUS_PARAMETER);

		// Link speed
		final Double linkSpeed = CollectHelper.getNumberParamValue(monitor, LINK_SPEED_PARAMETER);

		// Bandwidth utilization
		final Double bandwidthUtilization = CollectHelper.getNumberParamValue(monitor, BANDWIDTH_UTILIZATION_PARAMETER);

		final double powerConsumption;

		// Virtual or WAN card: 0 (it's not physical)
		if (lowerCaseName.contains("wan") || lowerCaseName.contains("virt")) {
			powerConsumption = 0.0;
		}

		// Unplugged, means not much
		else if (LinkStatus.UNPLUGGED.equals(linkStatus)) {
			// 1W for an unplugged card
			powerConsumption = 1.0;
		}

		// (0.5 + 0.5 * bandwidthUtilization) * 5 * log10(linkSpeed)
		else if (CollectHelper.isValidPercentage(bandwidthUtilization)) {
			if (CollectHelper.isValidPositive(linkSpeed) && linkSpeed > 10) {
				powerConsumption = (0.5 + 0.5 * bandwidthUtilization / 100.0) * 5.0 * Math.log10(linkSpeed);
			} else {
				powerConsumption = (0.5 + 0.5 * bandwidthUtilization / 100.0) * 5.0;
			}
		}

		// If we have the link speed, we'll go with 0.75 * 5 * log10(linkSpeed)
		else if (CollectHelper.isValidPositive(linkSpeed)) {
			if (linkSpeed > 10) {
				powerConsumption = 0.75 * 5.0 * Math.log10(linkSpeed);
			} else {
				powerConsumption = 2.0;
			}

		} else {
			// Some default value (what about 10W ? wet finger...)
			powerConsumption = 10.0;
		}

		CollectHelper.collectEnergyUsageFromPower(monitor,
				monitorCollectInfo.getCollectTime(),
				NumberHelper.round(powerConsumption, 2, RoundingMode.HALF_UP),
				monitorCollectInfo.getHostname());
	}

	/**
	 * Calculate the approximate power consumption of the media changer.<br>
	 * If it moved, 154W, if not, 48W Source:
	 * https://docs.oracle.com/en/storage/tape-storage/sl4000/slklg/calculate-total-power-consumption.html
	 */
	void estimateRoboticsPowerConsumption() {
		final Monitor monitor = monitorCollectInfo.getMonitor();

		final Double moveCount = CollectHelper.getNumberParamValue(monitor, MOVE_COUNT_PARAMETER);

		final double powerConsumption;
		if (moveCount != null && moveCount > 0.0) {
			powerConsumption = 154.0;
		} else {
			powerConsumption = 48.0;
		}

		CollectHelper.collectEnergyUsageFromPower(monitor,
				monitorCollectInfo.getCollectTime(),
				powerConsumption,
				monitorCollectInfo.getHostname());
	}

	/**
	 * Estimates the power consumed by a tape drive for its operation, based on some of its characteristics and activity Inspiration:
	 * https://docs.oracle.com/en/storage/tape-storage/sl4000/slklg/calculate-total-power-consumption.html
	 * https://www.ibm.com/support/knowledgecenter/STQRQ9/com.ibm.storage.ts4500.doc/ts4500_power_consumption_and_cooling_requirements.html
	 */
	void estimateTapeDrivePowerConsumption() {
		final Monitor monitor = monitorCollectInfo.getMonitor();

		Double mountCount = CollectHelper.getNumberParamValue(monitor, MOUNT_COUNT_PARAMETER);
		mountCount = mountCount != null ? mountCount : 0.0;

		Double unmountCount = CollectHelper.getNumberParamValue(monitor, UNMOUNT_COUNT_PARAMETER);
		unmountCount = unmountCount != null ? unmountCount : 0.0;

		final boolean active = mountCount + unmountCount > 0;
		final String lowerCaseName = monitor.getName().toLowerCase();

		final double powerConsumption = estimateTapeDrivePowerConsumption(active, lowerCaseName);

		CollectHelper.collectEnergyUsageFromPower(monitor,
				monitorCollectInfo.getCollectTime(),
				powerConsumption,
				monitorCollectInfo.getHostname());
	}

	/**
	 * Estimate the tape drive power consumption based on its name and its activity
	 *
	 * @param active        Whether the tape drive is active or not
	 * @param lowerCaseName The name of the tape drive in lower case
	 * @return double value
	 */
	double estimateTapeDrivePowerConsumption(final boolean active, final String lowerCaseName) {

		if (lowerCaseName.contains("lto")) {
			return active ? 46 : 30;
		} else if (lowerCaseName.contains("t10000d")) {
			return active ? 127 : 64;
		} else if (lowerCaseName.contains("t10000")) {
			return active ? 93 : 61;
		} else if (lowerCaseName.contains("ts")) {
			return active ? 53 : 35;
		}

		return active ? 80 : 55;
	}

	/**
	 * Collects the power consumption from {@link Fan} speed.
	 */
	void estimateFanPowerConsumption() {

		final Monitor monitor = monitorCollectInfo.getMonitor();

		// Approximately 5 Watt for standard fan
		double powerConsumption = 5.0;

		final Double fanSpeed = extractParameterValue(monitor.getMonitorType(),
			SPEED_PARAMETER);

		if (CollectHelper.isValidPositive(fanSpeed)) {
			// 1000 RPM = 1 Watt
			powerConsumption = fanSpeed / 1000.0;
		} else {
			final Double fanSpeedPercent = extractParameterValue(monitor.getMonitorType(),
					SPEED_PERCENT_PARAMETER);

			if (CollectHelper.isValidPercentage(fanSpeedPercent)) {
				// Approximately 5 Watt for 100%
				powerConsumption = fanSpeedPercent * 0.05;
			}
		}

		CollectHelper.collectEnergyUsageFromPower(monitor,
				monitorCollectInfo.getCollectTime(),
				NumberHelper.round(powerConsumption, 2, RoundingMode.HALF_UP),
				monitorCollectInfo.getHostname());
	}

	/**
	 * Collect the physical disks specific parameters.
	 */
	void collectPhysicalDiskParameters() {
		final Monitor monitor = monitorCollectInfo.getMonitor();

		// Getting the endurance remaining current value
		final Double rawEnduranceRemaining = extractParameterValue(monitor.getMonitorType(),
				ENDURANCE_REMAINING_PARAMETER);

		if (rawEnduranceRemaining != null && rawEnduranceRemaining >= 0 && rawEnduranceRemaining <= 100) {
			CollectHelper.updateNumberParameter(
				monitor,
				ENDURANCE_REMAINING_PARAMETER,
				PERCENT_PARAMETER_UNIT,
				monitorCollectInfo.getCollectTime(),
				rawEnduranceRemaining,
				rawEnduranceRemaining
			);
		}
	}

	/**
	 * Collects the color status for a {@link Led}.
	 */
	void collectLedColor() {

		// Getting the raw color from the current row
		final String colorRaw = CollectHelper.getValueTableColumnValue(monitorCollectInfo.getValueTable(),
			COLOR_PARAMETER,
			MonitorType.LED,
			monitorCollectInfo.getRow(),
			monitorCollectInfo.getMapping().get(COLOR_PARAMETER),
			monitorCollectInfo.getHostname());

		if (colorRaw != null) {

			final Monitor monitor = monitorCollectInfo.getMonitor();

			// Getting the color status
			Map<String, String> metadata = monitor.getMetadata();
			String warningOnColor = metadata.get(WARNING_ON_COLOR);
			String alarmOnColor = metadata.get(ALARM_ON_COLOR);

			String colorStatusValue;
			if (warningOnColor != null && warningOnColor.toUpperCase().contains(colorRaw.toUpperCase())) {
				colorStatusValue = "1";
			} else if (alarmOnColor != null && alarmOnColor.toUpperCase().contains(colorRaw.toUpperCase())) {
				colorStatusValue = "2";
			} else {
				colorStatusValue = "0";
			}

			// Translating the color status
			final Optional<LedColorStatus> colorStatus = LedColorStatus.interpret(colorStatusValue);

			// colorState is never null here
			colorStatus
				.ifPresent(ledColorStatus -> CollectHelper.updateDiscreteParameter(
					monitor,
					COLOR_PARAMETER,
					monitorCollectInfo.getCollectTime(),
					ledColorStatus
				));
		}
	}

	/**
	 * Collects the status and indicator status parameters for a {@link Led}.
	 */
	void collectLedStatusAndLedIndicatorStatus() {

		// Getting the raw status from the current row
		final String statusRaw = CollectHelper.getValueTableColumnValue(
				monitorCollectInfo.getValueTable(),
				STATUS_PARAMETER,
				MonitorType.LED,
				monitorCollectInfo.getRow(),
				monitorCollectInfo.getMapping().get(STATUS_PARAMETER),
				monitorCollectInfo.getHostname()
		);

		if (statusRaw != null) {

			final Monitor monitor = monitorCollectInfo.getMonitor();

			// Translating the LED indicator status
			Optional<LedIndicator> maybeLedIndicatorStatus = LedIndicator.interpret(statusRaw);

			if (maybeLedIndicatorStatus.isEmpty()) {
				return;
			}

			final LedIndicator ledIndicator = maybeLedIndicatorStatus.get();

			CollectHelper.updateDiscreteParameter(
					monitor,
					LED_INDICATOR_PARAMETER,
					monitorCollectInfo.getCollectTime(),
					ledIndicator
			);

			// Translating the status
			Map<String, String> metadata = monitor.getMetadata();

			String preTranslatedStatus;
			switch (ledIndicator) {
				case ON:
					preTranslatedStatus = metadata.get(ON_STATUS);
					break;
				case BLINKING:
					preTranslatedStatus = metadata.get(BLINKING_STATUS);
					break;
				case OFF:
				default:
					preTranslatedStatus = metadata.get(OFF_STATUS);
			}

			final IState status = CollectHelper.translateState(
					preTranslatedStatus,
					Status::interpret,
					STATUS_PARAMETER,
					monitor.getId(),
					monitorCollectInfo.getHostname()
			);

			if (status != null) {

				CollectHelper.updateDiscreteParameter(
						monitor,
						STATUS_PARAMETER,
						monitorCollectInfo.getCollectTime(),
						status
				);
			}

		}
	}

	/**
	 * Collect the {@link NetworkCard} duplex mode parameter.
	 */
	DuplexMode collectNetworkCardDuplexMode() {
		final Monitor monitor = monitorCollectInfo.getMonitor();

		// Not possible to monitor if the cable is not connected
		final IState linkStatus = CollectHelper.getParameterState(monitor, LINK_STATUS_PARAMETER);
		if (LinkStatus.PLUGGED.equals(linkStatus)) {

			// Getting the duplex mode
			final String duplexModeRaw = extractParameterStringValue(monitor.getMonitorType(), DUPLEX_MODE_PARAMETER);

			if (duplexModeRaw != null) {

				final DuplexMode duplexMode = DuplexMode
					.interpret(duplexModeRaw)
					.orElse(DuplexMode.HALF);
		
				CollectHelper.updateDiscreteParameter(
						monitor,
						DUPLEX_MODE_PARAMETER,
						monitorCollectInfo.getCollectTime(),
						duplexMode
				);

				return duplexMode;
			}

		}

		return null;
	}

	/**
	 * Collect the {@link NetworkCard} link speed parameter.
	 */
	Double collectNetworkCardLinkSpeed() {
		final Monitor monitor = monitorCollectInfo.getMonitor();

		// Getting the link speed
		final Double linkSpeed = extractParameterValue(monitor.getMonitorType(),
				LINK_SPEED_PARAMETER);

		if (linkSpeed != null && linkSpeed >= 0) {
			CollectHelper.updateNumberParameter(
					monitor,
					LINK_SPEED_PARAMETER,
					SPEED_MBITS_PARAMETER_UNIT,
					monitorCollectInfo.getCollectTime(),
					linkSpeed,
					linkSpeed
			);
		}

		return linkSpeed;
	}

	/**
	 * Collects the {@link NetworkCard} bytes rate and usage.
	 *
	 * @param bytesParameterName       The name of the bytes parameter where the raw value is collected
	 * @param byteRateParameterName    The name of the byte rate parameter to be calculated
	 * @param usageReportParameterName The name of the usage report parameter to be calculated
	 *
	 * @return bytesRate               Calculated byte rate in MB/s
	 */
	Double collectNetworkCardBytesRate(final String bytesParameterName, final String byteRateParameterName, final String usageReportParameterName) {

		final Monitor monitor = monitorCollectInfo.getMonitor();
		final String hostname = monitorCollectInfo.getHostname();

		// Getting the current value
		final Double bytesValue = extractParameterValue(monitor.getMonitorType(), bytesParameterName);
		if (bytesValue == null) {
			return null;
		}

		// Getting the current value's collect time
		Long collectTime = monitorCollectInfo.getCollectTime();

		// Setting the bytes parameter
		CollectHelper.updateNumberParameter(
				monitor,
				bytesParameterName,
				BYTES_PARAMETER_UNIT,
				collectTime,
				bytesValue,
				bytesValue
		);

		// Getting the previous value
		Double lastBytesValue = CollectHelper.getNumberParamRawValue(monitor, bytesParameterName, true);
		if (lastBytesValue == null) {
			log.warn("Hostname {} - No last bytes value to calculate the byte rate or usage.", hostname);
			return null;
		}

		// Getting the previous value's collect time
		final Double collectTimePrevious = CollectHelper.getNumberParamCollectTime(monitor, bytesParameterName, true);
		if (collectTimePrevious == null) {
			// This should never happen
			log.warn("Hostname {} - Found previous bytes value, but could not find previous collect time.", hostname);
			return null;
		}

		// Computing the value delta (in MBytes)
		final Double bytesDelta = CollectHelper.subtract(bytesParameterName, bytesValue, lastBytesValue, hostname);
		if (bytesDelta == null) {
			log.warn("Hostname {} - Found decreasing bytes count - must have been reset.", hostname);
			return null;
		}
		final double bytesDeltaMb = bytesDelta / 1048576.0;

		// Byte rate
		Double bytesRate = null;

		// Computing the time delta (in seconds)
		final Double timeDeltaMs = CollectHelper.subtract(bytesParameterName, collectTime.doubleValue(), collectTimePrevious, hostname);
		if (timeDeltaMs == null || timeDeltaMs == 0.0) {
			log.warn("Hostname {} - No denominator for collect time difference to calculate the byte rate.", hostname);
		} else {
			final double timeDelta = timeDeltaMs / 1000.0;

			// Setting the byte rate (in MB/s)
			bytesRate = bytesDeltaMb / timeDelta;
			CollectHelper.updateNumberParameter(monitor,
					byteRateParameterName,
					BYTES_RATE_PARAMETER_UNIT,
					collectTime,
					bytesRate,
					bytesRate
			);
		}

		// Setting the usage (in GB), even if it is zero
		final double bytesDeltaGb = bytesDeltaMb / 1024.0;
		CollectHelper.updateNumberParameter(
				monitor,
				usageReportParameterName,
				SPACE_GB_PARAMETER_UNIT,
				collectTime,
				bytesDeltaGb,
				bytesDeltaGb
		);

		return bytesRate;
	}

	/**
	 * Collects the {@link NetworkCard} packets rate and usage.
	 *
	 * @param packetsParameterName       The name of the packets parameter where the raw value is collected
	 * @param packetRateParameterName    The name of the packets rate parameter to be calculated
	 * @param usageReportParameterName   The name of the usage report parameter to be calculated
	 *
	 * @return packetsValue              Number of packets
	 */
	Double collectNetworkCardPacketsRate(final String packetsParameterName, final String packetRateParameterName, final String usageReportParameterName) {

		final Monitor monitor = monitorCollectInfo.getMonitor();
		final String hostname = monitorCollectInfo.getHostname();

		// Getting the current value
		final Double packetsValue = extractParameterValue(monitor.getMonitorType(),
			packetsParameterName);

		if (packetsValue == null) {
			return null;
		}

		// Getting the current value's collect time
		Long collectTime = monitorCollectInfo.getCollectTime();

		// Setting the packets parameter
		CollectHelper.updateNumberParameter(
				monitor,
				packetsParameterName,
				PACKETS_PARAMETER_UNIT,
				collectTime,
				packetsValue,
				packetsValue
		);

		// Getting the previous value
		Double lastPacketsValue = CollectHelper.getNumberParamRawValue(monitor, packetsParameterName, true);
		if (lastPacketsValue == null) {
			return packetsValue;
		}

		// Getting the previous value's collect time
		final Double collectTimePrevious = CollectHelper.getNumberParamCollectTime(monitor, packetsParameterName, true);

		if (collectTimePrevious == null) {
			// This should never happen
			log.warn("Hostname {} - Found previous packets value, but could not find previous collect time.", hostname);
			return packetsValue;
		}

		// Computing the packets delta
		final Double packetsDelta = CollectHelper.subtract(packetsParameterName, packetsValue, lastPacketsValue, hostname);
		if (packetsDelta == null) {
			log.warn("Hostname {} - Found decreasing packets count - must have been reset.", hostname);
			return packetsValue;
		}

		// Computing the time delta (in seconds)
		Double timeDelta = CollectHelper.subtract(packetsParameterName, collectTime.doubleValue(), collectTimePrevious, hostname);
		if (timeDelta == null || timeDelta == 0.0) {
			return packetsValue;
		}

		timeDelta /= 1000.0;

		// Setting the usage in packets
		CollectHelper.updateNumberParameter(
				monitor,
				usageReportParameterName,
				PACKETS_PARAMETER_UNIT,
				collectTime,
				packetsDelta,
				packetsValue
		);

		// Setting the packets rate
		final Double packetsRate = packetsDelta / timeDelta;
		CollectHelper.updateNumberParameter(
				monitor,
				packetRateParameterName,
				PACKETS_RATE_PARAMETER_UNIT,
				collectTime,
				packetsRate,
				packetsValue
		);

		return packetsValue;
	}

	/**
	 * Collect the {@link NetworkCard} bandwidth utilization.
	 */
	void collectNetworkCardBandwidthUtilization(final DuplexMode duplexMode, final Double linkSpeed,
			Double receivedBytesRate, Double transmittedBytesRate) {

		// No rate => no bandwidth
		if (receivedBytesRate == null && transmittedBytesRate == null) {
			return;
		}

		final Monitor monitor = monitorCollectInfo.getMonitor();

		if (linkSpeed != null && linkSpeed > 0) {

			if (receivedBytesRate == null) {
				receivedBytesRate = 0D;
			}
			if (transmittedBytesRate == null) {
				transmittedBytesRate = 0D;
			}

			double bandwidthUtilization;
			if (duplexMode == null || DuplexMode.FULL.equals(duplexMode))  {
				// Full-duplex mode, or unknown mode, in which case, we assume full-duplex.
				// In full-duplex mode, consider bandwidth as the maximum usage while receiving or transmitting.
				bandwidthUtilization = Math.max(transmittedBytesRate, receivedBytesRate) * 8 * 100 / linkSpeed;
			} else {
				// Half-duplex mode
				bandwidthUtilization = (transmittedBytesRate + receivedBytesRate) * 8 * 100 / linkSpeed;
			}

			CollectHelper.updateNumberParameter(
					monitor,
					BANDWIDTH_UTILIZATION_PARAMETER,
					PERCENT_PARAMETER_UNIT,
					monitorCollectInfo.getCollectTime(),
					bandwidthUtilization,
					bandwidthUtilization
			);
		}
	}

	/**
	 * Collect the {@link NetworkCard} error count/percentage.
	 */
	void collectNetworkCardErrorPercent(final Double receivedPackets, final Double transmittedPackets) {

		if (receivedPackets == null || transmittedPackets == null) {
			return;
		}

		final Monitor monitor = monitorCollectInfo.getMonitor();
		final String hostname = monitorCollectInfo.getHostname();

		// Getting the current error count
		final Double errorCount = extractParameterValue(monitor.getMonitorType(),
			ERROR_COUNT_PARAMETER);

		if (errorCount == null) {
			return;
		}

		// Setting the error count
		CollectHelper.updateNumberParameter(
				monitor,
				ERROR_COUNT_PARAMETER,
				ERROR_COUNT_PARAMETER_UNIT,
				monitorCollectInfo.getCollectTime(),
				errorCount,
				errorCount
		);

		// Setting the total packets
		final Double totalPackets = receivedPackets + transmittedPackets;
		CollectHelper.updateNumberParameter(
				monitor,
				TOTAL_PACKETS_PARAMETER,
				PACKETS_PARAMETER_UNIT,
				monitorCollectInfo.getCollectTime(),
				totalPackets,
				totalPackets
		);

		// Getting the previous error count
		final Double lastErrorCount = CollectHelper.getNumberParamRawValue(monitor,
			ERROR_COUNT_PARAMETER, true);

		if (lastErrorCount == null) {
			return;
		}

		// Getting the previous total packets count
		final Double lastTotalPackets = CollectHelper.getNumberParamRawValue(monitor,
				TOTAL_PACKETS_PARAMETER, true);

		if (lastTotalPackets == null) {
			return;
		}

		// Computing the total packets delta
		final Double totalPacketsDelta = CollectHelper.subtract(TOTAL_PACKETS_PARAMETER,
				totalPackets, lastTotalPackets, hostname);

		// Setting the error percent
		if (totalPacketsDelta != null && totalPacketsDelta > 10) {

			// Computing the error count delta
			final Double errorCountDelta = CollectHelper.subtract(ERROR_COUNT_PARAMETER,
				errorCount, lastErrorCount, hostname);

			if (errorCountDelta != null) {
				// Computing the error percent
				final Double errorPercent = Math.min(100 * errorCountDelta / totalPacketsDelta, 100);

				CollectHelper.updateNumberParameter(
						monitor,
						ERROR_PERCENT_PARAMETER,
						PERCENT_PARAMETER_UNIT,
						monitorCollectInfo.getCollectTime(),
						errorPercent,
						errorPercent
				);
			}
		}
	}

	/**
	 * Collect the {@link NetworkCard} zero credit buffer count/percent
	 */
	void collectNetworkCardZeroBufferCreditPercent() {
		final Monitor monitor = monitorCollectInfo.getMonitor();
		final String hostname = monitorCollectInfo.getHostname();

		// Getting the current zero buffer credit count
		final Double zeroBufferCreditCount = extractParameterValue(monitor.getMonitorType(),
				ZERO_BUFFER_CREDIT_COUNT_PARAMETER);

		// Getting the previous zero buffer credit count
		final Double lastZeroBufferCreditCount = CollectHelper.getNumberParamRawValue(monitor,
			ZERO_BUFFER_CREDIT_COUNT_PARAMETER, true);

		// Setting the zero buffer credit count
		CollectHelper.updateNumberParameter(
				monitor,
				ZERO_BUFFER_CREDIT_COUNT_PARAMETER,
				ZERO_BUFFER_CREDIT_COUNT_PARAMETER_UNIT,
				monitorCollectInfo.getCollectTime(),
				zeroBufferCreditCount,
				zeroBufferCreditCount
		);

		if (zeroBufferCreditCount == null || lastZeroBufferCreditCount == null) {
			return;
		}

		// Getting the transmitted packets since last collect
		final Double transmittedPacketsSinceLastCollect = CollectHelper.getNumberParamValue(monitor,
				USAGE_REPORT_TRANSMITTED_PACKETS_PARAMETER);

		if (transmittedPacketsSinceLastCollect == null) {
			return;
		}

		// Computing the zero buffer credit delta delta
		final Double zeroBufferCreditDelta = CollectHelper.subtract(ZERO_BUFFER_CREDIT_COUNT_PARAMETER,
				zeroBufferCreditCount, lastZeroBufferCreditCount, hostname);

		if (zeroBufferCreditDelta != null) {
			// Setting the zero buffer credit percent
			final Double lastZeroBufferCreditPercent = 100 * zeroBufferCreditDelta / (zeroBufferCreditDelta + transmittedPacketsSinceLastCollect);
			CollectHelper.updateNumberParameter(
					monitor,
					ZERO_BUFFER_CREDIT_PERCENT_PARAMETER,
					PERCENT_PARAMETER_UNIT,
					monitorCollectInfo.getCollectTime(),
					lastZeroBufferCreditPercent,
					lastZeroBufferCreditPercent
			);
		}
	}

	/**
	 * Collects the GPU used time ratio parameters
	 * (<i>DecoderUsedTimePercent</i>, <i>EncoderUsedTimePercent</i> and <i>UsedTimePercent</i>),
	 * from their matching time parameter (<i>DecoderUsedTime</i>, <i>EncoderUsedTime</i> and <i>UsedTime</i>).<br><br>
	 *
	 * If a parameter could not be computed, then tries to collect it directly.
	 */
	void collectGpuUsedTimeRatioParameters() {

		final Monitor monitor = monitorCollectInfo.getMonitor();
		final String hostname = monitorCollectInfo.getHostname();

		for (Map.Entry<String, String> entry : GPU_USED_TIME_PARAMETERS.entrySet()) {

			Double usedTimePercentValue = null;

			// Getting the used time current value
			final Double usedTimeCurrentRawValue = extractParameterValue(monitor.getMonitorType(), entry.getKey());
			if (usedTimeCurrentRawValue != null) {

				// Computing the used time percent value in the [0;1] range, based on the used time value
				usedTimePercentValue = CollectHelper.rate(entry.getKey(), usedTimeCurrentRawValue,
					monitorCollectInfo.getCollectTime(), monitor, hostname);
			}

			if (usedTimePercentValue != null) {

				// Converting from [0;1] range to [0;100] range
				usedTimePercentValue *= 100.0;

			} else {

				// If the used time percent could not be computed, trying to get its value directly
				usedTimePercentValue = extractParameterValue(monitor.getMonitorType(), entry.getValue());
			}

			if (usedTimePercentValue != null && usedTimePercentValue >= 0.0 && usedTimePercentValue <= 100.0) {

				CollectHelper.updateNumberParameter(
					monitor,
					entry.getValue(),
					PERCENT_PARAMETER_UNIT,
					monitorCollectInfo.getCollectTime(),
					usedTimePercentValue,
					usedTimePercentValue
				);
			}
		}
	}

	/**
	 * Collects the GPU transferred bytes parameters (<i>transmittedBytes</i> and <i>receivedBytes</i>),
	 * when possible.<br><br>
	 *
	 * If a parameter could not be collected, then tries to collect the matching rate parameter<br>
	 * (respectively: <i>transmittedBytesRate</i> and <i>receivedBytesRate</i>).
	 */
	void collectGpuTransferredBytesParameters() {

		final Monitor monitor = monitorCollectInfo.getMonitor();
		final String hostname = monitorCollectInfo.getHostname();

		Double transferredBytesRawValue;

		Double transferredBytesRateInBytesPerSecond = null;
		double transferredBytesRateInMegaBytesPerSecond;

		for (Map.Entry<String, String> entry : GPU_BYTES_TRANSFER_PARAMETERS.entrySet()) {

			// Getting the GPU transferred bytes value
			transferredBytesRawValue = extractParameterValue(monitor.getMonitorType(), entry.getKey());

			if (transferredBytesRawValue != null) {

				// Setting the GPU transferred bytes parameter
				CollectHelper.updateNumberParameter(
					monitor,
					entry.getKey(),
					BYTES_PARAMETER_UNIT,
					monitorCollectInfo.getCollectTime(),
					transferredBytesRawValue,
					transferredBytesRawValue
				);

			} else {

				// If the GPU bytes transfer parameter could not be collected,
				// let us try the GPU transferred bytes rate parameter
				transferredBytesRateInBytesPerSecond = extractParameterValue(monitor.getMonitorType(),
					entry.getValue());

				// Setting the GPU transferred bytes rate
				if (transferredBytesRateInBytesPerSecond != null) {

					// Converting from B/s to MB/s
					transferredBytesRateInMegaBytesPerSecond =
						transferredBytesRateInBytesPerSecond / 1048576.0; // 1048576 == 1024 * 1024

					// Setting the GPU transferred bytes rate parameter
					CollectHelper.updateNumberParameter(
						monitor,
						entry.getValue(),
						BYTES_RATE_PARAMETER_UNIT,
						monitorCollectInfo.getCollectTime(),
						transferredBytesRateInMegaBytesPerSecond,
						transferredBytesRateInBytesPerSecond
					);
				}
			}

			// If the transferred bytes rates could not be extracted,
			// let us try and compute it
			if (transferredBytesRateInBytesPerSecond == null && transferredBytesRawValue != null) {

				transferredBytesRateInBytesPerSecond = CollectHelper.rate(entry.getKey(), transferredBytesRawValue,
					monitorCollectInfo.getCollectTime(), monitor, hostname);

				if (transferredBytesRateInBytesPerSecond != null) {

					// Converting from B/s to MB/s
					transferredBytesRateInMegaBytesPerSecond =
						transferredBytesRateInBytesPerSecond / 1048576.0; // 1048576 == 1024 * 1024

					// Setting the GPU transferred bytes rate parameter
					CollectHelper.updateNumberParameter(
						monitor,
						entry.getValue(),
						BYTES_RATE_PARAMETER_UNIT,
						monitorCollectInfo.getCollectTime(),
						transferredBytesRateInMegaBytesPerSecond,
						transferredBytesRateInBytesPerSecond
					);
				}
			}
		}
	}

	/**
	 * Collect the status information on the current monitor instance. This
	 * method must be called after collecting the {@link Status}
	 */
	void collectStatusInformation() {

		final String valueTable = monitorCollectInfo.getValueTable();
		final Monitor monitor = monitorCollectInfo.getMonitor();
		final List<String> row = monitorCollectInfo.getRow();
		final Map<String, String> mapping = monitorCollectInfo.getMapping();
		final Long collectTime =  monitorCollectInfo.getCollectTime();

		final DiscreteParam statusParam = monitor.getParameter(STATUS_PARAMETER, DiscreteParam.class);
		Status status = null;
		if (statusParam != null && statusParam.getState() instanceof Status) {
			status = (Status) statusParam.getState();
		}

		final String statusInformation = CollectHelper.getValueTableColumnValue(
				valueTable,
				STATUS_INFORMATION_PARAMETER,
				monitor.getMonitorType(),
				row,
				mapping.get(STATUS_INFORMATION_PARAMETER),
				monitorCollectInfo.getHostname()
		);

		CollectHelper.updateStatusInformation(
				monitor,
				collectTime,
				statusInformation,
				status
		);
	}

	/**
	 * Collect the available path warning metadata
	 */
	void collectAvailablePathWarning() {

		final Monitor monitor = monitorCollectInfo.getMonitor();

		// Extract the available path count parameter value
		final Double availablePathCount = extractParameterValue(monitor.getMonitorType(), AVAILABLE_PATH_COUNT_PARAMETER);
		if (availablePathCount == null) {
			// Parameter not collected or simply not activated by the connector
			return;
		}

		// Get the maximum of available paths
		final Double maxAvailablePathCount = CollectHelper.getNumberParamRawValue(monitor, MAX_AVAILABLE_PATH_COUNT_PARAMETER, true);

		// The available path warning threshold
		final Double availablePathWarning = NumberHelper.parseDouble(monitor.getMetadata(AVAILABLE_PATH_WARNING), null);

		// If we do not have an available path warning threshold but we have the current
		// number of paths, set a warning thresholds of (current paths - 1).
		// Do this if the number of current paths increases too.
		if (((availablePathWarning == null)
				|| (maxAvailablePathCount != null && availablePathCount > maxAvailablePathCount))
				&& availablePathCount > 1) {

			// If the available path count is 1 or 0, this code is never reached as 0 is the ALARM threshold
			// when the LUN cannot be accessed anymore.
			monitor.addMetadata(AVAILABLE_PATH_WARNING, NumberHelper.formatNumber(availablePathCount - 1));

		}

		// If the number of available paths is higher than the highest number of paths we've found so far, set its new value.
		// Take new paths into account here and make sure that if error occurs on multiple paths at the same time
		// when they are plugged back in we don't change the thresholds.
		updateMaxAvailablePaths(monitor, availablePathCount, maxAvailablePathCount);

	}

	/**
	 * Update the maximum number of available paths
	 * 
	 * @param monitor               The monitor we want to update its maximum
	 *                              available paths parameter
	 * @param availablePathCount    The current available paths value
	 * @param maxAvailablePathCount The current maximum available paths value
	 */
	void updateMaxAvailablePaths(final Monitor monitor, final Double availablePathCount,
			Double maxAvailablePathCount) {

		if (maxAvailablePathCount == null || availablePathCount > maxAvailablePathCount) {

			log.info("Hostname {} - Number of paths increased to {} for LUN instance [id: {}, name: {}].",
					monitorCollectInfo.getHostname(), availablePathCount, monitor.getId(), monitor.getName());

			CollectHelper.updateNumberParameter(
					monitor,
					MAX_AVAILABLE_PATH_COUNT_PARAMETER,
					EMPTY,
					monitorCollectInfo.getCollectTime(),
					availablePathCount,
					availablePathCount
			);

		}
	}
}
