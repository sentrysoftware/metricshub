package com.sentrysoftware.metricshub.engine.connector.deserializer.source;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sentrysoftware.metricshub.engine.connector.deserializer.DeserializerTest;
import com.sentrysoftware.metricshub.engine.connector.model.Connector;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.OsCommandSource;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.Source;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class OsCommandSourceDeserializerTest extends DeserializerTest {

	@Override
	public String getResourcePath() {
		return "src/test/resources/test-files/source/osCommand/";
	}

	@Test
	void testDeserializeOsCommand() throws IOException {
		final Connector connector = getConnector("osCommand");

		Map<String, Source> expected = new LinkedHashMap<>();
		expected.put(
			"oscommand1",
			OsCommandSource
				.builder()
				.key("${source::pre.oscommand1}")
				.type("osCommand")
				.timeout((long) 30)
				.exclude("excludeRegExp")
				.keep("keepRegExp")
				.beginAtLineNumber(2)
				.endAtLineNumber(10)
				.separators(",;")
				.selectColumns("1-6")
				.forceSerialization(true)
				.executeLocally(true)
				.commandLine("myCommand")
				.computes(Collections.emptyList())
				.build()
		);

		assertEquals(expected, connector.getPre());
	}
}