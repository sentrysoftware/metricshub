package com.sentrysoftware.matrix.connector.deserializer.source;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.deserializer.DeserializerTest;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.Source;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.WmiSource;

class WmiSourceDeserializerTest extends DeserializerTest {

	@Override
	public String getResourcePath() {
		return "src/test/resources/test-files/source/wmi/";
	}

	@Test
	void testDeserializeWmiSource() throws IOException {
		final String testResource = "wmiSource";
		final Connector connector = getConnector(testResource);

		final Map<String, Source> expected = new LinkedHashMap<String, Source>(
			Map.of("testWmiSource",
				WmiSource
					.builder()
					.key("$pre.testWmiSource")
					.type("wmi")
					.query("testQuery")
					.namespace("testNamespace")
					.computes(Collections.emptyList())
					.build()
			)
		);

		assertEquals(expected, connector.getPre());
	}
}
