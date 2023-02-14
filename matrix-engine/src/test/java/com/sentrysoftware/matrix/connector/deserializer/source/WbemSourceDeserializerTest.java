package com.sentrysoftware.matrix.connector.deserializer.source;

import static org.junit.jupiter.api.Assertions.assertEquals;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.deserializer.DeserializerTest;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.Source;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.WbemSource;

class WbemSourceDeserializerTest extends DeserializerTest {

	@Override
	public String getResourcePath() {
		return "src/test/resources/test-files/source/wbem/";
	}

	@Test
	void testDeserializeWbemSource() throws IOException {
		final String testResource = "wbem";
		final Connector connector = getConnector(testResource);

		final Map<String, Source> expected = new LinkedHashMap<String, Source>(
				Map.of("testWbemSource",
						WbemSource.builder()
								.key("$pre.testWbemSource")
								.type("wbem")
								.query("testQuery")
								.namespace("testNamespace")
								.build()));

		assertEquals(expected, connector.getPre());
	}
}
