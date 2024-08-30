package org.sentrysoftware.metricshub.engine.connector.deserializer.source;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.sentrysoftware.metricshub.engine.connector.deserializer.DeserializerTest;
import org.sentrysoftware.metricshub.engine.connector.model.Connector;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.SnmpTableSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.Source;

class SnmpTableSourceDeserializerTest extends DeserializerTest {

	@Override
	public String getResourcePath() {
		return "src/test/resources/test-files/source/snmpTable/";
	}

	@Test
	void testDeserializeSnmpTable() throws IOException {
		final Connector connector = getConnector("snmpTable");

		Map<String, Source> expected = new LinkedHashMap<>();
		expected.put(
			"snmpTable1",
			SnmpTableSource
				.builder()
				.key("${source::pre.snmpTable1}")
				.type("snmpTable")
				.oid("1.3.6.1.4.1")
				.selectColumns("ID,1,2,3,4")
				.forceSerialization(true)
				.computes(Collections.emptyList())
				.build()
		);

		assertEquals(expected, connector.getBeforeAll());
	}
}
