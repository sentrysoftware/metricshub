package com.sentrysoftware.matrix.connector.deserializer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.IpmiSource;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.Source;

class PreDeserializerTest extends DeserializerTest {

	@Override
	public String getResourcePath() {
		return "src/test/resources/test-files/connector/pre/";
	}

	@Test
	void testDeserializePre() throws IOException {
		final String testResource = "pre";
		final Connector connector = getConnector(testResource);

		assertNotNull(connector);
		assertEquals(testResource, connector.getConnectorIdentity().getCompiledFilename());

		var pre = connector.getPre();

		assertTrue(
			pre instanceof LinkedHashMap,
			"pre are expected to be a LinkedHashMap."
		);

		Map<String, Source> expected = new LinkedHashMap<String, Source>(
			Map.of("ipmiSource", new IpmiSource("ipmi", Collections.emptyList(), false, "pre.ipmiSource", null))
		);
					// Map.of only supports 10 elements

		// We want to keep the order declared in the YAML file
		assertEquals(expected.keySet(), pre.keySet());
		assertEquals(expected.values().getClass(), pre.values().getClass());
	}

	@Test
	void testPreBlankSource() throws IOException {
		try {
			getConnector("preBlankSource");
			Assert.fail(IO_EXCEPTION_MSG);
		} catch (IOException e) {
			final String message = "";
			checkMessage(e, message);
		}
	}

}
