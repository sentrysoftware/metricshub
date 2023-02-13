package com.sentrysoftware.matrix.connector.deserializer.source;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.deserializer.DeserializerTest;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.Source;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.SnmpTableSource;

class snmpTableSourceDeserializerTest extends DeserializerTest{
	
	@Override
	public String getResourcePath() {
		return "src/test/resources/test-files/source/snmpTable/";
	}

	@Test
	void testDeserializeSnmpTable() throws IOException {

		List<String> columns = new ArrayList<>();
		columns.add("1");
		columns.add("2");
		columns.add("3");
		columns.add("4");

		final Connector connector = getConnector("snmpTable");
		
		Map<String, Source> expected = new LinkedHashMap<>();
		expected.put("snmpTable1",
				SnmpTableSource.builder()
						.key("$pre.snmpTable1")
						.type("snmpTable")
						.oid("1.3.6.1.4.1")
						.selectColumns(columns)
						.forceSerialization(true)
						.computes(Collections.emptyList())
						.build());

		assertEquals(expected, connector.getPre());
	}

}