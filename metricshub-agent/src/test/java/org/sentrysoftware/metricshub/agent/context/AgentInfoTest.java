package org.sentrysoftware.metricshub.agent.context;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Map;
import org.junit.jupiter.api.Test;

class AgentInfoTest {

	@Test
	void testConstructor() {
		final AgentInfo agentInfo = new AgentInfo();
		final Map<String, String> metricAttributes = agentInfo.getMetricAttributes();
		assertNotNull(metricAttributes);
		assertNotNull(metricAttributes.get("name"));
		assertNotNull(metricAttributes.get("version"));
		assertNotNull(metricAttributes.get("build_number"));
		assertNotNull(metricAttributes.get("build_date"));
		assertNotNull(metricAttributes.get("cc_version"));

		final Map<String, String> resourceAttributes = agentInfo.getResourceAttributes();
		assertNotNull(resourceAttributes);
		assertNotNull(resourceAttributes.get("service.name"));
		assertNotNull(resourceAttributes.get("host.id"));
		assertNotNull(resourceAttributes.get("host.name"));
		assertNotNull(resourceAttributes.get("agent.host.name"));
		assertNotNull(resourceAttributes.get("host.type"));
		assertNotNull(resourceAttributes.get("os.type"));
	}
}
