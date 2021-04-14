package com.sentrysoftware.hardware.cli.service;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoringFactory;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;
import com.sentrysoftware.matrix.model.parameter.IParameterValue;
import com.sentrysoftware.matrix.model.parameter.ParameterState;
import com.sentrysoftware.matrix.model.parameter.StatusParam;

public class JobResultFormatterServiceTest {

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
		connector1.setName("MS_HW_DellOpenManage");
		connector1.setParentId("parentId");

		Map<String, IParameterValue> parameters = new HashMap<>();
		parameters.put("status", StatusParam.builder().collectTime(1618319092125L).name("status")
				.status(ParameterState.OK).statusInformation("OK").build());
		connector1.setParameters(parameters);
		map.put("id1", connector1);

		monitoring.put(MonitorType.CONNECTOR, map);

		Map<String, Monitor> map2 = new HashMap<>();

		Monitor device1 = new Monitor();
		device1.setName("MS_HW_DellOpenManage_Device");
		device1.setParentId("id1");

		device1.setParameters(parameters);

		map2.put("id2", device1);

		monitoring.put(MonitorType.DEVICE, map2);

		hostMonitoring.setMonitors(monitoring);

		String formattedMonitoring = jobFormatter.format(hostMonitoring);

		String expected = "{\r\n" + 
				"  \"connector\" : [ {\r\n" + 
				"    \"deviceId\" : \"id1\",\r\n" + 
				"    \"name\" : \"MS_HW_DellOpenManage\",\r\n" + 
				"    \"monitorType\" : \"CONNECTOR\",\r\n" + 
				"    \"parentId\" : \"parentId\",\r\n" + 
				"    \"targetId\" : null,\r\n" + 
				"    \"parameters\" : {\r\n" + 
				"      \"status\" : {\r\n" + 
				"        \"name\" : \"status\",\r\n" + 
				"        \"collectTime\" : 1618319092125,\r\n" + 
				"        \"threshold\" : null,\r\n" + 
				"        \"state\" : \"OK\",\r\n" + 
				"        \"status\" : \"OK\",\r\n" + 
				"        \"statusInformation\" : \"OK\"\r\n" + 
				"      }\r\n" + 
				"    }\r\n" + 
				"  } ],\r\n" + 
				"  \"device\" : [ {\r\n" + 
				"    \"deviceId\" : \"id2\",\r\n" + 
				"    \"name\" : \"MS_HW_DellOpenManage_Device\",\r\n" + 
				"    \"monitorType\" : \"DEVICE\",\r\n" + 
				"    \"parentId\" : \"id1\",\r\n" + 
				"    \"targetId\" : null,\r\n" + 
				"    \"parameters\" : {\r\n" + 
				"      \"status\" : {\r\n" + 
				"        \"name\" : \"status\",\r\n" + 
				"        \"collectTime\" : 1618319092125,\r\n" + 
				"        \"threshold\" : null,\r\n" + 
				"        \"state\" : \"OK\",\r\n" + 
				"        \"status\" : \"OK\",\r\n" + 
				"        \"statusInformation\" : \"OK\"\r\n" + 
				"      }\r\n" + 
				"    }\r\n" + 
				"  } ]\r\n" + 
				"}";

		Assert.assertEquals(expected, formattedMonitoring);
	}

}
