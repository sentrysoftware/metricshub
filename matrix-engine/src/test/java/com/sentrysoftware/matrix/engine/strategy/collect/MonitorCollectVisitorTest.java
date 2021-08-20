package com.sentrysoftware.matrix.engine.strategy.collect;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.common.meta.monitor.Battery;
import com.sentrysoftware.matrix.common.meta.monitor.Blade;
import com.sentrysoftware.matrix.common.meta.monitor.Cpu;
import com.sentrysoftware.matrix.common.meta.monitor.CpuCore;
import com.sentrysoftware.matrix.common.meta.monitor.DiskController;
import com.sentrysoftware.matrix.common.meta.monitor.Enclosure;
import com.sentrysoftware.matrix.common.meta.monitor.Fan;
import com.sentrysoftware.matrix.common.meta.monitor.Led;
import com.sentrysoftware.matrix.common.meta.monitor.LogicalDisk;
import com.sentrysoftware.matrix.common.meta.monitor.Lun;
import com.sentrysoftware.matrix.common.meta.monitor.Memory;
import com.sentrysoftware.matrix.common.meta.monitor.MetaConnector;
import com.sentrysoftware.matrix.common.meta.monitor.NetworkCard;
import com.sentrysoftware.matrix.common.meta.monitor.OtherDevice;
import com.sentrysoftware.matrix.common.meta.monitor.PhysicalDisk;
import com.sentrysoftware.matrix.common.meta.monitor.PowerSupply;
import com.sentrysoftware.matrix.common.meta.monitor.Robotic;
import com.sentrysoftware.matrix.common.meta.monitor.TapeDrive;
import com.sentrysoftware.matrix.common.meta.monitor.Target;
import com.sentrysoftware.matrix.common.meta.monitor.Temperature;
import com.sentrysoftware.matrix.common.meta.monitor.Voltage;
import com.sentrysoftware.matrix.common.meta.parameter.MetaParameter;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoring;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;
import com.sentrysoftware.matrix.model.parameter.IParameterValue;
import com.sentrysoftware.matrix.model.parameter.NumberParam;
import com.sentrysoftware.matrix.model.parameter.ParameterState;
import com.sentrysoftware.matrix.model.parameter.StatusParam;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MonitorCollectVisitorTest {

	private static final String POWER_CONSUMPTION = "150";
	private static final String OK_RAW_STATUS = "OK";
	private static final String OPERABLE = "Operable";
	private static final String CHARGE = "39";
	private static final String TIME_LEFT = "60";
	private static final String VALUETABLE_COLUMN_1 = "Valuetable.Column(1)";
	private static final String VALUETABLE_COLUMN_2 = "Valuetable.Column(2)";
	private static final String VALUETABLE_COLUMN_3 = "Valuetable.Column(3)";
	private static final String VALUETABLE_COLUMN_4 = "Valuetable.Column(4)";
	private static final String VALUETABLE_COLUMN_5 = "Valuetable.Column(5)";
	private static final String MONITOR_DEVICE_ID = "1.1";
	private static final String MONITOR_ID = "myConnecctor1.connector_monitor_ecs1-01_1.1";
	private static final String ECS1_01 = "ecs1-01";
	private static final String MY_CONNECTOR_NAME = "myConnecctor.connector";
	private static final String VALUE_TABLE = "MonitorType.Collect.Source(1)";
	private static final String DEVICE_ID = "deviceId";
	private static final ParameterState UNKNOWN_STATUS_WARN = ParameterState.WARN;

	private static Long collectTime = new Date().getTime();

	private static  Map<String, String> mapping = Map.of(
			DEVICE_ID, VALUETABLE_COLUMN_1,
			HardwareConstants.STATUS_PARAMETER, VALUETABLE_COLUMN_2, 
			HardwareConstants.STATUS_INFORMATION_PARAMETER, VALUETABLE_COLUMN_3,
			HardwareConstants.INTRUSION_STATUS_PARAMETER, VALUETABLE_COLUMN_4,
			HardwareConstants.POWER_CONSUMPTION_PARAMETER, VALUETABLE_COLUMN_5);

	private static List<String> row = Arrays.asList(MONITOR_DEVICE_ID,
			OK_RAW_STATUS,
			OPERABLE,
			OK_RAW_STATUS,
			POWER_CONSUMPTION);

	private static IParameterValue powerConsumptionParam = NumberParam
			.builder()
			.name(HardwareConstants.POWER_CONSUMPTION_PARAMETER)
			.collectTime(collectTime)
			.unit(HardwareConstants.POWER_CONSUMPTION_PARAMETER_UNIT)
			.value(Double.parseDouble(POWER_CONSUMPTION))
			.rawValue(Double.parseDouble(POWER_CONSUMPTION))
			.build();

	private static IParameterValue intructionStatusParam = StatusParam
			.builder()
			.name(HardwareConstants.INTRUSION_STATUS_PARAMETER)
			.collectTime(collectTime)
			.state(ParameterState.OK)
			.unit(HardwareConstants.INTRUSION_STATUS_PARAMETER_UNIT)
			.statusInformation("intrusionStatus: 0 (No Intrusion Detected)")
			.build();

	private static IParameterValue statusParam = StatusParam
			.builder()
			.name(HardwareConstants.STATUS_PARAMETER)
			.collectTime(collectTime)
			.state(ParameterState.OK)
			.unit(HardwareConstants.STATUS_PARAMETER_UNIT)
			.statusInformation("status: 0 (Operable)")
			.build();

	private static IParameterValue statusParamWithIntrusion = StatusParam
			.builder()
			.name(HardwareConstants.STATUS_PARAMETER)
			.collectTime(collectTime)
			.state(ParameterState.OK)
			.unit(HardwareConstants.STATUS_PARAMETER_UNIT)
			.statusInformation("status: 0 (Operable)\nintrusionStatus: 0 (No Intrusion Detected)")
			.build();


	@Test
	void testVisitConcreteConnector() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).build();
		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		assertDoesNotThrow(() -> monitorCollectVisitor.visit(new MetaConnector()));  
	}

	@Test
	void testVisitTarget() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).build();
		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		assertDoesNotThrow(() -> monitorCollectVisitor.visit(new Target()));  
	}

	@Test
	void testVisitBattery() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).monitorType(MonitorType.BATTERY).build();
		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.visit(new Battery());

		final IParameterValue actual = monitor.getParameters().get(HardwareConstants.STATUS_PARAMETER);

		assertEquals(statusParam, actual);
	}

	@Test
	void testVisitBlade() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).build();
		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.visit(new Blade());

		final IParameterValue actual = monitor.getParameters().get(HardwareConstants.STATUS_PARAMETER);

		assertEquals(statusParam, actual);
	}

	@Test
	void testVisitCpu() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).build();
		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.visit(new Cpu());

		final IParameterValue actual = monitor.getParameters().get(HardwareConstants.STATUS_PARAMETER);

		assertEquals(statusParam, actual);
	}

	@Test
	void testVisitCpuCore() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).monitorType(MonitorType.CPU_CORE).build();
		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.visit(new CpuCore());

		final IParameterValue actual = monitor.getParameters().get(HardwareConstants.STATUS_PARAMETER);

		assertEquals(statusParam, actual);
	}

	@Test
	void testVisitDiskController() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).build();
		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.visit(new DiskController());

		final IParameterValue actual = monitor.getParameters().get(HardwareConstants.STATUS_PARAMETER);

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


		final String statusInformation = new StringBuilder()
				.append("status: 0 (Operable)")
				.append("\n")
				.append("intrusionStatus: 0 (No Intrusion Detected)")
				.append("\n")
				.append("powerConsumption: 150.0 Watts")
				.toString();

		final IParameterValue expected = StatusParam
				.builder()
				.name(HardwareConstants.STATUS_PARAMETER)
				.collectTime(collectTime)
				.state(ParameterState.OK)
				.unit(HardwareConstants.STATUS_PARAMETER_UNIT)
				.statusInformation(statusInformation)
				.build();

		final IParameterValue actual = monitor.getParameters().get(HardwareConstants.STATUS_PARAMETER);

		assertEquals(expected, actual);
	}

	@Test
	void testVisitFan() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).build();
		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.visit(new Fan());

		final IParameterValue actual = monitor.getParameters().get(HardwareConstants.STATUS_PARAMETER);

		assertEquals(statusParam, actual);
	}

	@Test
	void testVisitLed() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).build();
		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.visit(new Led());

		final IParameterValue actual = monitor.getParameters().get(HardwareConstants.STATUS_PARAMETER);

		assertEquals(statusParam, actual);
	}

	@Test
	void testVisitLogicalDisk() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).build();
		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.visit(new LogicalDisk());

		final IParameterValue actual = monitor.getParameters().get(HardwareConstants.STATUS_PARAMETER);

		assertEquals(statusParam, actual);
	}

	@Test
	void testVisitLun() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).build();
		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.visit(new Lun());

		final IParameterValue actual = monitor.getParameters().get(HardwareConstants.STATUS_PARAMETER);

		assertEquals(statusParam, actual);
	}

	@Test
	void testVisitMemory() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).build();
		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.visit(new Memory());

		final IParameterValue actual = monitor.getParameters().get(HardwareConstants.STATUS_PARAMETER);

		assertEquals(statusParam, actual);
	}

	@Test
	void testVisitNetworkCard() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).build();
		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.visit(new NetworkCard());

		final IParameterValue actual = monitor.getParameters().get(HardwareConstants.STATUS_PARAMETER);

		assertEquals(statusParam, actual);
	}

	@Test
	void testVisitOtherDevice() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).build();
		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.visit(new OtherDevice());

		final IParameterValue actual = monitor.getParameters().get(HardwareConstants.STATUS_PARAMETER);

		assertEquals(statusParam, actual);
	}

	@Test
	void testVisitPhysicalDisk() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).build();
		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.visit(new PhysicalDisk());

		final IParameterValue actual = monitor.getParameters().get(HardwareConstants.STATUS_PARAMETER);

		assertEquals(statusParamWithIntrusion, actual);
	}

	@Test
	void testVisitPowerSupply() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).build();
		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.visit(new PowerSupply());

		final IParameterValue actual = monitor.getParameters().get(HardwareConstants.STATUS_PARAMETER);

		assertEquals(statusParam, actual);
	}

	@Test
	void testVisitTapeDrive() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).build();
		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.visit(new TapeDrive());

		final IParameterValue actual = monitor.getParameters().get(HardwareConstants.STATUS_PARAMETER);

		assertEquals(statusParam, actual);
	}

	@Test
	void testVisitTemperature() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).build();
		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.visit(new Temperature());

		final IParameterValue actual = monitor.getParameters().get(HardwareConstants.STATUS_PARAMETER);

		assertEquals(statusParam, actual);
	}

	@Test
	void testVisitVoltage() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).build();
		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.visit(new Voltage());

		final IParameterValue actual = monitor.getParameters().get(HardwareConstants.STATUS_PARAMETER);

		assertEquals(statusParam, actual);
	}

	@Test
	void testVisitRobotic() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).build();
		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.visit(new Robotic());

		final IParameterValue actual = monitor.getParameters().get(HardwareConstants.STATUS_PARAMETER);

		assertEquals(statusParam, actual);
	}

	@Test
	void testCollectStatusParameterStatusInformationNotCollected() {
		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).build();
			final MonitorCollectVisitor monitorCollectVisitor = new MonitorCollectVisitor(
					buildCollectMonitorInfo(hostMonitoring,
							mapping,
							monitor,
							Arrays.asList(MONITOR_DEVICE_ID,
									OK_RAW_STATUS,
									HardwareConstants.EMPTY,
									OK_RAW_STATUS,
									POWER_CONSUMPTION))
					);

			monitorCollectVisitor.collectStatusParameter(MonitorType.ENCLOSURE,
					HardwareConstants.STATUS_PARAMETER,
					HardwareConstants.STATUS_PARAMETER_UNIT);

			final Map<String, IParameterValue> parameters = monitor.getParameters();
			final StatusParam expected = StatusParam
					.builder()
					.name(HardwareConstants.STATUS_PARAMETER)
					.collectTime(collectTime)
					.state(ParameterState.OK)
					.unit(HardwareConstants.STATUS_PARAMETER_UNIT)
					.statusInformation("status: 0 (OK)")
					.build();

			final IParameterValue actual = parameters.get(HardwareConstants.STATUS_PARAMETER);

			assertEquals(expected, actual);

		}

		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).build();
			final MonitorCollectVisitor monitorCollectVisitor = new MonitorCollectVisitor(
					buildCollectMonitorInfo(hostMonitoring,
							mapping,
							monitor,
							Arrays.asList(MONITOR_DEVICE_ID,
									OK_RAW_STATUS,
									null,
									OK_RAW_STATUS,
									POWER_CONSUMPTION))
					);

			monitorCollectVisitor.collectStatusParameter(MonitorType.ENCLOSURE,
					HardwareConstants.STATUS_PARAMETER,
					HardwareConstants.STATUS_PARAMETER_UNIT);

			final Map<String, IParameterValue> parameters = monitor.getParameters();
			final StatusParam expected = StatusParam
					.builder()
					.name(HardwareConstants.STATUS_PARAMETER)
					.collectTime(collectTime)
					.state(ParameterState.OK)
					.unit(HardwareConstants.STATUS_PARAMETER_UNIT)
					.statusInformation("status: 0 (OK)")
					.build();

			final IParameterValue actual = parameters.get(HardwareConstants.STATUS_PARAMETER);

			assertEquals(expected, actual);

		}
	}

	@Test
	void testCollectStatusParameterButCannotExtractValue() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).build();
		final MonitorCollectVisitor monitorCollectVisitor = new MonitorCollectVisitor(
				buildCollectMonitorInfo(hostMonitoring,
						mapping,
						monitor,
						Collections.emptyList())
				);

		monitorCollectVisitor.collectStatusParameter(MonitorType.ENCLOSURE,
				HardwareConstants.STATUS_PARAMETER,
				HardwareConstants.STATUS_PARAMETER_UNIT);

		final Map<String, IParameterValue> parameters = monitor.getParameters();
		assertTrue(parameters.isEmpty());

	}

	@Test
	void testCollectStatusParameter() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).build();
		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.collectStatusParameter(MonitorType.ENCLOSURE,
				HardwareConstants.STATUS_PARAMETER,
				HardwareConstants.STATUS_PARAMETER_UNIT);

		final Map<String, IParameterValue> parameters = monitor.getParameters();
		assertFalse(parameters.isEmpty());

		final IParameterValue actual = parameters.get(HardwareConstants.STATUS_PARAMETER);

		assertEquals(statusParam, actual);
	}

	private static MonitorCollectVisitor buildMonitorCollectVisitor(final IHostMonitoring hostMonitoring, final Monitor monitor) {
		return new MonitorCollectVisitor(
				buildCollectMonitorInfo(hostMonitoring,
						mapping,
						monitor,
						row)
				);
	}

	@Test
	void testAppendToStatusInformation() {
		final StatusParam statusParam = StatusParam
				.builder()
				.name(HardwareConstants.STATUS_PARAMETER)
				.collectTime(collectTime)
				.state(ParameterState.OK)
				.unit(HardwareConstants.STATUS_PARAMETER_UNIT)
				.statusInformation(null)
				.build();

		MonitorCollectVisitor.appendToStatusInformation(statusParam, intructionStatusParam);

		assertNotNull(statusParam.getStatusInformation());
	}

	@Test
	void testAppendToStatusInformationNullParameters() {

		assertDoesNotThrow(() -> MonitorCollectVisitor.appendToStatusInformation(null, null));
		assertDoesNotThrow(() -> MonitorCollectVisitor.appendToStatusInformation(new StatusParam(), null));
		assertDoesNotThrow(() -> MonitorCollectVisitor.appendToStatusInformation(null, intructionStatusParam));
	}

	@Test
	void testAppendToStatusInformationNullValue() {

		final StatusParam statusParam = StatusParam
				.builder()
				.name(HardwareConstants.STATUS_PARAMETER)
				.collectTime(collectTime)
				.state(ParameterState.OK)
				.unit(HardwareConstants.STATUS_PARAMETER_UNIT)
				.statusInformation(null)
				.build();

		MonitorCollectVisitor.appendToStatusInformation(statusParam, NumberParam.builder().build());

		assertNull(statusParam.getStatusInformation());
	}

	@Test
	void testAppendValuesToStatusParameterButNoStatusParam() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = new Monitor();
		monitor.collectParameter(intructionStatusParam);
		monitor.collectParameter(powerConsumptionParam);

		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);
		monitorCollectVisitor.appendValuesToStatusParameter(HardwareConstants.INTRUSION_STATUS_PARAMETER,
				HardwareConstants.POWER_CONSUMPTION_PARAMETER);
		assertNull(monitor.getParameters().get(HardwareConstants.STATUS_PARAMETER));
	}

	@Test
	void testAppendValuesToStatusParameter() {
		final IParameterValue statusParam = StatusParam
				.builder()
				.name(HardwareConstants.STATUS_PARAMETER)
				.collectTime(collectTime)
				.state(ParameterState.OK)
				.unit(HardwareConstants.STATUS_PARAMETER_UNIT)
				.statusInformation("status: 0 (Operable)")
				.build();

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = new Monitor();
		monitor.collectParameter(statusParam);
		monitor.collectParameter(intructionStatusParam);
		monitor.collectParameter(powerConsumptionParam);

		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.appendValuesToStatusParameter(HardwareConstants.INTRUSION_STATUS_PARAMETER,
				HardwareConstants.POWER_CONSUMPTION_PARAMETER);

		final IParameterValue actual = monitor.getParameters().get(HardwareConstants.STATUS_PARAMETER);

		final String statusInformation = new StringBuilder()
				.append("status: 0 (Operable)")
				.append("\n")
				.append("intrusionStatus: 0 (No Intrusion Detected)")
				.append("\n")
				.append("powerConsumption: 150.0 Watts")
				.toString();

		final IParameterValue expected = StatusParam
				.builder()
				.name(HardwareConstants.STATUS_PARAMETER)
				.collectTime(collectTime)
				.state(ParameterState.OK)
				.unit(HardwareConstants.STATUS_PARAMETER_UNIT)
				.statusInformation(statusInformation)
				.build();

		assertEquals(expected, actual);
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
				HardwareConstants.POWER_CONSUMPTION_PARAMETER,
				HardwareConstants.POWER_CONSUMPTION_PARAMETER_UNIT);

		final Map<String, IParameterValue> parameters = monitor.getParameters();

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
				HardwareConstants.POWER_CONSUMPTION_PARAMETER,
				HardwareConstants.POWER_CONSUMPTION_PARAMETER_UNIT);

		final Map<String, IParameterValue> parameters = monitor.getParameters();

		assertTrue(parameters.isEmpty());

	}

	@Test
	void testCollectNumberParameter() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = new Monitor();
		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.collectNumberParameter(MonitorType.ENCLOSURE,
				HardwareConstants.POWER_CONSUMPTION_PARAMETER,
				HardwareConstants.POWER_CONSUMPTION_PARAMETER_UNIT);

		final Map<String, IParameterValue> parameters = monitor.getParameters();
		assertFalse(parameters.isEmpty());

		final IParameterValue actual = parameters.get(HardwareConstants.POWER_CONSUMPTION_PARAMETER);

		assertEquals(powerConsumptionParam, actual);

	}

	private static MonitorCollectInfo buildCollectMonitorInfo(final IHostMonitoring hostMonitoring, final Map<String, String> mapping,
			Monitor monitor, final List<String> row) {
		return MonitorCollectInfo
		.builder()
		.collectTime(collectTime)
		.connectorName(MY_CONNECTOR_NAME)
		.hostMonitoring(hostMonitoring)
		.hostname(ECS1_01)
		.mapping(mapping)
		.monitor(monitor)
		.row(row)
		.unknownStatus(UNKNOWN_STATUS_WARN)
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
			.hostname(ECS1_01)
			.mapping(mapping)
			.monitor(monitor)
			.row(row)
			.unknownStatus(UNKNOWN_STATUS_WARN)
			.valueTable(VALUE_TABLE)
			.build();
	}

	@Test
	void testGetIntrusionStatusInformation() {
		assertNotNull(MonitorCollectVisitor.getIntrusionStatusInformation(ParameterState.OK));
		assertNotNull(MonitorCollectVisitor.getIntrusionStatusInformation(ParameterState.WARN));
		assertNotNull(MonitorCollectVisitor.getIntrusionStatusInformation(ParameterState.ALARM));
	}

	@Test
	void testStatusParamFirstComparatorCompare() {
		assertEquals(Arrays.asList("status", "energy", "energyUsage", "intrusionStatus", "powerConsumption"),
				new Enclosure()
				.getMetaParameters()
				.values()
				.stream()
				.sorted(new MonitorCollectVisitor.StatusParamFirstComparator())
				.map(MetaParameter::getName)
				.collect(Collectors.toList()));
	}

	@Test
	void testUpdateNumberParameter() {
		{
			final Monitor monitor = Monitor.builder().build();
			MonitorCollectVisitor.updateNumberParameter(monitor,
					HardwareConstants.ENERGY_USAGE_PARAMETER,
					HardwareConstants.ENERGY_USAGE_PARAMETER_UNIT,
					collectTime,
					100D,
					1500D);

			final NumberParam expected = NumberParam
					.builder()
					.name(HardwareConstants.ENERGY_USAGE_PARAMETER)
					.unit(HardwareConstants.ENERGY_USAGE_PARAMETER_UNIT)
					.collectTime(collectTime)
					.value(100D)
					.rawValue(1500D)
					.build();

			assertEquals(expected, monitor.getParameter(HardwareConstants.ENERGY_USAGE_PARAMETER, NumberParam.class));
		}

		{
			final NumberParam previousParameter = NumberParam
					.builder()
					.name(HardwareConstants.ENERGY_USAGE_PARAMETER)
					.unit(HardwareConstants.ENERGY_USAGE_PARAMETER_UNIT)
					.collectTime(collectTime)
					.value(100D)
					.rawValue(1500D)
					.build();

			previousParameter.reset();

			final Monitor monitor = Monitor.builder().parameters(new HashMap<>(
					Map.of(HardwareConstants.ENERGY_USAGE_PARAMETER, previousParameter)))
					.build();
			MonitorCollectVisitor.updateNumberParameter(monitor,
					HardwareConstants.ENERGY_USAGE_PARAMETER,
					HardwareConstants.ENERGY_USAGE_PARAMETER_UNIT,
					collectTime + (2 * 60 * 1000),
					50D,
					1550D);

			final NumberParam expected = NumberParam
					.builder()
					.name(HardwareConstants.ENERGY_USAGE_PARAMETER)
					.unit(HardwareConstants.ENERGY_USAGE_PARAMETER_UNIT)
					.collectTime(collectTime + (2 * 60 * 1000))
					.value(50D)
					.rawValue(1550D)
					.build();
			expected.setPreviousCollectTime(collectTime);
			expected.setPreviousRawValue(1500D);

			assertEquals(expected, monitor.getParameter(HardwareConstants.ENERGY_USAGE_PARAMETER, NumberParam.class));
		}
	}

	@Test
	void testUpdateStatusParameter() {
		{
			final Monitor monitor = Monitor.builder().build();
			MonitorCollectVisitor.updateStatusParameter(monitor, HardwareConstants.STATUS_PARAMETER,
					HardwareConstants.STATUS_PARAMETER_UNIT, collectTime, ParameterState.OK, "Operable");

			final StatusParam expected = StatusParam
					.builder()
					.name(HardwareConstants.STATUS_PARAMETER)
					.collectTime(collectTime)
					.state(ParameterState.OK)
					.unit(HardwareConstants.STATUS_PARAMETER_UNIT)
					.statusInformation("status: 0 (Operable)")
					.build();

			assertEquals(expected, monitor.getParameter(HardwareConstants.STATUS_PARAMETER, StatusParam.class));
		}

		{
			final StatusParam previousParameter = StatusParam.builder()
					.name(HardwareConstants.STATUS_PARAMETER)
					.collectTime(collectTime)
					.state(ParameterState.ALARM)
					.unit(HardwareConstants.STATUS_PARAMETER_UNIT)
					.statusInformation("status: 2 (DOWN)").build();

			previousParameter.reset();
			
			final Monitor monitor = Monitor.builder().parameters(new HashMap<>(
					Map.of(HardwareConstants.STATUS_PARAMETER, previousParameter)))
					.build();

			MonitorCollectVisitor.updateStatusParameter(monitor, HardwareConstants.STATUS_PARAMETER,
					HardwareConstants.STATUS_PARAMETER_UNIT, collectTime, ParameterState.OK, "Operable");

			final StatusParam expected = StatusParam
					.builder()
					.name(HardwareConstants.STATUS_PARAMETER)
					.collectTime(collectTime)
					.state(ParameterState.OK)
					.unit(HardwareConstants.STATUS_PARAMETER_UNIT)
					.statusInformation("status: 0 (Operable)")
					.build();

			expected.setPreviousState(ParameterState.ALARM);

			assertEquals(expected, monitor.getParameter(HardwareConstants.STATUS_PARAMETER, StatusParam.class));
		}
	}

	@Test
	void testCollectPowerWithEnergyUsageFirstCollect() {

		final Monitor monitor = Monitor.builder()
				.monitorType(MonitorType.ENCLOSURE)
				.build();

		MonitorCollectVisitor.collectPowerFromEnergyUsage(monitor, collectTime, 3138.358D, ECS1_01);

		assertNull(monitor.getParameter(HardwareConstants.ENERGY_USAGE_PARAMETER, NumberParam.class).getValue());
		assertNull(monitor.getParameter(HardwareConstants.POWER_CONSUMPTION_PARAMETER, NumberParam.class));
		assertEquals(3138.358D, monitor.getParameter(HardwareConstants.ENERGY_USAGE_PARAMETER, NumberParam.class).getRawValue());

	}

	@Test
	void testCollectPowerWithEnergyUsage() {

		final NumberParam energyUsage = NumberParam
				.builder()
				.name(HardwareConstants.ENERGY_USAGE_PARAMETER)
				.unit(HardwareConstants.ENERGY_USAGE_PARAMETER_UNIT)
				.collectTime(collectTime)
				.value(null)
				.rawValue(3138.358D) // kWatt-hours
				.build();
		energyUsage.reset();

		final Monitor monitor = Monitor.builder().monitorType(MonitorType.ENCLOSURE).parameters(new HashMap<>(
				Map.of(HardwareConstants.ENERGY_USAGE_PARAMETER, energyUsage)))
				.build();

		MonitorCollectVisitor.collectPowerFromEnergyUsage(monitor, collectTime + (2 * 60 * 1000), 3138.360, ECS1_01);

		Double joules = monitor.getParameter(HardwareConstants.ENERGY_USAGE_PARAMETER, NumberParam.class).getValue();
		joules  = Math.round(joules * 100000D) / 100000D;

		assertEquals(7200, joules); // Joules (Energy)

		final double watts = Math.round(monitor.getParameter(HardwareConstants.POWER_CONSUMPTION_PARAMETER, NumberParam.class).getValue());
		assertEquals(60.0, watts); // Watts (Power)

	}

	@Test
	void testCollectEnergyUsageWithPowerFirstCollect() {

		final Monitor monitor = Monitor.builder()
				.monitorType(MonitorType.ENCLOSURE)
				.build();

		MonitorCollectVisitor.collectEnergyUsageFromPower(monitor, collectTime, 60D, ECS1_01);

		assertNull(monitor.getParameter(HardwareConstants.ENERGY_USAGE_PARAMETER, NumberParam.class));
		assertEquals(60, monitor.getParameter(HardwareConstants.POWER_CONSUMPTION_PARAMETER, NumberParam.class).getValue());
		assertEquals(60, monitor.getParameter(HardwareConstants.POWER_CONSUMPTION_PARAMETER, NumberParam.class).getRawValue());
	}

	@Test
	void testCollectEnergyUsageWithPower() {

		final NumberParam powerConsumption = NumberParam
				.builder()
				.name(HardwareConstants.POWER_CONSUMPTION_PARAMETER)
				.unit(HardwareConstants.POWER_CONSUMPTION_PARAMETER_UNIT)
				.collectTime(collectTime)
				.value(null)
				.rawValue(60.0)
				.build();

		powerConsumption.reset();

		final NumberParam energyUsage = NumberParam
			.builder()
			.name(HardwareConstants.ENERGY_USAGE_PARAMETER)
			.unit(HardwareConstants.ENERGY_USAGE_PARAMETER_UNIT)
			.collectTime(collectTime)
			.value(null)
			.rawValue(999.0)
			.build();

		energyUsage.reset();

		final Monitor monitor = Monitor
			.builder()
			.monitorType(MonitorType.ENCLOSURE)
			.parameters(new HashMap<>(
				Map
					.of(HardwareConstants.POWER_CONSUMPTION_PARAMETER, powerConsumption,
						HardwareConstants.ENERGY_USAGE_PARAMETER, energyUsage)))
			.build();

		MonitorCollectVisitor.collectEnergyUsageFromPower(monitor, collectTime + (2 * 60 * 1000), 64D, ECS1_01);

		assertEquals(64, monitor.getParameter(HardwareConstants.POWER_CONSUMPTION_PARAMETER, NumberParam.class).getValue()); // Watts
		assertEquals(64, monitor.getParameter(HardwareConstants.POWER_CONSUMPTION_PARAMETER, NumberParam.class).getRawValue());

		assertEquals(27648.0, monitor.getParameter(HardwareConstants.ENERGY_USAGE_PARAMETER, NumberParam.class).getValue()); // Joules
		assertEquals(27651.5964, monitor.getParameter(HardwareConstants.ENERGY_PARAMETER, NumberParam.class).getValue()); // Joules
	}

	@Test
	void testCollectPowerConsumptionViaEnergyUsageFirstCollect() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor
				.builder()
				.id(MONITOR_ID)
				.monitorType(MonitorType.ENCLOSURE)
				.build();
		final Map<String, String> mapping = Map.of(
				DEVICE_ID, VALUETABLE_COLUMN_1,
				HardwareConstants.ENERGY_USAGE_PARAMETER, VALUETABLE_COLUMN_2);

		final List<String> row = Arrays.asList(MONITOR_DEVICE_ID, "3138.358");

		final MonitorCollectVisitor monitorCollectVisitor = new MonitorCollectVisitor(
				buildCollectMonitorInfo(hostMonitoring, mapping, monitor, row));

		monitorCollectVisitor.collectPowerConsumption();

		assertEquals(3138.358D,
				monitor.getParameter(HardwareConstants.ENERGY_USAGE_PARAMETER, NumberParam.class).getRawValue());
	}

	@Test
	void testCollectPowerConsumptionViaEnergyUsage() {
		final NumberParam energyUsage = NumberParam
				.builder()
				.name(HardwareConstants.ENERGY_USAGE_PARAMETER)
				.unit(HardwareConstants.ENERGY_USAGE_PARAMETER_UNIT)
				.collectTime(collectTime - (2 * 60 * 1000))
				.value(null)
				.rawValue(3138.358D)
				.build();

		energyUsage.reset();

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor
				.builder()
				.id(MONITOR_ID)
				.monitorType(MonitorType.ENCLOSURE)
				.parameters(new HashMap<>(Map.of(HardwareConstants.ENERGY_USAGE_PARAMETER, energyUsage)))
				.build();
		final Map<String, String> mapping = Map.of(
				DEVICE_ID, VALUETABLE_COLUMN_1,
				HardwareConstants.ENERGY_USAGE_PARAMETER, VALUETABLE_COLUMN_2);

		final List<String> row = Arrays.asList(MONITOR_DEVICE_ID, "3138.360");

		final MonitorCollectVisitor monitorCollectVisitor = new MonitorCollectVisitor(
				buildCollectMonitorInfo(hostMonitoring, mapping, monitor, row));

		monitorCollectVisitor.collectPowerConsumption();

		Double joules = monitor.getParameter(HardwareConstants.ENERGY_USAGE_PARAMETER, NumberParam.class).getValue();
		joules = Math.round(joules * 100000D) / 100000D;
		assertEquals(7200.0, joules);
		assertEquals(60, Math.round(monitor.getParameter(HardwareConstants.POWER_CONSUMPTION_PARAMETER, NumberParam.class).getValue()));
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
				HardwareConstants.POWER_CONSUMPTION_PARAMETER, VALUETABLE_COLUMN_2);

		final List<String> row = Arrays.asList(MONITOR_DEVICE_ID, POWER_CONSUMPTION);

		final MonitorCollectVisitor monitorCollectVisitor = new MonitorCollectVisitor(
				buildCollectMonitorInfo(hostMonitoring, mapping, monitor, row));

		monitorCollectVisitor.collectPowerConsumption();

		assertEquals(Double.parseDouble(POWER_CONSUMPTION),
				monitor.getParameter(HardwareConstants.POWER_CONSUMPTION_PARAMETER, NumberParam.class).getRawValue());
		assertEquals(Double.parseDouble(POWER_CONSUMPTION),
						monitor.getParameter(HardwareConstants.POWER_CONSUMPTION_PARAMETER, NumberParam.class).getValue());
	}

	@Test
	void testCollectPowerConsumptionViaPower() {
		final NumberParam powerConsumption = NumberParam
				.builder()
				.name(HardwareConstants.POWER_CONSUMPTION_PARAMETER)
				.unit(HardwareConstants.POWER_CONSUMPTION_PARAMETER)
				.collectTime(collectTime - (2 * 60 * 1000))
				.value(null)
				.rawValue(60.0)
				.build();

		powerConsumption.reset();

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor
				.builder()
				.id(MONITOR_ID)
				.monitorType(MonitorType.ENCLOSURE)
				.parameters(new HashMap<>(Map.of(HardwareConstants.POWER_CONSUMPTION_PARAMETER, powerConsumption)))
				.build();
		final Map<String, String> mapping = Map.of(
				DEVICE_ID, VALUETABLE_COLUMN_1,
				HardwareConstants.POWER_CONSUMPTION_PARAMETER, VALUETABLE_COLUMN_2);

		final List<String> row = Arrays.asList(MONITOR_DEVICE_ID, "60.2");

		final MonitorCollectVisitor monitorCollectVisitor = new MonitorCollectVisitor(
				buildCollectMonitorInfo(hostMonitoring, mapping, monitor, row));

		monitorCollectVisitor.collectPowerConsumption();

		assertEquals(60.2, monitor.getParameter(HardwareConstants.POWER_CONSUMPTION_PARAMETER, NumberParam.class).getValue());
		assertEquals(60.2, monitor.getParameter(HardwareConstants.POWER_CONSUMPTION_PARAMETER, NumberParam.class).getRawValue());

		assertEquals(26006, Math.round(monitor.getParameter(HardwareConstants.ENERGY_USAGE_PARAMETER, NumberParam.class).getValue()));
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
				HardwareConstants.POWER_CONSUMPTION_PARAMETER, VALUETABLE_COLUMN_2);

		final List<String> row = Arrays.asList(MONITOR_DEVICE_ID, "");

		final MonitorCollectVisitor monitorCollectVisitor = new MonitorCollectVisitor(
				buildCollectMonitorInfo(hostMonitoring, mapping, monitor, row));

		monitorCollectVisitor.collectPowerConsumption();

		assertNull(monitor.getParameter(HardwareConstants.POWER_CONSUMPTION_PARAMETER, NumberParam.class));
		assertNull(monitor.getParameter(HardwareConstants.ENERGY_USAGE_PARAMETER, NumberParam.class));

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
				HardwareConstants.POWER_CONSUMPTION_PARAMETER, VALUETABLE_COLUMN_2);

		final List<String> row = Arrays.asList(MONITOR_DEVICE_ID, "-1");

		final MonitorCollectVisitor monitorCollectVisitor = new MonitorCollectVisitor(
				buildCollectMonitorInfo(hostMonitoring, mapping, monitor, row));

		monitorCollectVisitor.collectPowerConsumption();

		assertNull(monitor.getParameter(HardwareConstants.POWER_CONSUMPTION_PARAMETER, NumberParam.class));
		assertNull(monitor.getParameter(HardwareConstants.ENERGY_USAGE_PARAMETER, NumberParam.class));

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
				HardwareConstants.ENERGY_USAGE_PARAMETER, VALUETABLE_COLUMN_2);

		final List<String> row = Arrays.asList(MONITOR_DEVICE_ID, "");

		final MonitorCollectVisitor monitorCollectVisitor = new MonitorCollectVisitor(
				buildCollectMonitorInfo(hostMonitoring, mapping, monitor, row));

		monitorCollectVisitor.collectPowerConsumption();

		assertNull(monitor.getParameter(HardwareConstants.POWER_CONSUMPTION_PARAMETER, NumberParam.class));
		assertNull(monitor.getParameter(HardwareConstants.ENERGY_USAGE_PARAMETER, NumberParam.class));

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
				HardwareConstants.ENERGY_USAGE_PARAMETER, VALUETABLE_COLUMN_2);

		final List<String> row = Arrays.asList(MONITOR_DEVICE_ID, "-1");

		final MonitorCollectVisitor monitorCollectVisitor = new MonitorCollectVisitor(
				buildCollectMonitorInfo(hostMonitoring, mapping, monitor, row));

		monitorCollectVisitor.collectPowerConsumption();

		assertNull(monitor.getParameter(HardwareConstants.POWER_CONSUMPTION_PARAMETER, NumberParam.class));
		assertNull(monitor.getParameter(HardwareConstants.ENERGY_USAGE_PARAMETER, NumberParam.class));

	}

	@Test
	void testCollectBatteryCharge() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).monitorType(MonitorType.BATTERY).build();
		MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		// No charge value
		monitorCollectVisitor.collectBatteryCharge();
		NumberParam chargeParameter = monitor.getParameter(HardwareConstants.CHARGE_PARAMETER, NumberParam.class);
		assertNull(chargeParameter);

		// Charge value collected, value is lower than 100
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(HardwareConstants.CHARGE_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList(CHARGE))
		);
		monitorCollectVisitor.collectBatteryCharge();
		chargeParameter = monitor.getParameter(HardwareConstants.CHARGE_PARAMETER, NumberParam.class);
		assertNotNull(chargeParameter);
		assertEquals(39.0, chargeParameter.getRawValue());
		assertEquals(39.0, chargeParameter.getValue());

		// Charge value collected, value is greter than 100
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(HardwareConstants.CHARGE_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList("125"))
		);
		monitorCollectVisitor.collectBatteryCharge();
		chargeParameter = monitor.getParameter(HardwareConstants.CHARGE_PARAMETER, NumberParam.class);
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
		NumberParam timeLeftParameter = monitor.getParameter(HardwareConstants.TIME_LEFT_PARAMETER, NumberParam.class);
		assertNull(timeLeftParameter);

		// Time left value collected
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(HardwareConstants.TIME_LEFT_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList(TIME_LEFT))
		);
		monitorCollectVisitor.collectBatteryTimeLeft();
		timeLeftParameter = monitor.getParameter(HardwareConstants.TIME_LEFT_PARAMETER, NumberParam.class);
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
		NumberParam usedTimePercentParameter = monitor.getParameter(HardwareConstants.USED_TIME_PERCENT_PARAMETER,
			NumberParam.class);
		assertNull(usedTimePercentParameter);

		// usedTimePercentRaw is not null, usedTimePercentPrevious is null
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(HardwareConstants.USED_TIME_PERCENT_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList("12"))
		);
		monitorCollectVisitor.collectCpuCoreUsedTimePercent();
		usedTimePercentParameter = monitor.getParameter(HardwareConstants.USED_TIME_PERCENT_PARAMETER, NumberParam.class);
		assertNotNull(usedTimePercentParameter);
		assertEquals(12.0, usedTimePercentParameter.getRawValue());
		assertNull(usedTimePercentParameter.getValue());

		// usedTimePercentRaw is not null, usedTimePercentPrevious is not null, collectTimePrevious is null
		usedTimePercentParameter.reset();
		usedTimePercentParameter.setPreviousCollectTime(null);
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(HardwareConstants.USED_TIME_PERCENT_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList("42"))
		);
		monitorCollectVisitor.collectCpuCoreUsedTimePercent();
		usedTimePercentParameter = monitor.getParameter(HardwareConstants.USED_TIME_PERCENT_PARAMETER, NumberParam.class);
		assertNotNull(usedTimePercentParameter);
		assertNull(usedTimePercentParameter.getRawValue());
		assertNull(usedTimePercentParameter.getValue());

		// usedTimePercentRaw is not null, usedTimePercentPrevious is not null, collectTimePrevious is not null
		// timeDeltaInSeconds == 0.0
		usedTimePercentParameter = NumberParam.builder().name(HardwareConstants.USED_TIME_PERCENT_PARAMETER).build();
		usedTimePercentParameter.setPreviousRawValue(12.0);
		usedTimePercentParameter.setPreviousCollectTime(System.currentTimeMillis() - 120000L); // 2 minutes ago
		monitor.addParameter(usedTimePercentParameter);
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(HardwareConstants.USED_TIME_PERCENT_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList("42"),
				usedTimePercentParameter.getPreviousCollectTime())
		);
		monitorCollectVisitor.collectCpuCoreUsedTimePercent();
		usedTimePercentParameter = monitor.getParameter(HardwareConstants.USED_TIME_PERCENT_PARAMETER, NumberParam.class);
		assertNotNull(usedTimePercentParameter);
		assertNull(usedTimePercentParameter.getRawValue());
		assertNull(usedTimePercentParameter.getValue());

		// OK
		usedTimePercentParameter = NumberParam.builder().name(HardwareConstants.USED_TIME_PERCENT_PARAMETER).build();
		usedTimePercentParameter.setPreviousRawValue(12.0);
		usedTimePercentParameter.setPreviousCollectTime(System.currentTimeMillis() - 120000L); // 2 minutes ago
		monitor.addParameter(usedTimePercentParameter);
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(HardwareConstants.USED_TIME_PERCENT_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList("42"),
				usedTimePercentParameter.getPreviousCollectTime() + 120000L)
		);
		monitorCollectVisitor.collectCpuCoreUsedTimePercent();
		usedTimePercentParameter = monitor.getParameter(HardwareConstants.USED_TIME_PERCENT_PARAMETER, NumberParam.class);
		assertNotNull(usedTimePercentParameter);
		assertEquals(42.0, usedTimePercentParameter.getRawValue());
		assertEquals(25.0, usedTimePercentParameter.getValue());
	}
}
