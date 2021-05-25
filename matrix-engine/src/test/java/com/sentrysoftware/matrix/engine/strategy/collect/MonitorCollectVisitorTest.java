package com.sentrysoftware.matrix.engine.strategy.collect;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.common.meta.monitor.Battery;
import com.sentrysoftware.matrix.common.meta.monitor.Blade;
import com.sentrysoftware.matrix.common.meta.monitor.MetaConnector;
import com.sentrysoftware.matrix.common.meta.monitor.Cpu;
import com.sentrysoftware.matrix.common.meta.monitor.CpuCore;
import com.sentrysoftware.matrix.common.meta.monitor.DiskController;
import com.sentrysoftware.matrix.common.meta.monitor.DiskEnclosure;
import com.sentrysoftware.matrix.common.meta.monitor.Enclosure;
import com.sentrysoftware.matrix.common.meta.monitor.Fan;
import com.sentrysoftware.matrix.common.meta.monitor.Led;
import com.sentrysoftware.matrix.common.meta.monitor.LogicalDisk;
import com.sentrysoftware.matrix.common.meta.monitor.Lun;
import com.sentrysoftware.matrix.common.meta.monitor.Memory;
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

class MonitorCollectVisitorTest {

	private static final String ENERGY_USAGE_15000_JOULES = "15000";
	private static final String OK_RAW_STATUS = "OK";
	private static final String OPERABLE = "Operable";
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
			HardwareConstants.ENERGY_USAGE_PARAMETER, VALUETABLE_COLUMN_5);

	private static List<String> row = Arrays.asList(MONITOR_DEVICE_ID,
			OK_RAW_STATUS,
			OPERABLE,
			OK_RAW_STATUS,
			ENERGY_USAGE_15000_JOULES);

	private static IParameterValue energyUsageParam = NumberParam
			.builder()
			.name(HardwareConstants.ENERGY_USAGE_PARAMETER)
			.collectTime(collectTime)
			.unit(HardwareConstants.ENERGY_USAGE_PARAMETER_UNIT)
			.value(Double.parseDouble(ENERGY_USAGE_15000_JOULES))
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
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).build();
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
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).build();
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
	void testVisitDiskEnclosure() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).build();
		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.visit(new DiskEnclosure());

		final IParameterValue actual = monitor.getParameters().get(HardwareConstants.STATUS_PARAMETER);

		assertEquals(statusParam, actual);
	}

	@Test
	void testVisitEnclosure() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).build();
		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.visit(new Enclosure());


		final String statusInformation = new StringBuilder()
				.append("status: 0 (Operable)")
				.append("\n")
				.append("intrusionStatus: 0 (No Intrusion Detected)")
				.append("\n")
				.append("energyUsage: 15000.0 Joules")
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
									ENERGY_USAGE_15000_JOULES))
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
									ENERGY_USAGE_15000_JOULES))
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

		assertEquals("intrusionStatus: 0 (No Intrusion Detected)", statusParam.getStatusInformation());
	}

	@Test
	void testAppendToStatusInformationNullParameters() {

		assertDoesNotThrow(() -> MonitorCollectVisitor.appendToStatusInformation(null, null));
		assertDoesNotThrow(() -> MonitorCollectVisitor.appendToStatusInformation(new StatusParam(), null));
		assertDoesNotThrow(() -> MonitorCollectVisitor.appendToStatusInformation(null, intructionStatusParam));
	}

	@Test
	void testAppendValuesToStatusParameterButNoStatusParam() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = new Monitor();
		monitor.addParameter(intructionStatusParam);
		monitor.addParameter(energyUsageParam);

		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);
		monitorCollectVisitor.appendValuesToStatusParameter(HardwareConstants.INTRUSION_STATUS_PARAMETER,
				HardwareConstants.ENERGY_USAGE_PARAMETER);
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
		monitor.addParameter(statusParam);
		monitor.addParameter(intructionStatusParam);
		monitor.addParameter(energyUsageParam);

		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.appendValuesToStatusParameter(HardwareConstants.INTRUSION_STATUS_PARAMETER,
				HardwareConstants.ENERGY_USAGE_PARAMETER);

		final IParameterValue actual = monitor.getParameters().get(HardwareConstants.STATUS_PARAMETER);

		final String statusInformation = new StringBuilder()
				.append("status: 0 (Operable)")
				.append("\n")
				.append("intrusionStatus: 0 (No Intrusion Detected)")
				.append("\n")
				.append("energyUsage: 15000.0 Joules")
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
				HardwareConstants.ENERGY_USAGE_PARAMETER,
				HardwareConstants.ENERGY_USAGE_PARAMETER_UNIT);

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
				HardwareConstants.ENERGY_USAGE_PARAMETER,
				HardwareConstants.ENERGY_USAGE_PARAMETER_UNIT);

		final Map<String, IParameterValue> parameters = monitor.getParameters();

		assertTrue(parameters.isEmpty());

	}

	@Test
	void testCollectNumberParameter() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = new Monitor();
		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.collectNumberParameter(MonitorType.ENCLOSURE,
				HardwareConstants.ENERGY_USAGE_PARAMETER,
				HardwareConstants.ENERGY_USAGE_PARAMETER_UNIT);

		final Map<String, IParameterValue> parameters = monitor.getParameters();
		assertFalse(parameters.isEmpty());

		final IParameterValue actual = parameters.get(HardwareConstants.ENERGY_USAGE_PARAMETER);

		assertEquals(energyUsageParam, actual);

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

	@Test
	void testGetIntrusionStatusInformation() {
		assertEquals("No Intrusion Detected", MonitorCollectVisitor.getIntrusionStatusInformation(ParameterState.OK));
		assertEquals("Unexpected Intrusion Status", MonitorCollectVisitor.getIntrusionStatusInformation(ParameterState.WARN));
		assertEquals("Intrusion Detected", MonitorCollectVisitor.getIntrusionStatusInformation(ParameterState.ALARM));
	}

	@Test
	void testStatusParamFirstComparatorCompare() {
		assertEquals(Arrays.asList("status", "energyUsage", "intrusionStatus", "powerConsumption"),
				new Enclosure()
				.getMetaParameters()
				.values()
				.stream()
				.sorted(new MonitorCollectVisitor.StatusParamFirstComparator())
				.map(MetaParameter::getName)
				.collect(Collectors.toList()));
	}
}
