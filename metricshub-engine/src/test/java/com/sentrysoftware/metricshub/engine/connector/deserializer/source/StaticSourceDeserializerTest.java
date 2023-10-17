package com.sentrysoftware.metricshub.engine.connector.deserializer.source;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sentrysoftware.metricshub.engine.connector.deserializer.DeserializerTest;
import com.sentrysoftware.metricshub.engine.connector.model.Connector;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.Source;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.StaticSource;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class StaticSourceDeserializerTest extends DeserializerTest {

	@Override
	public String getResourcePath() {
		return "src/test/resources/test-files/source/static/";
	}

	@Test
	void testDeserializeStatic() throws IOException {
		final String testResource = "static";
		final Connector connector = getConnector(testResource);

		final Map<String, Source> expected = new LinkedHashMap<>(
			Map.of(
				"testStaticSource",
				StaticSource.builder().key("${source::pre.testStaticSource}").type("static").value("testValue").build()
			)
		);

		assertEquals(expected, connector.getPre());
	}
}
