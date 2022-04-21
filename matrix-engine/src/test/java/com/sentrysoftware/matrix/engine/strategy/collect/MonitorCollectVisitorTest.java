package com.sentrysoftware.matrix.engine.strategy.collect;

import com.sentrysoftware.matrix.common.exception.MatsyaException;
import com.sentrysoftware.matrix.common.meta.monitor.Battery;
import com.sentrysoftware.matrix.common.meta.monitor.Blade;
import com.sentrysoftware.matrix.common.meta.monitor.Cpu;
import com.sentrysoftware.matrix.common.meta.monitor.CpuCore;
import com.sentrysoftware.matrix.common.meta.monitor.DiskController;
import com.sentrysoftware.matrix.common.meta.monitor.Enclosure;
import com.sentrysoftware.matrix.common.meta.monitor.Fan;
import com.sentrysoftware.matrix.common.meta.monitor.Gpu;
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
import com.sentrysoftware.matrix.common.meta.monitor.Target;
import com.sentrysoftware.matrix.common.meta.monitor.Temperature;
import com.sentrysoftware.matrix.common.meta.monitor.Vm;
import com.sentrysoftware.matrix.common.meta.monitor.Voltage;
import com.sentrysoftware.matrix.common.meta.parameter.DiscreteParamType;
import com.sentrysoftware.matrix.common.meta.parameter.state.DuplexMode;
import com.sentrysoftware.matrix.common.meta.parameter.state.LedColorStatus;
import com.sentrysoftware.matrix.common.meta.parameter.state.LedIndicator;
import com.sentrysoftware.matrix.common.meta.parameter.state.LinkStatus;
import com.sentrysoftware.matrix.common.meta.parameter.state.Status;
import com.sentrysoftware.matrix.common.meta.parameter.state.Up;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.engine.EngineConfiguration;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol;
import com.sentrysoftware.matrix.engine.protocol.SSHProtocol;
import com.sentrysoftware.matrix.engine.protocol.WBEMProtocol;
import com.sentrysoftware.matrix.engine.protocol.WMIProtocol;
import com.sentrysoftware.matrix.engine.strategy.StrategyConfig;
import com.sentrysoftware.matrix.engine.strategy.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.matrix.engine.strategy.utils.OsCommandHelper;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoring;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;
import com.sentrysoftware.matrix.model.parameter.DiscreteParam;
import com.sentrysoftware.matrix.model.parameter.IParameter;
import com.sentrysoftware.matrix.model.parameter.NumberParam;
import com.sentrysoftware.matsya.ssh.SSHClient;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.AVAILABLE_PATH_COUNT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.AVAILABLE_PATH_INFORMATION_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.BANDWIDTH_UTILIZATION_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.CHARGE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.COLOR_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DECODER_USED_TIME_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DECODER_USED_TIME_PERCENT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DUPLEX_MODE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENCODER_USED_TIME_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENCODER_USED_TIME_PERCENT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENDURANCE_REMAINING_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENERGY_USAGE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENERGY_USAGE_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ERROR_COUNT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ERROR_PERCENT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.INTRUSION_STATUS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.LAST_ERROR_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.LED_INDICATOR_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.LINK_SPEED_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.LINK_STATUS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.MOUNT_COUNT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.MOUNT_COUNT_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.MOVE_COUNT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.MOVE_COUNT_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.POWER_CONSUMPTION_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.POWER_CONSUMPTION_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.POWER_SUPPLY_POWER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PREDICTED_FAILURE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.RECEIVED_BYTES_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.RECEIVED_BYTES_RATE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SPEED_MBITS_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SPEED_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SPEED_PERCENT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.STARTING_ERROR_COUNT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.STATUS_INFORMATION_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.STATUS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TEMPERATURE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TIME_LEFT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TIME_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TOTAL_PACKETS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TRANSMITTED_BYTES_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TRANSMITTED_BYTES_RATE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TRANSMITTED_PACKETS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TRANSMITTED_PACKETS_RATE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.UNALLOCATED_SPACE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.UNMOUNT_COUNT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.UNMOUNT_COUNT_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.USAGE_REPORT_RECEIVED_BYTES_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.USAGE_REPORT_TRANSMITTED_PACKETS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.USED_CAPACITY_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.USED_PERCENT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.USED_TIME_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.USED_TIME_PERCENT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.USED_WATTS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.VOLTAGE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ZERO_BUFFER_CREDIT_COUNT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ZERO_BUFFER_CREDIT_PERCENT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.AVAILABLE_PATH_WARNING;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.MAX_AVAILABLE_PATH_COUNT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SNMP_UP_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.WBEM_UP_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SSH_UP_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.WMI_UP_PARAMETER;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ECHO_SSH_UP_TEST;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SSH_UP_TEST_RESPONSE;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;

@ExtendWith(MockitoExtension.class)
class MonitorCollectVisitorTest {

	private static final String PARENT_ID = "myConnecctor1.connector_monitor_ecs1-01_parent";
	private static final String POWER_CONSUMPTION = "150";
	private static final String OK_RAW_STATUS = "OK";
	private static final String OPERABLE = "Operable";
	private static final String CHARGE = "39";
	private static final String TIME_LEFT = "60";
	private static final String UNALLOCATED_SPACE = "10737418240";
	private static final String VALUETABLE_COLUMN_1 = "Valuetable.Column(1)";
	private static final String VALUETABLE_COLUMN_2 = "Valuetable.Column(2)";
	private static final String VALUETABLE_COLUMN_3 = "Valuetable.Column(3)";
	private static final String VALUETABLE_COLUMN_4 = "Valuetable.Column(4)";
	private static final String VALUETABLE_COLUMN_5 = "Valuetable.Column(5)";
	private static final String VALUETABLE_COLUMN_6 = "Valuetable.Column(6)";
	private static final String MONITOR_DEVICE_ID = "1.1";
	private static final String MONITOR_ID = "myConnecctor1.connector_monitor_ecs1-01_1.1";
	private static final String TARGET_ID = "target-id";
	private static final String MY_CONNECTOR_NAME = "myConnecctor";
	private static final String VALUE_TABLE = "MonitorType.Collect.Source(1)";
	private static final String DEVICE_ID = "deviceId";
	private static final String VOLTAGE = "50000";
	private static final String VOLTAGE_LOW = "-200000";
	private static final String VOLTAGE_HIGH = "460000";
	private static final String MEMORY_LAST_ERROR = "error 1234";
	private static final String TEMPERATURE = "20.0";
	private static final String TEMPERATURE_TOO_LOW = "-101.0";
	private static final String TEMPERATURE_TOO_HIGH = "201.0";
	private static final String ENDURANCE_REMAINING = "10.0";
	private static final String ENDURANCE_REMAINING_TOO_LOW = "-10.0";
	private static final String ENDURANCE_REMAINING_TOO_HIGH = "110.0";

	private static final Long collectTime = new Date().getTime();

	@Mock
	private StrategyConfig strategyConfig;

	@Mock
	private MatsyaClientsExecutor matsyaClientsExecutor;

	@Mock
	private SSHClient sshClient;

	private static final Map<String, String> mapping = Map.of(
		DEVICE_ID, VALUETABLE_COLUMN_1,
		STATUS_PARAMETER, VALUETABLE_COLUMN_2,
		STATUS_INFORMATION_PARAMETER, VALUETABLE_COLUMN_3,
		INTRUSION_STATUS_PARAMETER, VALUETABLE_COLUMN_4,
		POWER_CONSUMPTION_PARAMETER, VALUETABLE_COLUMN_5);

	private static final List<String> row = Arrays.asList(MONITOR_DEVICE_ID,
		OK_RAW_STATUS,
		OPERABLE,
		OK_RAW_STATUS,
		POWER_CONSUMPTION);

	private static final IParameter powerConsumptionParam = NumberParam
		.builder()
		.name(POWER_CONSUMPTION_PARAMETER)
		.collectTime(collectTime)
		.unit(POWER_CONSUMPTION_PARAMETER_UNIT)
		.value(Double.parseDouble(POWER_CONSUMPTION))
		.rawValue(Double.parseDouble(POWER_CONSUMPTION))
		.build();

	private static final IParameter statusParam = DiscreteParam
		.builder()
		.name(STATUS_PARAMETER)
		.collectTime(collectTime)
		.state(Status.OK)
		.build();

	private static final IParameter snmpUpParam = DiscreteParam
			.builder()
			.name(SNMP_UP_PARAMETER)
			.collectTime(collectTime)
			.state(Up.UP)
			.build();

	private static final IParameter snmpDownParam = DiscreteParam
			.builder()
			.name(SNMP_UP_PARAMETER)
			.collectTime(collectTime)
			.state(Up.DOWN)
			.build();

	private static final IParameter wbemUpParam = DiscreteParam
			.builder()
			.name(WBEM_UP_PARAMETER)
			.collectTime(collectTime)
			.state(Up.UP)
			.build();

	private static final IParameter wbemDownParam = DiscreteParam
			.builder()
			.name(WBEM_UP_PARAMETER)
			.collectTime(collectTime)
			.state(Up.DOWN)
			.build();

	private static final IParameter sshUpParam = DiscreteParam
			.builder()
			.name(SSH_UP_PARAMETER)
			.collectTime(collectTime)
			.state(Up.UP)
			.build();

	private static final IParameter sshDownParam = DiscreteParam
			.builder()
			.name(SSH_UP_PARAMETER)
			.collectTime(collectTime)
			.state(Up.DOWN)
			.build();

	private static final IParameter wmiUpParam = DiscreteParam
			.builder()
			.name(WMI_UP_PARAMETER)
			.collectTime(collectTime)
			.state(Up.UP)
			.build();

	private static final IParameter wmiDownParam = DiscreteParam
			.builder()
			.name(WMI_UP_PARAMETER)
			.collectTime(collectTime)
			.state(Up.DOWN)
			.build();

	@Test
	void testVisitConcreteConnector() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).monitorType(MonitorType.CONNECTOR).build();
		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		assertDoesNotThrow(() -> monitorCollectVisitor.visit(new MetaConnector()));
	}

	@Test
	void testVisitTarget() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).monitorType(MonitorType.TARGET).build();
		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);
		monitorCollectVisitor.getMonitorCollectInfo().setEngineConfiguration(EngineConfiguration.builder().build());

		assertDoesNotThrow(() -> monitorCollectVisitor.visit(new Target()));
	}

	@Test
	void testVisitTargetNoProtocol() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder()
				.id(MONITOR_ID)
				.monitorType(MonitorType.TARGET)
				.build();

		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.getMonitorCollectInfo().setEngineConfiguration(EngineConfiguration.builder().build());

		monitorCollectVisitor.getMonitorCollectInfo().setMatsyaClientsExecutor(matsyaClientsExecutor);
		monitorCollectVisitor.visit(new Target());

		assertNull(monitor.getParameters().get(SNMP_UP_PARAMETER));
		assertNull(monitor.getParameters().get(SSH_UP_PARAMETER));
		assertNull(monitor.getParameters().get(WMI_UP_PARAMETER));
		assertNull(monitor.getParameters().get(WBEM_UP_PARAMETER));

	}

	@Test
	void testVisitTargetSnmpUp() throws InterruptedException, ExecutionException, TimeoutException {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder()
				.id(MONITOR_ID)
				.monitorType(MonitorType.TARGET)
				.build();

		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.getMonitorCollectInfo().setEngineConfiguration(
				EngineConfiguration.builder()
						.protocolConfigurations(Collections.singletonMap(SNMPProtocol.class, new SNMPProtocol()))
						.build());

		final String snmpResult = "SNMP UP TEST Success";

		doReturn(snmpResult).when(matsyaClientsExecutor)
				.executeSNMPGetNext(eq("1.3.6.1"), any(), any(), anyBoolean());

		monitorCollectVisitor.getMonitorCollectInfo().setMatsyaClientsExecutor(matsyaClientsExecutor);
		monitorCollectVisitor.visit(new Target());

		final IParameter actual = monitor.getParameters().get(SNMP_UP_PARAMETER);
		assertEquals(snmpUpParam, actual);
	}

	@Test
	void testVisitTargetSnmpDown() throws InterruptedException, ExecutionException, TimeoutException {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder()
				.id(MONITOR_ID)
				.monitorType(MonitorType.TARGET)
				.build();

		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.getMonitorCollectInfo().setEngineConfiguration(
				EngineConfiguration.builder()
						.protocolConfigurations(Collections.singletonMap(SNMPProtocol.class, new SNMPProtocol()))
						.build());

		doReturn(null).when(matsyaClientsExecutor)
				.executeSNMPGetNext(eq("1.3.6.1"), any(), any(), anyBoolean());

		monitorCollectVisitor.getMonitorCollectInfo().setMatsyaClientsExecutor(matsyaClientsExecutor);
		monitorCollectVisitor.visit(new Target());

		final IParameter actual = monitor.getParameters().get(SNMP_UP_PARAMETER);
		assertEquals(snmpDownParam, actual);
	}

	@Test
	void testVisitTargetWbemUp() throws MatsyaException {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder()
				.id(MONITOR_ID)
				.monitorType(MonitorType.TARGET)
				.build();

		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.getMonitorCollectInfo().setEngineConfiguration(
				EngineConfiguration.builder()
						.protocolConfigurations(Collections.singletonMap(WBEMProtocol.class, new WBEMProtocol()))
						.build());

		List<List<String>> wbemResult = Collections.singletonList(Collections.singletonList("Success"));

		doReturn(wbemResult).when(matsyaClientsExecutor)
				.executeWbem(any(), any(), eq("SELECT Name FROM CIM_NameSpace"), any());

		monitorCollectVisitor.getMonitorCollectInfo().setMatsyaClientsExecutor(matsyaClientsExecutor);
		monitorCollectVisitor.visit(new Target());

		final IParameter actual = monitor.getParameters().get(WBEM_UP_PARAMETER);
		assertEquals(wbemUpParam, actual);
	}

	@Test
	void testVisitTargetWbemDown() throws MatsyaException {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder()
				.id(MONITOR_ID)
				.monitorType(MonitorType.TARGET)
				.build();

		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.getMonitorCollectInfo().setEngineConfiguration(
				EngineConfiguration.builder()
						.protocolConfigurations(Collections.singletonMap(WBEMProtocol.class, new WBEMProtocol()))
						.build());

		Mockito.when(matsyaClientsExecutor.executeWbem(any(), any(), eq("SELECT Name FROM CIM_NameSpace"), any()))
				.thenReturn(null);

		monitorCollectVisitor.getMonitorCollectInfo().setMatsyaClientsExecutor(matsyaClientsExecutor);
		monitorCollectVisitor.visit(new Target());

		final IParameter actual = monitor.getParameters().get(WBEM_UP_PARAMETER);
		assertEquals(wbemDownParam, actual);
	}

	@Test
	void testVisitTargetWmiUp() throws MatsyaException {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder()
				.id(MONITOR_ID)
				.monitorType(MonitorType.TARGET)
				.build();

		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.getMonitorCollectInfo().setEngineConfiguration(
				EngineConfiguration.builder()
						.protocolConfigurations(Collections.singletonMap(WMIProtocol.class, new WMIProtocol()))
						.build());

		List<List<String>> wmiResult = Collections.singletonList(Collections.singletonList("Success"));

		Mockito.when(matsyaClientsExecutor.executeWmi(any(), any(), eq("Select Name FROM Win32_ComputerSystem"),
				eq("root\\cimv2"))).thenReturn(wmiResult);

		monitorCollectVisitor.getMonitorCollectInfo().setMatsyaClientsExecutor(matsyaClientsExecutor);
		monitorCollectVisitor.visit(new Target());

		final IParameter actual = monitor.getParameters().get(WMI_UP_PARAMETER);
		assertEquals(wmiUpParam, actual);
	}

	@Test
	void testVisitTargetWmiDown() throws MatsyaException {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder()
				.id(MONITOR_ID)
				.monitorType(MonitorType.TARGET)
				.build();

		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.getMonitorCollectInfo().setEngineConfiguration(
				EngineConfiguration.builder()
						.protocolConfigurations(Collections.singletonMap(WMIProtocol.class, new WMIProtocol()))
						.build());

		Mockito.when(matsyaClientsExecutor.executeWmi(any(), any(), eq("Select Name FROM Win32_ComputerSystem"),
				eq("root\\cimv2"))).thenReturn(null);

		monitorCollectVisitor.getMonitorCollectInfo().setMatsyaClientsExecutor(matsyaClientsExecutor);
		monitorCollectVisitor.visit(new Target());

		final IParameter actual = monitor.getParameters().get(WMI_UP_PARAMETER);
		assertEquals(wmiDownParam, actual);
	}

	@Test
	void testVisitTargetSshUp() throws MatsyaException {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder()
				.id(MONITOR_ID)
				.monitorType(MonitorType.TARGET)
				.build();

		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);
		final SSHProtocol ssh = SSHProtocol.builder().username("username").timeout(30L).build();

		monitorCollectVisitor.getMonitorCollectInfo().setHostname("localhost");
		monitorCollectVisitor.getMonitorCollectInfo().setEngineConfiguration(
				EngineConfiguration.builder()
						.protocolConfigurations(Collections.singletonMap(SSHProtocol.class, ssh)).build());

		try (MockedStatic<OsCommandHelper> oscmd = mockStatic(OsCommandHelper.class)) {

			hostMonitoring.setLocalhost(true);

			oscmd.when(() -> OsCommandHelper.runSshCommand(ECHO_SSH_UP_TEST,
					monitorCollectVisitor.getMonitorCollectInfo().getHostname(), ssh, Math.toIntExact(ssh.getTimeout()),
					null, null)).thenReturn(SSH_UP_TEST_RESPONSE);

		monitorCollectVisitor.visit(new Target());

		final IParameter actual = monitor.getParameters().get(SSH_UP_PARAMETER);
		assertEquals(sshUpParam, actual);
	}
	}

	@Test
	void testVisitTargetSshDown() throws MatsyaException {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder()
				.id(MONITOR_ID)
				.monitorType(MonitorType.TARGET)
				.build();

		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);
		final SSHProtocol ssh = SSHProtocol.builder().username("username").timeout(30L).build();

		monitorCollectVisitor.getMonitorCollectInfo().setHostname("localhost");
		monitorCollectVisitor.getMonitorCollectInfo().setEngineConfiguration(
				EngineConfiguration.builder()
						.protocolConfigurations(Collections.singletonMap(SSHProtocol.class, ssh)).build());

		try (MockedStatic<OsCommandHelper> oscmd = mockStatic(OsCommandHelper.class)) {

			hostMonitoring.setLocalhost(true);

			oscmd.when(() -> OsCommandHelper.runSshCommand(ECHO_SSH_UP_TEST,
					monitorCollectVisitor.getMonitorCollectInfo().getHostname(), ssh, Math.toIntExact(ssh.getTimeout()),
					null, null)).thenReturn(null);

		monitorCollectVisitor.visit(new Target());

		final IParameter actual = monitor.getParameters().get(SSH_UP_PARAMETER);
		assertEquals(sshDownParam, actual);
	}
	}

	@Test
	void testVisitBattery() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).monitorType(MonitorType.BATTERY).build();
		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.visit(new Battery());

		final IParameter actual = monitor.getParameters().get(STATUS_PARAMETER);

		assertEquals(statusParam, actual);
	}

	@Test
	void testVisitBlade() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).monitorType(MonitorType.BLADE).build();
		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.visit(new Blade());

		final IParameter actual = monitor.getParameters().get(STATUS_PARAMETER);

		assertEquals(statusParam, actual);
	}

	@Test
	void testVisitCpu() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).monitorType(MonitorType.CPU).build();
		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.visit(new Cpu());

		final IParameter actual = monitor.getParameters().get(STATUS_PARAMETER);

		assertEquals(statusParam, actual);
	}

	@Test
	void testVisitCpuCore() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).monitorType(MonitorType.CPU_CORE).build();
		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.visit(new CpuCore());

		final IParameter actual = monitor.getParameters().get(STATUS_PARAMETER);

		assertEquals(statusParam, actual);
	}

	@Test
	void testVisitDiskController() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).monitorType(MonitorType.DISK_CONTROLLER).build();
		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.visit(new DiskController());

		final IParameter actual = monitor.getParameters().get(STATUS_PARAMETER);

		assertEquals(statusParam, actual);

	}

	@Test
	void testVisitEnclosure() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder()
			.id(MONITOR_ID)
			.monitorType(MonitorType.ENCLOSURE)
			.build();
		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.visit(new Enclosure());

		final IParameter expected = DiscreteParam
			.builder()
			.name(STATUS_PARAMETER)
			.collectTime(collectTime)
			.state(Status.OK)
			.build();

		final IParameter actual = monitor.getParameters().get(STATUS_PARAMETER);

		assertEquals(expected, actual);
	}

	@Test
	void testVisitFan() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor
			.builder()
			.id(MONITOR_ID)
			.monitorType(MonitorType.FAN)
			.build();
		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.visit(new Fan());

		final IParameter actual = monitor.getParameters().get(STATUS_PARAMETER);

		assertEquals(statusParam, actual);
	}

	@Test
	void testVisitLed() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();

		Map<String, String> customMetadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
		customMetadata.put("offstatus", "0");

		final Monitor monitor = Monitor
			.builder()
			.id(MONITOR_ID)
			.monitorType(MonitorType.LED)
			.metadata(customMetadata)
			.build();

		Map<String, String> customMapping = new HashMap<>(mapping);
		customMapping.put(STATUS_PARAMETER, VALUETABLE_COLUMN_6);

		List<String> customRow = new ArrayList<>(row);
		customRow.add("off");

		MonitorCollectVisitor monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				customMapping,
				monitor,
				customRow)
		);

		monitorCollectVisitor.visit(new Led());

		final IParameter expected = DiscreteParam
			.builder()
			.name(STATUS_PARAMETER)
			.collectTime(collectTime)
			.state(Status.OK)
			.build();

		final IParameter actual = monitor.getParameters().get(STATUS_PARAMETER);

		assertEquals(expected, actual);
	}

	@Test
	void testVisitLogicalDisk() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor
			.builder()
			.id(MONITOR_ID)
			.monitorType(MonitorType.LOGICAL_DISK)
			.build();
		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.visit(new LogicalDisk());

		final IParameter actual = monitor.getParameters().get(STATUS_PARAMETER);

		assertEquals(statusParam, actual);
	}

	@Test
	void testVisitLun() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).monitorType(MonitorType.LUN).build();
		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.visit(new Lun());

		final IParameter actual = monitor.getParameters().get(STATUS_PARAMETER);

		assertEquals(statusParam, actual);
	}

	@Test
	void testVisitMemory() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor
			.builder()
			.id(MONITOR_ID)
			.monitorType(MonitorType.MEMORY)
			.build();
		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.visit(new Memory());

		final IParameter actual = monitor.getParameters().get(STATUS_PARAMETER);

		assertEquals(statusParam, actual);

	}

	@Test
	void testVisitNetworkCard() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor
			.builder()
			.id(MONITOR_ID)
			.name("eth0")
			.monitorType(MonitorType.NETWORK_CARD)
			.build();
		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.visit(new NetworkCard());

		final IParameter actual = monitor.getParameters().get(STATUS_PARAMETER);

		assertEquals(statusParam, actual);
	}

	@Test
	void testVisitOtherDevice() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).monitorType(MonitorType.OTHER_DEVICE).build();
		MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.visit(new OtherDevice());

		final IParameter actual = monitor.getParameters().get(STATUS_PARAMETER);

		assertEquals(statusParam, actual);

		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(POWER_CONSUMPTION_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList(POWER_CONSUMPTION))
		);
		monitorCollectVisitor.visit(new OtherDevice());
		NumberParam powerConsumptionParameter = monitor.getParameter(POWER_CONSUMPTION_PARAMETER, NumberParam.class);
		assertNotNull(powerConsumptionParameter);
		assertEquals(150.0, powerConsumptionParameter.getRawValue());
		assertEquals(150.0, powerConsumptionParameter.getValue());
	}

	@Test
	void testVisitPhysicalDisk() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder()
			.id(MONITOR_ID)
			.monitorType(MonitorType.PHYSICAL_DISK)
			.parentId(PARENT_ID)
			.name("Disk 1 (1TB)")
			.build();
		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.visit(new PhysicalDisk());

		final IParameter actual = monitor.getParameters().get(STATUS_PARAMETER);

		assertEquals(statusParam, actual);
	}

	@Test
	void testVisitPowerSupply() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor
			.builder().
				id(MONITOR_ID)
			.monitorType(MonitorType.POWER_SUPPLY)
			.build();
		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.visit(new PowerSupply());

		final IParameter actual = monitor.getParameters().get(STATUS_PARAMETER);

		assertEquals(statusParam, actual);
	}

	@Test
	void testVisitRobotics() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor
			.builder().
				id(MONITOR_ID)
			.monitorType(MonitorType.ROBOTICS)
			.build();
		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.visit(new Robotics());

		final IParameter actual = monitor.getParameters().get(STATUS_PARAMETER);

		assertEquals(statusParam, actual);
	}

	@Test
	void testVisitTapeDrive() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor
			.builder()
			.name("TapeDrive 1")
			.id(MONITOR_ID)
			.monitorType(MonitorType.TAPE_DRIVE)
			.build();
		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.visit(new TapeDrive());

		final IParameter actual = monitor.getParameters().get(STATUS_PARAMETER);

		assertEquals(statusParam, actual);
	}

	@Test
	void testVisitTemperature() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).monitorType(MonitorType.TEMPERATURE).build();
		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.visit(new Temperature());

		final IParameter actual = monitor.getParameters().get(STATUS_PARAMETER);

		assertEquals(statusParam, actual);
	}

	@Test
	void testVisitVoltage() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).monitorType(MonitorType.VOLTAGE).build();
		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.visit(new Voltage());

		final IParameter actual = monitor.getParameters().get(STATUS_PARAMETER);

		assertEquals(statusParam, actual);
	}

	@Test
	void testCollectDiscreteParameterStatus() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).build();
		final MonitorCollectVisitor monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				mapping,
				monitor,
				Arrays.asList(MONITOR_DEVICE_ID,
					OK_RAW_STATUS,
					"",
					OK_RAW_STATUS,
					POWER_CONSUMPTION))
		);

		monitorCollectVisitor.collectDiscreteParameter(
			MonitorType.ENCLOSURE,
			STATUS_PARAMETER,
			(DiscreteParamType) Enclosure.STATUS.getType()
		);

		final Map<String, IParameter> parameters = monitor.getParameters();
		final DiscreteParam expected = DiscreteParam
			.builder()
			.name(STATUS_PARAMETER)
			.collectTime(collectTime)
			.state(Status.OK)
			.build();

		final IParameter actual = parameters.get(STATUS_PARAMETER);

		assertEquals(expected, actual);

	}

	@Test
	void testCollectDiscreteParameterStatusButCannotExtractValue() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).build();
		final MonitorCollectVisitor monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				mapping,
				monitor,
				Collections.emptyList())
		);

		monitorCollectVisitor.collectDiscreteParameter(
			MonitorType.ENCLOSURE,
			STATUS_PARAMETER,
			(DiscreteParamType) Enclosure.STATUS.getType()
		);


		final Map<String, IParameter> parameters = monitor.getParameters();
		assertTrue(parameters.isEmpty());

	}

	private static MonitorCollectVisitor buildMonitorCollectVisitor(final IHostMonitoring hostMonitoring,
																	final Monitor monitor) {
		return new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				mapping,
				monitor,
				row)
		);
	}

	private static MonitorCollectVisitor buildMonitorCollectVisitor(final IHostMonitoring hostMonitoring,
																	final Monitor monitor,
																	final Map<String, String> mapping,
																	final List<String> row) {
		return new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				mapping,
				monitor,
				row)
		);
	}

	@Test
	void testCollectNumberParameterIncorrectValue() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = new Monitor();
		final MonitorCollectVisitor monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				mapping,
				monitor,
				Arrays.asList(MONITOR_DEVICE_ID, OK_RAW_STATUS, OPERABLE, OK_RAW_STATUS, "NotEnergyUsageNumber"))
		);

		monitorCollectVisitor.collectNumberParameter(MonitorType.ENCLOSURE,
			POWER_CONSUMPTION_PARAMETER,
			POWER_CONSUMPTION_PARAMETER_UNIT);

		final Map<String, IParameter> parameters = monitor.getParameters();

		assertTrue(parameters.isEmpty());

	}

	@Test
	void testCollectNumberParameterValueNotFound() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = new Monitor();
		final MonitorCollectVisitor monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Collections.emptyMap(),
				monitor,
				row)
		);

		monitorCollectVisitor.collectNumberParameter(MonitorType.ENCLOSURE,
			POWER_CONSUMPTION_PARAMETER,
			POWER_CONSUMPTION_PARAMETER_UNIT);

		final Map<String, IParameter> parameters = monitor.getParameters();

		assertTrue(parameters.isEmpty());

	}

	@Test
	void testCollectNumberParameter() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = new Monitor();
		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.collectNumberParameter(MonitorType.ENCLOSURE,
			POWER_CONSUMPTION_PARAMETER,
			POWER_CONSUMPTION_PARAMETER_UNIT);

		final Map<String, IParameter> parameters = monitor.getParameters();
		assertFalse(parameters.isEmpty());

		final IParameter actual = parameters.get(POWER_CONSUMPTION_PARAMETER);

		assertEquals(powerConsumptionParam, actual);

	}

	private static MonitorCollectInfo buildCollectMonitorInfo(final IHostMonitoring hostMonitoring, final Map<String, String> mapping,
															  Monitor monitor, final List<String> row) {
		return MonitorCollectInfo
			.builder()
			.collectTime(collectTime)
			.connectorName(MY_CONNECTOR_NAME)
			.hostMonitoring(hostMonitoring)
			.hostname(TARGET_ID)
			.mapping(mapping)
			.monitor(monitor)
			.row(row)
			.valueTable(VALUE_TABLE)
			.build();
	}

	private static MonitorCollectInfo buildCollectMonitorInfo(final IHostMonitoring hostMonitoring, final Map<String, String> mapping,
															  Monitor monitor, final List<String> row, Long collectTime) {
		return MonitorCollectInfo
			.builder()
			.collectTime(collectTime)
			.connectorName(MY_CONNECTOR_NAME)
			.hostMonitoring(hostMonitoring)
			.hostname(TARGET_ID)
			.mapping(mapping)
			.monitor(monitor)
			.row(row)
			.valueTable(VALUE_TABLE)
			.build();
	}

	@Test
	void testCollectPowerConsumptionFromEnergyUsageFirstCollect() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor
			.builder()
			.id(MONITOR_ID)
			.monitorType(MonitorType.ENCLOSURE)
			.build();
		final Map<String, String> mapping = Map.of(
			DEVICE_ID, VALUETABLE_COLUMN_1,
			ENERGY_USAGE_PARAMETER, VALUETABLE_COLUMN_2);

		final List<String> row = Arrays.asList(MONITOR_DEVICE_ID, "3138.358");

		final MonitorCollectVisitor monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring, mapping, monitor, row));

		monitorCollectVisitor.collectPowerConsumption();

		assertEquals(3138.358D,
			monitor.getParameter(ENERGY_USAGE_PARAMETER, NumberParam.class).getRawValue());
	}

	@Test
	void testCollectPowerConsumptionFromEnergyUsage() {
		final NumberParam energyUsage = NumberParam
			.builder()
			.name(ENERGY_USAGE_PARAMETER)
			.unit(ENERGY_USAGE_PARAMETER_UNIT)
			.collectTime(collectTime - (2 * 60 * 1000))
			.value(null)
			.rawValue(3138.358D)
			.build();

		energyUsage.save();

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor
			.builder()
			.id(MONITOR_ID)
			.monitorType(MonitorType.ENCLOSURE)
			.parameters(new HashMap<>(Map.of(ENERGY_USAGE_PARAMETER, energyUsage)))
			.build();
		final Map<String, String> mapping = Map.of(
			DEVICE_ID, VALUETABLE_COLUMN_1,
			ENERGY_USAGE_PARAMETER, VALUETABLE_COLUMN_2);

		final List<String> row = Arrays.asList(MONITOR_DEVICE_ID, "3138.360");

		final MonitorCollectVisitor monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring, mapping, monitor, row));

		monitorCollectVisitor.collectPowerConsumption();

		Double joules = monitor.getParameter(ENERGY_USAGE_PARAMETER, NumberParam.class).getValue();
		joules = Math.round(joules * 100000D) / 100000D;
		assertEquals(7200.0, joules);
		assertEquals(60, Math.round(monitor.getParameter(POWER_CONSUMPTION_PARAMETER, NumberParam.class).getValue()));
	}

	@Test
	void testCollectPowerConsumptionViaPowerFirstCollect() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor
			.builder()
			.id(MONITOR_ID)
			.monitorType(MonitorType.ENCLOSURE)
			.build();
		final Map<String, String> mapping = Map.of(
			DEVICE_ID, VALUETABLE_COLUMN_1,
			POWER_CONSUMPTION_PARAMETER, VALUETABLE_COLUMN_2);

		final List<String> row = Arrays.asList(MONITOR_DEVICE_ID, POWER_CONSUMPTION);

		final MonitorCollectVisitor monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring, mapping, monitor, row));

		monitorCollectVisitor.collectPowerConsumption();

		assertEquals(Double.parseDouble(POWER_CONSUMPTION),
			monitor.getParameter(POWER_CONSUMPTION_PARAMETER, NumberParam.class).getRawValue());
		assertEquals(Double.parseDouble(POWER_CONSUMPTION),
			monitor.getParameter(POWER_CONSUMPTION_PARAMETER, NumberParam.class).getValue());
	}

	@Test
	void testCollectPowerConsumptionFromPower() {
		final NumberParam powerConsumption = NumberParam
			.builder()
			.name(POWER_CONSUMPTION_PARAMETER)
			.unit(POWER_CONSUMPTION_PARAMETER)
			.collectTime(collectTime - (2 * 60 * 1000))
			.value(null)
			.rawValue(60.0)
			.build();

		powerConsumption.save();

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor
			.builder()
			.id(MONITOR_ID)
			.monitorType(MonitorType.ENCLOSURE)
			.parameters(new HashMap<>(Map.of(POWER_CONSUMPTION_PARAMETER, powerConsumption)))
			.build();
		final Map<String, String> mapping = Map.of(
			DEVICE_ID, VALUETABLE_COLUMN_1,
			POWER_CONSUMPTION_PARAMETER, VALUETABLE_COLUMN_2);

		final List<String> row = Arrays.asList(MONITOR_DEVICE_ID, "60.2");

		final MonitorCollectVisitor monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring, mapping, monitor, row));

		monitorCollectVisitor.collectPowerConsumption();

		assertEquals(60.2, monitor.getParameter(POWER_CONSUMPTION_PARAMETER, NumberParam.class).getValue());
		assertEquals(60.2, monitor.getParameter(POWER_CONSUMPTION_PARAMETER, NumberParam.class).getRawValue());

		assertEquals(7224, Math.round(monitor.getParameter(ENERGY_USAGE_PARAMETER, NumberParam.class).getValue()));
	}

	@Test
	void testCollectPowerConsumptionNoPowerCollected() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor
			.builder()
			.id(MONITOR_ID)
			.monitorType(MonitorType.ENCLOSURE)
			.build();

		final Map<String, String> mapping = Map.of(
			DEVICE_ID, VALUETABLE_COLUMN_1,
			POWER_CONSUMPTION_PARAMETER, VALUETABLE_COLUMN_2);

		final List<String> row = Arrays.asList(MONITOR_DEVICE_ID, "");

		final MonitorCollectVisitor monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring, mapping, monitor, row));

		monitorCollectVisitor.collectPowerConsumption();

		assertNull(monitor.getParameter(POWER_CONSUMPTION_PARAMETER, NumberParam.class));
		assertNull(monitor.getParameter(ENERGY_USAGE_PARAMETER, NumberParam.class));

	}

	@Test
	void testCollectPowerConsumptionPowerNegative() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor
			.builder()
			.id(MONITOR_ID)
			.monitorType(MonitorType.ENCLOSURE)
			.build();

		final Map<String, String> mapping = Map.of(
			DEVICE_ID, VALUETABLE_COLUMN_1,
			POWER_CONSUMPTION_PARAMETER, VALUETABLE_COLUMN_2);

		final List<String> row = Arrays.asList(MONITOR_DEVICE_ID, "-1");

		final MonitorCollectVisitor monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring, mapping, monitor, row));

		monitorCollectVisitor.collectPowerConsumption();

		assertNull(monitor.getParameter(POWER_CONSUMPTION_PARAMETER, NumberParam.class));
		assertNull(monitor.getParameter(ENERGY_USAGE_PARAMETER, NumberParam.class));

	}

	@Test
	void testCollectPowerConsumptionNoEnergyUsage() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor
			.builder()
			.id(MONITOR_ID)
			.monitorType(MonitorType.ENCLOSURE)
			.build();

		final Map<String, String> mapping = Map.of(
			DEVICE_ID, VALUETABLE_COLUMN_1,
			ENERGY_USAGE_PARAMETER, VALUETABLE_COLUMN_2);

		final List<String> row = Arrays.asList(MONITOR_DEVICE_ID, "");

		final MonitorCollectVisitor monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring, mapping, monitor, row));

		monitorCollectVisitor.collectPowerConsumption();

		assertNull(monitor.getParameter(POWER_CONSUMPTION_PARAMETER, NumberParam.class));
		assertNull(monitor.getParameter(ENERGY_USAGE_PARAMETER, NumberParam.class));

	}

	@Test
	void testCollectPowerConsumptionEnergyUsageNegative() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor
			.builder()
			.id(MONITOR_ID)
			.monitorType(MonitorType.ENCLOSURE)
			.build();

		final Map<String, String> mapping = Map.of(
			DEVICE_ID, VALUETABLE_COLUMN_1,
			ENERGY_USAGE_PARAMETER, VALUETABLE_COLUMN_2);

		final List<String> row = Arrays.asList(MONITOR_DEVICE_ID, "-1");

		final MonitorCollectVisitor monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring, mapping, monitor, row));

		monitorCollectVisitor.collectPowerConsumption();

		assertNull(monitor.getParameter(POWER_CONSUMPTION_PARAMETER, NumberParam.class));
		assertNull(monitor.getParameter(ENERGY_USAGE_PARAMETER, NumberParam.class));

	}

	@Test
	void testCollectBatteryCharge() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).monitorType(MonitorType.BATTERY).build();
		MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		// No charge value
		monitorCollectVisitor.collectBatteryCharge();
		NumberParam chargeParameter = monitor.getParameter(CHARGE_PARAMETER, NumberParam.class);
		assertNull(chargeParameter);

		// Charge value collected, value is lower than 100
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(CHARGE_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList(CHARGE))
		);
		monitorCollectVisitor.collectBatteryCharge();
		chargeParameter = monitor.getParameter(CHARGE_PARAMETER, NumberParam.class);
		assertNotNull(chargeParameter);
		assertEquals(39.0, chargeParameter.getRawValue());
		assertEquals(39.0, chargeParameter.getValue());

		// Charge value collected, value is greater than 100
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(CHARGE_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList("125"))
		);
		monitorCollectVisitor.collectBatteryCharge();
		chargeParameter = monitor.getParameter(CHARGE_PARAMETER, NumberParam.class);
		assertNotNull(chargeParameter);
		assertEquals(125.0, chargeParameter.getRawValue());
		assertEquals(100.0, chargeParameter.getValue());
	}

	@Test
	void testCollectBatteryTimeLeft() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).monitorType(MonitorType.BATTERY).build();
		MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		// No time left value
		monitorCollectVisitor.collectBatteryTimeLeft();
		NumberParam timeLeftParameter = monitor.getParameter(TIME_LEFT_PARAMETER, NumberParam.class);
		assertNull(timeLeftParameter);

		// Time left value collected
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(TIME_LEFT_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList(TIME_LEFT))
		);
		monitorCollectVisitor.collectBatteryTimeLeft();
		timeLeftParameter = monitor.getParameter(TIME_LEFT_PARAMETER, NumberParam.class);
		assertNotNull(timeLeftParameter);
		assertEquals(60.0, timeLeftParameter.getRawValue());
		assertEquals(3600.0, timeLeftParameter.getValue());
	}

	@Test
	void testCollectCpuCoreUsedTimePercent() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).monitorType(MonitorType.CPU_CORE).build();
		MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		// usedTimePercentRaw is null
		monitorCollectVisitor.collectCpuCoreUsedTimePercent();
		NumberParam usedTimePercentParameter = monitor.getParameter(USED_TIME_PERCENT_PARAMETER,
			NumberParam.class);
		assertNull(usedTimePercentParameter);

		// usedTimePercentRaw is not null, usedTimePercentPrevious is null
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(USED_TIME_PERCENT_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList("12"))
		);
		monitorCollectVisitor.collectCpuCoreUsedTimePercent();
		usedTimePercentParameter = monitor.getParameter(USED_TIME_PERCENT_PARAMETER, NumberParam.class);
		assertNotNull(usedTimePercentParameter);
		assertEquals(12.0, usedTimePercentParameter.getRawValue());
		assertNull(usedTimePercentParameter.getValue());

		// usedTimePercentRaw is not null, usedTimePercentPrevious is not null, collectTimePrevious is null
		usedTimePercentParameter.save();
		usedTimePercentParameter.setPreviousCollectTime(null);
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(USED_TIME_PERCENT_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList("42"))
		);
		monitorCollectVisitor.collectCpuCoreUsedTimePercent();
		long collectTime = monitorCollectVisitor.getMonitorCollectInfo().getCollectTime();
		usedTimePercentParameter = monitor.getParameter(USED_TIME_PERCENT_PARAMETER, NumberParam.class);
		assertNotNull(usedTimePercentParameter);
		assertNotNull(usedTimePercentParameter.getRawValue());
		assertNull(usedTimePercentParameter.getValue());
		// The previous collect time is not changed, means the parameter is not collected
		assertEquals(collectTime, usedTimePercentParameter.getCollectTime());

		// usedTimePercentRaw is not null, usedTimePercentPrevious is not null, collectTimePrevious is not null
		// timeDeltaInSeconds == 0.0
		usedTimePercentParameter = NumberParam.builder().name(USED_TIME_PERCENT_PARAMETER).build();
		usedTimePercentParameter.setPreviousRawValue(12.0);
		usedTimePercentParameter.setPreviousCollectTime(System.currentTimeMillis() - 120000L); // 2 minutes ago
		monitor.addParameter(usedTimePercentParameter);
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(USED_TIME_PERCENT_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList("42"),
				usedTimePercentParameter.getPreviousCollectTime())
		);
		monitorCollectVisitor.collectCpuCoreUsedTimePercent();
		usedTimePercentParameter = monitor.getParameter(USED_TIME_PERCENT_PARAMETER, NumberParam.class);
		assertNotNull(usedTimePercentParameter);
		assertNull(usedTimePercentParameter.getRawValue());
		assertNull(usedTimePercentParameter.getValue());

		// OK
		usedTimePercentParameter = NumberParam.builder().name(USED_TIME_PERCENT_PARAMETER).build();
		usedTimePercentParameter.setPreviousRawValue(12.0);
		usedTimePercentParameter.setPreviousCollectTime(System.currentTimeMillis() - 120000L); // 2 minutes ago
		monitor.addParameter(usedTimePercentParameter);
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(USED_TIME_PERCENT_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList("42"),
				usedTimePercentParameter.getPreviousCollectTime() + 120000L)
		);
		monitorCollectVisitor.collectCpuCoreUsedTimePercent();
		usedTimePercentParameter = monitor.getParameter(USED_TIME_PERCENT_PARAMETER, NumberParam.class);
		assertNotNull(usedTimePercentParameter);
		assertEquals(42.0, usedTimePercentParameter.getRawValue());
		assertEquals(25.0, usedTimePercentParameter.getValue());
	}

	@Test
	void testCollectVoltage() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).monitorType(MonitorType.VOLTAGE).build();
		MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		// No voltage value
		monitorCollectVisitor.collectVoltage();
		NumberParam voltageParameter = monitor.getParameter(VOLTAGE_PARAMETER, NumberParam.class);
		assertNull(voltageParameter);

		// Voltage value collected
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(VOLTAGE_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList(VOLTAGE))
		);
		monitorCollectVisitor.collectVoltage();
		voltageParameter = monitor.getParameter(VOLTAGE_PARAMETER, NumberParam.class);
		assertNotNull(voltageParameter);
		assertEquals(50000.0, voltageParameter.getRawValue());
		assertEquals(50000.0, voltageParameter.getValue());

		// Voltage value collected < -100000
		monitor.setParameters(new HashMap<>());
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(VOLTAGE_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList(VOLTAGE_LOW))
		);
		monitorCollectVisitor.collectVoltage();
		voltageParameter = monitor.getParameter(VOLTAGE_PARAMETER, NumberParam.class);
		assertNull(voltageParameter);

		// Voltage value collected > 450000
		monitor.setParameters(new HashMap<>());
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(VOLTAGE_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList(VOLTAGE_HIGH))
		);
		monitorCollectVisitor.collectVoltage();
		voltageParameter = monitor.getParameter(VOLTAGE_PARAMETER, NumberParam.class);
		assertNull(voltageParameter);
	}

	@Test
	void testCollectErrorCount() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).monitorType(MonitorType.TAPE_DRIVE).build();
		MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		// No error count set
		monitorCollectVisitor.collectErrorCount(ERROR_COUNT_PARAMETER, STARTING_ERROR_COUNT_PARAMETER);
		NumberParam errorCountParameter = monitor.getParameter(ERROR_COUNT_PARAMETER, NumberParam.class);
		NumberParam startingErrorCountParameter = monitor.getParameter(STARTING_ERROR_COUNT_PARAMETER, NumberParam.class);
		assertNull(errorCountParameter);
		assertNull(startingErrorCountParameter);

		// Error count value collected for the first time
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(ERROR_COUNT_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList("10"))
		);

		monitorCollectVisitor.collectErrorCount(ERROR_COUNT_PARAMETER, STARTING_ERROR_COUNT_PARAMETER);
		errorCountParameter = monitor.getParameter(ERROR_COUNT_PARAMETER, NumberParam.class);
		startingErrorCountParameter = monitor.getParameter(STARTING_ERROR_COUNT_PARAMETER, NumberParam.class);
		assertEquals(0.0, errorCountParameter.getRawValue());
		assertEquals(0.0, errorCountParameter.getValue());
		assertEquals(10.0, startingErrorCountParameter.getValue());

		// Error count value collected with an increased error count
		startingErrorCountParameter = NumberParam.builder().name(STARTING_ERROR_COUNT_PARAMETER).build();
		startingErrorCountParameter.setPreviousRawValue(15.0);
		monitor.addParameter(startingErrorCountParameter);
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(ERROR_COUNT_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList("25"))
		);

		monitorCollectVisitor.collectErrorCount(ERROR_COUNT_PARAMETER, STARTING_ERROR_COUNT_PARAMETER);
		errorCountParameter = monitor.getParameter(ERROR_COUNT_PARAMETER, NumberParam.class);
		startingErrorCountParameter = monitor.getParameter(STARTING_ERROR_COUNT_PARAMETER, NumberParam.class);
		assertEquals(10.0, errorCountParameter.getRawValue());
		assertEquals(10.0, errorCountParameter.getValue());
		assertEquals(15.0, startingErrorCountParameter.getRawValue());
		assertEquals(15.0, startingErrorCountParameter.getValue());

	}

	@Test
	void testCollectIncrementalCount() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).monitorType(MonitorType.TAPE_DRIVE).build();
		MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		// No mount count set
		monitorCollectVisitor.collectIncrementCount(MOUNT_COUNT_PARAMETER, MOUNT_COUNT_PARAMETER_UNIT);
		NumberParam mountCountParameter = monitor.getParameter(MOUNT_COUNT_PARAMETER, NumberParam.class);
		assertNull(mountCountParameter);

		// Mount count value collected
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(MOUNT_COUNT_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList("10"))
		);

		monitorCollectVisitor.collectIncrementCount(MOUNT_COUNT_PARAMETER, MOUNT_COUNT_PARAMETER_UNIT);
		mountCountParameter = monitor.getParameter(MOUNT_COUNT_PARAMETER, NumberParam.class);
		assertEquals(10.0, mountCountParameter.getRawValue());
		assertEquals(0.0, mountCountParameter.getValue());

		// Both current and previous mount counts are set (previous = 12, current = 20)
		mountCountParameter = NumberParam.builder().name(MOUNT_COUNT_PARAMETER).build();
		mountCountParameter.setPreviousRawValue(12.0);
		monitor.addParameter(mountCountParameter);
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(MOUNT_COUNT_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList("20"))
		);
		monitorCollectVisitor.collectIncrementCount(MOUNT_COUNT_PARAMETER, MOUNT_COUNT_PARAMETER_UNIT);
		mountCountParameter = monitor.getParameter(MOUNT_COUNT_PARAMETER, NumberParam.class);
		assertEquals(20.0, mountCountParameter.getRawValue());
		assertEquals(8.0, mountCountParameter.getValue());

		// Both current and previous mount counts are set (previous = 32, current = 20)
		mountCountParameter = NumberParam.builder().name(MOUNT_COUNT_PARAMETER).build();
		mountCountParameter.setPreviousRawValue(32.0);
		monitor.addParameter(mountCountParameter);
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(MOUNT_COUNT_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList("20"))
		);
		monitorCollectVisitor.collectIncrementCount(MOUNT_COUNT_PARAMETER, MOUNT_COUNT_PARAMETER_UNIT);
		mountCountParameter = monitor.getParameter(MOUNT_COUNT_PARAMETER, NumberParam.class);
		assertEquals(20.0, mountCountParameter.getRawValue());
		assertEquals(0.0, mountCountParameter.getValue());
	}

	@Test
	void testCollectPowerSupplyUsedCapacity() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).monitorType(MonitorType.POWER_SUPPLY).build();
		MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		// No parameter set
		monitorCollectVisitor.collectPowerSupplyUsedCapacity();
		NumberParam usedCapacityParameter = monitor.getParameter(USED_CAPACITY_PARAMETER, NumberParam.class);
		assertNull(usedCapacityParameter);

		// Used capacity set
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(USED_PERCENT_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList("10"))
		);

		monitorCollectVisitor.collectPowerSupplyUsedCapacity();
		usedCapacityParameter = monitor.getParameter(USED_CAPACITY_PARAMETER, NumberParam.class);
		assertEquals(10.0, usedCapacityParameter.getValue());

		// No used capacity, derive from used & total power
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map
					.of(USED_WATTS_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Arrays.asList("25"))
		);

		monitor.setMetadata(Map.of(POWER_SUPPLY_POWER, "50"));

		monitorCollectVisitor.collectPowerSupplyUsedCapacity();
		usedCapacityParameter = monitor.getParameter(USED_CAPACITY_PARAMETER, NumberParam.class);
		assertNull(usedCapacityParameter.getRawValue());
		assertEquals(50.0, usedCapacityParameter.getValue());
	}

	@Test
	void testCollectMemoryStatusInformationWithLastError() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).monitorType(MonitorType.MEMORY).build();
		final MonitorCollectVisitor monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(
				hostMonitoring,
				Map.of(
					DEVICE_ID, VALUETABLE_COLUMN_1,
					STATUS_PARAMETER, VALUETABLE_COLUMN_2,
					LAST_ERROR_PARAMETER, VALUETABLE_COLUMN_3),
				monitor,
				Arrays.asList(MONITOR_DEVICE_ID,
					OK_RAW_STATUS,
					MEMORY_LAST_ERROR)
			));

		monitorCollectVisitor.visit(new Memory());

		final Map<String, IParameter> parameters = monitor.getParameters();
		final DiscreteParam expected = DiscreteParam
			.builder()
			.name(STATUS_PARAMETER)
			.collectTime(collectTime)
			.state(Status.OK)
			.build();

		final IParameter actual = parameters.get(STATUS_PARAMETER);

		assertEquals(expected, actual);
	}

	@Test
	void testCollectLogicalDiskUnallocatedSpace() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).monitorType(MonitorType.LOGICAL_DISK).build();
		MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		// No unallocated space value
		monitorCollectVisitor.collectLogicalDiskUnallocatedSpace();
		NumberParam unallocatedSpaceParameter = monitor.getParameter(UNALLOCATED_SPACE_PARAMETER, NumberParam.class);
		assertNull(unallocatedSpaceParameter);

		// Unallocated space value collected
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(UNALLOCATED_SPACE_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList(UNALLOCATED_SPACE))
		);
		monitorCollectVisitor.collectLogicalDiskUnallocatedSpace();
		unallocatedSpaceParameter = monitor.getParameter(UNALLOCATED_SPACE_PARAMETER, NumberParam.class);
		assertNotNull(unallocatedSpaceParameter);
		assertEquals(10737418240.0, unallocatedSpaceParameter.getRawValue());
		assertEquals(10.0, unallocatedSpaceParameter.getValue());
	}

	@Test
	void testEstimateFanPowerConsumption() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).monitorType(MonitorType.FAN).build();
		MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		// No fan speed, no fan speed percent -> 5W
		monitorCollectVisitor.estimateFanPowerConsumption();
		NumberParam powerConsumptionParameter = monitor.getParameter(POWER_CONSUMPTION_PARAMETER, NumberParam.class);
		assertNotNull(powerConsumptionParameter);
		assertEquals(5.0, powerConsumptionParameter.getValue());

		// Fan speed set
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(SPEED_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList("7000"))
		);
		monitorCollectVisitor.estimateFanPowerConsumption();
		powerConsumptionParameter = monitor.getParameter(POWER_CONSUMPTION_PARAMETER, NumberParam.class);
		assertNotNull(powerConsumptionParameter);
		assertEquals(7.0, powerConsumptionParameter.getValue());

		// No fan speed, but fan speed percent set
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(SPEED_PERCENT_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList("80"))
		);
		monitorCollectVisitor.estimateFanPowerConsumption();
		powerConsumptionParameter = monitor.getParameter(POWER_CONSUMPTION_PARAMETER, NumberParam.class);
		assertNotNull(powerConsumptionParameter);
		assertEquals(4.0, powerConsumptionParameter.getValue());
	}

	@Test
	void testCollectTemperature() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).monitorType(MonitorType.TEMPERATURE).build();
		MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		// No temperature value
		monitorCollectVisitor.collectTemperature();
		NumberParam temperatureParameter = monitor.getParameter(TEMPERATURE_PARAMETER, NumberParam.class);
		assertNull(temperatureParameter);

		// Temperature < -100°
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(TEMPERATURE_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList(TEMPERATURE_TOO_LOW))
		);

		monitorCollectVisitor.collectTemperature();
		temperatureParameter = monitor.getParameter(TEMPERATURE_PARAMETER, NumberParam.class);
		assertNull(temperatureParameter);

		// Temperature > 200°
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(TEMPERATURE_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList(TEMPERATURE_TOO_HIGH))
		);

		monitorCollectVisitor.collectTemperature();
		temperatureParameter = monitor.getParameter(TEMPERATURE_PARAMETER, NumberParam.class);
		assertNull(temperatureParameter);

		// Temperature value collected
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(TEMPERATURE_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList(TEMPERATURE))
		);

		monitorCollectVisitor.collectTemperature();
		temperatureParameter = monitor.getParameter(TEMPERATURE_PARAMETER, NumberParam.class);

		assertNotNull(temperatureParameter);
		assertEquals(20.0, temperatureParameter.getRawValue());
		assertEquals(20.0, temperatureParameter.getValue());
	}

	@Test
	void testEstimateNetworkCardPowerConsumptionVirtOrWan() {
		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			// WAN
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).name("wan 01").monitorType(MonitorType.NETWORK_CARD).build();
			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

			monitorCollectVisitor.estimateNetworkCardPowerConsumption();

			assertEquals(0.0, CollectHelper.getNumberParamValue(monitor, POWER_CONSUMPTION_PARAMETER));
		}

		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();

			// Virtual interface
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).name("Virtual itf:01").monitorType(MonitorType.NETWORK_CARD).build();
			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

			monitorCollectVisitor.estimateNetworkCardPowerConsumption();

			assertEquals(0.0, CollectHelper.getNumberParamValue(monitor, POWER_CONSUMPTION_PARAMETER));
		}

	}

	@Test
	void testEstimateNetworkCardPowerConsumptionDown() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).name("FC 01").monitorType(MonitorType.NETWORK_CARD).build();

		// Link status unplugged
		monitor.addParameter(
			DiscreteParam
				.builder()
				.name(LINK_STATUS_PARAMETER)
				.state(LinkStatus.UNPLUGGED)
				.collectTime(collectTime)
				.build()
		);

		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.estimateNetworkCardPowerConsumption();

		assertEquals(1.0, CollectHelper.getNumberParamValue(monitor, POWER_CONSUMPTION_PARAMETER));
	}

	@Test
	void testEstimateNetworkCardPowerConsumptionFromBandwidthUtilization() {
		{
			// Bandwith utilization + Link Speed

			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).name("FC 01").monitorType(MonitorType.NETWORK_CARD).build();


			CollectHelper.updateNumberParameter(
				monitor,
				BANDWIDTH_UTILIZATION_PARAMETER,
				"percent",
				collectTime,
				60.0,
				60.0
			);
			CollectHelper.updateNumberParameter(
				monitor,
				LINK_SPEED_PARAMETER,
				SPEED_MBITS_PARAMETER_UNIT,
				collectTime,
				300.0,
				300.0
			);

			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

			monitorCollectVisitor.estimateNetworkCardPowerConsumption();

			assertEquals(9.91, CollectHelper.getNumberParamValue(monitor, POWER_CONSUMPTION_PARAMETER));
		}

		{
			// Bandwith utilization with Link Speed < 10

			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).name("FC 01").monitorType(MonitorType.NETWORK_CARD).build();


			CollectHelper.updateNumberParameter(
				monitor,
				BANDWIDTH_UTILIZATION_PARAMETER,
				"percent",
				collectTime,
				60.0,
				60.0
			);
			CollectHelper.updateNumberParameter(
				monitor,
				LINK_SPEED_PARAMETER,
				SPEED_MBITS_PARAMETER_UNIT,
				collectTime,
				9.0,
				9.0
			);

			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

			monitorCollectVisitor.estimateNetworkCardPowerConsumption();

			assertEquals(4.0, CollectHelper.getNumberParamValue(monitor, POWER_CONSUMPTION_PARAMETER));
		}

		{
			// Bandwith utilization without link speed

			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).name("FC 01").monitorType(MonitorType.NETWORK_CARD).build();


			CollectHelper.updateNumberParameter(
				monitor,
				BANDWIDTH_UTILIZATION_PARAMETER,
				"percent",
				collectTime,
				60.0,
				60.0
			);

			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

			monitorCollectVisitor.estimateNetworkCardPowerConsumption();

			assertEquals(4.0, CollectHelper.getNumberParamValue(monitor, POWER_CONSUMPTION_PARAMETER));
		}
	}

	@Test
	void testEstimateNetworkCardPowerConsumptionFromLinkSpeed() {
		{
			// Link Speed

			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).name("FC 01").monitorType(MonitorType.NETWORK_CARD).build();

			CollectHelper.updateNumberParameter(
				monitor,
				LINK_SPEED_PARAMETER,
				SPEED_MBITS_PARAMETER_UNIT,
				collectTime,
				300.0,
				300.0
			);

			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

			monitorCollectVisitor.estimateNetworkCardPowerConsumption();

			assertEquals(9.29, CollectHelper.getNumberParamValue(monitor, POWER_CONSUMPTION_PARAMETER));
		}

		{
			// Bandwith utilization with Link Speed < 10

			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).name("FC 01").monitorType(MonitorType.NETWORK_CARD).build();

			CollectHelper.updateNumberParameter(
				monitor,
				LINK_SPEED_PARAMETER,
				SPEED_MBITS_PARAMETER_UNIT,
				collectTime,
				9.0,
				9.0
			);

			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

			monitorCollectVisitor.estimateNetworkCardPowerConsumption();

			assertEquals(2.0, CollectHelper.getNumberParamValue(monitor, POWER_CONSUMPTION_PARAMETER));
		}
	}

	@Test
	void testEstimateNetworkCardPowerConsumptionDefault() {
		// No link speed, no bandwidth utilization
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).name("FC 01").monitorType(MonitorType.NETWORK_CARD).build();

		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.estimateNetworkCardPowerConsumption();

		assertEquals(10.0, CollectHelper.getNumberParamValue(monitor, POWER_CONSUMPTION_PARAMETER));
	}

	@Test
	void testEstimateRoboticsPowerConsumption() {
		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).parentId(PARENT_ID).name("Robotics").monitorType(MonitorType.ROBOTICS).build();
			CollectHelper.updateNumberParameter(
				monitor,
				MOVE_COUNT_PARAMETER,
				MOVE_COUNT_PARAMETER_UNIT,
				collectTime,
				1.0,
				1.0
			);
			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);
			monitorCollectVisitor.estimateRoboticsPowerConsumption();
			assertEquals(154.0, CollectHelper.getNumberParamValue(monitor, POWER_CONSUMPTION_PARAMETER));
		}

		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).parentId(PARENT_ID).name("Robotics").monitorType(MonitorType.ROBOTICS).build();
			CollectHelper.updateNumberParameter(
				monitor,
				MOVE_COUNT_PARAMETER,
				MOVE_COUNT_PARAMETER_UNIT,
				collectTime,
				0.0,
				0.0
			);
			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);
			monitorCollectVisitor.estimateRoboticsPowerConsumption();
			assertEquals(48.0, CollectHelper.getNumberParamValue(monitor, POWER_CONSUMPTION_PARAMETER));
		}

		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).parentId(PARENT_ID).name("Robotics").monitorType(MonitorType.ROBOTICS).build();
			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);
			monitorCollectVisitor.estimateRoboticsPowerConsumption();
			assertEquals(48.0, CollectHelper.getNumberParamValue(monitor, POWER_CONSUMPTION_PARAMETER));
		}
	}

	@Test
	void testEstimateTapeDrivePowerConsumptionLto() {
		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).parentId(PARENT_ID).name("lto td").monitorType(MonitorType.TAPE_DRIVE).build();
			CollectHelper.updateNumberParameter(
				monitor,
				MOUNT_COUNT_PARAMETER,
				MOUNT_COUNT_PARAMETER_UNIT,
				collectTime,
				1.0,
				1.0
			);
			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);
			monitorCollectVisitor.estimateTapeDrivePowerConsumption();
			assertEquals(46.0, CollectHelper.getNumberParamValue(monitor, POWER_CONSUMPTION_PARAMETER));
		}
		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).parentId(PARENT_ID).name("lto td").monitorType(MonitorType.TAPE_DRIVE).build();
			CollectHelper.updateNumberParameter(
				monitor,
				UNMOUNT_COUNT_PARAMETER,
				UNMOUNT_COUNT_PARAMETER_UNIT,
				collectTime,
				1.0,
				1.0
			);
			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);
			monitorCollectVisitor.estimateTapeDrivePowerConsumption();
			assertEquals(46.0, CollectHelper.getNumberParamValue(monitor, POWER_CONSUMPTION_PARAMETER));
		}
		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).parentId(PARENT_ID).name("lto td").monitorType(MonitorType.TAPE_DRIVE).build();
			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);
			monitorCollectVisitor.estimateTapeDrivePowerConsumption();
			assertEquals(30.0, CollectHelper.getNumberParamValue(monitor, POWER_CONSUMPTION_PARAMETER));
		}
	}

	@Test
	void testEstimateTapeDrivePowerConsumptionT10000d() {
		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).parentId(PARENT_ID).name("t10000d td").monitorType(MonitorType.TAPE_DRIVE).build();
			CollectHelper.updateNumberParameter(
				monitor,
				MOUNT_COUNT_PARAMETER,
				MOUNT_COUNT_PARAMETER_UNIT,
				collectTime,
				1.0,
				1.0
			);
			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);
			monitorCollectVisitor.estimateTapeDrivePowerConsumption();
			assertEquals(127.0, CollectHelper.getNumberParamValue(monitor, POWER_CONSUMPTION_PARAMETER));
		}
		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).parentId(PARENT_ID).name("t10000d td").monitorType(MonitorType.TAPE_DRIVE).build();
			CollectHelper.updateNumberParameter(
				monitor,
				UNMOUNT_COUNT_PARAMETER,
				UNMOUNT_COUNT_PARAMETER_UNIT,
				collectTime,
				1.0,
				1.0
			);
			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);
			monitorCollectVisitor.estimateTapeDrivePowerConsumption();
			assertEquals(127.0, CollectHelper.getNumberParamValue(monitor, POWER_CONSUMPTION_PARAMETER));
		}
		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).parentId(PARENT_ID).name("t10000d td").monitorType(MonitorType.TAPE_DRIVE).build();
			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);
			monitorCollectVisitor.estimateTapeDrivePowerConsumption();
			assertEquals(64.0, CollectHelper.getNumberParamValue(monitor, POWER_CONSUMPTION_PARAMETER));
		}
	}

	@Test
	void testEstimateTapeDrivePowerConsumptionT10000() {
		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).parentId(PARENT_ID).name("t10000 td").monitorType(MonitorType.TAPE_DRIVE).build();
			CollectHelper.updateNumberParameter(
				monitor,
				MOUNT_COUNT_PARAMETER,
				MOUNT_COUNT_PARAMETER_UNIT,
				collectTime,
				1.0,
				1.0
			);
			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);
			monitorCollectVisitor.estimateTapeDrivePowerConsumption();
			assertEquals(93.0, CollectHelper.getNumberParamValue(monitor, POWER_CONSUMPTION_PARAMETER));
		}
		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).parentId(PARENT_ID).name("t10000 td").monitorType(MonitorType.TAPE_DRIVE).build();
			CollectHelper.updateNumberParameter(
				monitor,
				UNMOUNT_COUNT_PARAMETER,
				UNMOUNT_COUNT_PARAMETER_UNIT,
				collectTime,
				1.0,
				1.0
			);
			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);
			monitorCollectVisitor.estimateTapeDrivePowerConsumption();
			assertEquals(93.0, CollectHelper.getNumberParamValue(monitor, POWER_CONSUMPTION_PARAMETER));
		}
		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).parentId(PARENT_ID).name("t10000 td").monitorType(MonitorType.TAPE_DRIVE).build();
			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);
			monitorCollectVisitor.estimateTapeDrivePowerConsumption();
			assertEquals(61.0, CollectHelper.getNumberParamValue(monitor, POWER_CONSUMPTION_PARAMETER));
		}
	}

	@Test
	void testEstimateTapeDrivePowerConsumptionTs() {
		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).parentId(PARENT_ID).name("ts td").monitorType(MonitorType.TAPE_DRIVE).build();
			CollectHelper.updateNumberParameter(
				monitor,
				MOUNT_COUNT_PARAMETER,
				MOUNT_COUNT_PARAMETER_UNIT,
				collectTime,
				1.0,
				1.0
			);
			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);
			monitorCollectVisitor.estimateTapeDrivePowerConsumption();
			assertEquals(53.0, CollectHelper.getNumberParamValue(monitor, POWER_CONSUMPTION_PARAMETER));
		}
		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).parentId(PARENT_ID).name("ts td").monitorType(MonitorType.TAPE_DRIVE).build();
			CollectHelper.updateNumberParameter(
				monitor,
				UNMOUNT_COUNT_PARAMETER,
				UNMOUNT_COUNT_PARAMETER_UNIT,
				collectTime,
				1.0,
				1.0
			);
			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);
			monitorCollectVisitor.estimateTapeDrivePowerConsumption();
			assertEquals(53.0, CollectHelper.getNumberParamValue(monitor, POWER_CONSUMPTION_PARAMETER));
		}
		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).parentId(PARENT_ID).name("ts td").monitorType(MonitorType.TAPE_DRIVE).build();
			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);
			monitorCollectVisitor.estimateTapeDrivePowerConsumption();
			assertEquals(35.0, CollectHelper.getNumberParamValue(monitor, POWER_CONSUMPTION_PARAMETER));
		}
	}

	@Test
	void testEstimateTapeDrivePowerConsumptionDefault() {
		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).parentId(PARENT_ID).name("td1").monitorType(MonitorType.TAPE_DRIVE).build();
			CollectHelper.updateNumberParameter(
				monitor,
				MOUNT_COUNT_PARAMETER,
				MOUNT_COUNT_PARAMETER_UNIT,
				collectTime,
				1.0,
				1.0
			);
			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);
			monitorCollectVisitor.estimateTapeDrivePowerConsumption();
			assertEquals(80.0, CollectHelper.getNumberParamValue(monitor, POWER_CONSUMPTION_PARAMETER));
		}
		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).parentId(PARENT_ID).name("td1").monitorType(MonitorType.TAPE_DRIVE).build();
			CollectHelper.updateNumberParameter(
				monitor,
				UNMOUNT_COUNT_PARAMETER,
				UNMOUNT_COUNT_PARAMETER_UNIT,
				collectTime,
				1.0,
				1.0
			);
			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);
			monitorCollectVisitor.estimateTapeDrivePowerConsumption();
			assertEquals(80.0, CollectHelper.getNumberParamValue(monitor, POWER_CONSUMPTION_PARAMETER));
		}
		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).parentId(PARENT_ID).name("td1").monitorType(MonitorType.TAPE_DRIVE).build();
			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);
			monitorCollectVisitor.estimateTapeDrivePowerConsumption();
			assertEquals(55.0, CollectHelper.getNumberParamValue(monitor, POWER_CONSUMPTION_PARAMETER));
		}
	}

	@Test
	void testCollectPhysicalDiskParameters() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).monitorType(MonitorType.PHYSICAL_DISK).build();
		MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		// No value
		monitorCollectVisitor.collectPhysicalDiskParameters();
		NumberParam predictedFailure = monitor.getParameter(PREDICTED_FAILURE_PARAMETER, NumberParam.class);
		NumberParam enduranceRemaining = monitor.getParameter(ENDURANCE_REMAINING_PARAMETER, NumberParam.class);
		assertNull(predictedFailure);
		assertNull(enduranceRemaining);

		// Values collected
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(ENDURANCE_REMAINING_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList(ENDURANCE_REMAINING))
		);
		monitorCollectVisitor.collectPhysicalDiskParameters();
		enduranceRemaining = monitor.getParameter(ENDURANCE_REMAINING_PARAMETER, NumberParam.class);
		assertNotNull(enduranceRemaining);
		assertEquals(10.0, enduranceRemaining.getRawValue());
		assertEquals(10.0, enduranceRemaining.getValue());

		// rawEnduranceRemaining value collected < 0
		monitor.getParameters().clear();
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(ENDURANCE_REMAINING_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList(ENDURANCE_REMAINING_TOO_LOW))
		);
		monitorCollectVisitor.collectPhysicalDiskParameters();
		enduranceRemaining = monitor.getParameter(ENDURANCE_REMAINING_PARAMETER, NumberParam.class);
		assertNull(enduranceRemaining);

		// rawEnduranceRemaining value collected > 100
		monitor.getParameters().clear();
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(ENDURANCE_REMAINING_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList(ENDURANCE_REMAINING_TOO_HIGH))
		);
		monitorCollectVisitor.collectPhysicalDiskParameters();
		enduranceRemaining = monitor.getParameter(ENDURANCE_REMAINING_PARAMETER, NumberParam.class);
		assertNull(enduranceRemaining);
	}

	@Test
	void testCollectLedColor() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();

		final Monitor monitor = Monitor.builder().id(MONITOR_ID).monitorType(MonitorType.LED).build();

		// colorRaw != null, colorRaw is a warning color
		monitor.addMetadata("warningOnColor", "amber,yellow");
		Map<String, String> customMapping = new HashMap<>(mapping);
		customMapping.put(COLOR_PARAMETER, VALUETABLE_COLUMN_6);

		List<String> customRow = new ArrayList<>(row);
		customRow.add("amber");

		MonitorCollectVisitor monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				customMapping,
				monitor,
				customRow)
		);

		monitorCollectVisitor.collectLedColor();
		DiscreteParam colorParameter = monitor.getParameter(COLOR_PARAMETER, DiscreteParam.class);
		assertNotNull(colorParameter);
		assertEquals(LedColorStatus.DEGRADED, colorParameter.getState());

		// colorRaw != null, warningOnColor != null, colorRaw not in warningOnColor
		monitor.addMetadata("warningOnColor", "blue,yellow");
		monitorCollectVisitor.collectLedColor();
		colorParameter = monitor.getParameter(COLOR_PARAMETER, DiscreteParam.class);
		assertNotNull(colorParameter);
		assertEquals(LedColorStatus.OK, colorParameter.getState());

		// colorRaw != null, warningOnColor == null, alarmOnColor != null, colorRaw is an alarm color
		monitor.addMetadata("warningOnColor", null);
		monitor.addMetadata("alarmOnColor", "amber,yellow");
		monitorCollectVisitor.collectLedColor();
		colorParameter = monitor.getParameter(COLOR_PARAMETER, DiscreteParam.class);
		assertNotNull(colorParameter);
		assertEquals(LedColorStatus.FAILED, colorParameter.getState());

		// colorRaw != null, warningOnColor == null, alarmOnColor != null, colorRaw is not an alarm color
		monitor.addMetadata("alarmOnColor", "blue,yellow");
		monitorCollectVisitor.collectLedColor();
		colorParameter = monitor.getParameter(COLOR_PARAMETER, DiscreteParam.class);
		assertNotNull(colorParameter);
		assertEquals(LedColorStatus.OK, colorParameter.getState());

		// colorRaw != null, warningOnColor == null, alarmOnColor == null
		monitor.addMetadata("alarmOnColor", null);
		monitorCollectVisitor.collectLedColor();
		colorParameter = monitor.getParameter(COLOR_PARAMETER, DiscreteParam.class);
		assertNotNull(colorParameter);
		assertEquals(LedColorStatus.OK, colorParameter.getState());
	}

	@Test
	void testCollectLedStatusAndLedIndicatorStatus() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();

		final Monitor monitor = Monitor.builder().id(MONITOR_ID).monitorType(MonitorType.LED).build();

		// statusRaw == null
		Map<String, String> customMapping = new HashMap<>(mapping);
		customMapping.put(STATUS_PARAMETER, VALUETABLE_COLUMN_6);
		MonitorCollectVisitor monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				customMapping,
				monitor,
				row)
		);
		monitorCollectVisitor.collectLedStatusAndLedIndicatorStatus();
		assertNull(monitor.getParameter(STATUS_PARAMETER, DiscreteParam.class));
		assertNull(monitor.getParameter(LED_INDICATOR_PARAMETER, DiscreteParam.class));

		// statusRaw.equals("on")
		List<String> customRow = new ArrayList<>(row);
		customRow.add("on");
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				customMapping,
				monitor,
				customRow)
		);
		monitorCollectVisitor.collectLedStatusAndLedIndicatorStatus();
		assertNull(monitor.getParameter(STATUS_PARAMETER, DiscreteParam.class));
		DiscreteParam ledIndicatorParameter = monitor.getParameter(LED_INDICATOR_PARAMETER, DiscreteParam.class);
		assertNotNull(ledIndicatorParameter);
		assertEquals(LedIndicator.ON, ledIndicatorParameter.getState());

		// statusRaw.equals("blinking"), no blinking status metadata
		customRow = new ArrayList<>(row);
		customRow.add("blinking");
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				customMapping,
				monitor,
				customRow)
		);
		monitorCollectVisitor.collectLedStatusAndLedIndicatorStatus();
		assertNull(monitor.getParameter(STATUS_PARAMETER, DiscreteParam.class));
		ledIndicatorParameter = monitor.getParameter(LED_INDICATOR_PARAMETER, DiscreteParam.class);
		assertNotNull(ledIndicatorParameter);
		assertEquals(LedIndicator.BLINKING, ledIndicatorParameter.getState());

		// statusRaw.equals("blinking"), blinking status meta data found
		monitor.addMetadata("blinkingstatus", "WARN");
		monitorCollectVisitor.collectLedStatusAndLedIndicatorStatus();
		DiscreteParam statusParameter = monitor.getParameter(STATUS_PARAMETER, DiscreteParam.class);
		assertNotNull(statusParameter);
		assertEquals(Status.DEGRADED, statusParameter.getState());
		ledIndicatorParameter = monitor.getParameter(LED_INDICATOR_PARAMETER, DiscreteParam.class);
		assertNotNull(ledIndicatorParameter);
		assertEquals(LedIndicator.BLINKING, ledIndicatorParameter.getState());
	}

	@Test
	void testCollectNetworkCardDuplexMode() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).monitorType(MonitorType.NETWORK_CARD).build();
		monitor.addParameter(DiscreteParam.builder().name(LINK_STATUS_PARAMETER).state(LinkStatus.UNPLUGGED).build());
		MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		// duplexMode = null
		monitorCollectVisitor.collectNetworkCardDuplexMode();
		DiscreteParam duplexModeParameter = monitor.getParameter(DUPLEX_MODE_PARAMETER, DiscreteParam.class);
		assertNull(duplexModeParameter);

		// duplexMode = blabla
		monitor.addParameter(DiscreteParam.builder().name(LINK_STATUS_PARAMETER).state(LinkStatus.PLUGGED).build());
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(DUPLEX_MODE_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList("blabla"))
		);
		monitorCollectVisitor.collectNetworkCardDuplexMode();
		duplexModeParameter = monitor.getParameter(DUPLEX_MODE_PARAMETER, DiscreteParam.class);
		assertNotNull(duplexModeParameter);
		assertEquals(0, duplexModeParameter.getNumericValue());

		// duplexMode = "YES"
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(DUPLEX_MODE_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList("YES"))
		);
		monitorCollectVisitor.collectNetworkCardDuplexMode();
		duplexModeParameter = monitor.getParameter(DUPLEX_MODE_PARAMETER, DiscreteParam.class);
		assertNotNull(duplexModeParameter);
		assertEquals(1, duplexModeParameter.getNumericValue());

		// duplexMode = "Full"
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(DUPLEX_MODE_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList("Full"))
		);
		monitorCollectVisitor.collectNetworkCardDuplexMode();
		duplexModeParameter = monitor.getParameter(DUPLEX_MODE_PARAMETER, DiscreteParam.class);
		assertNotNull(duplexModeParameter);
		assertEquals(1, duplexModeParameter.getNumericValue());

		// duplexMode = "1"
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(DUPLEX_MODE_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList("1"))
		);
		monitorCollectVisitor.collectNetworkCardDuplexMode();
		duplexModeParameter = monitor.getParameter(DUPLEX_MODE_PARAMETER, DiscreteParam.class);
		assertNotNull(duplexModeParameter);
		assertEquals(1, duplexModeParameter.getNumericValue());
	}

	@Test
	void testCollectNetworkCardLinkSpeed() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).monitorType(MonitorType.NETWORK_CARD).build();
		MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		// No link speed value
		monitorCollectVisitor.collectNetworkCardLinkSpeed();
		NumberParam linkSpeedParameter = monitor.getParameter(LINK_SPEED_PARAMETER, NumberParam.class);
		assertNull(linkSpeedParameter);

		// Unallocated space value collected
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(LINK_SPEED_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList("10"))
		);
		monitorCollectVisitor.collectNetworkCardLinkSpeed();
		linkSpeedParameter = monitor.getParameter(LINK_SPEED_PARAMETER, NumberParam.class);
		assertNotNull(linkSpeedParameter);
		assertEquals(10.0, linkSpeedParameter.getRawValue());
		assertEquals(10.0, linkSpeedParameter.getValue());
	}

	@Test
	void testCollectNetworkCardBytesRate() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).monitorType(MonitorType.NETWORK_CARD).build();
		MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		// No received bytes
		monitorCollectVisitor.collectNetworkCardBytesRate(
			RECEIVED_BYTES_PARAMETER,
			RECEIVED_BYTES_RATE_PARAMETER,
			USAGE_REPORT_RECEIVED_BYTES_PARAMETER
		);
		NumberParam bytesRateParameter = monitor.getParameter(RECEIVED_BYTES_RATE_PARAMETER, NumberParam.class);
		assertNull(bytesRateParameter);

		// Received bytes set, but no last received bytes
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(RECEIVED_BYTES_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList("10"))
		);
		monitorCollectVisitor.collectNetworkCardBytesRate(
			RECEIVED_BYTES_PARAMETER,
			RECEIVED_BYTES_RATE_PARAMETER,
			USAGE_REPORT_RECEIVED_BYTES_PARAMETER
		);
		bytesRateParameter = monitor.getParameter(RECEIVED_BYTES_RATE_PARAMETER, NumberParam.class);
		assertNull(bytesRateParameter);
		NumberParam bytesParameter = monitor.getParameter(RECEIVED_BYTES_PARAMETER, NumberParam.class);
		assertNotNull(bytesParameter);
		assertEquals(10.0, bytesParameter.getRawValue());

		// Received bytes & last received bytes set
		bytesParameter = NumberParam.builder().name(RECEIVED_BYTES_PARAMETER).build();
		bytesParameter.setPreviousRawValue(80.0 * 1048576); // 80 MB
		bytesParameter.setPreviousCollectTime(System.currentTimeMillis() - 120000L); // 2 minutes ago
		monitor.addParameter(bytesParameter);
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(RECEIVED_BYTES_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList("209715200")) // 200 MB
		);
		monitorCollectVisitor.collectNetworkCardBytesRate(
			RECEIVED_BYTES_PARAMETER,
			RECEIVED_BYTES_RATE_PARAMETER,
			USAGE_REPORT_RECEIVED_BYTES_PARAMETER
		);
		bytesParameter = monitor.getParameter(RECEIVED_BYTES_PARAMETER, NumberParam.class);
		assertNotNull(bytesParameter);
		assertEquals(209715200.0, bytesParameter.getValue());
		NumberParam usageReportParameter = monitor.getParameter(USAGE_REPORT_RECEIVED_BYTES_PARAMETER, NumberParam.class);
		assertNotNull(usageReportParameter);
		assertEquals(120.0 / 1024, usageReportParameter.getValue());
		bytesRateParameter = monitor.getParameter(RECEIVED_BYTES_RATE_PARAMETER, NumberParam.class);
		assertNotNull(bytesRateParameter);
		assertEquals(1, bytesRateParameter.getValue().intValue());
	}

	@Test
	void testCollectNetworkCardPacketsRate() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).monitorType(MonitorType.NETWORK_CARD).build();
		MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		// No transmitted packets
		monitorCollectVisitor.collectNetworkCardPacketsRate(
			TRANSMITTED_PACKETS_PARAMETER,
			TRANSMITTED_PACKETS_RATE_PARAMETER,
			USAGE_REPORT_TRANSMITTED_PACKETS_PARAMETER
		);
		NumberParam packetsRateParameter = monitor.getParameter(TRANSMITTED_PACKETS_RATE_PARAMETER, NumberParam.class);
		assertNull(packetsRateParameter);

		// Transmitted packets set, but no last transmitted packets
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(TRANSMITTED_PACKETS_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList("10"))
		);
		monitorCollectVisitor.collectNetworkCardPacketsRate(
			TRANSMITTED_PACKETS_PARAMETER,
			TRANSMITTED_PACKETS_RATE_PARAMETER,
			USAGE_REPORT_TRANSMITTED_PACKETS_PARAMETER
		);
		packetsRateParameter = monitor.getParameter(TRANSMITTED_PACKETS_RATE_PARAMETER, NumberParam.class);
		assertNull(packetsRateParameter);
		NumberParam packetsParameter = monitor.getParameter(TRANSMITTED_PACKETS_PARAMETER, NumberParam.class);
		assertNotNull(packetsParameter);
		assertEquals(10.0, packetsParameter.getRawValue());

		// Transmitted packets & last transmitted packets set
		packetsParameter = NumberParam.builder().name(TRANSMITTED_PACKETS_PARAMETER).build();
		packetsParameter.setPreviousRawValue(80.0);
		packetsParameter.setPreviousCollectTime(System.currentTimeMillis() - 120000L); // 2 minutes ago
		monitor.addParameter(packetsParameter);
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(TRANSMITTED_PACKETS_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList("200"))
		);
		monitorCollectVisitor.collectNetworkCardPacketsRate(
			TRANSMITTED_PACKETS_PARAMETER,
			TRANSMITTED_PACKETS_RATE_PARAMETER,
			USAGE_REPORT_TRANSMITTED_PACKETS_PARAMETER
		);
		packetsParameter = monitor.getParameter(TRANSMITTED_PACKETS_PARAMETER, NumberParam.class);
		assertNotNull(packetsParameter);
		assertEquals(200.0, packetsParameter.getValue());
		NumberParam usageReportParameter = monitor.getParameter(USAGE_REPORT_TRANSMITTED_PACKETS_PARAMETER, NumberParam.class);
		assertNotNull(usageReportParameter);
		assertEquals(120.0, usageReportParameter.getValue());
		packetsRateParameter = monitor.getParameter(TRANSMITTED_PACKETS_RATE_PARAMETER, NumberParam.class);
		assertNotNull(packetsRateParameter);
		assertEquals(1, packetsRateParameter.getValue().intValue());
	}

	@Test
	void testCollectNetworkCardBandwidthUtilization() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).monitorType(MonitorType.NETWORK_CARD).build();
		MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		// Full-duplex mode with null receivedBytesRate
		monitorCollectVisitor.collectNetworkCardBandwidthUtilization(DuplexMode.FULL, 1000.0, null, 200.0);
		NumberParam bandwidthUtilizationParameter = monitor.getParameter(BANDWIDTH_UTILIZATION_PARAMETER, NumberParam.class);
		assertNotNull(bandwidthUtilizationParameter);
		assertEquals(200.0 * 8 * 100 / 1000, bandwidthUtilizationParameter.getValue());

		// Full-duplex mode with null transmittedBytesRate
		bandwidthUtilizationParameter.save();
		monitorCollectVisitor.collectNetworkCardBandwidthUtilization(DuplexMode.FULL, 1000.0, 100.0, null);
		bandwidthUtilizationParameter = monitor.getParameter(BANDWIDTH_UTILIZATION_PARAMETER, NumberParam.class);
		assertNotNull(bandwidthUtilizationParameter);
		assertEquals(100.0 * 8 * 100 / 1000, bandwidthUtilizationParameter.getValue());

		// Full-duplex mode
		bandwidthUtilizationParameter.save();
		monitorCollectVisitor.collectNetworkCardBandwidthUtilization(DuplexMode.FULL, 1000.0, 100.0, 200.0);
		bandwidthUtilizationParameter = monitor.getParameter(BANDWIDTH_UTILIZATION_PARAMETER, NumberParam.class);
		assertNotNull(bandwidthUtilizationParameter);
		assertEquals(200.0 * 8 * 100 / 1000, bandwidthUtilizationParameter.getValue());

		// Full-duplex mode, when the duplex mode is null
		bandwidthUtilizationParameter.save();
		monitorCollectVisitor.collectNetworkCardBandwidthUtilization(null, 1000.0, 100.0, 200.0);
		bandwidthUtilizationParameter = monitor.getParameter(BANDWIDTH_UTILIZATION_PARAMETER, NumberParam.class);
		assertNotNull(bandwidthUtilizationParameter);
		assertEquals(200.0 * 8 * 100 / 1000, bandwidthUtilizationParameter.getValue());

		// Half-duplex mode
		bandwidthUtilizationParameter.save();
		monitorCollectVisitor.collectNetworkCardBandwidthUtilization(DuplexMode.HALF, 1000.0, 100.0, 200.0);
		bandwidthUtilizationParameter = monitor.getParameter(BANDWIDTH_UTILIZATION_PARAMETER, NumberParam.class);
		assertNotNull(bandwidthUtilizationParameter);
		assertEquals((100.0 + 200.0) * 8 * 100 / 1000, bandwidthUtilizationParameter.getValue());

		// No link speed
		bandwidthUtilizationParameter.save();
		final long newCollectTime = new Date().getTime();
		final long previousCollectTime = monitorCollectVisitor.getMonitorCollectInfo().getCollectTime();
		monitorCollectVisitor.getMonitorCollectInfo().setCollectTime(newCollectTime); // new collect
		monitorCollectVisitor.collectNetworkCardBandwidthUtilization(DuplexMode.HALF, null, 100.0, 200.0);
		bandwidthUtilizationParameter = monitor.getParameter(BANDWIDTH_UTILIZATION_PARAMETER, NumberParam.class);
		assertNotNull(bandwidthUtilizationParameter.getValue());
		assertNotEquals(newCollectTime, bandwidthUtilizationParameter.getCollectTime());
		assertEquals(previousCollectTime, bandwidthUtilizationParameter.getCollectTime());

	}

	@Test
	void testCollectNetworkCardBandwidthUtilizationLinkSpeedZero() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).monitorType(MonitorType.NETWORK_CARD).build();
		MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.collectNetworkCardBandwidthUtilization(DuplexMode.FULL, 0.0, 100.0, 200.0);

		assertNull(monitor.getParameter(BANDWIDTH_UTILIZATION_PARAMETER, NumberParam.class));
	}

	@Test
	void testCollectNetworkCardErrorPercent() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).monitorType(MonitorType.NETWORK_CARD).build();
		MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		// No error count set
		monitorCollectVisitor.collectNetworkCardErrorPercent(null, null);
		NumberParam errorCountParameter = monitor.getParameter(ERROR_COUNT_PARAMETER, NumberParam.class);
		assertNull(errorCountParameter);

		// Error count set but no last total packets
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(ERROR_COUNT_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList("10"))
		);

		monitorCollectVisitor.collectNetworkCardErrorPercent(100.0, 200.0);
		errorCountParameter = monitor.getParameter(ERROR_COUNT_PARAMETER, NumberParam.class);
		assertEquals(10.0, errorCountParameter.getRawValue());
		assertEquals(10.0, errorCountParameter.getValue());
		NumberParam errorPercentParameter = monitor.getParameter(ERROR_PERCENT_PARAMETER, NumberParam.class);
		assertNull(errorPercentParameter);

		// Error count and last total packets set, but no last error count
		NumberParam totalPacketsParameter = NumberParam.builder().name(TOTAL_PACKETS_PARAMETER).build();
		totalPacketsParameter.setPreviousRawValue(500.0);
		totalPacketsParameter.setPreviousCollectTime(System.currentTimeMillis() - 120000L); // 2 minutes ago
		monitor.addParameter(totalPacketsParameter);
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(ERROR_COUNT_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList("100"))
		);

		monitorCollectVisitor.collectNetworkCardErrorPercent(100.0, 200.0);
		errorCountParameter = monitor.getParameter(ERROR_COUNT_PARAMETER, NumberParam.class);
		assertEquals(100.0, errorCountParameter.getRawValue());
		assertEquals(100.0, errorCountParameter.getValue());
		errorPercentParameter = monitor.getParameter(ERROR_PERCENT_PARAMETER, NumberParam.class);
		assertNull(errorPercentParameter);

		// Error count, last error and last total packets set
		errorCountParameter = NumberParam.builder().name(ERROR_COUNT_PARAMETER).build();
		errorCountParameter.setPreviousRawValue(50.0);
		errorCountParameter.setPreviousCollectTime(System.currentTimeMillis() - 120000L); // 2 minutes ago
		monitor.addParameter(errorCountParameter);
		totalPacketsParameter = NumberParam.builder().name(TOTAL_PACKETS_PARAMETER).build();
		totalPacketsParameter.setPreviousRawValue(150.0);
		totalPacketsParameter.setPreviousCollectTime(System.currentTimeMillis() - 120000L); // 2 minutes ago
		monitor.addParameter(totalPacketsParameter);
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(ERROR_COUNT_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList("100"))
		);

		monitorCollectVisitor.collectNetworkCardErrorPercent(100.0, 200.0);
		errorCountParameter = monitor.getParameter(ERROR_COUNT_PARAMETER, NumberParam.class);
		assertEquals(100.0, errorCountParameter.getRawValue());
		assertEquals(100.0, errorCountParameter.getValue());
		errorPercentParameter = monitor.getParameter(ERROR_PERCENT_PARAMETER, NumberParam.class);
		assertNotNull(errorPercentParameter);
		assertEquals(100 * 50.0 / 150.0, errorPercentParameter.getRawValue());
		assertEquals(100 * 50.0 / 150.0, errorPercentParameter.getValue());
	}

	@Test
	void testCollectNetworkCardZeroBufferCreditPercent() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).monitorType(MonitorType.NETWORK_CARD).build();
		MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		// No zero buffer credit count value
		monitorCollectVisitor.collectNetworkCardZeroBufferCreditPercent();
		NumberParam zeroBufferCreditCountParameter = monitor.getParameter(ZERO_BUFFER_CREDIT_COUNT_PARAMETER, NumberParam.class);
		assertNull(zeroBufferCreditCountParameter.getRawValue());

		// Zero buffer credit count set along with last value and transmitted packets
		zeroBufferCreditCountParameter = NumberParam.builder().name(ZERO_BUFFER_CREDIT_COUNT_PARAMETER).build();
		zeroBufferCreditCountParameter.setPreviousRawValue(50.0);
		zeroBufferCreditCountParameter.setPreviousCollectTime(System.currentTimeMillis() - 120000L); // 2 minutes ago
		monitor.addParameter(zeroBufferCreditCountParameter);
		NumberParam usageReportTransmittedPacketsParameter = NumberParam.builder().name(USAGE_REPORT_TRANSMITTED_PACKETS_PARAMETER).build();
		usageReportTransmittedPacketsParameter.setValue(400.0);
		monitor.addParameter(usageReportTransmittedPacketsParameter);
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(ZERO_BUFFER_CREDIT_COUNT_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList("150"))
		);
		monitorCollectVisitor.collectNetworkCardZeroBufferCreditPercent();
		zeroBufferCreditCountParameter = monitor.getParameter(ZERO_BUFFER_CREDIT_COUNT_PARAMETER, NumberParam.class);
		assertNotNull(zeroBufferCreditCountParameter);
		assertEquals(150.0, zeroBufferCreditCountParameter.getRawValue());
		assertEquals(150.0, zeroBufferCreditCountParameter.getValue());
		NumberParam zeroBufferCreditPercentParameter = monitor.getParameter(ZERO_BUFFER_CREDIT_PERCENT_PARAMETER, NumberParam.class);
		assertNotNull(zeroBufferCreditPercentParameter);
		assertEquals(100 * 100.0 / (100.0 + 400.0), zeroBufferCreditPercentParameter.getRawValue());
		assertEquals(100 * 100.0 / (100.0 + 400.0), zeroBufferCreditPercentParameter.getValue());
	}

	@Test
	void testVisitVm() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).monitorType(MonitorType.GPU).build();
		MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		assertDoesNotThrow(() -> monitorCollectVisitor.visit(new Vm()));
	}

	@Test
	void testVisitGpu() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).monitorType(MonitorType.GPU).build();
		MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		assertDoesNotThrow(() -> monitorCollectVisitor.visit(new Gpu()));
	}

	@Test
	void testCollectGpuUsedTimeRatioParameters() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).monitorType(MonitorType.GPU).build();
		Map<String, String> mapping = new HashMap<>();
		List<String> row = new ArrayList<>();
		MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor, mapping, row);

		// Ratio computation returns null
		assertNull(monitor.getParameter(DECODER_USED_TIME_PARAMETER, NumberParam.class));
		assertNull(monitor.getParameter(DECODER_USED_TIME_PERCENT_PARAMETER, NumberParam.class));
		assertNull(monitor.getParameter(ENCODER_USED_TIME_PARAMETER, NumberParam.class));
		assertNull(monitor.getParameter(ENCODER_USED_TIME_PERCENT_PARAMETER, NumberParam.class));
		assertNull(monitor.getParameter(USED_TIME_PARAMETER, NumberParam.class));
		assertNull(monitor.getParameter(USED_TIME_PERCENT_PARAMETER, NumberParam.class));

		monitorCollectVisitor.collectGpuUsedTimeRatioParameters();

		assertNull(monitor.getParameter(DECODER_USED_TIME_PARAMETER, NumberParam.class));
		assertNull(monitor.getParameter(DECODER_USED_TIME_PERCENT_PARAMETER, NumberParam.class));
		assertNull(monitor.getParameter(ENCODER_USED_TIME_PARAMETER, NumberParam.class));
		assertNull(monitor.getParameter(ENCODER_USED_TIME_PERCENT_PARAMETER, NumberParam.class));
		assertNull(monitor.getParameter(USED_TIME_PARAMETER, NumberParam.class));
		assertNull(monitor.getParameter(USED_TIME_PERCENT_PARAMETER, NumberParam.class));

		// Ratio computation does not return null, ratio is out of bounds
		NumberParam usedTimeParameter = NumberParam
			.builder()
			.name(USED_TIME_PARAMETER)
			.unit(TIME_PARAMETER_UNIT)
			.rawValue(0.0)
			.value(0.0)
			.collectTime(collectTime - 120000) // 2 minutes ago
			.build();
		usedTimeParameter.save();
		monitor.addParameter(usedTimeParameter);
		mapping.put(USED_TIME_PARAMETER, VALUETABLE_COLUMN_1);
		row.add("200");

		assertNull(monitor.getParameter(DECODER_USED_TIME_PARAMETER, NumberParam.class));
		assertNull(monitor.getParameter(DECODER_USED_TIME_PERCENT_PARAMETER, NumberParam.class));
		assertNull(monitor.getParameter(ENCODER_USED_TIME_PARAMETER, NumberParam.class));
		assertNull(monitor.getParameter(ENCODER_USED_TIME_PERCENT_PARAMETER, NumberParam.class));
		assertNotNull(monitor.getParameter(USED_TIME_PARAMETER, NumberParam.class)); // This one is initially not null
		assertNull(monitor.getParameter(USED_TIME_PERCENT_PARAMETER, NumberParam.class));

		monitorCollectVisitor.collectGpuUsedTimeRatioParameters();

		assertNull(monitor.getParameter(DECODER_USED_TIME_PARAMETER, NumberParam.class));
		assertNull(monitor.getParameter(DECODER_USED_TIME_PERCENT_PARAMETER, NumberParam.class));
		assertNull(monitor.getParameter(ENCODER_USED_TIME_PARAMETER, NumberParam.class));
		assertNull(monitor.getParameter(ENCODER_USED_TIME_PERCENT_PARAMETER, NumberParam.class));
		assertNotNull(monitor.getParameter(USED_TIME_PARAMETER, NumberParam.class));
		assertEquals(0.0, monitor.getParameter(USED_TIME_PARAMETER, NumberParam.class).getValue());
		assertNull(monitor.getParameter(USED_TIME_PERCENT_PARAMETER, NumberParam.class));

		// Ratio computation does not return null, ratio is valid
		usedTimeParameter.setRawValue(140.0); // usedTimeDelta will be 200 - 140 == 60, and collectTimeDelta will be 120
		usedTimeParameter.save();

		assertNull(monitor.getParameter(DECODER_USED_TIME_PARAMETER, NumberParam.class));
		assertNull(monitor.getParameter(DECODER_USED_TIME_PERCENT_PARAMETER, NumberParam.class));
		assertNull(monitor.getParameter(ENCODER_USED_TIME_PARAMETER, NumberParam.class));
		assertNull(monitor.getParameter(ENCODER_USED_TIME_PERCENT_PARAMETER, NumberParam.class));
		assertNotNull(monitor.getParameter(USED_TIME_PARAMETER, NumberParam.class)); // This one is initially not null
		assertNull(monitor.getParameter(USED_TIME_PERCENT_PARAMETER, NumberParam.class));

		monitorCollectVisitor.collectGpuUsedTimeRatioParameters();

		assertNull(monitor.getParameter(DECODER_USED_TIME_PARAMETER, NumberParam.class));
		assertNull(monitor.getParameter(DECODER_USED_TIME_PERCENT_PARAMETER, NumberParam.class));
		assertNull(monitor.getParameter(ENCODER_USED_TIME_PARAMETER, NumberParam.class));
		assertNull(monitor.getParameter(ENCODER_USED_TIME_PERCENT_PARAMETER, NumberParam.class));
		assertNotNull(monitor.getParameter(USED_TIME_PARAMETER, NumberParam.class));
		assertEquals(0.0, monitor.getParameter(USED_TIME_PARAMETER, NumberParam.class).getValue());
		assertNotNull(monitor.getParameter(USED_TIME_PERCENT_PARAMETER, NumberParam.class));
		assertEquals(50.0, monitor.getParameter(USED_TIME_PERCENT_PARAMETER, NumberParam.class).getValue());
	}

	@Test
	void testCollectGpuTransferredBytesParameters() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).monitorType(MonitorType.GPU).build();
		Map<String, String> mapping = new HashMap<>();
		List<String> row = new ArrayList<>();
		MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor, mapping, row);

		// transferred bytes value is null, transferred bytes rate is null
		assertNull(monitor.getParameter(TRANSMITTED_BYTES_PARAMETER, NumberParam.class));
		assertNull(monitor.getParameter(TRANSMITTED_BYTES_RATE_PARAMETER, NumberParam.class));
		assertNull(monitor.getParameter(RECEIVED_BYTES_PARAMETER, NumberParam.class));
		assertNull(monitor.getParameter(RECEIVED_BYTES_RATE_PARAMETER, NumberParam.class));

		monitorCollectVisitor.collectGpuTransferredBytesParameters();

		assertNull(monitor.getParameter(TRANSMITTED_BYTES_PARAMETER, NumberParam.class));
		assertNull(monitor.getParameter(TRANSMITTED_BYTES_RATE_PARAMETER, NumberParam.class));
		assertNull(monitor.getParameter(RECEIVED_BYTES_PARAMETER, NumberParam.class));
		assertNull(monitor.getParameter(RECEIVED_BYTES_RATE_PARAMETER, NumberParam.class));

		// transferred bytes value is not null
		mapping.put(TRANSMITTED_BYTES_PARAMETER, VALUETABLE_COLUMN_1);
		row.add("1073741824");

		assertNull(monitor.getParameter(TRANSMITTED_BYTES_PARAMETER, NumberParam.class));
		assertNull(monitor.getParameter(TRANSMITTED_BYTES_RATE_PARAMETER, NumberParam.class));
		assertNull(monitor.getParameter(RECEIVED_BYTES_PARAMETER, NumberParam.class));
		assertNull(monitor.getParameter(RECEIVED_BYTES_RATE_PARAMETER, NumberParam.class));

		monitorCollectVisitor.collectGpuTransferredBytesParameters();

		assertNotNull(monitor.getParameter(TRANSMITTED_BYTES_PARAMETER, NumberParam.class));
		assertEquals(1073741824.0, monitor.getParameter(TRANSMITTED_BYTES_PARAMETER, NumberParam.class).getValue());
		assertNull(monitor.getParameter(TRANSMITTED_BYTES_RATE_PARAMETER, NumberParam.class));
		assertNull(monitor.getParameter(RECEIVED_BYTES_PARAMETER, NumberParam.class));
		assertNull(monitor.getParameter(RECEIVED_BYTES_RATE_PARAMETER, NumberParam.class));

		// transferred bytes value is null, transferred bytes rate is not null
		mapping.clear();
		mapping.put(TRANSMITTED_BYTES_RATE_PARAMETER, VALUETABLE_COLUMN_1);
		monitor.setParameters(new TreeMap<>(String.CASE_INSENSITIVE_ORDER));

		assertNull(monitor.getParameter(TRANSMITTED_BYTES_PARAMETER, NumberParam.class));
		assertNull(monitor.getParameter(TRANSMITTED_BYTES_RATE_PARAMETER, NumberParam.class));
		assertNull(monitor.getParameter(RECEIVED_BYTES_PARAMETER, NumberParam.class));
		assertNull(monitor.getParameter(RECEIVED_BYTES_RATE_PARAMETER, NumberParam.class));

		monitorCollectVisitor.collectGpuTransferredBytesParameters();

		assertNull(monitor.getParameter(TRANSMITTED_BYTES_PARAMETER, NumberParam.class));
		assertNotNull(monitor.getParameter(TRANSMITTED_BYTES_RATE_PARAMETER, NumberParam.class));
		assertEquals(1024.0, monitor.getParameter(TRANSMITTED_BYTES_RATE_PARAMETER, NumberParam.class).getValue());
		assertNull(monitor.getParameter(RECEIVED_BYTES_PARAMETER, NumberParam.class));
		assertNull(monitor.getParameter(RECEIVED_BYTES_RATE_PARAMETER, NumberParam.class));
	}

	@Test
	void testCollectAvailablePathWarning() {
		// Case 1: available path count is not collected.
		// Expect that the available path warning is not collected.
		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor lun = Monitor.builder().id(DEVICE_ID)
					.name("lun")
					.monitorType(MonitorType.LUN)
					.build();
			final Map<String, String> mapping = Map.of(
					STATUS_PARAMETER, VALUETABLE_COLUMN_1,
					AVAILABLE_PATH_COUNT_PARAMETER, VALUETABLE_COLUMN_2,
					AVAILABLE_PATH_INFORMATION_PARAMETER, VALUETABLE_COLUMN_3);
			final List<String> row = List.of("OK", "", "FC Paths of the LUN");
			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, lun, mapping, row);
			monitorCollectVisitor.collectAvailablePathWarning();
			assertNull(lun.getMetadata(AVAILABLE_PATH_WARNING));
		}

		// Case 2: available path count is collected and available path warning is collected.
		// Expect that the available path warning is unchanged in the collect
		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor lun = Monitor.builder().id(DEVICE_ID)
					.name("lun")
					.monitorType(MonitorType.LUN)
					.build();
			lun.addMetadata(AVAILABLE_PATH_WARNING, "3");
			final Map<String, String> mapping = Map.of(
					STATUS_PARAMETER, VALUETABLE_COLUMN_1,
					AVAILABLE_PATH_COUNT_PARAMETER, VALUETABLE_COLUMN_2,
					AVAILABLE_PATH_INFORMATION_PARAMETER, VALUETABLE_COLUMN_3);
			final List<String> row = List.of("OK", "10", "FC Paths of the LUN");
			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, lun, mapping, row);
			monitorCollectVisitor.collectAvailablePathWarning();
			assertEquals(3, Double.parseDouble(lun.getMetadata(AVAILABLE_PATH_WARNING)));
		}

		// Case 3: available path count is collected and available path warning isn't collected.
		// Expect that the available path warning is updated in the collect
		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor lun = Monitor.builder().id(DEVICE_ID)
					.name("lun")
					.monitorType(MonitorType.LUN)
					.build();

			final Map<String, String> mapping = Map.of(
					STATUS_PARAMETER, VALUETABLE_COLUMN_1,
					AVAILABLE_PATH_COUNT_PARAMETER, VALUETABLE_COLUMN_2,
					AVAILABLE_PATH_INFORMATION_PARAMETER, VALUETABLE_COLUMN_3);
			final List<String> row = List.of("OK", "10", "FC Paths of the LUN"); // availablePathCount = 10
			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, lun, mapping, row);
			monitorCollectVisitor.collectAvailablePathWarning();
			assertEquals(9, Double.parseDouble(lun.getMetadata(AVAILABLE_PATH_WARNING)));
		}

		// Case 4: available path count is collected, and unchanged during 2 collects, the available path warning isn't collected by the connector.
		// Expect that the available path warning is not updated in the second collect
		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor lun = Monitor.builder().id(DEVICE_ID)
					.name("lun")
					.monitorType(MonitorType.LUN)
					.build();

			// Collect 1
			final Map<String, String> mapping = Map.of(
					STATUS_PARAMETER, VALUETABLE_COLUMN_1,
					AVAILABLE_PATH_COUNT_PARAMETER, VALUETABLE_COLUMN_2,
					AVAILABLE_PATH_INFORMATION_PARAMETER, VALUETABLE_COLUMN_3);
			final List<String> row = List.of("OK", "10", "FC Paths of the LUN"); // availablePathCount = 10
			MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, lun, mapping, row);
			monitorCollectVisitor.collectAvailablePathWarning();

			assertEquals(9, Double.parseDouble(lun.getMetadata(AVAILABLE_PATH_WARNING)));

			// Collect 2
			monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, lun, mapping, row);
			monitorCollectVisitor.collectAvailablePathWarning();

			assertEquals(9, Double.parseDouble(lun.getMetadata(AVAILABLE_PATH_WARNING)));
			
		}

		// Case 5: available path count is collected, and increased in the 2nd collect, the available path warning isn't collected by the connector.
		// Expect that the available path warning is updated in the second collect
		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor lun = Monitor.builder().id(DEVICE_ID)
					.name("lun")
					.id(DEVICE_ID)
					.name(DEVICE_ID)
					.parentId(TARGET_ID)
					.targetId(TARGET_ID)
					.monitorType(MonitorType.LUN)
					.build();

			hostMonitoring.addMonitor(lun);

			// Collect 1
			final Map<String, String> mapping = Map.of(
					STATUS_PARAMETER, VALUETABLE_COLUMN_1,
					AVAILABLE_PATH_COUNT_PARAMETER, VALUETABLE_COLUMN_2,
					AVAILABLE_PATH_INFORMATION_PARAMETER, VALUETABLE_COLUMN_3);
			List<String> row = List.of("OK", "10", "FC Paths of the LUN"); // availablePathCount = 10
			MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, lun, mapping, row);
			monitorCollectVisitor.collectAvailablePathWarning();

			assertEquals(10, CollectHelper.getNumberParamValue(lun, MAX_AVAILABLE_PATH_COUNT_PARAMETER));

			// Collect 2
			hostMonitoring.saveParameters();

			row = List.of("OK", "12", "FC Paths of the LUN"); // availablePathCount = 12

			monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, lun, mapping, row);
			monitorCollectVisitor.collectAvailablePathWarning();

			assertEquals(11, Double.parseDouble(lun.getMetadata(AVAILABLE_PATH_WARNING)));
			assertEquals(12, CollectHelper.getNumberParamValue(lun, MAX_AVAILABLE_PATH_COUNT_PARAMETER));
			
		}

		// Case 6: available path count is collected, and equals 1, the available path warning isn't collected by the connector.
		// Expect that the available path warning is not collected. Because 0 is reserved for the ALARM threshold
		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor lun = Monitor.builder().id(DEVICE_ID)
					.name("lun")
					.id(DEVICE_ID)
					.name(DEVICE_ID)
					.parentId(TARGET_ID)
					.targetId(TARGET_ID)
					.monitorType(MonitorType.LUN)
					.build();

			final Map<String, String> mapping = Map.of(
					STATUS_PARAMETER, VALUETABLE_COLUMN_1,
					AVAILABLE_PATH_COUNT_PARAMETER, VALUETABLE_COLUMN_2,
					AVAILABLE_PATH_INFORMATION_PARAMETER, VALUETABLE_COLUMN_3);
			List<String> row = List.of("OK", "1", "FC Paths of the LUN"); // availablePathCount = 10
			MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, lun, mapping, row);
			monitorCollectVisitor.collectAvailablePathWarning();

			assertNull(lun.getMetadata(AVAILABLE_PATH_WARNING));

		}

		// Case 7: available path count is collected, the available path warning isn't
		// collected by the connector in the first collect but collected in the second one.
		// Expect that the 2nd collect updates the available path warning
		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor lun = Monitor.builder().id(DEVICE_ID)
					.name("lun")
					.id(DEVICE_ID)
					.name(DEVICE_ID)
					.parentId(TARGET_ID)
					.targetId(TARGET_ID)
					.monitorType(MonitorType.LUN)
					.build();

			hostMonitoring.addMonitor(lun);

			// Collect 1
			final Map<String, String> mapping = Map.of(
					STATUS_PARAMETER, VALUETABLE_COLUMN_1,
					AVAILABLE_PATH_COUNT_PARAMETER, VALUETABLE_COLUMN_2,
					AVAILABLE_PATH_INFORMATION_PARAMETER, VALUETABLE_COLUMN_3);
			List<String> row = List.of("OK", "10", "FC Paths of the LUN"); // availablePathCount = 10
			MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, lun, mapping, row);
			monitorCollectVisitor.collectAvailablePathWarning();

			assertEquals(9, Double.parseDouble(lun.getMetadata(AVAILABLE_PATH_WARNING)));

			// Collect 2
			hostMonitoring.saveParameters();

			lun.addMetadata(AVAILABLE_PATH_WARNING, "5");

			monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, lun, mapping, row);
			monitorCollectVisitor.collectAvailablePathWarning();

			assertEquals(5, Double.parseDouble(lun.getMetadata(AVAILABLE_PATH_WARNING)));
			
		}

	}
}
