package com.sentrysoftware.matrix.connector.deserializer.source;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.deserializer.DeserializerTest;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.Source;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.UcsSource;

class UcsSourceDeserializerTest extends DeserializerTest{
	@Override
	public String getResourcePath() {
		return "src/test/resources/test-files/source/ucs/";
	}

	@Test
	void testDeserializeUcs() throws IOException {
		final String testResource = "ucs";

		final Set<String> queries = new LinkedHashSet<>(
			Set.of("testQuery1", "testQuery2", "testQuery3")
		);

		final Connector connector = getConnector(testResource);
		Map<String, Source> expected = new LinkedHashMap<>();
		expected.put("testUcsSource",
			UcsSource.builder()
				.key("$pre.testUcsSource")
				.type("ucs")
				.queries(queries)
				.exclude("testExclude")
				.keep("testKeep")
				.selectColumns("1,2,3")
				.build()
		);

		assertEquals(expected, connector.getPre());
	}
}
