package com.sentrysoftware.hardware.cli.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.common.helpers.JsonHelper;
import com.sentrysoftware.matrix.common.meta.parameter.state.Status;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoringFactory;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoringVO;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;
import com.sentrysoftware.matrix.model.parameter.DiscreteParam;
import com.sentrysoftware.matrix.model.parameter.IParameter;
class JobResultFormatterServiceTest {

	@Test
	void formatTestNullMonitoring() {
		JobResultFormatterService jobFormatter = new JobResultFormatterService();

		IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance().createHostMonitoring("hostMonitoringId", null);
		Map<MonitorType, Map<String, Monitor>> monitoring = null;
		hostMonitoring.setMonitors(monitoring);

		String formattedMonitoring = jobFormatter.format(hostMonitoring);

		Assert.assertEquals("{}", formattedMonitoring);
	}

	@Test
	void formatTestEmptyMonitoring() {
		JobResultFormatterService jobFormatter = new JobResultFormatterService();

		IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance().createHostMonitoring("hostMonitoringId", null);
		Map<MonitorType, Map<String, Monitor>> monitoring = new HashMap<>();
		hostMonitoring.setMonitors(monitoring);

		String formattedMonitoring = jobFormatter.format(hostMonitoring);

		Assert.assertEquals("{}", formattedMonitoring);
	}

	@Test
	void formatTestOK() throws Exception {
		JobResultFormatterService jobFormatter = new JobResultFormatterService();

		IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance().createHostMonitoring("hostMonitoringId", null);
		Map<MonitorType, Map<String, Monitor>> monitoring = new HashMap<>();

		Map<String, Monitor> map = new HashMap<>();

		Monitor connector1 = new Monitor();
		connector1.setId("id1");
		connector1.setName("MS_HW_DellOpenManage");
		connector1.setParentId("parentId");
		connector1.setMonitorType(MonitorType.CONNECTOR);

		Map<String, IParameter> parameters = new HashMap<>();
		parameters.put("status", DiscreteParam.builder().collectTime(1618319092125L).name("status")
				.state(Status.OK).build());
		connector1.setParameters(parameters);
		map.put("id1", connector1);

		Monitor connector2 = new Monitor();
		connector2.setId("id2");
		connector2.setName("MS_HW_DellCMC");
		connector2.setParentId("parentId");
		connector2.setMonitorType(MonitorType.CONNECTOR);

		Map<String, IParameter> parameters2 = new HashMap<>();
		parameters2.put("status", DiscreteParam.builder().collectTime(1618319092125L).name("status")
				.state(Status.DEGRADED).build());
		connector2.setParameters(parameters2);
		map.put("id2", connector2);

		monitoring.put(MonitorType.CONNECTOR, map);

		Map<String, Monitor> map2 = new HashMap<>();

		Monitor device1 = new Monitor();
		device1.setId("id3");
		device1.setName("MS_HW_DellOpenManage_Device");
		device1.setParentId("id1");
		device1.setMonitorType(MonitorType.TARGET);

		device1.setParameters(parameters);

		map2.put("id3", device1);

		monitoring.put(MonitorType.TARGET, map2);

		hostMonitoring.setMonitors(monitoring);

		String formattedHostMonitoring = jobFormatter.format(hostMonitoring);

		final HostMonitoringVO expected = JsonHelper.deserialize(
				new FileInputStream(new File("src/test/resources/json/formatTestResource.json")),
				HostMonitoringVO.class
		);

		final HostMonitoringVO actual = JsonHelper.deserialize(formattedHostMonitoring, HostMonitoringVO.class);

		assertEquals(expected, actual);

	}

}
