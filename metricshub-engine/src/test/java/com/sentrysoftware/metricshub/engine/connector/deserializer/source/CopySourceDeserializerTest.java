package com.sentrysoftware.metricshub.engine.connector.deserializer.source;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sentrysoftware.metricshub.engine.connector.deserializer.DeserializerTest;
import com.sentrysoftware.metricshub.engine.connector.model.Connector;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.CopySource;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.Source;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class CopySourceDeserializerTest extends DeserializerTest {

	@Override
	public String getResourcePath() {
		return "src/test/resources/test-files/source/copy/";
	}

	@Test
	void testDeserializeCopySource() throws IOException {
		final String testResource = "copySource";
		final Connector connector = getConnector(testResource);

		final Map<String, Source> expected = new LinkedHashMap<String, Source>(
			Map.of(
				"testCopySource",
				CopySource
					.builder()
					.key("${source::pre.testCopySource}")
					.type("copy")
					.from("${source::pre.anotherSource}")
					.computes(Collections.emptyList())
					.build()
			)
		);

		assertEquals(expected, connector.getPre());
	}
}