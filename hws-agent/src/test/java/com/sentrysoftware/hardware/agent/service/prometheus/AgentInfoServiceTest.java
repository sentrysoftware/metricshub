package com.sentrysoftware.hardware.agent.service.prometheus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.HashSet;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import io.prometheus.client.Collector;
import io.prometheus.client.Collector.MetricFamilySamples;

@SpringBootTest
@Deprecated(since = "1.1")
class AgentInfoServiceTest {

	@Autowired
	private AgentInfoService agentInfoService;

	@Autowired
	private Map<String, String> agentInfo;

	@Test
	void testBuildAgentInfoMetric() {
		final MetricFamilySamples mfs = agentInfoService.buildAgentInfoMetric();
		assertNotNull(mfs);
		assertNotNull(mfs.help);
		assertNotNull(mfs.name);
		assertEquals(Collector.Type.GAUGE, mfs.type);
		assertEquals(agentInfo.keySet(), new HashSet<>(mfs.samples.get(0).labelNames));
		mfs.samples.get(0).labelValues.forEach(Assertions::assertNotNull);
		assertEquals(1.0, mfs.samples.get(0).value);
	}

}
