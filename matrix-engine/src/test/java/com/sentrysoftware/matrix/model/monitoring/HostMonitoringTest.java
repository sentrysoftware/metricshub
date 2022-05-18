package com.sentrysoftware.matrix.model.monitoring;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.COMPUTER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.STORAGE;
import static com.sentrysoftware.matrix.connector.model.monitor.MonitorType.ENCLOSURE;
import static com.sentrysoftware.matrix.connector.model.monitor.MonitorType.FAN;
import static com.sentrysoftware.matrix.connector.model.monitor.MonitorType.HOST;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.File;
import java.io.FileInputStream;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.common.helpers.JsonHelper;
import com.sentrysoftware.matrix.common.meta.parameter.state.Present;
import com.sentrysoftware.matrix.common.meta.parameter.state.Status;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.engine.strategy.source.SourceTable;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.parameter.DiscreteParam;
import com.sentrysoftware.matrix.model.parameter.IParameter;
import com.sentrysoftware.matrix.model.parameter.NumberParam;
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
	private static final String FULL_FAN_ID = "myConnector_fan_hostId_fanId";
	private static final String CONNECTOR_NAME = "myConnector";
	private static final String FAN_NAME = "fan";
	private static final String ENCLOSURE_NAME = "enclosure";
	private static final String target_NAME = "target";
	private static final String FAN_ID = "fanId";
	private static final String ENCLOSURE_ID = "enclosureId";
	private static final String HOST_ID = "hostId";

	@Test
	void testRemoveMonitorException() {
		final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance()
				.createHostMonitoring(UUID.randomUUID().toString(), null);
		final Monitor noHostId = Monitor.builder().hostId(HOST_ID).name(target_NAME)
				.monitorType(HOST).build();
		assertThrows(IllegalArgumentException.class, () -> hostMonitoring.removeMonitor(noHostId));


		final Monitor noMonitorType = Monitor.builder().hostId(HOST_ID).id(HOST_ID).name(target_NAME).build();
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
			final Monitor target = Monitor.builder().id(HOST_ID).hostId(HOST_ID).name(target_NAME)
					.monitorType(HOST).build();
			final Monitor enclosure = Monitor.builder().id(ENCLOSURE_ID).name(ENCLOSURE_NAME).hostId(HOST_ID)
					.parentId(HOST_ID).monitorType(ENCLOSURE).build();
			final Monitor fan = Monitor.builder().id(FAN_ID).name(FAN_NAME).hostId(HOST_ID)
					.parentId(ENCLOSURE_ID).monitorType(FAN).build();

			hostMonitoring.addMonitor(target);
			hostMonitoring.addMonitor(enclosure);
			hostMonitoring.addMonitor(fan);

			hostMonitoring.removeMonitor(target);

			assertTrue(hostMonitoring.selectFromType(HOST).isEmpty());
			assertTrue(hostMonitoring.selectFromType(ENCLOSURE).isEmpty());
			assertTrue(hostMonitoring.selectFromType(FAN).isEmpty());

		}

		{
			final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance()
					.createHostMonitoring(UUID.randomUUID().toString(), null);
			final Monitor target = Monitor.builder().id(HOST_ID).hostId(HOST_ID).name(target_NAME).monitorType(HOST).build();
			final Monitor enclosure = Monitor.builder().id(ENCLOSURE_ID).name(ENCLOSURE_NAME).hostId(HOST_ID)
					.parentId(HOST_ID).monitorType(ENCLOSURE).build();
			final Monitor fan = Monitor.builder().id(FAN_ID).name(FAN_NAME).hostId(HOST_ID)
					.parentId(ENCLOSURE_ID).monitorType(FAN).build();

			hostMonitoring.addMonitor(target);
			hostMonitoring.addMonitor(enclosure);
			hostMonitoring.addMonitor(fan);

			hostMonitoring.removeMonitor(enclosure);

			assertFalse(hostMonitoring.selectFromType(HOST).isEmpty());
			assertTrue(hostMonitoring.selectFromType(ENCLOSURE).isEmpty());
			assertTrue(hostMonitoring.selectFromType(FAN).isEmpty());

		}

		{
			final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance()
					.createHostMonitoring(UUID.randomUUID().toString(), null);
			final Monitor target = Monitor.builder().id(HOST_ID).hostId(HOST_ID).name(target_NAME).monitorType(HOST).build();
			final Monitor enclosure = Monitor.builder().id(ENCLOSURE_ID).name(ENCLOSURE_NAME).hostId(HOST_ID)
					.parentId(HOST_ID).monitorType(ENCLOSURE).build();
			final Monitor fan = Monitor.builder().id(FAN_ID).name(FAN_NAME).hostId(HOST_ID)
					.parentId(ENCLOSURE_ID).monitorType(FAN).build();

			hostMonitoring.addMonitor(enclosure);
			hostMonitoring.addMonitor(fan);

			hostMonitoring.removeMonitor(target);

			assertNull(hostMonitoring.selectFromType(HOST));
			assertTrue(hostMonitoring.selectFromType(ENCLOSURE).isEmpty());
			assertTrue(hostMonitoring.selectFromType(FAN).isEmpty());

		}

		{
			final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance()
					.createHostMonitoring(UUID.randomUUID().toString(), null);
			final Monitor target = Monitor.builder().id(HOST_ID).hostId(HOST_ID).name(target_NAME).monitorType(HOST).build();
			final Monitor enclosure = Monitor.builder().id(ENCLOSURE_ID).name(ENCLOSURE_NAME).hostId(HOST_ID)
					.parentId(HOST_ID).monitorType(ENCLOSURE).build();
			final Monitor fan = Monitor.builder().id(FAN_ID).name(FAN_NAME).hostId(HOST_ID)
					.parentId(ENCLOSURE_ID).monitorType(FAN).build();

			hostMonitoring.getMonitors().put(MonitorType.HOST, new HashMap<>());
			hostMonitoring.addMonitor(enclosure);
			hostMonitoring.addMonitor(fan);

			hostMonitoring.removeMonitor(target);

			assertTrue(hostMonitoring.selectFromType(HOST).isEmpty());
			assertTrue(hostMonitoring.selectFromType(ENCLOSURE).isEmpty());
			assertTrue(hostMonitoring.selectFromType(FAN).isEmpty());

			hostMonitoring.getMonitors().put(MonitorType.HOST, null);
			hostMonitoring.addMonitor(enclosure);
			hostMonitoring.addMonitor(fan);

			hostMonitoring.removeMonitor(target);

			assertNull(hostMonitoring.selectFromType(HOST));
			assertTrue(hostMonitoring.selectFromType(ENCLOSURE).isEmpty());
			assertTrue(hostMonitoring.selectFromType(FAN).isEmpty());

		}
	}

	@Test
	void testAddMonitorException() {
		final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance()
				.createHostMonitoring(UUID.randomUUID().toString(), null);

		assertThrows(IllegalArgumentException.class, () -> hostMonitoring.addMonitor(null));

		final Monitor nohostId = Monitor.builder().hostId(HOST_ID).name(target_NAME).monitorType(HOST).build();
		assertThrows(IllegalArgumentException.class, () -> hostMonitoring.addMonitor(nohostId));

		final Monitor noMonitorType = Monitor.builder().id(HOST_ID).hostId(HOST_ID).name(target_NAME).build();
		assertThrows(IllegalArgumentException.class, () -> hostMonitoring.addMonitor(noMonitorType));

		final Monitor noHostId = Monitor.builder().id(HOST_ID).monitorType(HOST).name(target_NAME).build();
		assertThrows(IllegalArgumentException.class, () -> hostMonitoring.addMonitor(noHostId));

		final Monitor noParentId = Monitor.builder().id(ENCLOSURE_ID).name(ENCLOSURE_NAME).hostId(HOST_ID).monitorType(ENCLOSURE).build();
		assertThrows(IllegalArgumentException.class, () -> hostMonitoring.addMonitor(noParentId));
	}

	@Test
	void testAddMonitor() {
		final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance()
				.createHostMonitoring(UUID.randomUUID().toString(), null);

		final Monitor enclosure = Monitor.builder().id(ENCLOSURE_ID).name(ENCLOSURE_NAME).hostId(HOST_ID)
				.parentId(HOST_ID).monitorType(ENCLOSURE).build();

		hostMonitoring.addMonitor(enclosure);

		assertEquals(enclosure, hostMonitoring.selectFromType(ENCLOSURE).get(ENCLOSURE_ID));

		final String enclosureBisId = ENCLOSURE_ID + "bis";
		final Monitor enclosureBis = Monitor.builder().id(enclosureBisId).name(ENCLOSURE_NAME + "bis")
				.hostId(HOST_ID).parentId(HOST_ID).monitorType(ENCLOSURE).build();

		hostMonitoring.addMonitor(enclosureBis);
		assertEquals(enclosure, hostMonitoring.selectFromType(ENCLOSURE).get(ENCLOSURE_ID));
		assertEquals(enclosureBis, hostMonitoring.selectFromType(ENCLOSURE).get(enclosureBisId));
	}

	@Test
	void testAddMonitorWithArguments() {
		{
			final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance()
					.createHostMonitoring(UUID.randomUUID().toString(), null);

			final Monitor enclosure = Monitor.builder().id(ENCLOSURE_ID).name(ENCLOSURE_NAME).hostId(HOST_ID)
					.parentId(HOST_ID).monitorType(ENCLOSURE).build();

			hostMonitoring.addMonitor(enclosure, ENCLOSURE_ID, CONNECTOR_NAME, ENCLOSURE, HOST_ID, HOST.getNameInConnector());

			assertEquals(enclosure, hostMonitoring.selectFromType(ENCLOSURE).get(ENCLOSURE_ID));
		}

		{
			final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance()
					.createHostMonitoring(UUID.randomUUID().toString(), null);

			final String expectedEnclosureId = HostMonitoring.buildMonitorId(CONNECTOR_NAME, MonitorType.ENCLOSURE, HOST_ID, ENCLOSURE_ID);
			final Monitor enclosure = Monitor.builder().id(expectedEnclosureId).name(ENCLOSURE_NAME).hostId(HOST_ID)
					.parentId(HOST_ID).monitorType(ENCLOSURE).build();

			hostMonitoring.addMonitor(enclosure);

			final Monitor fan = Monitor.builder().name(FAN_NAME).hostId(HOST_ID).monitorType(FAN).id(null).parentId(null).build();

			hostMonitoring.addMonitor(fan, FAN_ID, CONNECTOR_NAME, FAN, ENCLOSURE_ID, ENCLOSURE.getNameInConnector());

			final Monitor fanResult = hostMonitoring.selectFromType(FAN).values().stream().findFirst().get();
			assertNotNull(fanResult);
			assertEquals(FULL_FAN_ID, fanResult.getId());
			assertEquals(expectedEnclosureId, fanResult.getParentId());
			assertEquals(HOST_ID, fanResult.getHostId());
		}

		{
			final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance()
					.createHostMonitoring(UUID.randomUUID().toString(), null);

			final String expectedEnclosureId = HostMonitoring.buildMonitorId(CONNECTOR_NAME, MonitorType.ENCLOSURE, HOST_ID, ENCLOSURE_ID);
			final Monitor enclosure = Monitor.builder().id(expectedEnclosureId).name(ENCLOSURE_NAME).hostId(HOST_ID)
					.parentId(HOST_ID).monitorType(ENCLOSURE).extendedType(COMPUTER).build();

			hostMonitoring.addMonitor(enclosure);

			final Monitor fan = Monitor.builder().name(FAN_NAME).hostId(HOST_ID).monitorType(FAN).id(null).parentId(null).build();

			hostMonitoring.addMonitor(fan, FAN_ID, CONNECTOR_NAME, FAN, null, ENCLOSURE.getNameInConnector());

			final Monitor fanResult = hostMonitoring.selectFromType(FAN).values().stream().findFirst().get();
			assertNotNull(fanResult);
			assertEquals(FULL_FAN_ID, fanResult.getId());
			assertEquals(expectedEnclosureId, fanResult.getParentId());
			assertEquals(HOST_ID, fanResult.getHostId());
		}

		{
			final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance()
					.createHostMonitoring(UUID.randomUUID().toString(), null);

			final String expectedEnclosureId = HostMonitoring.buildMonitorId(CONNECTOR_NAME, MonitorType.ENCLOSURE, HOST_ID, ENCLOSURE_ID);
			final Monitor enclosure = Monitor.builder().id(expectedEnclosureId).name(ENCLOSURE_NAME).hostId(HOST_ID)
					.parentId(HOST_ID).monitorType(ENCLOSURE).extendedType(STORAGE).build();

			hostMonitoring.addMonitor(enclosure);

			final Monitor fan = Monitor.builder().name(FAN_NAME).hostId(HOST_ID).monitorType(FAN).id(null).parentId(null).build();

			hostMonitoring.addMonitor(fan, FAN_ID, CONNECTOR_NAME, FAN, null, ENCLOSURE.getNameInConnector());

			final Monitor fanResult = hostMonitoring.selectFromType(FAN).values().stream().findFirst().get();
			assertNotNull(fanResult);
			assertEquals(FULL_FAN_ID, fanResult.getId());
			// The Fan is attached to the target id because we haven't a "Computer" enclosure and AttachedTohostId is not set
			assertEquals(enclosure.getId(), fanResult.getParentId());
			assertEquals(HOST_ID, fanResult.getHostId());
		}

		{
			final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance()
					.createHostMonitoring(UUID.randomUUID().toString(), null);

			final Monitor fan = Monitor.builder().name(FAN_NAME).hostId(HOST_ID).monitorType(FAN).id(null).parentId(null).build();

			hostMonitoring.addMonitor(fan, FAN_ID, CONNECTOR_NAME, FAN, null, ENCLOSURE.getNameInConnector());

			final Monitor fanResult = hostMonitoring.selectFromType(FAN).values().stream().findFirst().get();
			assertNotNull(fanResult);
			assertEquals(FULL_FAN_ID, fanResult.getId());
			// The Fan is attached to the target id because there is no enclosure
			assertEquals("hostId", fanResult.getParentId());
			assertEquals(HOST_ID, fanResult.getHostId());
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
	void testClear() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();

		final Monitor enclosure = Monitor.builder().id(ENCLOSURE_ID).name(ENCLOSURE_NAME).hostId(HOST_ID)
				.parentId(HOST_ID).monitorType(ENCLOSURE).build();

		hostMonitoring.addMonitor(enclosure, ENCLOSURE_ID, CONNECTOR_NAME, ENCLOSURE, HOST_ID, HOST.getNameInConnector());

		hostMonitoring.clear();

		assertTrue(hostMonitoring.getMonitors().isEmpty());
	}


	@Test
	void testSaveParametersNumber() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();

		final Monitor enclosure = Monitor.builder().id(ENCLOSURE_ID).name(ENCLOSURE_NAME).hostId(HOST_ID)
				.parentId(HOST_ID).monitorType(ENCLOSURE).build();

		final long now = new Date().getTime();
		final IParameter parameter = NumberParam.builder().name(POWER_CONSUMPTION)
				.collectTime(now).value(100.0).rawValue(100.0).build();
		enclosure.addParameter(parameter);

		hostMonitoring.addMonitor(enclosure, ENCLOSURE_ID, CONNECTOR_NAME, ENCLOSURE, HOST_ID, HOST.getNameInConnector());

		hostMonitoring.saveParameters();

		final Monitor result = hostMonitoring.getMonitors().get(ENCLOSURE).get(ENCLOSURE_ID);

		assertNotNull(result);

		final NumberParam parameterAfterSave = (NumberParam) result.getParameters().get(POWER_CONSUMPTION);

		assertNotNull(parameterAfterSave.getCollectTime());
		assertEquals(now, parameterAfterSave.getPreviousCollectTime());
		assertEquals(POWER_CONSUMPTION, parameterAfterSave.getName());
		assertNotNull(parameterAfterSave.getValue());
		assertEquals(100.0, parameterAfterSave.getPreviousRawValue());
	}

	@Test
	void testSaveParametersPresent() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();

		final Monitor enclosure = Monitor.builder().id(ENCLOSURE_ID).name(ENCLOSURE_NAME).hostId(HOST_ID)
				.parentId(HOST_ID).monitorType(ENCLOSURE).build();

		final IParameter parameter = DiscreteParam.present();
		enclosure.addParameter(parameter);

		hostMonitoring.addMonitor(enclosure, ENCLOSURE_ID, CONNECTOR_NAME, ENCLOSURE, HOST_ID, HOST.getNameInConnector());

		hostMonitoring.saveParameters();

		final Monitor result = hostMonitoring.getMonitors().get(ENCLOSURE).get(ENCLOSURE_ID);

		assertNotNull(result);

		final DiscreteParam parameterAfterReset = result.getParameter(PRESENT, DiscreteParam.class);

		assertEquals(Present.PRESENT, parameterAfterReset.getState());
		assertEquals(1, parameterAfterReset.numberValue());
	}

	@Test
	void testSaveParametersText() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();

		final Monitor enclosure = Monitor.builder().id(ENCLOSURE_ID).name(ENCLOSURE_NAME).hostId(HOST_ID)
				.parentId(HOST_ID).monitorType(ENCLOSURE).build();

		final IParameter parameter = TextParam.builder().name(TEST_REPORT).collectTime(new Date().getTime())
				.value("test").build();
		enclosure.addParameter(parameter);

		hostMonitoring.addMonitor(enclosure, ENCLOSURE_ID, CONNECTOR_NAME, ENCLOSURE, HOST_ID, HOST.getNameInConnector());

		hostMonitoring.saveParameters();

		final Monitor result = hostMonitoring.getMonitors().get(ENCLOSURE).get(ENCLOSURE_ID);

		assertNotNull(result);

		final TextParam parameterAfterSave = (TextParam) result.getParameters().get(TEST_REPORT);

		assertNotNull(parameterAfterSave.getCollectTime());
		assertEquals(TEST_REPORT, parameterAfterSave.getName());
		assertNotNull(parameterAfterSave.getValue());
	}

	@Test
	void testSaveParametersStatus() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();

		final Monitor enclosure = Monitor.builder().id(ENCLOSURE_ID).name(ENCLOSURE_NAME).hostId(HOST_ID)
				.parentId(HOST_ID).monitorType(ENCLOSURE).build();

		final IParameter parameter = DiscreteParam.builder()
				.name(STATUS)
				.collectTime(new Date().getTime())
				.state(Status.DEGRADED).build();
		enclosure.addParameter(parameter);

		hostMonitoring.addMonitor(enclosure, ENCLOSURE_ID, CONNECTOR_NAME, ENCLOSURE, HOST_ID, HOST.getNameInConnector());

		hostMonitoring.saveParameters();

		final Monitor result = hostMonitoring.getMonitors().get(ENCLOSURE).get(ENCLOSURE_ID);

		assertNotNull(result);

		final DiscreteParam parameterAfterSave =  result.getParameter(STATUS, DiscreteParam.class);

		assertNotNull(parameterAfterSave.getCollectTime());
		assertEquals(STATUS, parameterAfterSave.getName());
		assertEquals(Status.DEGRADED, parameterAfterSave.getState());
		assertNotNull(parameterAfterSave.getState());
		assertEquals(Status.DEGRADED, parameterAfterSave.getPreviousState());
	}

	@Test
	void testToJson() throws Exception {
		final IHostMonitoring hostMonitoring = new HostMonitoring();

		final Monitor enclosure1 = Monitor
				.builder()
				.id(ENCLOSURE_1)
				.name(ENCLOSURE_1)
				.hostId(HOST_ID)
				.parentId(HOST_ID)
				.monitorType(ENCLOSURE)
				.discoveryTime(1633620837079L)
				.build();
		enclosure1.setAsPresent();
		hostMonitoring.getMonitors().put(ENCLOSURE, new HashMap<>(Map.of(ENCLOSURE_1, enclosure1)));

		final Monitor enclosure2 = Monitor
				.builder()
				.id(ENCLOSURE_2)
				.name(ENCLOSURE_2)
				.hostId(HOST_ID)
				.parentId(HOST_ID)
				.monitorType(ENCLOSURE)
				.discoveryTime(1633620837079L)
				.build();
		enclosure2.setAsPresent();
		hostMonitoring.getMonitors().get(ENCLOSURE).put(ENCLOSURE_2, enclosure2);

		final HostMonitoringVo expected = JsonHelper.deserialize(
				new FileInputStream(new File("src/test/resources/data/host-monitoring-vo.json")),
				HostMonitoringVo.class);

		final HostMonitoringVo actual = JsonHelper.deserialize(hostMonitoring.toJson(), HostMonitoringVo.class);

		assertEquals(expected, actual);

	}

	@Test
	void addMissingMonitor() {
		final IHostMonitoring hostMonitoring = new HostMonitoring();

		final Monitor fan = Monitor.builder()
				.id(FAN_ID)
				.name(FAN_NAME)
				.hostId(HOST_ID)
				.parentId(ENCLOSURE_ID)
				.monitorType(FAN)
				.build();

		hostMonitoring.addMissingMonitor(fan);

		final Monitor expectedFan = Monitor.builder()
				.id(FAN_ID)
				.name(FAN_NAME)
				.hostId(HOST_ID)
				.parentId(ENCLOSURE_ID)
				.monitorType(FAN)
				.build();
		expectedFan.setAsMissing();

		final String fanBisId = FAN_ID + "bis";
		final Monitor fanBis = Monitor.builder()
				.id(fanBisId)
				.name(FAN_NAME + "bis")
				.hostId(HOST_ID)
				.parentId(ENCLOSURE_ID)
				.monitorType(FAN)
				.build();

		hostMonitoring.addMissingMonitor(fanBis);

		final Monitor expectedFanBis = Monitor.builder()
				.id(fanBisId)
				.name(FAN_NAME + "bis")
				.hostId(HOST_ID)
				.parentId(ENCLOSURE_ID)
				.monitorType(FAN)
				.build();
		expectedFanBis.setAsMissing();

		assertEquals(expectedFanBis, hostMonitoring.selectFromType(FAN).get(fanBisId));
		assertEquals(expectedFan, hostMonitoring.selectFromType(FAN).get(FAN_ID));

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
