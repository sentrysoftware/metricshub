package com.sentrysoftware.matrix.agent.deserialization;

import static com.sentrysoftware.matrix.agent.helper.TestConstants.GRAFANA_DB_STATE_METRIC;
import static com.sentrysoftware.matrix.agent.helper.TestConstants.GRAFANA_HEALTH_SOURCE_KEY;
import static com.sentrysoftware.matrix.agent.helper.TestConstants.GRAFANA_HEALTH_SOURCE_REF;
import static com.sentrysoftware.matrix.agent.helper.TestConstants.GRAFANA_MONITOR_JOB_KEY;
import static com.sentrysoftware.matrix.agent.helper.TestConstants.HTTP_ACCEPT_HEADER;
import static com.sentrysoftware.matrix.agent.helper.TestConstants.HTTP_KEY_TYPE;
import static com.sentrysoftware.matrix.agent.helper.TestConstants.HTTP_SERVICE_URL;
import static com.sentrysoftware.matrix.agent.helper.TestConstants.ID_ATTRIBUTE_KEY;
import static com.sentrysoftware.matrix.agent.helper.TestConstants.SERVICE_VERSION;
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
		attributes.put(ID_ATTRIBUTE_KEY, "$1");
		attributes.put(SERVICE_VERSION, "$3");

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
		final Map<String, SimpleMonitorJob> expected = Map.of(GRAFANA_MONITOR_JOB_KEY, simpleMonitorJobExpected);

		final Map<String, MonitorJob> result = new MonitorJobsDeserializer().deserialize(yamlParserMock, null);

		assertEquals(expected, result);
		assertNotNull(resourceConfig.getConnector());
	}
}
