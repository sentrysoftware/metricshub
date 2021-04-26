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

import java.util.HashMap;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.engine.strategy.source.SourceTable;
import com.sentrysoftware.matrix.model.monitor.Monitor;

class HostMonitoringTest {

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
}
