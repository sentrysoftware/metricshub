package com.sentrysoftware.matrix.connector.deserializer.source.copy;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.deserializer.DeserializerTest;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.CopySource;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.Source;

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
				Map.of("testCopySource",
					CopySource.builder()
						.key("$pre.testCopySource")
						.type("copy")
						.from("$pre.anotherSource")
						.computes(Collections.emptyList())
						.build()
					)
				);

		assertEquals(expected, connector.getPre());
	}
}