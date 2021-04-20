package com.sentrysoftware.matrix.model.monitoring;

import static com.sentrysoftware.matrix.connector.model.monitor.MonitorType.DEVICE;
import static com.sentrysoftware.matrix.connector.model.monitor.MonitorType.ENCLOSURE;
import static com.sentrysoftware.matrix.connector.model.monitor.MonitorType.FAN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.HashMap;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.model.monitor.Monitor;

class HostMonitoringTest {

	private static final String FAN_NAME = "fan";
	private static final String ENCLOSURE_NAME = "enclosure";
	private static final String DEVICE_NAME = "device";
	private static final String FAN_ID = "fanId";
	private static final String ENCLOSURE_ID = "enclosureId";
	private static final String DEVICE_ID = "deviceId";

	@Test
	void testRemoveMonitorException() {
		final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance()
				.createHostMonitoring(UUID.randomUUID().toString());
		final Monitor noDeviceId = Monitor.builder().targetId(DEVICE_ID).name(DEVICE_NAME)
				.monitorType(DEVICE).build();
		assertThrows(IllegalArgumentException.class, () -> hostMonitoring.removeMonitor(noDeviceId));
		

		final Monitor noMonitorType = Monitor.builder().targetId(DEVICE_ID).deviceId(DEVICE_ID).name(DEVICE_NAME).build();
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
			final Monitor device = Monitor.builder().deviceId(DEVICE_ID).targetId(DEVICE_ID).name(DEVICE_NAME)
					.monitorType(DEVICE).build();
			final Monitor enclosure = Monitor.builder().deviceId(ENCLOSURE_ID).name(ENCLOSURE_NAME).targetId(DEVICE_ID)
					.parentId(DEVICE_ID).monitorType(ENCLOSURE).build();
			final Monitor fan = Monitor.builder().deviceId(FAN_ID).name(FAN_NAME).targetId(DEVICE_ID)
					.parentId(ENCLOSURE_ID).monitorType(FAN).build();

			hostMonitoring.addMonitor(device);
			hostMonitoring.addMonitor(enclosure);
			hostMonitoring.addMonitor(fan);

			hostMonitoring.removeMonitor(device);

			assertTrue(hostMonitoring.selectFromType(DEVICE).isEmpty());
			assertTrue(hostMonitoring.selectFromType(ENCLOSURE).isEmpty());
			assertTrue(hostMonitoring.selectFromType(FAN).isEmpty());

		}

		{
			final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance()
					.createHostMonitoring(UUID.randomUUID().toString());
			final Monitor device = Monitor.builder().deviceId(DEVICE_ID).targetId(DEVICE_ID).name(DEVICE_NAME).monitorType(DEVICE).build();
			final Monitor enclosure = Monitor.builder().deviceId(ENCLOSURE_ID).name(ENCLOSURE_NAME).targetId(DEVICE_ID)
					.parentId(DEVICE_ID).monitorType(ENCLOSURE).build();
			final Monitor fan = Monitor.builder().deviceId(FAN_ID).name(FAN_NAME).targetId(DEVICE_ID)
					.parentId(ENCLOSURE_ID).monitorType(FAN).build();

			hostMonitoring.addMonitor(device);
			hostMonitoring.addMonitor(enclosure);
			hostMonitoring.addMonitor(fan);

			hostMonitoring.removeMonitor(enclosure);

			assertFalse(hostMonitoring.selectFromType(DEVICE).isEmpty());
			assertTrue(hostMonitoring.selectFromType(ENCLOSURE).isEmpty());
			assertTrue(hostMonitoring.selectFromType(FAN).isEmpty());

		}

		{
			final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance()
					.createHostMonitoring(UUID.randomUUID().toString());
			final Monitor device = Monitor.builder().deviceId(DEVICE_ID).targetId(DEVICE_ID).name(DEVICE_NAME).monitorType(DEVICE).build();
			final Monitor enclosure = Monitor.builder().deviceId(ENCLOSURE_ID).name(ENCLOSURE_NAME).targetId(DEVICE_ID)
					.parentId(DEVICE_ID).monitorType(ENCLOSURE).build();
			final Monitor fan = Monitor.builder().deviceId(FAN_ID).name(FAN_NAME).targetId(DEVICE_ID)
					.parentId(ENCLOSURE_ID).monitorType(FAN).build();

			hostMonitoring.addMonitor(enclosure);
			hostMonitoring.addMonitor(fan);

			hostMonitoring.removeMonitor(device);

			assertNull(hostMonitoring.selectFromType(DEVICE));
			assertTrue(hostMonitoring.selectFromType(ENCLOSURE).isEmpty());
			assertTrue(hostMonitoring.selectFromType(FAN).isEmpty());

		}

		{
			final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance()
					.createHostMonitoring(UUID.randomUUID().toString());
			final Monitor device = Monitor.builder().deviceId(DEVICE_ID).targetId(DEVICE_ID).name(DEVICE_NAME).monitorType(DEVICE).build();
			final Monitor enclosure = Monitor.builder().deviceId(ENCLOSURE_ID).name(ENCLOSURE_NAME).targetId(DEVICE_ID)
					.parentId(DEVICE_ID).monitorType(ENCLOSURE).build();
			final Monitor fan = Monitor.builder().deviceId(FAN_ID).name(FAN_NAME).targetId(DEVICE_ID)
					.parentId(ENCLOSURE_ID).monitorType(FAN).build();

			hostMonitoring.getMonitors().put(MonitorType.DEVICE, new HashMap<String, Monitor>());
			hostMonitoring.addMonitor(enclosure);
			hostMonitoring.addMonitor(fan);

			hostMonitoring.removeMonitor(device);

			assertTrue(hostMonitoring.selectFromType(DEVICE).isEmpty());
			assertTrue(hostMonitoring.selectFromType(ENCLOSURE).isEmpty());
			assertTrue(hostMonitoring.selectFromType(FAN).isEmpty());
			
			hostMonitoring.getMonitors().put(MonitorType.DEVICE, null);
			hostMonitoring.addMonitor(enclosure);
			hostMonitoring.addMonitor(fan);

			hostMonitoring.removeMonitor(device);

			assertNull(hostMonitoring.selectFromType(DEVICE));
			assertTrue(hostMonitoring.selectFromType(ENCLOSURE).isEmpty());
			assertTrue(hostMonitoring.selectFromType(FAN).isEmpty());

		}
	}

	@Test
	void testAddMonitorException() {
		final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance()
				.createHostMonitoring(UUID.randomUUID().toString());

		assertThrows(IllegalArgumentException.class, () -> hostMonitoring.addMonitor(null));

		final Monitor noDeviceId = Monitor.builder().targetId(DEVICE_ID).name(DEVICE_NAME).monitorType(DEVICE).build();
		assertThrows(IllegalArgumentException.class, () -> hostMonitoring.addMonitor(noDeviceId));

		final Monitor noMonitorType = Monitor.builder().deviceId(DEVICE_ID).targetId(DEVICE_ID).name(DEVICE_NAME).build();
		assertThrows(IllegalArgumentException.class, () -> hostMonitoring.addMonitor(noMonitorType));

		final Monitor noTargetId = Monitor.builder().deviceId(DEVICE_ID).monitorType(DEVICE).name(DEVICE_NAME).build();
		assertThrows(IllegalArgumentException.class, () -> hostMonitoring.addMonitor(noTargetId));

		final Monitor noParentId = Monitor.builder().deviceId(ENCLOSURE_ID).name(ENCLOSURE_NAME).targetId(DEVICE_ID).monitorType(ENCLOSURE).build();
		assertThrows(IllegalArgumentException.class, () -> hostMonitoring.addMonitor(noParentId));
	}

	@Test
	void testAddMonitor() {
		final IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance()
				.createHostMonitoring(UUID.randomUUID().toString());

		final Monitor enclosure = Monitor.builder().deviceId(ENCLOSURE_ID).name(ENCLOSURE_NAME).targetId(DEVICE_ID)
				.parentId(DEVICE_ID).monitorType(ENCLOSURE).build();

		hostMonitoring.addMonitor(enclosure);

		assertEquals(enclosure, hostMonitoring.selectFromType(ENCLOSURE).get(ENCLOSURE_ID));

		final String enclosureBisId = ENCLOSURE_ID + "bis";
		final Monitor enclosureBis = Monitor.builder().deviceId(enclosureBisId).name(ENCLOSURE_NAME + "bis")
				.targetId(DEVICE_ID).parentId(DEVICE_ID).monitorType(ENCLOSURE).build();

		hostMonitoring.addMonitor(enclosureBis);
		assertEquals(enclosure, hostMonitoring.selectFromType(ENCLOSURE).get(ENCLOSURE_ID));
		assertEquals(enclosureBis, hostMonitoring.selectFromType(ENCLOSURE).get(enclosureBisId));
	}
}
