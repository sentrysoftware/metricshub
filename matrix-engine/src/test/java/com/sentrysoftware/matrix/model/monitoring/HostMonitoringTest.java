package com.sentrysoftware.matrix.model.monitoring;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.COMPUTER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENERGY_USAGE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PRESENT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.STATUS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.STORAGE;
import static com.sentrysoftware.matrix.connector.model.monitor.MonitorType.ENCLOSURE;
import static com.sentrysoftware.matrix.connector.model.monitor.MonitorType.FAN;
import static com.sentrysoftware.matrix.connector.model.monitor.MonitorType.TARGET;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.common.helpers.ResourceHelper;
import com.sentrysoftware.matrix.common.meta.monitor.Fan;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.engine.strategy.source.SourceTable;
import com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder;
import com.sentrysoftware.matrix.model.alert.AlertRule;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.parameter.IParameterValue;
import com.sentrysoftware.matrix.model.parameter.NumberParam;
import com.sentrysoftware.matrix.model.parameter.ParameterState;
import com.sentrysoftware.matrix.model.parameter.PresentParam;
import com.sentrysoftware.matrix.model.parameter.StatusParam;
import com.sentrysoftware.matrix.model.parameter.TextParam;

class HostMonitoringTest {

	private static final String ENCLOSURE_2 = "enclosure-2";
	private static final String ENCLOSURE_1 = "enclosure-1";
	private static final String STATUS = "Status";
	private static final String TEST_REPORT = "TestReport";
	private static final String PRESENT = "present";
	private static final String POWER_CONSUMPTION = "PowerConsumption";
	private static final String SOURCE_KEY_LOWER = "enclosure.discovery.source(1)";
	private static final String SOURCE_KEY_PASCAL = "Enclosure.discovery.Source(1)";
	private static final String FULL_FAN_ID = "myConnector_fan_targetId_fanId";
	private static final String CONNECTOR_NAME = "myConnector";
	private static final String FAN_NAME = "fan";
	private static final String ENCLOSURE_NAME = "enclosure";
	private static final String target_NAME = "target";
	private static final String FAN_ID = "fanId";
	private static final String ENCLOSURE_ID = "enclosureId";
	private static final String TARGET_ID = "targetId";

	@Test
	void testRemoveMonitorException() {
		final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance()
				.createHostMonitoring(UUID.randomUUID().toString(), null);
		final Monitor notargetId = Monitor.builder().targetId(TARGET_ID).name(target_NAME)
				.monitorType(TARGET).build();
		assertThrows(IllegalArgumentException.class, () -> hostMonitoring.removeMonitor(notargetId));


		final Monitor noMonitorType = Monitor.builder().targetId(TARGET_ID).id(TARGET_ID).name(target_NAME).build();
		assertThrows(IllegalArgumentException.class, () -> hostMonitoring.removeMonitor(noMonitorType));

		try {
			hostMonitoring.removeMonitor(null);
		} catch (Exception e) {
			fail("Unexpected Exception", e);
		}
	}

	@Test
	void testRemoveMonitor() {
		{
			final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance()
					.createHostMonitoring(UUID.randomUUID().toString(), null);
			final Monitor target = Monitor.builder().id(TARGET_ID).targetId(TARGET_ID).name(target_NAME)
					.monitorType(TARGET).build();
			final Monitor enclosure = Monitor.builder().id(ENCLOSURE_ID).name(ENCLOSURE_NAME).targetId(TARGET_ID)
					.parentId(TARGET_ID).monitorType(ENCLOSURE).build();
			final Monitor fan = Monitor.builder().id(FAN_ID).name(FAN_NAME).targetId(TARGET_ID)
					.parentId(ENCLOSURE_ID).monitorType(FAN).build();

			hostMonitoring.addMonitor(target);
			hostMonitoring.addMonitor(enclosure);
			hostMonitoring.addMonitor(fan);

			hostMonitoring.removeMonitor(target);

			assertTrue(hostMonitoring.selectFromType(TARGET).isEmpty());
			assertTrue(hostMonitoring.selectFromType(ENCLOSURE).isEmpty());
			assertTrue(hostMonitoring.selectFromType(FAN).isEmpty());

		}

		{
			final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance()
					.createHostMonitoring(UUID.randomUUID().toString(), null);
			final Monitor target = Monitor.builder().id(TARGET_ID).targetId(TARGET_ID).name(target_NAME).monitorType(TARGET).build();
			final Monitor enclosure = Monitor.builder().id(ENCLOSURE_ID).name(ENCLOSURE_NAME).targetId(TARGET_ID)
					.parentId(TARGET_ID).monitorType(ENCLOSURE).build();
			final Monitor fan = Monitor.builder().id(FAN_ID).name(FAN_NAME).targetId(TARGET_ID)
					.parentId(ENCLOSURE_ID).monitorType(FAN).build();

			hostMonitoring.addMonitor(target);
			hostMonitoring.addMonitor(enclosure);
			hostMonitoring.addMonitor(fan);

			hostMonitoring.removeMonitor(enclosure);

			assertFalse(hostMonitoring.selectFromType(TARGET).isEmpty());
			assertTrue(hostMonitoring.selectFromType(ENCLOSURE).isEmpty());
			assertTrue(hostMonitoring.selectFromType(FAN).isEmpty());

		}

		{
			final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance()
					.createHostMonitoring(UUID.randomUUID().toString(), null);
			final Monitor target = Monitor.builder().id(TARGET_ID).targetId(TARGET_ID).name(target_NAME).monitorType(TARGET).build();
			final Monitor enclosure = Monitor.builder().id(ENCLOSURE_ID).name(ENCLOSURE_NAME).targetId(TARGET_ID)
					.parentId(TARGET_ID).monitorType(ENCLOSURE).build();
			final Monitor fan = Monitor.builder().id(FAN_ID).name(FAN_NAME).targetId(TARGET_ID)
					.parentId(ENCLOSURE_ID).monitorType(FAN).build();

			hostMonitoring.addMonitor(enclosure);
			hostMonitoring.addMonitor(fan);

			hostMonitoring.removeMonitor(target);

			assertNull(hostMonitoring.selectFromType(TARGET));
			assertTrue(hostMonitoring.selectFromType(ENCLOSURE).isEmpty());
			assertTrue(hostMonitoring.selectFromType(FAN).isEmpty());

		}

		{
			final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance()
					.createHostMonitoring(UUID.randomUUID().toString(), null);
			final Monitor target = Monitor.builder().id(TARGET_ID).targetId(TARGET_ID).name(target_NAME).monitorType(TARGET).build();
			final Monitor enclosure = Monitor.builder().id(ENCLOSURE_ID).name(ENCLOSURE_NAME).targetId(TARGET_ID)
					.parentId(TARGET_ID).monitorType(ENCLOSURE).build();
			final Monitor fan = Monitor.builder().id(FAN_ID).name(FAN_NAME).targetId(TARGET_ID)
					.parentId(ENCLOSURE_ID).monitorType(FAN).build();

			hostMonitoring.getMonitors().put(MonitorType.TARGET, new HashMap<>());
			hostMonitoring.addMonitor(enclosure);
			hostMonitoring.addMonitor(fan);

			hostMonitoring.removeMonitor(target);

			assertTrue(hostMonitoring.selectFromType(TARGET).isEmpty());
			assertTrue(hostMonitoring.selectFromType(ENCLOSURE).isEmpty());
			assertTrue(hostMonitoring.selectFromType(FAN).isEmpty());

			hostMonitoring.getMonitors().put(MonitorType.TARGET, null);
			hostMonitoring.addMonitor(enclosure);
			hostMonitoring.addMonitor(fan);

			hostMonitoring.removeMonitor(target);

			assertNull(hostMonitoring.selectFromType(TARGET));
			assertTrue(hostMonitoring.selectFromType(ENCLOSURE).isEmpty());
			assertTrue(hostMonitoring.selectFromType(FAN).isEmpty());

		}
	}

	@Test
	void testAddMonitorException() {
		final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance()
				.createHostMonitoring(UUID.randomUUID().toString(), null);

		assertThrows(IllegalArgumentException.class, () -> hostMonitoring.addMonitor(null));

		final Monitor notargetId = Monitor.builder().targetId(TARGET_ID).name(target_NAME).monitorType(TARGET).build();
		assertThrows(IllegalArgumentException.class, () -> hostMonitoring.addMonitor(notargetId));

		final Monitor noMonitorType = Monitor.builder().id(TARGET_ID).targetId(TARGET_ID).name(target_NAME).build();
		assertThrows(IllegalArgumentException.class, () -> hostMonitoring.addMonitor(noMonitorType));

		final Monitor noTargetId = Monitor.builder().id(TARGET_ID).monitorType(TARGET).name(target_NAME).build();
		assertThrows(IllegalArgumentException.class, () -> hostMonitoring.addMonitor(noTargetId));

		final Monitor noParentId = Monitor.builder().id(ENCLOSURE_ID).name(ENCLOSURE_NAME).targetId(TARGET_ID).monitorType(ENCLOSURE).build();
		assertThrows(IllegalArgumentException.class, () -> hostMonitoring.addMonitor(noParentId));
	}

	@Test
	void testAddMonitor() {
		final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance()
				.createHostMonitoring(UUID.randomUUID().toString(), null);

		final Monitor enclosure = Monitor.builder().id(ENCLOSURE_ID).name(ENCLOSURE_NAME).targetId(TARGET_ID)
				.parentId(TARGET_ID).monitorType(ENCLOSURE).build();

		hostMonitoring.addMonitor(enclosure);

		assertEquals(enclosure, hostMonitoring.selectFromType(ENCLOSURE).get(ENCLOSURE_ID));

		final String enclosureBisId = ENCLOSURE_ID + "bis";
		final Monitor enclosureBis = Monitor.builder().id(enclosureBisId).name(ENCLOSURE_NAME + "bis")
				.targetId(TARGET_ID).parentId(TARGET_ID).monitorType(ENCLOSURE).build();

		hostMonitoring.addMonitor(enclosureBis);
		assertEquals(enclosure, hostMonitoring.selectFromType(ENCLOSURE).get(ENCLOSURE_ID));
		assertEquals(enclosureBis, hostMonitoring.selectFromType(ENCLOSURE).get(enclosureBisId));
	}

	@Test
	void testAddMonitorWithArguments() {
		{
			final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance()
					.createHostMonitoring(UUID.randomUUID().toString(), null);

			final Monitor enclosure = Monitor.builder().id(ENCLOSURE_ID).name(ENCLOSURE_NAME).targetId(TARGET_ID)
					.parentId(TARGET_ID).monitorType(ENCLOSURE).build();

			hostMonitoring.addMonitor(enclosure, ENCLOSURE_ID, CONNECTOR_NAME, ENCLOSURE, TARGET_ID, TARGET.getName());

			assertEquals(enclosure, hostMonitoring.selectFromType(ENCLOSURE).get(ENCLOSURE_ID));
		}

		{
			final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance()
					.createHostMonitoring(UUID.randomUUID().toString(), null);

			final String expectedEnclosureId = HostMonitoring.buildMonitorId(CONNECTOR_NAME, MonitorType.ENCLOSURE, TARGET_ID, ENCLOSURE_ID);
			final Monitor enclosure = Monitor.builder().id(expectedEnclosureId).name(ENCLOSURE_NAME).targetId(TARGET_ID)
					.parentId(TARGET_ID).monitorType(ENCLOSURE).build();

			hostMonitoring.addMonitor(enclosure);

			final Monitor fan = Monitor.builder().name(FAN_NAME).targetId(TARGET_ID).monitorType(FAN).id(null).parentId(null).build();

			hostMonitoring.addMonitor(fan, FAN_ID, CONNECTOR_NAME, FAN, ENCLOSURE_ID, ENCLOSURE.getName());

			final Monitor fanResult = hostMonitoring.selectFromType(FAN).values().stream().findFirst().get();
			assertNotNull(fanResult);
			assertEquals(FULL_FAN_ID, fanResult.getId());
			assertEquals(expectedEnclosureId, fanResult.getParentId());
			assertEquals(TARGET_ID, fanResult.getTargetId());
		}

		{
			final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance()
					.createHostMonitoring(UUID.randomUUID().toString(), null);

			final String expectedEnclosureId = HostMonitoring.buildMonitorId(CONNECTOR_NAME, MonitorType.ENCLOSURE, TARGET_ID, ENCLOSURE_ID);
			final Monitor enclosure = Monitor.builder().id(expectedEnclosureId).name(ENCLOSURE_NAME).targetId(TARGET_ID)
					.parentId(TARGET_ID).monitorType(ENCLOSURE).extendedType(COMPUTER).build();

			hostMonitoring.addMonitor(enclosure);

			final Monitor fan = Monitor.builder().name(FAN_NAME).targetId(TARGET_ID).monitorType(FAN).id(null).parentId(null).build();

			hostMonitoring.addMonitor(fan, FAN_ID, CONNECTOR_NAME, FAN, null, ENCLOSURE.getName());

			final Monitor fanResult = hostMonitoring.selectFromType(FAN).values().stream().findFirst().get();
			assertNotNull(fanResult);
			assertEquals(FULL_FAN_ID, fanResult.getId());
			assertEquals(expectedEnclosureId, fanResult.getParentId());
			assertEquals(TARGET_ID, fanResult.getTargetId());
		}

		{
			final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance()
					.createHostMonitoring(UUID.randomUUID().toString(), null);

			final String expectedEnclosureId = HostMonitoring.buildMonitorId(CONNECTOR_NAME, MonitorType.ENCLOSURE, TARGET_ID, ENCLOSURE_ID);
			final Monitor enclosure = Monitor.builder().id(expectedEnclosureId).name(ENCLOSURE_NAME).targetId(TARGET_ID)
					.parentId(TARGET_ID).monitorType(ENCLOSURE).extendedType(STORAGE).build();

			hostMonitoring.addMonitor(enclosure);

			final Monitor fan = Monitor.builder().name(FAN_NAME).targetId(TARGET_ID).monitorType(FAN).id(null).parentId(null).build();

			hostMonitoring.addMonitor(fan, FAN_ID, CONNECTOR_NAME, FAN, null, ENCLOSURE.getName());

			final Monitor fanResult = hostMonitoring.selectFromType(FAN).values().stream().findFirst().get();
			assertNotNull(fanResult);
			assertEquals(FULL_FAN_ID, fanResult.getId());
			// The Fan is attached to the target id because we haven't a "Computer" enclosure and AttachedTotargetId is not set
			assertEquals(enclosure.getId(), fanResult.getParentId());
			assertEquals(TARGET_ID, fanResult.getTargetId());
		}

		{
			final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance()
					.createHostMonitoring(UUID.randomUUID().toString(), null);

			final Monitor fan = Monitor.builder().name(FAN_NAME).targetId(TARGET_ID).monitorType(FAN).id(null).parentId(null).build();

			hostMonitoring.addMonitor(fan, FAN_ID, CONNECTOR_NAME, FAN, null, ENCLOSURE.getName());

			final Monitor fanResult = hostMonitoring.selectFromType(FAN).values().stream().findFirst().get();
			assertNotNull(fanResult);
			assertEquals(FULL_FAN_ID, fanResult.getId());
			// The Fan is attached to the target id because there is no enclosure
			assertEquals("targetId", fanResult.getParentId());
			assertEquals(TARGET_ID, fanResult.getTargetId());
		}
	}

	@Test
	void testAddSourceTable() {
		final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance()
				.createHostMonitoring(UUID.randomUUID().toString(), null);

		final SourceTable sourceTable = SourceTable.builder().build();

		hostMonitoring.getConnectorNamespace(CONNECTOR_NAME).addSourceTable(SOURCE_KEY_LOWER, sourceTable);

		assertEquals(1, hostMonitoring.getConnectorNamespace(CONNECTOR_NAME).getSourceTables().size());
		assertEquals(sourceTable, hostMonitoring.getConnectorNamespace(CONNECTOR_NAME).getSourceTables().get(SOURCE_KEY_LOWER));
		assertEquals(sourceTable, hostMonitoring.getConnectorNamespace(CONNECTOR_NAME).getSourceTable(SOURCE_KEY_PASCAL));
	}

	@Test
	void testClearCurrent() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();

		final Monitor enclosure = Monitor.builder().id(ENCLOSURE_ID).name(ENCLOSURE_NAME).targetId(TARGET_ID)
				.parentId(TARGET_ID).monitorType(ENCLOSURE).build();

		hostMonitoring.addMonitor(enclosure, ENCLOSURE_ID, CONNECTOR_NAME, ENCLOSURE, TARGET_ID, TARGET.getName());

		hostMonitoring.clearCurrent();

		assertTrue(hostMonitoring.getMonitors().isEmpty());
	}

	@Test
	void testBackup() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();

		final Monitor enclosure = Monitor.builder().id(ENCLOSURE_ID).name(ENCLOSURE_NAME).targetId(TARGET_ID)
				.parentId(TARGET_ID).monitorType(ENCLOSURE).build();

		hostMonitoring.addMonitor(enclosure, ENCLOSURE_ID, CONNECTOR_NAME, ENCLOSURE, TARGET_ID, TARGET.getName());

		hostMonitoring.backup();

		assertFalse(hostMonitoring.getPreviousMonitors().isEmpty());
		assertEquals(enclosure, hostMonitoring.getPreviousMonitors().get(ENCLOSURE).get(ENCLOSURE_ID));
	}

	@Test
	void testClearPrevious() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();

		final Monitor enclosure = Monitor.builder().id(ENCLOSURE_ID).name(ENCLOSURE_NAME).targetId(TARGET_ID)
				.parentId(TARGET_ID).monitorType(ENCLOSURE).build();

		hostMonitoring.addMonitor(enclosure, ENCLOSURE_ID, CONNECTOR_NAME, ENCLOSURE, TARGET_ID, TARGET.getName());

		hostMonitoring.backup();

		hostMonitoring.clearPrevious();

		assertTrue(hostMonitoring.getPreviousMonitors().isEmpty());
	}

	@Test
	void testResetParametersNumber() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();

		final Monitor enclosure = Monitor.builder().id(ENCLOSURE_ID).name(ENCLOSURE_NAME).targetId(TARGET_ID)
				.parentId(TARGET_ID).monitorType(ENCLOSURE).build();

		final long now = new Date().getTime();
		final IParameterValue parameter = NumberParam.builder().name(POWER_CONSUMPTION)
				.collectTime(now).value(100.0).rawValue(100.0).build();
		enclosure.addParameter(parameter);

		hostMonitoring.addMonitor(enclosure, ENCLOSURE_ID, CONNECTOR_NAME, ENCLOSURE, TARGET_ID, TARGET.getName());

		hostMonitoring.resetParameters();

		final Monitor result = hostMonitoring.getMonitors().get(ENCLOSURE).get(ENCLOSURE_ID);

		assertNotNull(result);

		final NumberParam parameterAfterReset = (NumberParam) result.getParameters().get(POWER_CONSUMPTION);

		assertNull(parameterAfterReset.getCollectTime());
		assertEquals(now, parameterAfterReset.getPreviousCollectTime());
		assertEquals(POWER_CONSUMPTION, parameterAfterReset.getName());
		assertEquals(ParameterState.OK, parameterAfterReset.getState());
		assertNull(parameterAfterReset.getValue());
		assertEquals(100.0, parameterAfterReset.getPreviousRawValue());
	}

	@Test
	void testResetParametersPresent() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();

		final Monitor enclosure = Monitor.builder().id(ENCLOSURE_ID).name(ENCLOSURE_NAME).targetId(TARGET_ID)
				.parentId(TARGET_ID).monitorType(ENCLOSURE).build();

		final IParameterValue parameter = PresentParam.builder()
				.state(ParameterState.OK).build();
		enclosure.addParameter(parameter);

		hostMonitoring.addMonitor(enclosure, ENCLOSURE_ID, CONNECTOR_NAME, ENCLOSURE, TARGET_ID, TARGET.getName());

		hostMonitoring.resetParameters();

		final Monitor result = hostMonitoring.getMonitors().get(ENCLOSURE).get(ENCLOSURE_ID);

		assertNotNull(result);

		final PresentParam parameterAfterReset = (PresentParam) result.getParameters().get(PRESENT);

		assertEquals(ParameterState.OK, parameterAfterReset.getState());
		assertEquals(1, parameterAfterReset.getPresent());
	}

	@Test
	void testResetParametersText() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();

		final Monitor enclosure = Monitor.builder().id(ENCLOSURE_ID).name(ENCLOSURE_NAME).targetId(TARGET_ID)
				.parentId(TARGET_ID).monitorType(ENCLOSURE).build();

		final IParameterValue parameter = TextParam.builder().name(TEST_REPORT).collectTime(new Date().getTime())
				.value("test").build();
		enclosure.addParameter(parameter);

		hostMonitoring.addMonitor(enclosure, ENCLOSURE_ID, CONNECTOR_NAME, ENCLOSURE, TARGET_ID, TARGET.getName());

		hostMonitoring.resetParameters();

		final Monitor result = hostMonitoring.getMonitors().get(ENCLOSURE).get(ENCLOSURE_ID);

		assertNotNull(result);

		final TextParam parameterAfterReset = (TextParam) result.getParameters().get(TEST_REPORT);

		assertNull(parameterAfterReset.getCollectTime());
		assertEquals(TEST_REPORT, parameterAfterReset.getName());
		assertEquals(ParameterState.OK, parameterAfterReset.getState());
		assertNull(parameterAfterReset.getValue());
	}

	@Test
	void testResetParametersStatus() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();

		final Monitor enclosure = Monitor.builder().id(ENCLOSURE_ID).name(ENCLOSURE_NAME).targetId(TARGET_ID)
				.parentId(TARGET_ID).monitorType(ENCLOSURE).build();

		final IParameterValue parameter = StatusParam.builder().name(STATUS).collectTime(new Date().getTime())
				.state(ParameterState.WARN).build();
		enclosure.addParameter(parameter);

		hostMonitoring.addMonitor(enclosure, ENCLOSURE_ID, CONNECTOR_NAME, ENCLOSURE, TARGET_ID, TARGET.getName());

		hostMonitoring.resetParameters();

		final Monitor result = hostMonitoring.getMonitors().get(ENCLOSURE).get(ENCLOSURE_ID);

		assertNotNull(result);

		final StatusParam parameterAfterReset = (StatusParam) result.getParameters().get(STATUS);

		assertNull(parameterAfterReset.getCollectTime());
		assertEquals(STATUS, parameterAfterReset.getName());
		assertEquals(ParameterState.OK, parameterAfterReset.getState());
		assertNull(parameterAfterReset.getStatus());
		assertNull(parameterAfterReset.getStatusInformation());
		assertEquals(ParameterState.WARN, parameterAfterReset.getPreviousState());
	}

	@Test
	void testToJson() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();

		final Monitor enclosure1 = Monitor.builder().id(ENCLOSURE_1).name(ENCLOSURE_1).targetId(TARGET_ID)
				.parentId(TARGET_ID).monitorType(ENCLOSURE).build();

		hostMonitoring.addMonitor(enclosure1);

		final Monitor enclosure2 = Monitor.builder().id(ENCLOSURE_2).name(ENCLOSURE_2)
				.targetId(TARGET_ID).parentId(TARGET_ID).monitorType(ENCLOSURE).build();

		hostMonitoring.addMonitor(enclosure2);
		System.out.println();
		assertEquals(ResourceHelper.getResourceAsString("/data/host-monitoring-vo.json", HostMonitoringTest.class).replaceAll("\\s", ""),
				hostMonitoring.toJson().replaceAll("\\s", ""));

	}

	@Test
	void testCopyParameters() {
		final StatusParam status = StatusParam
				.builder()
				.state(ParameterState.OK)
				.name(STATUS_PARAMETER)
				.statusInformation("OK")
				.build();
		final long collectTime = new Date().getTime();
		final NumberParam energyUsage = NumberParam
				.builder()
				.value(100.0)
				.rawValue(1500.0)
				.collectTime(collectTime)
				.name(ENERGY_USAGE_PARAMETER)
				.build();

		final long previousCollectTime = collectTime - (2 * 60 * 1000);

		energyUsage.setPreviousRawValue(1400.00);
		energyUsage.setPreviousCollectTime(previousCollectTime);

		final Monitor currentMonitor = Monitor.builder()
				.id(ENCLOSURE_1)
				.parameters(new HashMap<>(Map.of(STATUS_PARAMETER, status)))
				.build();

		final Monitor previousMonitor = Monitor.builder()
				.id(ENCLOSURE_1)
				.parameters(new HashMap<>(Map.of(STATUS_PARAMETER, status,
						ENERGY_USAGE_PARAMETER, energyUsage)))
				.build();

		HostMonitoring.copyParameters(previousMonitor, currentMonitor);

		assertEquals(status, currentMonitor.getParameter(STATUS_PARAMETER, StatusParam.class));
		assertEquals(energyUsage, currentMonitor.getParameter(ENERGY_USAGE_PARAMETER, NumberParam.class));
	}

	@Test
	void addMissingMonitor() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();

		final Monitor fan = Monitor.builder()
				.id(FAN_ID)
				.name(FAN_NAME)
				.targetId(TARGET_ID)
				.parentId(ENCLOSURE_ID)
				.monitorType(FAN)
				.build();

		hostMonitoring.addMissingMonitor(fan);

		final Monitor expectedFan = Monitor.builder()
				.id(FAN_ID)
				.name(FAN_NAME)
				.targetId(TARGET_ID)
				.parentId(ENCLOSURE_ID)
				.monitorType(FAN)
				.build();
		expectedFan.setAsMissing();

		final String fanBisId = FAN_ID + "bis";
		final Monitor fanBis = Monitor.builder()
				.id(fanBisId)
				.name(FAN_NAME + "bis")
				.targetId(TARGET_ID)
				.parentId(ENCLOSURE_ID)
				.monitorType(FAN)
				.build();

		hostMonitoring.addMissingMonitor(fanBis);

		final Monitor expectedFanBis = Monitor.builder()
				.id(fanBisId)
				.name(FAN_NAME + "bis")
				.targetId(TARGET_ID)
				.parentId(ENCLOSURE_ID)
				.monitorType(FAN)
				.build();
		expectedFanBis.setAsMissing();

		assertEquals(expectedFanBis, hostMonitoring.selectFromType(FAN).get(fanBisId));
		assertEquals(expectedFan, hostMonitoring.selectFromType(FAN).get(FAN_ID));

		Monitor voltageMonitor = Monitor.builder()
				.id("volatageID1")
				.targetId(TARGET_ID)
				.parentId(ENCLOSURE_ID)
				.name("volatageName1")
				.monitorType(MonitorType.VOLTAGE)
				.build();

		hostMonitoring.addMissingMonitor(voltageMonitor);
		assertNull(hostMonitoring.selectFromType(MonitorType.VOLTAGE)); // Voltage cannot be missing, we have already deleted it

	}

	@Test
	void testCopyAlertRules() {

		{
			final Monitor currentMonitor = Monitor.builder()
					.id(FAN_ID)
					.build();

			final Map<String, List<AlertRule>> staticAlertRules = new Fan().getStaticAlertRules();

			final Monitor previousMonitor = Monitor.builder()
					.id(FAN_ID)
					.alertRules(staticAlertRules)
					.build();

			HostMonitoring.copyAlertRules(previousMonitor, currentMonitor);

			assertEquals(staticAlertRules, currentMonitor.getAlertRules());
		}

		{
			final AlertRule alertRule = new AlertRule((monitor, conditions) -> null, AlertConditionsBuilder.newInstance().gte((double) Integer.MAX_VALUE).build() , ParameterState.ALARM);
			final Monitor currentMonitor = Monitor.builder()
					.alertRules(new HashMap<>(Map.of(PRESENT_PARAMETER, Collections.singletonList(alertRule))))
					.id(FAN_ID)
					.build();

			final Map<String, List<AlertRule>> staticAlertRules = new Fan().getStaticAlertRules();

			final Monitor previousMonitor = Monitor.builder()
					.id(FAN_ID)
					.alertRules(staticAlertRules)
					.build();

			HostMonitoring.copyAlertRules(previousMonitor, currentMonitor);

			assertNotEquals(currentMonitor.getAlertRules().get(PRESENT_PARAMETER),
					staticAlertRules.get(PRESENT_PARAMETER));
		}
	}

	@Test
	void testFindById() {

		IHostMonitoring hostMonitoring = new HostMonitoring();

		// monitorIdentifier is null
		assertThrows(IllegalArgumentException.class, () -> hostMonitoring.findById(null));

		// monitorIdentifier is not null, monitor not found
		hostMonitoring.setMonitors(Collections.emptyMap());
		assertNull(hostMonitoring.findById(FAN_ID));

		// monitorIdentifier is not null, monitor found
		Monitor expected = Monitor
			.builder()
			.id(FAN_ID)
			.monitorType(FAN)
			.build();
		hostMonitoring.setMonitors(Map.of(FAN, Map.of(FAN_ID, expected)));
		assertEquals(expected, hostMonitoring.findById(FAN_ID));
	}
}
