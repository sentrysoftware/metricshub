package com.sentrysoftware.metricshub.engine.connector.deserializer.source;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sentrysoftware.metricshub.engine.connector.deserializer.DeserializerTest;
import com.sentrysoftware.metricshub.engine.connector.model.Connector;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.Source;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.WbemSource;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class WbemSourceDeserializerTest extends DeserializerTest {

	@Override
	public String getResourcePath() {
		return "src/test/resources/test-files/source/wbem/";
	}

	@Test
	void testDeserializeWbemSource() throws IOException {
		final String testResource = "wbem";
		final Connector connector = getConnector(testResource);

		final Map<String, Source> expected = new LinkedHashMap<>(
			Map.of(
				"testWbemSource",
				WbemSource
					.builder()
					.key("${source::pre.testWbemSource}")
					.type("wbem")
					.query("testQuery")
					.namespace("testNamespace")
					.build()
			)
		);

		assertEquals(expected, connector.getPre());
	}
}
