package com.sentrysoftware.matrix.model.monitoring;

import static com.sentrysoftware.matrix.connector.model.monitor.MonitorType.TARGET;
import static com.sentrysoftware.matrix.connector.model.monitor.MonitorType.ENCLOSURE;
import static com.sentrysoftware.matrix.connector.model.monitor.MonitorType.FAN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.engine.strategy.source.SourceTable;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.parameter.BooleanParam;
import com.sentrysoftware.matrix.model.parameter.IParameterValue;
import com.sentrysoftware.matrix.model.parameter.NumberParam;
import com.sentrysoftware.matrix.model.parameter.ParameterState;
import com.sentrysoftware.matrix.model.parameter.StatusParam;
import com.sentrysoftware.matrix.model.parameter.TextParam;

class HostMonitoringTest {

	private static final String STATUS = "Status";
	private static final String TEST_REPORT = "TestReport";
	private static final String PRESENT = "Present";
	private static final String POWER_CONSUMPTION = "PowerConsumption";
	private static final String SOURCE_KEY_LOWER = "enclosure.discovery.source(1)";
	private static final String SOURCE_KEY_PASCAL = "Enclosure.discovery.Source(1)";
	private static final String FULL_FAN_ID = "myConnector.connector_fan_targetId_fanId";
	private static final String CONNECTOR_NAME = "myConnector.connector";
	private static final String FAN_NAME = "fan";
	private static final String ENCLOSURE_NAME = "enclosure";
	private static final String target_NAME = "target";
	private static final String FAN_ID = "fanId";
	private static final String ENCLOSURE_ID = "enclosureId";
	private static final String TARGET_ID = "targetId";

	@Test
	void testRemoveMonitorException() {
		final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance()
				.createHostMonitoring(UUID.randomUUID().toString());
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
					.createHostMonitoring(UUID.randomUUID().toString());
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
					.createHostMonitoring(UUID.randomUUID().toString());
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
					.createHostMonitoring(UUID.randomUUID().toString());
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
					.createHostMonitoring(UUID.randomUUID().toString());
			final Monitor target = Monitor.builder().id(TARGET_ID).targetId(TARGET_ID).name(target_NAME).monitorType(TARGET).build();
			final Monitor enclosure = Monitor.builder().id(ENCLOSURE_ID).name(ENCLOSURE_NAME).targetId(TARGET_ID)
					.parentId(TARGET_ID).monitorType(ENCLOSURE).build();
			final Monitor fan = Monitor.builder().id(FAN_ID).name(FAN_NAME).targetId(TARGET_ID)
					.parentId(ENCLOSURE_ID).monitorType(FAN).build();

			hostMonitoring.getMonitors().put(MonitorType.TARGET, new HashMap<String, Monitor>());
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
				.createHostMonitoring(UUID.randomUUID().toString());

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
				.createHostMonitoring(UUID.randomUUID().toString());

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
					.createHostMonitoring(UUID.randomUUID().toString());

			final Monitor enclosure = Monitor.builder().id(ENCLOSURE_ID).name(ENCLOSURE_NAME).targetId(TARGET_ID)
					.parentId(TARGET_ID).monitorType(ENCLOSURE).build();

			hostMonitoring.addMonitor(enclosure, ENCLOSURE_ID, CONNECTOR_NAME, ENCLOSURE, TARGET_ID, TARGET.getName());

			assertEquals(enclosure, hostMonitoring.selectFromType(ENCLOSURE).get(ENCLOSURE_ID));
		}

		{
			final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance()
					.createHostMonitoring(UUID.randomUUID().toString());
			
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
					.createHostMonitoring(UUID.randomUUID().toString());
			
			final String expectedEnclosureId = HostMonitoring.buildMonitorId(CONNECTOR_NAME, MonitorType.ENCLOSURE, TARGET_ID, ENCLOSURE_ID);
			final Monitor enclosure = Monitor.builder().id(expectedEnclosureId).name(ENCLOSURE_NAME).targetId(TARGET_ID)
					.parentId(TARGET_ID).monitorType(ENCLOSURE).extendedType(HardwareConstants.COMPUTER).build();

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
					.createHostMonitoring(UUID.randomUUID().toString());
			
			final String expectedEnclosureId = HostMonitoring.buildMonitorId(CONNECTOR_NAME, MonitorType.ENCLOSURE, TARGET_ID, ENCLOSURE_ID);
			final Monitor enclosure = Monitor.builder().id(expectedEnclosureId).name(ENCLOSURE_NAME).targetId(TARGET_ID)
					.parentId(TARGET_ID).monitorType(ENCLOSURE).extendedType(HardwareConstants.STORAGE).build();

			hostMonitoring.addMonitor(enclosure);

			final Monitor fan = Monitor.builder().name(FAN_NAME).targetId(TARGET_ID).monitorType(FAN).id(null).parentId(null).build();

			hostMonitoring.addMonitor(fan, FAN_ID, CONNECTOR_NAME, FAN, null, ENCLOSURE.getName());

			final Monitor fanResult = hostMonitoring.selectFromType(FAN).values().stream().findFirst().get();
			assertNotNull(fanResult);
			assertEquals(FULL_FAN_ID, fanResult.getId());
			// The Fan is attached to the target id because we haven't a "Computer" enclosure and AttachedTotargetId is not set
			assertEquals(TARGET_ID, fanResult.getParentId());
			assertEquals(TARGET_ID, fanResult.getTargetId());
		}

		{
			final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance()
					.createHostMonitoring(UUID.randomUUID().toString());

			final Monitor fan = Monitor.builder().name(FAN_NAME).targetId(TARGET_ID).monitorType(FAN).id(null).parentId(null).build();

			hostMonitoring.addMonitor(fan, FAN_ID, CONNECTOR_NAME, FAN, null, ENCLOSURE.getName());

			final Monitor fanResult = hostMonitoring.selectFromType(FAN).values().stream().findFirst().get();
			assertNotNull(fanResult);
			assertEquals(FULL_FAN_ID, fanResult.getId());
			// The Fan is attached to the target id because there is no enclosure
			assertEquals(TARGET_ID, fanResult.getParentId());
			assertEquals(TARGET_ID, fanResult.getTargetId());
		}
	}

	@Test
	void testAddSourceTable() {
		final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance()
				.createHostMonitoring(UUID.randomUUID().toString());

		final SourceTable sourceTable = SourceTable.builder().build();

		hostMonitoring.addSourceTable(SOURCE_KEY_LOWER, sourceTable);

		assertEquals(1, hostMonitoring.getSourceTables().size());
		assertEquals(sourceTable, hostMonitoring.getSourceTables().get(SOURCE_KEY_LOWER));
		assertEquals(sourceTable, hostMonitoring.getSourceTableByKey(SOURCE_KEY_PASCAL));
	}

	@Test
	void testClear() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();

		final Monitor enclosure = Monitor.builder().id(ENCLOSURE_ID).name(ENCLOSURE_NAME).targetId(TARGET_ID)
				.parentId(TARGET_ID).monitorType(ENCLOSURE).build();

		hostMonitoring.addMonitor(enclosure, ENCLOSURE_ID, CONNECTOR_NAME, ENCLOSURE, TARGET_ID, TARGET.getName());

		hostMonitoring.clear();

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
	void testResetParametersNumber() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();

		final Monitor enclosure = Monitor.builder().id(ENCLOSURE_ID).name(ENCLOSURE_NAME).targetId(TARGET_ID)
				.parentId(TARGET_ID).monitorType(ENCLOSURE).build();

		final long now = new Date().getTime();
		final IParameterValue parameter = NumberParam.builder().name(POWER_CONSUMPTION)
				.collectTime(now).value(100.0).build();
		enclosure.addParameter(parameter);

		hostMonitoring.addMonitor(enclosure, ENCLOSURE_ID, CONNECTOR_NAME, ENCLOSURE, TARGET_ID, TARGET.getName());

		hostMonitoring.resetParameters();

		final Monitor result = hostMonitoring.getMonitors().get(ENCLOSURE).get(ENCLOSURE_ID);

		assertNotNull(result);

		final NumberParam parameterAfterReset = (NumberParam) result.getParameters().get(POWER_CONSUMPTION);

		assertNull(parameterAfterReset.getCollectTime());
		assertEquals(now, parameterAfterReset.getLastCollectTime());
		assertEquals(POWER_CONSUMPTION, parameterAfterReset.getName());
		assertEquals(ParameterState.OK, parameterAfterReset.getState());
		assertNull(parameterAfterReset.getThreshold());
		assertNull(parameterAfterReset.getValue());
		assertEquals(100.0, parameterAfterReset.getLastValue());
	}

	@Test
	void testResetParametersBoolean() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();

		final Monitor enclosure = Monitor.builder().id(ENCLOSURE_ID).name(ENCLOSURE_NAME).targetId(TARGET_ID)
				.parentId(TARGET_ID).monitorType(ENCLOSURE).build();

		final IParameterValue parameter = BooleanParam.builder().name(PRESENT).collectTime(new Date().getTime())
				.value(true).build();
		enclosure.addParameter(parameter);

		hostMonitoring.addMonitor(enclosure, ENCLOSURE_ID, CONNECTOR_NAME, ENCLOSURE, TARGET_ID, TARGET.getName());

		hostMonitoring.resetParameters();

		final Monitor result = hostMonitoring.getMonitors().get(ENCLOSURE).get(ENCLOSURE_ID);

		assertNotNull(result);

		final BooleanParam parameterAfterReset = (BooleanParam) result.getParameters().get(PRESENT);

		assertNull(parameterAfterReset.getCollectTime());
		assertEquals(PRESENT, parameterAfterReset.getName());
		assertEquals(ParameterState.OK, parameterAfterReset.getState());
		assertNull(parameterAfterReset.getThreshold());
		assertFalse(parameterAfterReset.isValue());
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
		assertNull(parameterAfterReset.getThreshold());
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
		assertNull(parameterAfterReset.getThreshold());
		assertNull(parameterAfterReset.getStatus());
		assertNull(parameterAfterReset.getStatusInformation());
	}
}
