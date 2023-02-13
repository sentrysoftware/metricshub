package com.sentrysoftware.matrix.connector.deserializer.source;

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
import com.sentrysoftware.matrix.connector.model.monitor.task.source.OsCommandSource;

public class OsCommandSourceDeserializerTest extends DeserializerTest{
	
	@Override
	public String getResourcePath() {
		return "src/test/resources/test-files/source/osCommand/";
	}

	@Test
	void testDeserializeOsCommand() throws IOException {

		List<String> columns = new ArrayList<>();
		columns.add("1");
		columns.add("2");
		columns.add("3");
		columns.add("4");

		final Connector connector = getConnector("osCommand");
		
		Map<String, Source> expected = new LinkedHashMap<>();
		expected.put("oscommand1",
				OsCommandSource.builder()
						.key("$pre.oscommand1")
						.type("oscommand")
						.timeout((long) 30)
						.exclude("excludeRegExp")
						.keep("keepRegExp")
						.removeHeader(true)
						.removeFooter(true)
						.separators(",;")
						.selectColumns(columns)
						.forceSerialization(true)
						.build());

		assertEquals(expected, connector.getPre());
	}

}