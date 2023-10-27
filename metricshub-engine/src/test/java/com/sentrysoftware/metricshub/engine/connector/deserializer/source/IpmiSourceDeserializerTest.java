package com.sentrysoftware.metricshub.engine.connector.deserializer.source;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.sentrysoftware.metricshub.engine.connector.deserializer.DeserializerTest;
import com.sentrysoftware.metricshub.engine.connector.model.Connector;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.IpmiSource;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.Source;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class IpmiSourceDeserializerTest extends DeserializerTest {

	@Override
	public String getResourcePath() {
		return "src/test/resources/test-files/source/ipmi/";
	}

	@Test
	void testDeserializeStatic() throws IOException {
		final String testResource = "ipmi";
		final Connector connector = getConnector(testResource);

		final Map<String, Source> expected = new LinkedHashMap<String, Source>(
			Map.of("testIpmiSource", IpmiSource.builder().key("${source::pre.testIpmiSource}").type("ipmi").build())
		);

		assertEquals(expected, connector.getPre());
	}
}