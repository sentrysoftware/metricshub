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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.COLOR_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.STATUS_PARAMETER;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
	private static final String ECS1_01 = "ecs1-01";
	private static final String MY_CONNECTOR_NAME = "myConnecctor.connector";
	private static final String VALUE_TABLE = "MonitorType.Collect.Source(1)";
	private static final String DEVICE_ID = "deviceId";
	private static final ParameterState UNKNOWN_STATUS_WARN = ParameterState.WARN;
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

	private static Long collectTime = new Date().getTime();

	private static Map<String, String> mapping = Map.of(
			DEVICE_ID, VALUETABLE_COLUMN_1,
			STATUS_PARAMETER, VALUETABLE_COLUMN_2,
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
			.name(STATUS_PARAMETER)
			.collectTime(collectTime)
			.state(ParameterState.OK)
			.unit(HardwareConstants.STATUS_PARAMETER_UNIT)
			.statusInformation("status: 0 (Operable)")
			.build();

	private static IParameterValue statusParamWithIntrusion = StatusParam
			.builder()
			.name(STATUS_PARAMETER)
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

		final IParameterValue actual = monitor.getParameters().get(STATUS_PARAMETER);

		assertEquals(statusParam, actual);
	}

	@Test
	void testVisitBlade() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).build();
		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.visit(new Blade());

		final IParameterValue actual = monitor.getParameters().get(STATUS_PARAMETER);

		assertEquals(statusParam, actual);
	}

	@Test
	void testVisitCpu() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).build();
		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.visit(new Cpu());

		final IParameterValue actual = monitor.getParameters().get(STATUS_PARAMETER);

		assertEquals(statusParam, actual);
	}

	@Test
	void testVisitCpuCore() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).monitorType(MonitorType.CPU_CORE).build();
		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.visit(new CpuCore());

		final IParameterValue actual = monitor.getParameters().get(STATUS_PARAMETER);

		assertEquals(statusParam, actual);
	}

	@Test
	void testVisitDiskController() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).build();
		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.visit(new DiskController());

		final IParameterValue actual = monitor.getParameters().get(STATUS_PARAMETER);

		assertEquals(statusParam, actual);

		assertEquals(15.0, CollectHelper.getNumberParamValue(monitor, HardwareConstants.POWER_CONSUMPTION_PARAMETER));
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
				.name(STATUS_PARAMETER)
				.collectTime(collectTime)
				.state(ParameterState.OK)
				.unit(HardwareConstants.STATUS_PARAMETER_UNIT)
				.statusInformation(statusInformation)
				.build();

		final IParameterValue actual = monitor.getParameters().get(STATUS_PARAMETER);

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

		final IParameterValue actual = monitor.getParameters().get(STATUS_PARAMETER);

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

		final IParameterValue expected = StatusParam
			.builder()
			.name(STATUS_PARAMETER)
			.collectTime(collectTime)
			.state(ParameterState.OK)
			.unit(HardwareConstants.STATUS_PARAMETER_UNIT)
			.statusInformation("status: 0 (OK)")
			.build();

		final IParameterValue actual = monitor.getParameters().get(STATUS_PARAMETER);

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

		final IParameterValue actual = monitor.getParameters().get(STATUS_PARAMETER);

		assertEquals(statusParam, actual);
	}

	@Test
	void testVisitLun() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).build();
		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.visit(new Lun());

		final IParameterValue actual = monitor.getParameters().get(STATUS_PARAMETER);

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

		final IParameterValue actual = monitor.getParameters().get(STATUS_PARAMETER);

		assertEquals(statusParam, actual);
	
		assertEquals(4.0, CollectHelper.getNumberParamValue(monitor, HardwareConstants.POWER_CONSUMPTION_PARAMETER));
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

		final IParameterValue actual = monitor.getParameters().get(STATUS_PARAMETER);

		assertEquals(statusParam, actual);
	}

	@Test
	void testVisitOtherDevice() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).monitorType(MonitorType.OTHER_DEVICE).build();
		MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.visit(new OtherDevice());

		final IParameterValue actual = monitor.getParameters().get(STATUS_PARAMETER);

		assertEquals(statusParam, actual);

		monitorCollectVisitor = new MonitorCollectVisitor(
				buildCollectMonitorInfo(hostMonitoring,
						Map.of(HardwareConstants.POWER_CONSUMPTION_PARAMETER, VALUETABLE_COLUMN_1),
						monitor,
						Collections.singletonList(POWER_CONSUMPTION))
				);
		monitorCollectVisitor.visit(new OtherDevice());
		NumberParam powerConsumptionParameter = monitor.getParameter(HardwareConstants.POWER_CONSUMPTION_PARAMETER, NumberParam.class);
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

		final IParameterValue actual = monitor.getParameters().get(STATUS_PARAMETER);

		assertEquals(statusParamWithIntrusion, actual);
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

		final IParameterValue actual = monitor.getParameters().get(STATUS_PARAMETER);

		assertEquals(statusParam, actual);
	}

	@Test
	void testVisitRobotic() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor
				.builder().
				id(MONITOR_ID)
				.monitorType(MonitorType.ROBOTIC)
				.build();
		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.visit(new Robotic());

		final IParameterValue actual = monitor.getParameters().get(STATUS_PARAMETER);

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

		final IParameterValue actual = monitor.getParameters().get(STATUS_PARAMETER);

		assertEquals(statusParam, actual);
	}

	@Test
	void testVisitTemperature() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).monitorType(MonitorType.TEMPERATURE).build();
		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.visit(new Temperature());

		final IParameterValue actual = monitor.getParameters().get(STATUS_PARAMETER);

		assertEquals(statusParam, actual);
	}

	@Test
	void testVisitVoltage() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).monitorType(MonitorType.VOLTAGE).build();
		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.visit(new Voltage());

		final IParameterValue actual = monitor.getParameters().get(STATUS_PARAMETER);

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
					STATUS_PARAMETER,
					HardwareConstants.STATUS_PARAMETER_UNIT);

			final Map<String, IParameterValue> parameters = monitor.getParameters();
			final StatusParam expected = StatusParam
					.builder()
					.name(STATUS_PARAMETER)
					.collectTime(collectTime)
					.state(ParameterState.OK)
					.unit(HardwareConstants.STATUS_PARAMETER_UNIT)
					.statusInformation("status: 0 (OK)")
					.build();

			final IParameterValue actual = parameters.get(STATUS_PARAMETER);

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
					STATUS_PARAMETER,
					HardwareConstants.STATUS_PARAMETER_UNIT);

			final Map<String, IParameterValue> parameters = monitor.getParameters();
			final StatusParam expected = StatusParam
					.builder()
					.name(STATUS_PARAMETER)
					.collectTime(collectTime)
					.state(ParameterState.OK)
					.unit(HardwareConstants.STATUS_PARAMETER_UNIT)
					.statusInformation("status: 0 (OK)")
					.build();

			final IParameterValue actual = parameters.get(STATUS_PARAMETER);

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
				STATUS_PARAMETER,
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
				STATUS_PARAMETER,
				HardwareConstants.STATUS_PARAMETER_UNIT);

		final Map<String, IParameterValue> parameters = monitor.getParameters();
		assertFalse(parameters.isEmpty());

		final IParameterValue actual = parameters.get(STATUS_PARAMETER);

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
				.name(STATUS_PARAMETER)
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
				.name(STATUS_PARAMETER)
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
		assertNull(monitor.getParameters().get(STATUS_PARAMETER));
	}

	@Test
	void testAppendValuesToStatusParameter() {
		final IParameterValue statusParam = StatusParam
				.builder()
				.name(STATUS_PARAMETER)
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

		final IParameterValue actual = monitor.getParameters().get(STATUS_PARAMETER);

		final String statusInformation = new StringBuilder()
				.append("status: 0 (Operable)")
				.append("\n")
				.append("intrusionStatus: 0 (No Intrusion Detected)")
				.append("\n")
				.append("powerConsumption: 150.0 Watts")
				.toString();

		final IParameterValue expected = StatusParam
				.builder()
				.name(STATUS_PARAMETER)
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
	void testCollectPowerConsumptionFromEnergyUsageFirstCollect() {
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
	void testCollectPowerConsumptionFromEnergyUsage() {
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
	void testCollectPowerConsumptionFromPower() {
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

		assertEquals(7224, Math.round(monitor.getParameter(HardwareConstants.ENERGY_USAGE_PARAMETER, NumberParam.class).getValue()));
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

		// Charge value collected, value is greater than 100
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

	@Test
	void testCollectVoltage() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).monitorType(MonitorType.VOLTAGE).build();
		MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		// No voltage value
		monitorCollectVisitor.collectVoltage();
		NumberParam voltageParameter = monitor.getParameter(HardwareConstants.VOLTAGE_PARAMETER, NumberParam.class);
		assertNull(voltageParameter);

		// Voltage value collected
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(HardwareConstants.VOLTAGE_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList(VOLTAGE))
		);
		monitorCollectVisitor.collectVoltage();
		voltageParameter = monitor.getParameter(HardwareConstants.VOLTAGE_PARAMETER, NumberParam.class);
		assertNotNull(voltageParameter);
		assertEquals(50000.0, voltageParameter.getRawValue());
		assertEquals(50000.0, voltageParameter.getValue());

		// Voltage value collected < -100000
		monitor.setParameters(new HashMap<>());
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(HardwareConstants.VOLTAGE_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList(VOLTAGE_LOW))
		);
		monitorCollectVisitor.collectVoltage();
		voltageParameter = monitor.getParameter(HardwareConstants.VOLTAGE_PARAMETER, NumberParam.class);
		assertNull(voltageParameter);

		// Voltage value collected > 450000
		monitor.setParameters(new HashMap<>());
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(HardwareConstants.VOLTAGE_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList(VOLTAGE_HIGH))
		);
		monitorCollectVisitor.collectVoltage();
		voltageParameter = monitor.getParameter(HardwareConstants.VOLTAGE_PARAMETER, NumberParam.class);
		assertNull(voltageParameter);
	}

	@Test
	void testCollectErrorCount() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).monitorType(MonitorType.TAPE_DRIVE).build();
		MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		// No error count set
		monitorCollectVisitor.collectErrorCount();
		NumberParam errorCountParameter = monitor.getParameter(HardwareConstants.ERROR_COUNT_PARAMETER, NumberParam.class);
		NumberParam startingErrorCountParameter = monitor.getParameter(HardwareConstants.STARTING_ERROR_COUNT_PARAMETER, NumberParam.class);
		assertNull(errorCountParameter);
		assertNull(startingErrorCountParameter);

		// Error count value collected for the first time
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(HardwareConstants.ERROR_COUNT_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList("10"))
		);

		monitorCollectVisitor.collectErrorCount();
		errorCountParameter = monitor.getParameter(HardwareConstants.ERROR_COUNT_PARAMETER, NumberParam.class);
		startingErrorCountParameter = monitor.getParameter(HardwareConstants.STARTING_ERROR_COUNT_PARAMETER, NumberParam.class);
		assertEquals(0.0, errorCountParameter.getRawValue());
		assertEquals(0.0, errorCountParameter.getValue());
		assertEquals(10.0, startingErrorCountParameter.getValue());

		// Error count value collected with an increased error count
		startingErrorCountParameter = NumberParam.builder().name(HardwareConstants.STARTING_ERROR_COUNT_PARAMETER).build();
		startingErrorCountParameter.setPreviousRawValue(15.0);
		monitor.addParameter(startingErrorCountParameter);
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(HardwareConstants.ERROR_COUNT_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList("25"))
		);

		monitorCollectVisitor.collectErrorCount();
		errorCountParameter = monitor.getParameter(HardwareConstants.ERROR_COUNT_PARAMETER, NumberParam.class);
		startingErrorCountParameter = monitor.getParameter(HardwareConstants.STARTING_ERROR_COUNT_PARAMETER, NumberParam.class);
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
		monitorCollectVisitor.collectIncrementCount(HardwareConstants.MOUNT_COUNT_PARAMETER, HardwareConstants.MOUNT_COUNT_PARAMETER_UNIT);
		NumberParam mountCountParameter = monitor.getParameter(HardwareConstants.MOUNT_COUNT_PARAMETER, NumberParam.class);
		assertNull(mountCountParameter);

		// Mount count value collected
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(HardwareConstants.MOUNT_COUNT_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList("10"))
		);

		monitorCollectVisitor.collectIncrementCount(HardwareConstants.MOUNT_COUNT_PARAMETER, HardwareConstants.MOUNT_COUNT_PARAMETER_UNIT);
		mountCountParameter = monitor.getParameter(HardwareConstants.MOUNT_COUNT_PARAMETER, NumberParam.class);
		assertEquals(10.0, mountCountParameter.getRawValue());
		assertEquals(0.0, mountCountParameter.getValue());

		// Both current and previous mount counts are set (previous = 12, current = 20)
		mountCountParameter = NumberParam.builder().name(HardwareConstants.MOUNT_COUNT_PARAMETER).build();
		mountCountParameter.setPreviousRawValue(12.0);
		monitor.addParameter(mountCountParameter);
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(HardwareConstants.MOUNT_COUNT_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList("20"))
		);
		monitorCollectVisitor.collectIncrementCount(HardwareConstants.MOUNT_COUNT_PARAMETER, HardwareConstants.MOUNT_COUNT_PARAMETER_UNIT);
		mountCountParameter = monitor.getParameter(HardwareConstants.MOUNT_COUNT_PARAMETER, NumberParam.class);
		assertEquals(20.0, mountCountParameter.getRawValue());
		assertEquals(8.0, mountCountParameter.getValue());

		// Both current and previous mount counts are set (previous = 32, current = 20)
		mountCountParameter = NumberParam.builder().name(HardwareConstants.MOUNT_COUNT_PARAMETER).build();
		mountCountParameter.setPreviousRawValue(32.0);
		monitor.addParameter(mountCountParameter);
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(HardwareConstants.MOUNT_COUNT_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList("20"))
		);
		monitorCollectVisitor.collectIncrementCount(HardwareConstants.MOUNT_COUNT_PARAMETER, HardwareConstants.MOUNT_COUNT_PARAMETER_UNIT);
		mountCountParameter = monitor.getParameter(HardwareConstants.MOUNT_COUNT_PARAMETER, NumberParam.class);
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
		NumberParam usedCapacityParameter = monitor.getParameter(HardwareConstants.USED_CAPACITY_PARAMETER, NumberParam.class);
		assertNull(usedCapacityParameter);

		// Used capacity set
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(HardwareConstants.USED_PERCENT_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList("10"))
		);
		
		monitorCollectVisitor.collectPowerSupplyUsedCapacity();
		usedCapacityParameter = monitor.getParameter(HardwareConstants.USED_CAPACITY_PARAMETER, NumberParam.class);
		assertEquals(10.0, usedCapacityParameter.getValue());
		
		// No used capacity, derive from used & total power
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map
					.of(HardwareConstants.USED_WATTS_PARAMETER, VALUETABLE_COLUMN_1,
						HardwareConstants.POWER_SUPPLY_POWER, VALUETABLE_COLUMN_2),
				monitor,
				Arrays.asList("25", "50"))
		);
		monitorCollectVisitor.collectPowerSupplyUsedCapacity();
		usedCapacityParameter = monitor.getParameter(HardwareConstants.USED_CAPACITY_PARAMETER, NumberParam.class);
		assertEquals(null, usedCapacityParameter.getRawValue());
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
								HardwareConstants.LAST_ERROR_PARAMETER, VALUETABLE_COLUMN_3),
						monitor,
						Arrays.asList(MONITOR_DEVICE_ID,
								OK_RAW_STATUS,
								MEMORY_LAST_ERROR)
				));

		monitorCollectVisitor.visit(new Memory());

		final Map<String, IParameterValue> parameters = monitor.getParameters();
		final StatusParam expected = StatusParam
				.builder()
				.name(STATUS_PARAMETER)
				.collectTime(collectTime)
				.state(ParameterState.OK)
				.unit(HardwareConstants.STATUS_PARAMETER_UNIT)
				.statusInformation("status: 0 (OK)\nlastError: " + MEMORY_LAST_ERROR)
				.build();

		final IParameterValue actual = parameters.get(HardwareConstants.STATUS_PARAMETER);

		assertEquals(expected, actual);
	}

	@Test
	void testCollectLogicalDiskUnallocatedSpace() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).monitorType(MonitorType.LOGICAL_DISK).build();
		MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		// No unallocated space value
		monitorCollectVisitor.collectLogicalDiskUnallocatedSpace();
		NumberParam unallocatedSpaceParameter = monitor.getParameter(HardwareConstants.UNALLOCATED_SPACE_PARAMETER, NumberParam.class);
		assertNull(unallocatedSpaceParameter);

		// Unallocated space value collected
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(HardwareConstants.UNALLOCATED_SPACE_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList(UNALLOCATED_SPACE))
		);
		monitorCollectVisitor.collectLogicalDiskUnallocatedSpace();
		unallocatedSpaceParameter = monitor.getParameter(HardwareConstants.UNALLOCATED_SPACE_PARAMETER, NumberParam.class);
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
		NumberParam powerConsumptionParameter = monitor.getParameter(HardwareConstants.POWER_CONSUMPTION_PARAMETER, NumberParam.class);
		assertNotNull(powerConsumptionParameter);
		assertEquals(5.0, powerConsumptionParameter.getValue());

		// Fan speed set
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(HardwareConstants.SPEED_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList("7000"))
		);
		monitorCollectVisitor.estimateFanPowerConsumption();
		powerConsumptionParameter = monitor.getParameter(HardwareConstants.POWER_CONSUMPTION_PARAMETER, NumberParam.class);
		assertNotNull(powerConsumptionParameter);
		assertEquals(7.0, powerConsumptionParameter.getValue());
		
		// No fan speed, but fan speed percent set
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(HardwareConstants.SPEED_PERCENT_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList("80"))
		);
		monitorCollectVisitor.estimateFanPowerConsumption();
		powerConsumptionParameter = monitor.getParameter(HardwareConstants.POWER_CONSUMPTION_PARAMETER, NumberParam.class);
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
		NumberParam temperatureParameter = monitor.getParameter(HardwareConstants.TEMPERATURE_PARAMETER, NumberParam.class);
		assertNull(temperatureParameter);

		// Temperature < -100°
		monitorCollectVisitor = new MonitorCollectVisitor(
				buildCollectMonitorInfo(hostMonitoring,
						Map.of(HardwareConstants.TEMPERATURE_PARAMETER, VALUETABLE_COLUMN_1),
						monitor,
						Collections.singletonList(TEMPERATURE_TOO_LOW))
				);

		monitorCollectVisitor.collectTemperature();
		temperatureParameter = monitor.getParameter(HardwareConstants.TEMPERATURE_PARAMETER, NumberParam.class);
		assertNull(temperatureParameter);

		// Temperature > 200°
		monitorCollectVisitor = new MonitorCollectVisitor(
				buildCollectMonitorInfo(hostMonitoring,
						Map.of(HardwareConstants.TEMPERATURE_PARAMETER, VALUETABLE_COLUMN_1),
						monitor,
						Collections.singletonList(TEMPERATURE_TOO_HIGH))
				);

		monitorCollectVisitor.collectTemperature();
		temperatureParameter = monitor.getParameter(HardwareConstants.TEMPERATURE_PARAMETER, NumberParam.class);
		assertNull(temperatureParameter);

		// Temperature value collected
		monitorCollectVisitor = new MonitorCollectVisitor(
				buildCollectMonitorInfo(hostMonitoring,
						Map.of(HardwareConstants.TEMPERATURE_PARAMETER, VALUETABLE_COLUMN_1),
						monitor,
						Collections.singletonList(TEMPERATURE))
				);

		monitorCollectVisitor.collectTemperature();
		temperatureParameter = monitor.getParameter(HardwareConstants.TEMPERATURE_PARAMETER, NumberParam.class);

		assertNotNull(temperatureParameter);
		assertEquals(20.0, temperatureParameter.getRawValue());
		assertEquals(20.0, temperatureParameter.getValue());
	}

	@Test
	void testEstimateDiskControllerPowerConsumption() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).monitorType(MonitorType.DISK_CONTROLLER).build();
		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.estimateDiskControllerPowerConsumption();

		assertEquals(15.0, CollectHelper.getNumberParamValue(monitor, HardwareConstants.POWER_CONSUMPTION_PARAMETER));
	}

	@Test
	void testEstimateMemoryPowerConsumption() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).monitorType(MonitorType.MEMORY).build();
		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.estimateMemoryPowerConsumption();

		assertEquals(4.0, CollectHelper.getNumberParamValue(monitor, HardwareConstants.POWER_CONSUMPTION_PARAMETER));
	}

	@Test
	void testEstimateNetworkCardPowerConsumptionVirtOrWan() {
		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			// WAN
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).name("wan 01").monitorType(MonitorType.NETWORK_CARD).build();
			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

			monitorCollectVisitor.estimateNetworkCardPowerConsumption();

			assertEquals(0.0, CollectHelper.getNumberParamValue(monitor, HardwareConstants.POWER_CONSUMPTION_PARAMETER));
		}

		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();

			// Virtual interface
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).name("Virtual itf:01").monitorType(MonitorType.NETWORK_CARD).build();
			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

			monitorCollectVisitor.estimateNetworkCardPowerConsumption();

			assertEquals(0.0, CollectHelper.getNumberParamValue(monitor, HardwareConstants.POWER_CONSUMPTION_PARAMETER));
		}

	}

	@Test
	void testEstimateNetworkCardPowerConsumptionDown() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).name("FC 01").monitorType(MonitorType.NETWORK_CARD).build();

		// Link status unplugged
		monitor.addParameter(StatusParam.builder().name(HardwareConstants.LINK_STATUS_PARAMETER).state(ParameterState.WARN).build());

		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.estimateNetworkCardPowerConsumption();

		assertEquals(1.0, CollectHelper.getNumberParamValue(monitor, HardwareConstants.POWER_CONSUMPTION_PARAMETER));
	}

	@Test
	void testEstimateNetworkCardPowerConsumptionFromBandwidthUtilization() {
		{
			// Bandwith utilization + Link Speed

			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).name("FC 01").monitorType(MonitorType.NETWORK_CARD).build();


			CollectHelper.updateNumberParameter(
				monitor,
				HardwareConstants.BANDWIDTH_UTILIZATION_PARAMETER,
				"percent",
				collectTime,
				60.0,
				60.0
			);
			CollectHelper.updateNumberParameter(
				monitor,
				HardwareConstants.LINK_SPEED_PARAMETER,
				HardwareConstants.SPEED_MBITS_PARAMETER_UNIT,
					collectTime,
					300.0,
					300.0
			);

			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

			monitorCollectVisitor.estimateNetworkCardPowerConsumption();

			assertEquals(9.91, CollectHelper.getNumberParamValue(monitor, HardwareConstants.POWER_CONSUMPTION_PARAMETER));
		}

		{
			// Bandwith utilization with Link Speed < 10

			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).name("FC 01").monitorType(MonitorType.NETWORK_CARD).build();


			CollectHelper.updateNumberParameter(
					monitor,
					HardwareConstants.BANDWIDTH_UTILIZATION_PARAMETER,
					"percent",
					collectTime,
					60.0,
					60.0
			);
			CollectHelper.updateNumberParameter(
				monitor,
				HardwareConstants.LINK_SPEED_PARAMETER,
				HardwareConstants.SPEED_MBITS_PARAMETER_UNIT,
				collectTime,
				9.0,
				9.0
			);
	
			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

			monitorCollectVisitor.estimateNetworkCardPowerConsumption();

			assertEquals(4.0, CollectHelper.getNumberParamValue(monitor, HardwareConstants.POWER_CONSUMPTION_PARAMETER));
		}

		{
			// Bandwith utilization without link speed

			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).name("FC 01").monitorType(MonitorType.NETWORK_CARD).build();


			CollectHelper.updateNumberParameter(
				monitor,
				HardwareConstants.BANDWIDTH_UTILIZATION_PARAMETER,
				"percent",
				collectTime,
				60.0,
				60.0
			);

			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

			monitorCollectVisitor.estimateNetworkCardPowerConsumption();

			assertEquals(4.0, CollectHelper.getNumberParamValue(monitor, HardwareConstants.POWER_CONSUMPTION_PARAMETER));
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
				HardwareConstants.LINK_SPEED_PARAMETER,
				HardwareConstants.SPEED_MBITS_PARAMETER_UNIT,
				collectTime,
				300.0,
				300.0
			);

			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

			monitorCollectVisitor.estimateNetworkCardPowerConsumption();

			assertEquals(9.29, CollectHelper.getNumberParamValue(monitor, HardwareConstants.POWER_CONSUMPTION_PARAMETER));
		}

		{
			// Bandwith utilization with Link Speed < 10

			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).name("FC 01").monitorType(MonitorType.NETWORK_CARD).build();

			CollectHelper.updateNumberParameter(
				monitor,
				HardwareConstants.LINK_SPEED_PARAMETER,
				HardwareConstants.SPEED_MBITS_PARAMETER_UNIT,
				collectTime,
				9.0,
				9.0
			);
	
			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

			monitorCollectVisitor.estimateNetworkCardPowerConsumption();

			assertEquals(2.0, CollectHelper.getNumberParamValue(monitor, HardwareConstants.POWER_CONSUMPTION_PARAMETER));
		}
	}

	@Test
	void testEstimateNetworkCardPowerConsumptionDefault() {
		// No link speed, no bandwidth utilization
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).name("FC 01").monitorType(MonitorType.NETWORK_CARD).build();

		final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		monitorCollectVisitor.estimateNetworkCardPowerConsumption();

		assertEquals(10.0, CollectHelper.getNumberParamValue(monitor, HardwareConstants.POWER_CONSUMPTION_PARAMETER));
	}

	@Test
	void testEestimatePhysicalDiskPowerConsumptionSsd() {
		{
			// SSD & PCIE -> 18W
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor diskController =  Monitor.builder().id(PARENT_ID).name("DC 1").parentId(ECS1_01).targetId(ECS1_01).monitorType(MonitorType.DISK_CONTROLLER).build();
			hostMonitoring.addMonitor(diskController);
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).parentId(PARENT_ID).name("SSD 1").monitorType(MonitorType.PHYSICAL_DISK).build();
			monitor.addMetadata(HardwareConstants.ADDITIONAL_INFORMATION1, "pcie 1");
			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);
			monitorCollectVisitor.estimatePhysicalDiskPowerConsumption();
			assertEquals(18.0, CollectHelper.getNumberParamValue(monitor, HardwareConstants.POWER_CONSUMPTION_PARAMETER));
		}
		{
			// SSD & NVM -> 6W
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor diskController =  Monitor.builder().id(PARENT_ID).name("DC 1").parentId(ECS1_01).targetId(ECS1_01).monitorType(MonitorType.DISK_CONTROLLER).build();
			hostMonitoring.addMonitor(diskController);
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).parentId(PARENT_ID).name("SSD 1").monitorType(MonitorType.PHYSICAL_DISK).build();
			monitor.addMetadata(HardwareConstants.ADDITIONAL_INFORMATION1, "nvm 1");
			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);
			monitorCollectVisitor.estimatePhysicalDiskPowerConsumption();
			assertEquals(6.0, CollectHelper.getNumberParamValue(monitor, HardwareConstants.POWER_CONSUMPTION_PARAMETER));
		}

		{
			// SOLID -> 3W
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor diskController =  Monitor.builder().id(PARENT_ID).name("DC 1").parentId(ECS1_01).targetId(ECS1_01).monitorType(MonitorType.DISK_CONTROLLER).build();
			hostMonitoring.addMonitor(diskController);
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).parentId(PARENT_ID).name("Solid 1").monitorType(MonitorType.PHYSICAL_DISK).build();
			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);
			monitorCollectVisitor.estimatePhysicalDiskPowerConsumption();
			assertEquals(3.0, CollectHelper.getNumberParamValue(monitor, HardwareConstants.POWER_CONSUMPTION_PARAMETER));
		}
	}

	@Test
	void testEestimatePhysicalDiskPowerConsumptionSas() {
		{
			// SAS & 15k -> 17W 
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor diskController =  Monitor.builder().id(PARENT_ID).name("DC 1").parentId(ECS1_01).targetId(ECS1_01).monitorType(MonitorType.DISK_CONTROLLER).build();
			hostMonitoring.addMonitor(diskController);
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).parentId(PARENT_ID).name("Sas 1").monitorType(MonitorType.PHYSICAL_DISK).build();
			monitor.addMetadata(HardwareConstants.ADDITIONAL_INFORMATION1, "15k drive");
			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);
			monitorCollectVisitor.estimatePhysicalDiskPowerConsumption();
			assertEquals(17.0, CollectHelper.getNumberParamValue(monitor, HardwareConstants.POWER_CONSUMPTION_PARAMETER));
		}

		{
			// SOLID -> 3W
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor diskController =  Monitor.builder().id(PARENT_ID).name("DC 1").parentId(ECS1_01).targetId(ECS1_01).monitorType(MonitorType.DISK_CONTROLLER).build();
			hostMonitoring.addMonitor(diskController);
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).parentId(PARENT_ID).name("Sas 1").monitorType(MonitorType.PHYSICAL_DISK).build();
			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);
			monitorCollectVisitor.estimatePhysicalDiskPowerConsumption();
			assertEquals(12.0, CollectHelper.getNumberParamValue(monitor, HardwareConstants.POWER_CONSUMPTION_PARAMETER));
		}
	}

	@Test
	void testEestimatePhysicalDiskPowerConsumptionScsiAndIde() {
		{
			// SCSI & 10k -> 32W
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor diskController =  Monitor.builder().id(PARENT_ID).name("DC 1").parentId(ECS1_01).targetId(ECS1_01).monitorType(MonitorType.DISK_CONTROLLER).build();
			hostMonitoring.addMonitor(diskController);
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).parentId(PARENT_ID).name("SCSI 1").monitorType(MonitorType.PHYSICAL_DISK).build();
			monitor.addMetadata(HardwareConstants.ADDITIONAL_INFORMATION1, "10k drive 1");
			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);
			monitorCollectVisitor.estimatePhysicalDiskPowerConsumption();
			assertEquals(32.0, CollectHelper.getNumberParamValue(monitor, HardwareConstants.POWER_CONSUMPTION_PARAMETER));
		}
		{
			// SCSI & 15k -> 35W
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor diskController =  Monitor.builder().id(PARENT_ID).name("DC 1").parentId(ECS1_01).targetId(ECS1_01).monitorType(MonitorType.DISK_CONTROLLER).build();
			hostMonitoring.addMonitor(diskController);
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).parentId(PARENT_ID).name("SCSI 1").monitorType(MonitorType.PHYSICAL_DISK).build();
			monitor.addMetadata(HardwareConstants.ADDITIONAL_INFORMATION1, "15k drive 1");
			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);
			monitorCollectVisitor.estimatePhysicalDiskPowerConsumption();
			assertEquals(35.0, CollectHelper.getNumberParamValue(monitor, HardwareConstants.POWER_CONSUMPTION_PARAMETER));
		}

		{
			// SCSI & 5400 -> 19W
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor diskController =  Monitor.builder().id(PARENT_ID).name("DC 1").parentId(ECS1_01).targetId(ECS1_01).monitorType(MonitorType.DISK_CONTROLLER).build();
			hostMonitoring.addMonitor(diskController);
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).parentId(PARENT_ID).name("SCSI 1").monitorType(MonitorType.PHYSICAL_DISK).build();
			monitor.addMetadata(HardwareConstants.ADDITIONAL_INFORMATION1, "5400 drive 1");
			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);
			monitorCollectVisitor.estimatePhysicalDiskPowerConsumption();
			assertEquals(19.0, CollectHelper.getNumberParamValue(monitor, HardwareConstants.POWER_CONSUMPTION_PARAMETER));
		}
		{
			// IDE & 5.4 -> 19W
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor diskController =  Monitor.builder().id(PARENT_ID).name("DC 1").parentId(ECS1_01).targetId(ECS1_01).monitorType(MonitorType.DISK_CONTROLLER).build();
			hostMonitoring.addMonitor(diskController);
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).parentId(PARENT_ID).name("IDE 1").monitorType(MonitorType.PHYSICAL_DISK).build();
			monitor.addMetadata(HardwareConstants.ADDITIONAL_INFORMATION1, "drive 1 (5.4)");
			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);
			monitorCollectVisitor.estimatePhysicalDiskPowerConsumption();
			assertEquals(19.0, CollectHelper.getNumberParamValue(monitor, HardwareConstants.POWER_CONSUMPTION_PARAMETER));
		}

		{
			// SCSI -> 30W
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor diskController =  Monitor.builder().id(PARENT_ID).name("DC 1").parentId(ECS1_01).targetId(ECS1_01).monitorType(MonitorType.DISK_CONTROLLER).build();
			hostMonitoring.addMonitor(diskController);
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).parentId(PARENT_ID).name("SCSI 1").monitorType(MonitorType.PHYSICAL_DISK).build();
			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);
			monitorCollectVisitor.estimatePhysicalDiskPowerConsumption();
			assertEquals(30.0, CollectHelper.getNumberParamValue(monitor, HardwareConstants.POWER_CONSUMPTION_PARAMETER));
		}
	}

	@Test
	void estimateSataOrDefault() {
		{
			// SATA & 10k -> 27W
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor diskController =  Monitor.builder().id(PARENT_ID).name("DC 1").parentId(ECS1_01).targetId(ECS1_01).monitorType(MonitorType.DISK_CONTROLLER).build();
			hostMonitoring.addMonitor(diskController);
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).parentId(PARENT_ID).name("SATA 1").monitorType(MonitorType.PHYSICAL_DISK).build();
			monitor.addMetadata(HardwareConstants.ADDITIONAL_INFORMATION1, "10k drive 1");
			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);
			monitorCollectVisitor.estimatePhysicalDiskPowerConsumption();
			assertEquals(27.0, CollectHelper.getNumberParamValue(monitor, HardwareConstants.POWER_CONSUMPTION_PARAMETER));
		}
		{
			// SATA & 15k -> 32W
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor diskController =  Monitor.builder().id(PARENT_ID).name("DC 1").parentId(ECS1_01).targetId(ECS1_01).monitorType(MonitorType.DISK_CONTROLLER).build();
			hostMonitoring.addMonitor(diskController);
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).parentId(PARENT_ID).name("SATA 1").monitorType(MonitorType.PHYSICAL_DISK).build();
			monitor.addMetadata(HardwareConstants.ADDITIONAL_INFORMATION1, "15k drive 1");
			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);
			monitorCollectVisitor.estimatePhysicalDiskPowerConsumption();
			assertEquals(32.0, CollectHelper.getNumberParamValue(monitor, HardwareConstants.POWER_CONSUMPTION_PARAMETER));
		}

		{
			// SATA & 5400 -> 7W
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor diskController =  Monitor.builder().id(PARENT_ID).name("DC 1").parentId(ECS1_01).targetId(ECS1_01).monitorType(MonitorType.DISK_CONTROLLER).build();
			hostMonitoring.addMonitor(diskController);
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).parentId(PARENT_ID).name("SATA 1").monitorType(MonitorType.PHYSICAL_DISK).build();
			monitor.addMetadata(HardwareConstants.ADDITIONAL_INFORMATION1, "5400 drive 1");
			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);
			monitorCollectVisitor.estimatePhysicalDiskPowerConsumption();
			assertEquals(7.0, CollectHelper.getNumberParamValue(monitor, HardwareConstants.POWER_CONSUMPTION_PARAMETER));
		}
		{
			// SATA & 5.4 -> 7W
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor diskController =  Monitor.builder().id(PARENT_ID).name("DC 1").parentId(ECS1_01).targetId(ECS1_01).monitorType(MonitorType.DISK_CONTROLLER).build();
			hostMonitoring.addMonitor(diskController);
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).parentId(PARENT_ID).name("SATA 1").monitorType(MonitorType.PHYSICAL_DISK).build();
			monitor.addMetadata(HardwareConstants.ADDITIONAL_INFORMATION1, "drive 1 (5.4)");
			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);
			monitorCollectVisitor.estimatePhysicalDiskPowerConsumption();
			assertEquals(7.0, CollectHelper.getNumberParamValue(monitor, HardwareConstants.POWER_CONSUMPTION_PARAMETER));
		}

		{
			// Default -> 11W
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor diskController =  Monitor.builder().id(PARENT_ID).name("DC 1").parentId(ECS1_01).targetId(ECS1_01).monitorType(MonitorType.DISK_CONTROLLER).build();
			hostMonitoring.addMonitor(diskController);
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).parentId(PARENT_ID).name("SATA 1").monitorType(MonitorType.PHYSICAL_DISK).build();
			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);
			monitorCollectVisitor.estimatePhysicalDiskPowerConsumption();
			assertEquals(11.0, CollectHelper.getNumberParamValue(monitor, HardwareConstants.POWER_CONSUMPTION_PARAMETER));
		}
	}

	@Test
	void testEstimateRoboticPowerConsumption() {
		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).parentId(PARENT_ID).name("Robotic").monitorType(MonitorType.ROBOTIC).build();
			CollectHelper.updateNumberParameter(
				monitor,
				HardwareConstants.MOVE_COUNT_PARAMETER,
				HardwareConstants.MOVE_COUNT_PARAMETER_UNIT,
				collectTime,
				1.0,
				1.0
			);
			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);
			monitorCollectVisitor.estimateRoboticPowerConsumption();
			assertEquals(154.0, CollectHelper.getNumberParamValue(monitor, HardwareConstants.POWER_CONSUMPTION_PARAMETER));
		}

		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).parentId(PARENT_ID).name("Robotic").monitorType(MonitorType.ROBOTIC).build();
			CollectHelper.updateNumberParameter(
				monitor,
				HardwareConstants.MOVE_COUNT_PARAMETER,
				HardwareConstants.MOVE_COUNT_PARAMETER_UNIT,
				collectTime,
				0.0,
				0.0
			);
			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);
			monitorCollectVisitor.estimateRoboticPowerConsumption();
			assertEquals(48.0, CollectHelper.getNumberParamValue(monitor, HardwareConstants.POWER_CONSUMPTION_PARAMETER));
		}

		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).parentId(PARENT_ID).name("Robotic").monitorType(MonitorType.ROBOTIC).build();
			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);
			monitorCollectVisitor.estimateRoboticPowerConsumption();
			assertEquals(48.0, CollectHelper.getNumberParamValue(monitor, HardwareConstants.POWER_CONSUMPTION_PARAMETER));
		}
	}

	@Test
	void testEstimateTapeDrivePowerConsumptionLto() {
		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).parentId(PARENT_ID).name("lto td").monitorType(MonitorType.TAPE_DRIVE).build();
			CollectHelper.updateNumberParameter(
				monitor,
				HardwareConstants.MOUNT_COUNT_PARAMETER,
				HardwareConstants.MOUNT_COUNT_PARAMETER_UNIT,
				collectTime,
				1.0,
				1.0
			);
			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);
			monitorCollectVisitor.estimateTapeDrivePowerConsumption();
			assertEquals(46.0, CollectHelper.getNumberParamValue(monitor, HardwareConstants.POWER_CONSUMPTION_PARAMETER));
		}
		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).parentId(PARENT_ID).name("lto td").monitorType(MonitorType.TAPE_DRIVE).build();
			CollectHelper.updateNumberParameter(
				monitor,
				HardwareConstants.UNMOUNT_COUNT_PARAMETER,
				HardwareConstants.UNMOUNT_COUNT_PARAMETER_UNIT,
				collectTime,
				1.0,
				1.0
			);
			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);
			monitorCollectVisitor.estimateTapeDrivePowerConsumption();
			assertEquals(46.0, CollectHelper.getNumberParamValue(monitor, HardwareConstants.POWER_CONSUMPTION_PARAMETER));
		}
		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).parentId(PARENT_ID).name("lto td").monitorType(MonitorType.TAPE_DRIVE).build();
			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);
			monitorCollectVisitor.estimateTapeDrivePowerConsumption();
			assertEquals(30.0, CollectHelper.getNumberParamValue(monitor, HardwareConstants.POWER_CONSUMPTION_PARAMETER));
		}
	}

	@Test
	void testEstimateTapeDrivePowerConsumptionT10000d() {
		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).parentId(PARENT_ID).name("t10000d td").monitorType(MonitorType.TAPE_DRIVE).build();
			CollectHelper.updateNumberParameter(
				monitor,
				HardwareConstants.MOUNT_COUNT_PARAMETER,
				HardwareConstants.MOUNT_COUNT_PARAMETER_UNIT,
				collectTime,
				1.0,
				1.0
			);
			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);
			monitorCollectVisitor.estimateTapeDrivePowerConsumption();
			assertEquals(127.0, CollectHelper.getNumberParamValue(monitor, HardwareConstants.POWER_CONSUMPTION_PARAMETER));
		}
		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).parentId(PARENT_ID).name("t10000d td").monitorType(MonitorType.TAPE_DRIVE).build();
			CollectHelper.updateNumberParameter(
				monitor,
				HardwareConstants.UNMOUNT_COUNT_PARAMETER,
				HardwareConstants.UNMOUNT_COUNT_PARAMETER_UNIT,
				collectTime,
				1.0,
				1.0
			);
			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);
			monitorCollectVisitor.estimateTapeDrivePowerConsumption();
			assertEquals(127.0, CollectHelper.getNumberParamValue(monitor, HardwareConstants.POWER_CONSUMPTION_PARAMETER));
		}
		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).parentId(PARENT_ID).name("t10000d td").monitorType(MonitorType.TAPE_DRIVE).build();
			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);
			monitorCollectVisitor.estimateTapeDrivePowerConsumption();
			assertEquals(64.0, CollectHelper.getNumberParamValue(monitor, HardwareConstants.POWER_CONSUMPTION_PARAMETER));
		}
	}

	@Test
	void testEstimateTapeDrivePowerConsumptionT10000() {
		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).parentId(PARENT_ID).name("t10000 td").monitorType(MonitorType.TAPE_DRIVE).build();
			CollectHelper.updateNumberParameter(
				monitor,
				HardwareConstants.MOUNT_COUNT_PARAMETER,
				HardwareConstants.MOUNT_COUNT_PARAMETER_UNIT,
				collectTime,
				1.0,
				1.0
			);
			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);
			monitorCollectVisitor.estimateTapeDrivePowerConsumption();
			assertEquals(93.0, CollectHelper.getNumberParamValue(monitor, HardwareConstants.POWER_CONSUMPTION_PARAMETER));
		}
		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).parentId(PARENT_ID).name("t10000 td").monitorType(MonitorType.TAPE_DRIVE).build();
			CollectHelper.updateNumberParameter(
				monitor,
				HardwareConstants.UNMOUNT_COUNT_PARAMETER,
				HardwareConstants.UNMOUNT_COUNT_PARAMETER_UNIT,
				collectTime,
				1.0,
				1.0
			);
			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);
			monitorCollectVisitor.estimateTapeDrivePowerConsumption();
			assertEquals(93.0, CollectHelper.getNumberParamValue(monitor, HardwareConstants.POWER_CONSUMPTION_PARAMETER));
		}
		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).parentId(PARENT_ID).name("t10000 td").monitorType(MonitorType.TAPE_DRIVE).build();
			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);
			monitorCollectVisitor.estimateTapeDrivePowerConsumption();
			assertEquals(61.0, CollectHelper.getNumberParamValue(monitor, HardwareConstants.POWER_CONSUMPTION_PARAMETER));
		}
	}

	@Test
	void testEstimateTapeDrivePowerConsumptionTs() {
		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).parentId(PARENT_ID).name("ts td").monitorType(MonitorType.TAPE_DRIVE).build();
			CollectHelper.updateNumberParameter(
				monitor,
				HardwareConstants.MOUNT_COUNT_PARAMETER,
				HardwareConstants.MOUNT_COUNT_PARAMETER_UNIT,
				collectTime,
				1.0,
				1.0
			);
			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);
			monitorCollectVisitor.estimateTapeDrivePowerConsumption();
			assertEquals(53.0, CollectHelper.getNumberParamValue(monitor, HardwareConstants.POWER_CONSUMPTION_PARAMETER));
		}
		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).parentId(PARENT_ID).name("ts td").monitorType(MonitorType.TAPE_DRIVE).build();
			CollectHelper.updateNumberParameter(
				monitor,
				HardwareConstants.UNMOUNT_COUNT_PARAMETER,
				HardwareConstants.UNMOUNT_COUNT_PARAMETER_UNIT,
				collectTime,
				1.0,
				1.0
			);
			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);
			monitorCollectVisitor.estimateTapeDrivePowerConsumption();
			assertEquals(53.0, CollectHelper.getNumberParamValue(monitor, HardwareConstants.POWER_CONSUMPTION_PARAMETER));
		}
		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).parentId(PARENT_ID).name("ts td").monitorType(MonitorType.TAPE_DRIVE).build();
			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);
			monitorCollectVisitor.estimateTapeDrivePowerConsumption();
			assertEquals(35.0, CollectHelper.getNumberParamValue(monitor, HardwareConstants.POWER_CONSUMPTION_PARAMETER));
		}
	}

	@Test
	void testEstimateTapeDrivePowerConsumptionDefault() {
		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).parentId(PARENT_ID).name("td1").monitorType(MonitorType.TAPE_DRIVE).build();
			CollectHelper.updateNumberParameter(
				monitor,
				HardwareConstants.MOUNT_COUNT_PARAMETER,
				HardwareConstants.MOUNT_COUNT_PARAMETER_UNIT,
				collectTime,
				1.0,
				1.0
			);
			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);
			monitorCollectVisitor.estimateTapeDrivePowerConsumption();
			assertEquals(80.0, CollectHelper.getNumberParamValue(monitor, HardwareConstants.POWER_CONSUMPTION_PARAMETER));
		}
		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).parentId(PARENT_ID).name("td1").monitorType(MonitorType.TAPE_DRIVE).build();
			CollectHelper.updateNumberParameter(
				monitor,
				HardwareConstants.UNMOUNT_COUNT_PARAMETER,
				HardwareConstants.UNMOUNT_COUNT_PARAMETER_UNIT,
				collectTime,
				1.0,
				1.0
			);
			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);
			monitorCollectVisitor.estimateTapeDrivePowerConsumption();
			assertEquals(80.0, CollectHelper.getNumberParamValue(monitor, HardwareConstants.POWER_CONSUMPTION_PARAMETER));
		}
		{
			final IHostMonitoring hostMonitoring = new HostMonitoring();
			final Monitor monitor = Monitor.builder().id(MONITOR_ID).parentId(PARENT_ID).name("td1").monitorType(MonitorType.TAPE_DRIVE).build();
			final MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);
			monitorCollectVisitor.estimateTapeDrivePowerConsumption();
			assertEquals(55.0, CollectHelper.getNumberParamValue(monitor, HardwareConstants.POWER_CONSUMPTION_PARAMETER));
		}
	}

	@Test
	void testCollectPhysicalDiskParameters() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).monitorType(MonitorType.PHYSICAL_DISK).build();
		MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		// No value
		monitorCollectVisitor.collectPhysicalDiskParameters();
		NumberParam predictedFailure = monitor.getParameter(HardwareConstants.PREDICTED_FAILURE_PARAMETER, NumberParam.class);
		NumberParam enduranceRemaining = monitor.getParameter(HardwareConstants.ENDURANCE_REMAINING_PARAMETER, NumberParam.class);
		assertNull(predictedFailure);
		assertNull(enduranceRemaining);

		// Values collected
		monitorCollectVisitor = new MonitorCollectVisitor(
				buildCollectMonitorInfo(hostMonitoring,
						Map.of(HardwareConstants.ENDURANCE_REMAINING_PARAMETER, VALUETABLE_COLUMN_1),
						monitor,
						Arrays.asList(ENDURANCE_REMAINING))
				);
		monitorCollectVisitor.collectPhysicalDiskParameters();
		enduranceRemaining = monitor.getParameter(HardwareConstants.ENDURANCE_REMAINING_PARAMETER, NumberParam.class);
		assertNotNull(enduranceRemaining);
		assertEquals(10.0, enduranceRemaining.getRawValue());
		assertEquals(10.0, enduranceRemaining.getValue());

		// rawEnduranceRemaining value collected < 0
		monitor.getParameters().clear();
		monitorCollectVisitor = new MonitorCollectVisitor(
				buildCollectMonitorInfo(hostMonitoring,
						Map.of(HardwareConstants.ENDURANCE_REMAINING_PARAMETER, VALUETABLE_COLUMN_1),
						monitor,
						Arrays.asList(ENDURANCE_REMAINING_TOO_LOW))
				);
		monitorCollectVisitor.collectPhysicalDiskParameters();
		enduranceRemaining = monitor.getParameter(HardwareConstants.ENDURANCE_REMAINING_PARAMETER, NumberParam.class);
		assertNull(enduranceRemaining);

		// rawEnduranceRemaining value collected > 100
		monitor.getParameters().clear();
		monitorCollectVisitor = new MonitorCollectVisitor(
				buildCollectMonitorInfo(hostMonitoring,
						Map.of(HardwareConstants.ENDURANCE_REMAINING_PARAMETER, VALUETABLE_COLUMN_1),
						monitor,
						Arrays.asList(ENDURANCE_REMAINING_TOO_HIGH))
				);
		monitorCollectVisitor.collectPhysicalDiskParameters();
		enduranceRemaining = monitor.getParameter(HardwareConstants.ENDURANCE_REMAINING_PARAMETER, NumberParam.class);
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
		StatusParam colorParameter = monitor.getParameter(COLOR_PARAMETER, StatusParam.class);
		assertNotNull(colorParameter);
		assertEquals(ParameterState.WARN, colorParameter.getState());

		// colorRaw != null, warningOnColor != null, colorRaw not in warningOnColor
		monitor.addMetadata("warningOnColor", "blue,yellow");
		monitorCollectVisitor.collectLedColor();
		colorParameter = monitor.getParameter(COLOR_PARAMETER, StatusParam.class);
		assertNotNull(colorParameter);
		assertEquals(ParameterState.OK, colorParameter.getState());

		// colorRaw != null, warningOnColor == null, alarmOnColor != null, colorRaw is an alarm color
		monitor.addMetadata("warningOnColor", null);
		monitor.addMetadata("alarmOnColor", "amber,yellow");
		monitorCollectVisitor.collectLedColor();
		colorParameter = monitor.getParameter(COLOR_PARAMETER, StatusParam.class);
		assertNotNull(colorParameter);
		assertEquals(ParameterState.ALARM, colorParameter.getState());

		// colorRaw != null, warningOnColor == null, alarmOnColor != null, colorRaw is not an alarm color
		monitor.addMetadata("alarmOnColor", "blue,yellow");
		monitorCollectVisitor.collectLedColor();
		colorParameter = monitor.getParameter(COLOR_PARAMETER, StatusParam.class);
		assertNotNull(colorParameter);
		assertEquals(ParameterState.OK, colorParameter.getState());

		// colorRaw != null, warningOnColor == null, alarmOnColor == null
		monitor.addMetadata("alarmOnColor", null);
		monitorCollectVisitor.collectLedColor();
		colorParameter = monitor.getParameter(COLOR_PARAMETER, StatusParam.class);
		assertNotNull(colorParameter);
		assertEquals(ParameterState.OK, colorParameter.getState());
	}

	@Test
	void testCollectLedStatus() {

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
		monitorCollectVisitor.collectLedStatus();
		assertNull(monitor.getParameter(STATUS_PARAMETER, StatusParam.class));

		// statusRaw.equals("on")
		List<String> customRow = new ArrayList<>(row);
		customRow.add("on");
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				customMapping,
				monitor,
				customRow)
		);
		monitorCollectVisitor.collectLedStatus();
		assertNull(monitor.getParameter(STATUS_PARAMETER, StatusParam.class));

		// statusRaw.equals("blinking"), no blinking status metadata
		customRow = new ArrayList<>(row);
		customRow.add("blinking");
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				customMapping,
				monitor,
				customRow)
		);
		monitorCollectVisitor.collectLedStatus();
		assertNull(monitor.getParameter(STATUS_PARAMETER, StatusParam.class));

		// statusRaw.equals("blinking"), blinking status meta data found
		monitor.addMetadata("blinkingstatus", "WARN");
		monitorCollectVisitor.collectLedStatus();
		StatusParam statusParameter = monitor.getParameter(STATUS_PARAMETER, StatusParam.class);
		assertNotNull(statusParameter);
		assertEquals(ParameterState.WARN, statusParameter.getState());
	}
	
	@Test
	void testCollectNetworkCardDuplexMode() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).monitorType(MonitorType.NETWORK_CARD).build();
		MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		// duplexMode = null
		monitorCollectVisitor.collectNetworkCardDuplexMode();
		NumberParam duplexModeParameter = monitor.getParameter(HardwareConstants.DUPLEX_MODE_PARAMETER, NumberParam.class);
		assertNotNull(duplexModeParameter);
		assertEquals(0.0, duplexModeParameter.getRawValue());
		assertEquals(0.0, duplexModeParameter.getValue());

		// duplexMode = blabla
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(HardwareConstants.DUPLEX_MODE_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList("blabla"))
		);
		monitorCollectVisitor.collectNetworkCardDuplexMode();
		duplexModeParameter = monitor.getParameter(HardwareConstants.DUPLEX_MODE_PARAMETER, NumberParam.class);
		assertNotNull(duplexModeParameter);
		assertEquals(0.0, duplexModeParameter.getRawValue());
		assertEquals(0.0, duplexModeParameter.getValue());

		// duplexMode = "YES"
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(HardwareConstants.DUPLEX_MODE_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList("YES"))
		);
		monitorCollectVisitor.collectNetworkCardDuplexMode();
		duplexModeParameter = monitor.getParameter(HardwareConstants.DUPLEX_MODE_PARAMETER, NumberParam.class);
		assertNotNull(duplexModeParameter);
		assertEquals(1.0, duplexModeParameter.getRawValue());
		assertEquals(1.0, duplexModeParameter.getValue());
		
		// duplexMode = "Full"
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(HardwareConstants.DUPLEX_MODE_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList("Full"))
		);
		monitorCollectVisitor.collectNetworkCardDuplexMode();
		duplexModeParameter = monitor.getParameter(HardwareConstants.DUPLEX_MODE_PARAMETER, NumberParam.class);
		assertNotNull(duplexModeParameter);
		assertEquals(1.0, duplexModeParameter.getRawValue());
		assertEquals(1.0, duplexModeParameter.getValue());
		
		// duplexMode = "1"
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(HardwareConstants.DUPLEX_MODE_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList("1"))
		);
		monitorCollectVisitor.collectNetworkCardDuplexMode();
		duplexModeParameter = monitor.getParameter(HardwareConstants.DUPLEX_MODE_PARAMETER, NumberParam.class);
		assertNotNull(duplexModeParameter);
		assertEquals(1.0, duplexModeParameter.getRawValue());
		assertEquals(1.0, duplexModeParameter.getValue());
	}
	
	@Test
	void testCollectNetworkCardLinkSpeed() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).monitorType(MonitorType.NETWORK_CARD).build();
		MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		// No link speed value
		monitorCollectVisitor.collectNetworkCardLinkSpeed();
		NumberParam linkSpeedParameter = monitor.getParameter(HardwareConstants.LINK_SPEED_PARAMETER, NumberParam.class);
		assertNull(linkSpeedParameter);

		// Unallocated space value collected
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(HardwareConstants.LINK_SPEED_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList("10"))
		);
		monitorCollectVisitor.collectNetworkCardLinkSpeed();
		linkSpeedParameter = monitor.getParameter(HardwareConstants.LINK_SPEED_PARAMETER, NumberParam.class);
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
			HardwareConstants.RECEIVED_BYTES_PARAMETER,
			HardwareConstants.RECEIVED_BYTES_RATE_PARAMETER,
			HardwareConstants.USAGE_REPORT_RECEIVED_BYTES_PARAMETER
		);
		NumberParam bytesRateParameter = monitor.getParameter(HardwareConstants.RECEIVED_BYTES_RATE_PARAMETER, NumberParam.class);
		assertNull(bytesRateParameter);

		// Received bytes set, but no last received bytes
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(HardwareConstants.RECEIVED_BYTES_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList("10"))
		);
		monitorCollectVisitor.collectNetworkCardBytesRate(
			HardwareConstants.RECEIVED_BYTES_PARAMETER,
			HardwareConstants.RECEIVED_BYTES_RATE_PARAMETER,
			HardwareConstants.USAGE_REPORT_RECEIVED_BYTES_PARAMETER
		);
		bytesRateParameter = monitor.getParameter(HardwareConstants.RECEIVED_BYTES_RATE_PARAMETER, NumberParam.class);
		assertNull(bytesRateParameter);
		NumberParam bytesParameter = monitor.getParameter(HardwareConstants.RECEIVED_BYTES_PARAMETER, NumberParam.class);
		assertNotNull(bytesParameter);
		assertEquals(10.0, bytesParameter.getRawValue());

		// Received bytes & last received bytes set
		bytesParameter = NumberParam.builder().name(HardwareConstants.RECEIVED_BYTES_PARAMETER).build();
		bytesParameter.setPreviousRawValue(80.0 * 1048576); // 80 MB
		bytesParameter.setPreviousCollectTime(System.currentTimeMillis() - 120000L); // 2 minutes ago
		monitor.addParameter(bytesParameter);
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(HardwareConstants.RECEIVED_BYTES_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList("209715200")) // 200 MB
		);
		monitorCollectVisitor.collectNetworkCardBytesRate(
			HardwareConstants.RECEIVED_BYTES_PARAMETER,
			HardwareConstants.RECEIVED_BYTES_RATE_PARAMETER,
			HardwareConstants.USAGE_REPORT_RECEIVED_BYTES_PARAMETER
		);
		bytesParameter = monitor.getParameter(HardwareConstants.RECEIVED_BYTES_PARAMETER, NumberParam.class);
		assertNotNull(bytesParameter);
		assertEquals(209715200.0, bytesParameter.getValue());
		NumberParam usageReportParameter = monitor.getParameter(HardwareConstants.USAGE_REPORT_RECEIVED_BYTES_PARAMETER, NumberParam.class);
		assertNotNull(usageReportParameter);
		assertEquals(120.0 / 1024, usageReportParameter.getValue());
		bytesRateParameter = monitor.getParameter(HardwareConstants.RECEIVED_BYTES_RATE_PARAMETER, NumberParam.class);
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
				HardwareConstants.TRANSMITTED_PACKETS_PARAMETER,
				HardwareConstants.TRANSMITTED_PACKETS_RATE_PARAMETER,
				HardwareConstants.USAGE_REPORT_TRANSMITTED_PACKETS_PARAMETER
		);
		NumberParam packetsRateParameter = monitor.getParameter(HardwareConstants.TRANSMITTED_PACKETS_RATE_PARAMETER, NumberParam.class);
		assertNull(packetsRateParameter);

		// Transmitted packets set, but no last transmitted packets
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(HardwareConstants.TRANSMITTED_PACKETS_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList("10"))
		);
		monitorCollectVisitor.collectNetworkCardPacketsRate(
			HardwareConstants.TRANSMITTED_PACKETS_PARAMETER,
			HardwareConstants.TRANSMITTED_PACKETS_RATE_PARAMETER,
			HardwareConstants.USAGE_REPORT_TRANSMITTED_PACKETS_PARAMETER
		);
		packetsRateParameter = monitor.getParameter(HardwareConstants.TRANSMITTED_PACKETS_RATE_PARAMETER, NumberParam.class);
		assertNull(packetsRateParameter);
		NumberParam packetsParameter = monitor.getParameter(HardwareConstants.TRANSMITTED_PACKETS_PARAMETER, NumberParam.class);
		assertNotNull(packetsParameter);
		assertEquals(10.0, packetsParameter.getRawValue());

		// Transmitted packets & last transmitted packets set
		packetsParameter = NumberParam.builder().name(HardwareConstants.TRANSMITTED_PACKETS_PARAMETER).build();
		packetsParameter.setPreviousRawValue(80.0);
		packetsParameter.setPreviousCollectTime(System.currentTimeMillis() - 120000L); // 2 minutes ago
		monitor.addParameter(packetsParameter);
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(HardwareConstants.TRANSMITTED_PACKETS_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList("200"))
		);
		monitorCollectVisitor.collectNetworkCardPacketsRate(
			HardwareConstants.TRANSMITTED_PACKETS_PARAMETER,
			HardwareConstants.TRANSMITTED_PACKETS_RATE_PARAMETER,
			HardwareConstants.USAGE_REPORT_TRANSMITTED_PACKETS_PARAMETER
		);
		packetsParameter = monitor.getParameter(HardwareConstants.TRANSMITTED_PACKETS_PARAMETER, NumberParam.class);
		assertNotNull(packetsParameter);
		assertEquals(200.0, packetsParameter.getValue());
		NumberParam usageReportParameter = monitor.getParameter(HardwareConstants.USAGE_REPORT_TRANSMITTED_PACKETS_PARAMETER, NumberParam.class);
		assertNotNull(usageReportParameter);
		assertEquals(120.0, usageReportParameter.getValue());
		packetsRateParameter = monitor.getParameter(HardwareConstants.TRANSMITTED_PACKETS_RATE_PARAMETER, NumberParam.class);
		assertNotNull(packetsRateParameter);
		assertEquals(1, packetsRateParameter.getValue().intValue());
	}
	
	@Test
	void testCollectNetworkCardBandwidthUtilization() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).monitorType(MonitorType.NETWORK_CARD).build();
		MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		// Full-duplex mode with null receivedBytesRate
		monitorCollectVisitor.collectNetworkCardBandwidthUtilization(1.0, 1000.0, null, 200.0);
		NumberParam bandwidthUtilizationParameter = monitor.getParameter(HardwareConstants.BANDWIDTH_UTILIZATION_PARAMETER, NumberParam.class);
		assertNotNull(bandwidthUtilizationParameter);
		assertEquals(200.0 * 8 * 100 / 1000, bandwidthUtilizationParameter.getValue());
		
		// Full-duplex mode with null transmittedBytesRate
		bandwidthUtilizationParameter.reset();
		monitorCollectVisitor.collectNetworkCardBandwidthUtilization(1.0, 1000.0, 100.0, null);
		bandwidthUtilizationParameter = monitor.getParameter(HardwareConstants.BANDWIDTH_UTILIZATION_PARAMETER, NumberParam.class);
		assertNotNull(bandwidthUtilizationParameter);
		assertEquals(100.0 * 8 * 100 / 1000, bandwidthUtilizationParameter.getValue());

		// Full-duplex mode
		bandwidthUtilizationParameter.reset();
		monitorCollectVisitor.collectNetworkCardBandwidthUtilization(1.0, 1000.0, 100.0, 200.0);
		bandwidthUtilizationParameter = monitor.getParameter(HardwareConstants.BANDWIDTH_UTILIZATION_PARAMETER, NumberParam.class);
		assertNotNull(bandwidthUtilizationParameter);
		assertEquals(200.0 * 8 * 100 / 1000, bandwidthUtilizationParameter.getValue());
		
		// Full-duplex mode, when the duplex mode is null 
		bandwidthUtilizationParameter.reset();
		monitorCollectVisitor.collectNetworkCardBandwidthUtilization(null, 1000.0, 100.0, 200.0);
		bandwidthUtilizationParameter = monitor.getParameter(HardwareConstants.BANDWIDTH_UTILIZATION_PARAMETER, NumberParam.class);
		assertNotNull(bandwidthUtilizationParameter);
		assertEquals(200.0 * 8 * 100 / 1000, bandwidthUtilizationParameter.getValue());
		
		// Full-duplex mode, when the duplex mode is null 
		bandwidthUtilization = monitorCollectVisitor.collectNetworkCardBandwidthUtilization(null, 1000.0, 100.0, 200.0);
		assertEquals(200.0 * 8 * 100 / 1000, bandwidthUtilization);
		
		// Half-duplex mode
		bandwidthUtilizationParameter.reset();
		monitorCollectVisitor.collectNetworkCardBandwidthUtilization(0.0, 1000.0, 100.0, 200.0);
		bandwidthUtilizationParameter = monitor.getParameter(HardwareConstants.BANDWIDTH_UTILIZATION_PARAMETER, NumberParam.class);
		assertNotNull(bandwidthUtilizationParameter);
		assertEquals((100.0 + 200.0) * 8 * 100 / 1000, bandwidthUtilizationParameter.getValue());
		
		// No link speed
		bandwidthUtilizationParameter.reset();
		monitorCollectVisitor.collectNetworkCardBandwidthUtilization(0.0, null, 100.0, 200.0);
		bandwidthUtilizationParameter = monitor.getParameter(HardwareConstants.BANDWIDTH_UTILIZATION_PARAMETER, NumberParam.class);
		assertNull(bandwidthUtilizationParameter.getValue());
	}
	
	@Test
	void testCollectNetworkCardErrorPercent() {

		final IHostMonitoring hostMonitoring = new HostMonitoring();
		final Monitor monitor = Monitor.builder().id(MONITOR_ID).monitorType(MonitorType.NETWORK_CARD).build();
		MonitorCollectVisitor monitorCollectVisitor = buildMonitorCollectVisitor(hostMonitoring, monitor);

		// No error count set
		monitorCollectVisitor.collectNetworkCardErrorPercent(null, null);
		NumberParam errorCountParameter = monitor.getParameter(HardwareConstants.ERROR_COUNT_PARAMETER, NumberParam.class);
		assertNull(errorCountParameter);

		// Error count set but no last total packets
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(HardwareConstants.ERROR_COUNT_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList("10"))
		);

		monitorCollectVisitor.collectNetworkCardErrorPercent(100.0, 200.0);
		errorCountParameter = monitor.getParameter(HardwareConstants.ERROR_COUNT_PARAMETER, NumberParam.class);
		assertEquals(10.0, errorCountParameter.getRawValue());
		assertEquals(10.0, errorCountParameter.getValue());
		NumberParam errorPercentParameter = monitor.getParameter(HardwareConstants.ERROR_PERCENT_PARAMETER, NumberParam.class);
		assertNull(errorPercentParameter);
		
		// Error count and last total packets set, but no last error count
		NumberParam totalPacketsParameter = NumberParam.builder().name(HardwareConstants.TOTAL_PACKETS_PARAMETER).build();
		totalPacketsParameter.setPreviousRawValue(500.0);
		totalPacketsParameter.setPreviousCollectTime(System.currentTimeMillis() - 120000L); // 2 minutes ago
		monitor.addParameter(totalPacketsParameter);
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(HardwareConstants.ERROR_COUNT_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList("100"))
		);

		monitorCollectVisitor.collectNetworkCardErrorPercent(100.0, 200.0);
		errorCountParameter = monitor.getParameter(HardwareConstants.ERROR_COUNT_PARAMETER, NumberParam.class);
		assertEquals(100.0, errorCountParameter.getRawValue());
		assertEquals(100.0, errorCountParameter.getValue());
		errorPercentParameter = monitor.getParameter(HardwareConstants.ERROR_PERCENT_PARAMETER, NumberParam.class);
		assertNull(errorPercentParameter);
		
		// Error count, last error and last total packets set
		errorCountParameter = NumberParam.builder().name(HardwareConstants.ERROR_COUNT_PARAMETER).build();
		errorCountParameter.setPreviousRawValue(50.0);
		errorCountParameter.setPreviousCollectTime(System.currentTimeMillis() - 120000L); // 2 minutes ago
		monitor.addParameter(errorCountParameter);
		totalPacketsParameter = NumberParam.builder().name(HardwareConstants.TOTAL_PACKETS_PARAMETER).build();
		totalPacketsParameter.setPreviousRawValue(150.0);
		totalPacketsParameter.setPreviousCollectTime(System.currentTimeMillis() - 120000L); // 2 minutes ago
		monitor.addParameter(totalPacketsParameter);
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(HardwareConstants.ERROR_COUNT_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList("100"))
		);

		monitorCollectVisitor.collectNetworkCardErrorPercent(100.0, 200.0);
		errorCountParameter = monitor.getParameter(HardwareConstants.ERROR_COUNT_PARAMETER, NumberParam.class);
		assertEquals(100.0, errorCountParameter.getRawValue());
		assertEquals(100.0, errorCountParameter.getValue());
		errorPercentParameter = monitor.getParameter(HardwareConstants.ERROR_PERCENT_PARAMETER, NumberParam.class);
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
		NumberParam zeroBufferCreditCountParameter = monitor.getParameter(HardwareConstants.ZERO_BUFFER_CREDIT_COUNT_PARAMETER, NumberParam.class);
		assertNull(zeroBufferCreditCountParameter.getRawValue());

		// Zero buffer credit count set along with last value and transmitted packets
		zeroBufferCreditCountParameter = NumberParam.builder().name(HardwareConstants.ZERO_BUFFER_CREDIT_COUNT_PARAMETER).build();
		zeroBufferCreditCountParameter.setPreviousRawValue(50.0);
		zeroBufferCreditCountParameter.setPreviousCollectTime(System.currentTimeMillis() - 120000L); // 2 minutes ago
		monitor.addParameter(zeroBufferCreditCountParameter);
		NumberParam usageReportTransmittedPacketsParameter = NumberParam.builder().name(HardwareConstants.USAGE_REPORT_TRANSMITTED_PACKETS_PARAMETER).build();
		usageReportTransmittedPacketsParameter.setValue(400.0);
		monitor.addParameter(usageReportTransmittedPacketsParameter);
		monitorCollectVisitor = new MonitorCollectVisitor(
			buildCollectMonitorInfo(hostMonitoring,
				Map.of(HardwareConstants.ZERO_BUFFER_CREDIT_COUNT_PARAMETER, VALUETABLE_COLUMN_1),
				monitor,
				Collections.singletonList("150"))
		);
		monitorCollectVisitor.collectNetworkCardZeroBufferCreditPercent();
		zeroBufferCreditCountParameter = monitor.getParameter(HardwareConstants.ZERO_BUFFER_CREDIT_COUNT_PARAMETER, NumberParam.class);
		assertNotNull(zeroBufferCreditCountParameter);
		assertEquals(150.0, zeroBufferCreditCountParameter.getRawValue());
		assertEquals(150.0, zeroBufferCreditCountParameter.getValue());
		NumberParam zeroBufferCreditPercentParameter = monitor.getParameter(HardwareConstants.ZERO_BUFFER_CREDIT_PERCENT_PARAMETER, NumberParam.class);
		assertNotNull(zeroBufferCreditPercentParameter);
		assertEquals(100 * 100.0 / (100.0 + 400.0), zeroBufferCreditPercentParameter.getRawValue());
		assertEquals(100 * 100.0 / (100.0 + 400.0), zeroBufferCreditPercentParameter.getValue());
	}
}
