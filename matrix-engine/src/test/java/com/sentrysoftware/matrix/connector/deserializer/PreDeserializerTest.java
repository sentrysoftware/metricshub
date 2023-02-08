package com.sentrysoftware.matrix.connector.deserializer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.CopySource;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.HttpSource;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.IpmiSource;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.OsCommandSource;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.SnmpGetSource;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.SnmpTableSource;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.Source;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.SshInteractiveSource;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.StaticSource;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.TableJoinSource;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.TableUnionSource;

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
				"pre are expected to be a LinkedHashMap.");

		Map<String, Source> expected = new LinkedHashMap<String, Source>(
			Map.of(
					"firstSource", new CopySource(),
					"secondSource", new HttpSource(),
					"thirdSource", new IpmiSource(),
					"forthSource", new OsCommandSource(),
					"fifthSource", new SnmpGetSource(),
					"sixthSource", new SnmpTableSource(),
					"seventhSource", new SshInteractiveSource(),
					"eighthSource", new StaticSource(),
					"ninthSource", new TableJoinSource(),
					"tenthSource", new TableUnionSource()));
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

	@Test
	void testPreNullSource() throws IOException {
		try {
			getConnector("preNullSource");
			Assert.fail(IO_EXCEPTION_MSG);
		} catch (IOException e) {
			final String message = "expected <block end>, but found ':'";
			checkMessage(e, message);
		}
	}
}
