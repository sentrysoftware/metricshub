package com.sentrysoftware.matrix.connector.deserializer.source;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.deserializer.DeserializerTest;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.Source;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.HttpSource;

class HttpSourceDeserializerTest extends DeserializerTest {

    @Override
	public String getResourcePath() {
		return "src/test/resources/test-files/source/http/";
	}

	@Test
	void testDeserializeHttpSource() throws IOException {
		final String testResource = "http";
		final Connector connector = getConnector(testResource);

        final String body = "test\nbody";

		final Map<String, Source> expected = new LinkedHashMap<String, Source>(
			Map.of("testHttpSource",
				HttpSource
					.builder()
					.key("$pre.testHttpSource")
					.type("http")
                    .url("/testUrl/")
                    .method("POST")
                    .body(body)
					.build()
			)
		);

		assertEquals(expected, connector.getPre());
	}
}
