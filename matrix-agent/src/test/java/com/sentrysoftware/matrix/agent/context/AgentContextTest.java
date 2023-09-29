package com.sentrysoftware.matrix.agent.context;

import static com.sentrysoftware.matrix.agent.helper.TestConstants.GRAFANA_DB_STATE_METRIC;
import static com.sentrysoftware.matrix.agent.helper.TestConstants.GRAFANA_HEALTH_SOURCE_KEY;
import static com.sentrysoftware.matrix.agent.helper.TestConstants.GRAFANA_HEALTH_SOURCE_REF;
import static com.sentrysoftware.matrix.agent.helper.TestConstants.GRAFANA_MONITOR_JOB_KEY;
import static com.sentrysoftware.matrix.agent.helper.TestConstants.GRAFANA_SERVICE_RESOURCE_CONFIG_KEY;
import static com.sentrysoftware.matrix.agent.helper.TestConstants.HTTP_ACCEPT_HEADER;
import static com.sentrysoftware.matrix.agent.helper.TestConstants.HTTP_KEY_TYPE;
import static com.sentrysoftware.matrix.agent.helper.TestConstants.HTTP_SERVICE_URL;
import static com.sentrysoftware.matrix.agent.helper.TestConstants.ID_ATTRIBUTE_KEY;
import static com.sentrysoftware.matrix.agent.helper.TestConstants.SENTRY_PARIS_RESOURCE_GROUP_KEY;
import static com.sentrysoftware.matrix.agent.helper.TestConstants.SENTRY_PARIS_SITE_VALUE;
import static com.sentrysoftware.matrix.agent.helper.TestConstants.SERVER_1_RESOURCE_GROUP_KEY;
import static com.sentrysoftware.matrix.agent.helper.TestConstants.SERVICE_VERSION_ATTRIBUTE_KEY;
import static com.sentrysoftware.matrix.agent.helper.TestConstants.SITE_ATTRIBUTE_KEY;
import static com.sentrysoftware.matrix.agent.helper.TestConstants.TEST_CONFIG_FILE_PATH;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.HOST_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sentrysoftware.matrix.agent.config.AgentConfig;
import com.sentrysoftware.matrix.agent.config.ResourceConfig;
import com.sentrysoftware.matrix.agent.config.ResourceGroupConfig;
import com.sentrysoftware.matrix.common.helpers.MapHelper;
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

	@Test
	void testInitialize() throws IOException {
		final AgentContext agentContext = new AgentContext(TEST_CONFIG_FILE_PATH);

		assertNotNull(agentContext.getAgentInfo());
		assertNotNull(agentContext.getConfigFile());
		assertNotNull(agentContext.getPid());

		final AgentConfig agentConfig = agentContext.getAgentConfig();
		assertNotNull(agentConfig);

		final Map<String, ResourceGroupConfig> resourceGroupsConfig = agentConfig.getResourceGroups();
		assertNotNull(resourceGroupsConfig);
		final ResourceGroupConfig resourceGroupConfig = resourceGroupsConfig.get(SENTRY_PARIS_RESOURCE_GROUP_KEY);
		assertNotNull(resourceGroupConfig);
		final Map<String, ResourceConfig> resourcesConfigInTheGroup = resourceGroupConfig.getResources();
		assertNotNull(resourcesConfigInTheGroup);
		assertNotNull(resourcesConfigInTheGroup.get(SERVER_1_RESOURCE_GROUP_KEY));
		final ResourceConfig grafanaServiceResourceConfig = resourcesConfigInTheGroup.get(
			GRAFANA_SERVICE_RESOURCE_CONFIG_KEY
		);
		assertNotNull(grafanaServiceResourceConfig);
		final Map<String, String> attributesConfig = grafanaServiceResourceConfig.getAttributes();
		assertNotNull(attributesConfig);
		assertEquals(SENTRY_PARIS_SITE_VALUE, attributesConfig.get(SITE_ATTRIBUTE_KEY));

		final Map<String, String> attributes = new LinkedHashMap<>();
		attributes.put(ID_ATTRIBUTE_KEY, "$1");
		attributes.put(SERVICE_VERSION_ATTRIBUTE_KEY, "$3");

		final Simple simple = Simple
			.builder()
			.sources(
				Map.of(
					GRAFANA_HEALTH_SOURCE_KEY,
					HttpSource
						.builder()
						.header(HTTP_ACCEPT_HEADER)
						.method(HttpMethod.GET)
						.resultContent(ResultContent.BODY)
						.url(HTTP_SERVICE_URL)
						.key(GRAFANA_HEALTH_SOURCE_REF)
						.type(HTTP_KEY_TYPE)
						.build()
				)
			)
			.mapping(
				Mapping
					.builder()
					.source(GRAFANA_HEALTH_SOURCE_REF)
					.attributes(attributes)
					.metrics(Map.of(GRAFANA_DB_STATE_METRIC, "$2"))
					.build()
			)
			.build();

		simple.setSourceDep(List.of(Set.of(GRAFANA_HEALTH_SOURCE_KEY)));

		final SimpleMonitorJob simpleMonitorJobExpected = SimpleMonitorJob.builder().simple(simple).build();
		final Map<String, SimpleMonitorJob> expectedMonitors = Map.of(GRAFANA_MONITOR_JOB_KEY, simpleMonitorJobExpected);
		assertEquals(expectedMonitors, grafanaServiceResourceConfig.getMonitors());

		// Multi-hosts checks
		final ResourceConfig server2ResourceConfig = resourcesConfigInTheGroup.get("snmp-resources-server-2");
		assertNotNull(server2ResourceConfig);
		assertEquals("server-2", server2ResourceConfig.getAttributes().get(HOST_NAME));
		final ResourceConfig server3ResourceConfig = resourcesConfigInTheGroup.get("snmp-resources-server-3");
		assertEquals("server-3", server3ResourceConfig.getAttributes().get(HOST_NAME));
		assertNotNull(server3ResourceConfig);

		// Check the TelemetryManager map is correctly created
		assertEquals(3, agentContext.getTelemetryManagers().get(SENTRY_PARIS_RESOURCE_GROUP_KEY).size());

		// Check the OpenTelemetry SDK configuration is correctly created
		final Map<String, String> expectedOtelSdkConfiguration = Map.of(
			"otel.logs.exporter",
			"otlp",
			"otel.exporter.otlp.endpoint",
			"https://localhost:4317",
			"otel.exporter.otlp.headers",
			"Authorization=Basic aHdzOlNlbnRyeVNvZnR3YXJlMSE=",
			"otel.metric.export.interval",
			"315360000000",
			"otel.metrics.exporter",
			"otlp"
		);
		final Map<String, String> otelSdkConfiguration = agentContext.getOtelSdkConfiguration();

		assertTrue(
			MapHelper.areEqual(expectedOtelSdkConfiguration, agentContext.getOtelSdkConfiguration()),
			() -> String.format("expected %s but was: %s", expectedOtelSdkConfiguration, otelSdkConfiguration)
		);
	}
}
