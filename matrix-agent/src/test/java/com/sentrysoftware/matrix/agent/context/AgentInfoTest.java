package com.sentrysoftware.matrix.agent.context;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Map;
import org.junit.jupiter.api.Test;

class AgentInfoTest {

	@Test
	void testConstructor() {
		final AgentInfo agent = new AgentInfo();
		final Map<String, String> metricAttributes = agent.getMetricAttributes();
		assertNotNull(metricAttributes);
		assertNotNull(metricAttributes.get("name"));
		assertNotNull(metricAttributes.get("version"));
		assertNotNull(metricAttributes.get("build_number"));
		assertNotNull(metricAttributes.get("build_date"));
		assertNotNull(metricAttributes.get("hc_version"));
		assertNotNull(metricAttributes.get("otel_version"));

		final Map<String, String> resourceAttributes = agent.getResourceAttributes();
		assertNotNull(resourceAttributes);
		assertNotNull(resourceAttributes.get("service.name"));
		assertNotNull(resourceAttributes.get("host.id"));
		assertNotNull(resourceAttributes.get("host.name"));
		assertNotNull(resourceAttributes.get("agent.host.name"));
		assertNotNull(resourceAttributes.get("host.type"));
		assertNotNull(resourceAttributes.get("os.type"));
	}
}
