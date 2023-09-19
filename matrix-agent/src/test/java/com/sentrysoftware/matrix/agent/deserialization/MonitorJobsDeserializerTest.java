package com.sentrysoftware.matrix.agent.deserialization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.doReturn;

import com.fasterxml.jackson.core.json.JsonReadContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import com.sentrysoftware.matrix.agent.config.ResourceConfig;
import com.sentrysoftware.matrix.agent.helper.ConfigHelper;
import com.sentrysoftware.matrix.common.helpers.ResourceHelper;
import com.sentrysoftware.matrix.connector.model.common.HttpMethod;
import com.sentrysoftware.matrix.connector.model.common.ResultContent;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorJob;
import com.sentrysoftware.matrix.connector.model.monitor.SimpleMonitorJob;
import com.sentrysoftware.matrix.connector.model.monitor.task.Mapping;
import com.sentrysoftware.matrix.connector.model.monitor.task.Simple;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.HttpSource;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MonitorJobsDeserializerTest {

	public static final String SOURCE_REF = "${source::monitors.grafana.simple.sources.grafanaHealth}";

	@Mock
	private YAMLParser yamlParserMock;

	@Mock
	private JsonReadContext jsonReadContext;

	@Test
	void testDeserializeNullParser() throws IOException {
		assertEquals(Collections.emptyMap(), new MonitorJobsDeserializer().deserialize(null, null));
	}

	@Test
	void testDeserialize() throws IOException {
		doReturn(
			ConfigHelper
				.newObjectMapper()
				.readTree(ResourceHelper.getResourceAsString("/data/monitors.yaml", MonitorJobsDeserializerTest.class))
		)
			.when(yamlParserMock)
			.readValueAs(JsonNode.class);

		doReturn(jsonReadContext).when(yamlParserMock).getParsingContext();

		final ResourceConfig resourceConfig = ResourceConfig.builder().build();

		doReturn(resourceConfig).when(jsonReadContext).getCurrentValue();

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
		final Map<String, SimpleMonitorJob> expected = Map.of("grafana", simpleMonitorJobExpected);

		final Map<String, MonitorJob> result = new MonitorJobsDeserializer().deserialize(yamlParserMock, null);

		assertEquals(expected, result);
		assertNotNull(resourceConfig.getConnector());
	}
}
