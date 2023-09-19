package com.sentrysoftware.matrix.agent.context;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.sentrysoftware.matrix.agent.config.AgentConfig;
import com.sentrysoftware.matrix.agent.config.ResourceConfig;
import com.sentrysoftware.matrix.agent.config.ResourceGroupConfig;
import com.sentrysoftware.matrix.connector.model.common.HttpMethod;
import com.sentrysoftware.matrix.connector.model.common.ResultContent;
import com.sentrysoftware.matrix.connector.model.monitor.SimpleMonitorJob;
import com.sentrysoftware.matrix.connector.model.monitor.task.Mapping;
import com.sentrysoftware.matrix.connector.model.monitor.task.Simple;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.HttpSource;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class AgentContextTest {

	private static final String HOST_NAME = "host.name";
	public static final String SOURCE_REF = "${source::monitors.grafana.simple.sources.grafanaHealth}";

	@Test
	void testInitialize() throws IOException {
		AgentContext.initialize("src/test/resources/config/metricshub.yaml");

		final AgentContext agentContextSingleton = AgentContext.getInstance();
		assertNotNull(agentContextSingleton.getAgentInfo());
		assertNotNull(agentContextSingleton.getConfigFile());
		assertNotNull(agentContextSingleton.getPid());

		final AgentConfig agentConfig = agentContextSingleton.getAgentConfig();
		assertNotNull(agentConfig);

		final Map<String, ResourceGroupConfig> resourceGroupsConfig = agentConfig.getResourceGroups();
		assertNotNull(resourceGroupsConfig);
		final ResourceGroupConfig resourceGroupConfig = resourceGroupsConfig.get("sentry-paris");
		assertNotNull(resourceGroupConfig);
		final Map<String, ResourceConfig> resourcesConfigInTheGroup = resourceGroupConfig.getResources();
		assertNotNull(resourcesConfigInTheGroup);
		assertNotNull(resourcesConfigInTheGroup.get("server-1"));
		final ResourceConfig grafanaServiceResourceConfig = resourcesConfigInTheGroup.get("grafana-service");
		assertNotNull(grafanaServiceResourceConfig);
		final Map<String, String> attributesConfig = grafanaServiceResourceConfig.getAttributes();
		assertNotNull(attributesConfig);
		assertEquals("Sentry-Paris", attributesConfig.get("site"));

		final Map<String, String> attributes = new LinkedHashMap<>();
		attributes.put("id", "$1");
		attributes.put("service.version", "$3");

		final Simple simple = Simple
			.builder()
			.sources(
				Map.of(
					"grafanaHealth",
					HttpSource
						.builder()
						.header("Accept: application/json")
						.method(HttpMethod.GET)
						.resultContent(ResultContent.BODY)
						.url("https://hws-demo.sentrysoftware.com/api/health")
						.key(SOURCE_REF)
						.type("http")
						.build()
				)
			)
			.mapping(
				Mapping.builder().source(SOURCE_REF).attributes(attributes).metrics(Map.of("grafana.db.state", "$2")).build()
			)
			.build();

		simple.setSourceDep(List.of(Set.of("grafanaHealth")));

		final SimpleMonitorJob simpleMonitorJobExpected = SimpleMonitorJob.builder().simple(simple).build();
		final Map<String, SimpleMonitorJob> expectedMonitors = Map.of("grafana", simpleMonitorJobExpected);
		assertEquals(expectedMonitors, grafanaServiceResourceConfig.getMonitors());

		// Multi-hosts checks
		final ResourceConfig server2ResourceConfig = resourcesConfigInTheGroup.get("snmp-resources-server-2");
		assertNotNull(server2ResourceConfig);
		assertEquals("server-2", server2ResourceConfig.getAttributes().get(HOST_NAME));
		final ResourceConfig server3ResourceConfig = resourcesConfigInTheGroup.get("snmp-resources-server-3");
		assertEquals("server-3", server3ResourceConfig.getAttributes().get(HOST_NAME));
		assertNotNull(server3ResourceConfig);
	}
}
