package com.sentrysoftware.matrix.connector.deserializer.source.ucs;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.deserializer.DeserializerTest;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.Source;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.UcsSource;

public class UcsDeserializerTest extends DeserializerTest{
	@Override
	public String getResourcePath() {
		return "src/test/resources/test-files/source/ucs/";
	}

	@Test
	void testDeserializeUcs() throws IOException {
		final String testResource = "ucs";

		List<String> queries = new ArrayList<>();
		queries.add("testQuery1");
		queries.add("testQuery2");
		queries.add("testQuery3");

		List<String> columns = new ArrayList<>();
		columns.add("1");
		columns.add("2");
		columns.add("3");
		

		final Connector connector = getConnector(testResource);
		Map<String, Source> expected = new LinkedHashMap<>();
		expected.put("testUcsSource",
				UcsSource.builder()
						.key("$pre.testUcsSource")
						.type("ucs")
						.queries(queries)
						.exclude("testExclude")
						.keep("testKeep")
						.selectColumns(columns)
						.build());

		assertEquals(expected, connector.getPre());
	}
}
