package com.sentrysoftware.hardware.cli.service;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.common.helpers.ResourceHelper;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoringFactory;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;
import com.sentrysoftware.matrix.model.parameter.IParameterValue;
import com.sentrysoftware.matrix.model.parameter.ParameterState;
import com.sentrysoftware.matrix.model.parameter.StatusParam;

class JobResultFormatterServiceTest {

	@Test
	void formatTestNullMonitoring() {
		JobResultFormatterService jobFormatter = new JobResultFormatterService();

		IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance().createHostMonitoring("hostMonitoringId");
		Map<MonitorType, Map<String, Monitor>> monitoring = null;
		hostMonitoring.setMonitors(monitoring);

		String formattedMonitoring = jobFormatter.format(hostMonitoring);

		Assert.assertNull(formattedMonitoring);
	}

	@Test
	void formatTestEmptyMonitoring() {
		JobResultFormatterService jobFormatter = new JobResultFormatterService();

		IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance().createHostMonitoring("hostMonitoringId");
		Map<MonitorType, Map<String, Monitor>> monitoring = new HashMap<>();
		hostMonitoring.setMonitors(monitoring);

		String formattedMonitoring = jobFormatter.format(hostMonitoring);

		Assert.assertNull(formattedMonitoring);
	}

	@Test
	void formatTestOK() {
		JobResultFormatterService jobFormatter = new JobResultFormatterService();

		IHostMonitoring hostMonitoring = HostMonitoringFactory.getInstance().createHostMonitoring("hostMonitoringId");
		Map<MonitorType, Map<String, Monitor>> monitoring = new HashMap<>();

		Map<String, Monitor> map = new HashMap<>();

		Monitor connector1 = new Monitor();
		connector1.setId("id1");
		connector1.setName("MS_HW_DellOpenManage");
		connector1.setParentId("parentId");
		connector1.setMonitorType(MonitorType.CONNECTOR);

		Map<String, IParameterValue> parameters = new HashMap<>();
		parameters.put("status", StatusParam.builder().collectTime(1618319092125L).name("status")
				.state(ParameterState.OK).statusInformation("OK").build());
		connector1.setParameters(parameters);
		map.put("id1", connector1);

		Monitor connector2 = new Monitor();
		connector2.setId("id2");
		connector2.setName("MS_HW_DellCMC");
		connector2.setParentId("parentId");
		connector2.setMonitorType(MonitorType.CONNECTOR);

		Map<String, IParameterValue> parameters2 = new HashMap<>();
		parameters2.put("status", StatusParam.builder().collectTime(1618319092125L).name("status")
				.state(ParameterState.WARN).statusInformation("WARNING").build());
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

		String formattedMonitoring = jobFormatter.format(hostMonitoring);

		String expected = ResourceHelper.getResourceAsString("/json/formatTestResource.json", this.getClass());

		Assert.assertEquals(expected.replace("\r", ""), formattedMonitoring.replace("\r", ""));
	}

}
