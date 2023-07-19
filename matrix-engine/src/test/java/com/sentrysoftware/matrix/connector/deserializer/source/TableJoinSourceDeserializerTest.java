package com.sentrysoftware.matrix.connector.deserializer.source;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.deserializer.DeserializerTest;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.Source;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.TableJoinSource;

class TableJoinSourceDeserializerTest extends DeserializerTest {

	@Override
	public String getResourcePath() {
		return "src/test/resources/test-files/source/tableJoin/";
	}

	@Test
	void testDeserializeTableJoin() throws IOException {
		final String testResource = "tableJoin";

		try {
			final Connector connector = getConnector(testResource);
			Map<String, Source> expected = new LinkedHashMap<>();
			expected.put("testTableJoinSource",
				TableJoinSource.builder()
					.type("tableJoin")
					.forceSerialization(false)
					.leftTable("testLeft")
					.rightTable("testRight")
					.leftKeyColumn(2)
					.rightKeyColumn(3)
					.defaultRightLine("testdefault;;;")
					.key("${source::pre.testTableJoinSource}")
					.computes(Collections.emptyList())
					.build()
			);

			comparePreSource(connector, expected);
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
	}
}
