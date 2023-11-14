package com.sentrysoftware.metricshub.cli.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sentrysoftware.metricshub.cli.helper.StringBuilderWriter;
import com.sentrysoftware.metricshub.cli.service.MetricsHubCliService.CliPasswordReader;
import com.sentrysoftware.metricshub.cli.service.converter.SnmpPrivacyConverter;
import com.sentrysoftware.metricshub.cli.service.converter.SnmpVersionConverter;
import com.sentrysoftware.metricshub.cli.service.protocol.IpmiConfigCli;
import com.sentrysoftware.metricshub.cli.service.protocol.SnmpConfigCli;
import com.sentrysoftware.metricshub.engine.configuration.SnmpConfiguration;
import com.sentrysoftware.metricshub.engine.connector.model.Connector;
import com.sentrysoftware.metricshub.engine.connector.model.ConnectorStore;
import com.sentrysoftware.metricshub.engine.connector.model.common.DeviceKind;
import com.sentrysoftware.metricshub.engine.connector.model.identity.ConnectorIdentity;
import com.sentrysoftware.metricshub.engine.connector.model.identity.Detection;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.Test;

class MetricsHubCliServiceTest {

	private static final char[] PASSWORD = new char[] { 'p', 'a', 's', 's', 'w', 'o', 'r', 'd' };

	@Test
	void testListAllConnectors() {
		final ConnectorStore connectorStore = new ConnectorStore();
		final String connectorId = "TestConnector";
		final String displayName = "Test Connector";
		final Map<String, Connector> store = Map.of(
			connectorId,
			Connector
				.builder()
				.connectorIdentity(
					ConnectorIdentity
						.builder()
						.detection(Detection.builder().appliesTo(Set.of(DeviceKind.LINUX)).build())
						.compiledFilename(connectorId)
						.displayName(displayName)
						.build()
				)
				.build()
		);
		connectorStore.setStore(store);
		final StringBuilder builder = new StringBuilder();
		final Writer writer = new StringBuilderWriter(builder);
		final PrintWriter printWriter = new PrintWriter(writer);
		new MetricsHubCliService().listAllConnectors(connectorStore, printWriter);
		final String result = builder.toString();
		assertTrue(result.contains(displayName));
		assertTrue(result.contains(connectorId));
		assertTrue(result.contains(DeviceKind.LINUX.getDisplayName()));
	}

	@Test
	void testSetLogLevel() {
		final MetricsHubCliService metricsHubCliService = new MetricsHubCliService();

		assertDoesNotThrow(() -> {
			metricsHubCliService.verbose = new boolean[] {};
			metricsHubCliService.setLogLevel();
		});
		assertDoesNotThrow(() -> {
			metricsHubCliService.verbose = new boolean[] { true };
			metricsHubCliService.setLogLevel();
		});
		assertDoesNotThrow(() -> {
			metricsHubCliService.verbose = new boolean[] { true, true };
			metricsHubCliService.setLogLevel();
		});
		assertDoesNotThrow(() -> {
			metricsHubCliService.verbose = new boolean[] { true, true, true };
			metricsHubCliService.setLogLevel();
		});
		assertDoesNotThrow(() -> {
			metricsHubCliService.verbose = new boolean[] { true, true, true, true };
			metricsHubCliService.setLogLevel();
		});
	}

	@Test
	void testTryInteractiveGlobalPassword() {
		final MetricsHubCliService metricsHubCliService = new MetricsHubCliService();

		// Initialize a StringBuilder to capture the input password
		final StringBuilder builder = new StringBuilder();

		// Define a CliPasswordReader that appends the password to the StringBuilder
		final CliPasswordReader<char[]> cliPasswordReader = (format, args) -> {
			builder.append(PASSWORD, 0, PASSWORD.length);
			return PASSWORD;
		};

		// Test tryInteractiveGlobalPassword method with the CliPasswordReader
		metricsHubCliService.tryInteractiveGlobalPassword(cliPasswordReader);

		// Make sure the StringBuilder is blank
		// This confirms that tryInteractiveGlobalPassword hasn't triggered the password reader
		// because the username is not present
		assertTrue(builder.isEmpty());

		// Set a username in MetricsHubCliService
		metricsHubCliService.username = "user";

		// Test tryInteractiveGlobalPassword method with the CliPasswordReader
		metricsHubCliService.tryInteractiveGlobalPassword(cliPasswordReader);

		// Assert that the captured password in the StringBuilder matches the expected value
		// This confirms that tryInteractiveGlobalPassword has triggered the password reader
		// because the username is present but the password is null
		assertEquals(new String(PASSWORD), builder.toString());

		// Clear the StringBuilder
		builder.delete(0, PASSWORD.length);

		// Set the password in MetricsHubCliService
		metricsHubCliService.password = PASSWORD;

		// Test tryInteractiveGlobalPassword method with the CliPasswordReader again
		metricsHubCliService.tryInteractiveGlobalPassword(cliPasswordReader);

		// Ensure that the StringBuilder is empty after the method call
		// This confirms that tryInteractiveGlobalPassword hasn't triggered the password reader
		// because the both username and password are already present
		assertTrue(builder.isEmpty());
	}

	@Test
	void testTryInteractiveIpmiPassword() {
		final MetricsHubCliService metricsHubCliService = new MetricsHubCliService();

		// Initialize a StringBuilder to capture the input password
		final StringBuilder builder = new StringBuilder();

		// Define a CliPasswordReader that appends the password to the StringBuilder
		final CliPasswordReader<char[]> cliPasswordReader = (format, args) -> {
			builder.append(PASSWORD, 0, PASSWORD.length);
			return PASSWORD;
		};

		// Test tryInteractiveIpmiPassword method with the CliPasswordReader
		metricsHubCliService.tryInteractiveIpmiPassword(cliPasswordReader);

		// Make sure the StringBuilder is blank
		// This confirms that tryInteractiveIpmiPassword hasn't triggered the password reader
		// because ipmiConfigCli is not present
		assertTrue(builder.isEmpty());

		// Set a new ipmiConfigCli in MetricsHubCliService
		metricsHubCliService.ipmiConfigCli = new IpmiConfigCli();

		// Make sure the StringBuilder is blank
		// This confirms that tryInteractiveIpmiPassword hasn't triggered the password reader
		// because ipmiConfigCli is present but it doesn't define the username
		assertTrue(builder.isEmpty());

		// Set a username in ipmiConfigCli
		metricsHubCliService.ipmiConfigCli.setUsername("ipmiUser");

		// Test tryInteractiveIpmiPassword method with the CliPasswordReader
		metricsHubCliService.tryInteractiveIpmiPassword(cliPasswordReader);

		// Assert that the captured password in the StringBuilder matches the expected value
		// This confirms that tryInteractiveIpmiPassword has triggered the password reader
		// because the username is present in ipmiConfigCli but the password is null
		assertEquals(new String(PASSWORD), builder.toString());

		// Set a password in ipmiConfigCli
		metricsHubCliService.ipmiConfigCli.setPassword(PASSWORD);

		// Clear the StringBuilder
		builder.delete(0, PASSWORD.length);

		// Test tryInteractiveIpmiPassword method with the CliPasswordReader
		metricsHubCliService.tryInteractiveIpmiPassword(cliPasswordReader);

		// Ensure that the StringBuilder is empty after the method call
		// This confirms that tryInteractiveIpmiPassword hasn't triggered the password reader
		// because the both username and password are already present in ipmiConfigCli
		assertTrue(builder.isEmpty());
	}

	@Test
	void testTryInteractiveSnmpPassword() {
		final MetricsHubCliService metricsHubCliService = new MetricsHubCliService();

		// Initialize a StringBuilder to capture the input password
		final StringBuilder builder = new StringBuilder();

		// Define a CliPasswordReader that appends the password to the StringBuilder
		final CliPasswordReader<char[]> cliPasswordReader = (format, args) -> {
			builder.append(PASSWORD, 0, PASSWORD.length);
			return PASSWORD;
		};

		// Test tryInteractiveSnmpPassword method with the CliPasswordReader
		metricsHubCliService.tryInteractiveSnmpPassword(cliPasswordReader);

		// Make sure the StringBuilder is blank
		// This confirms that tryInteractiveSnmpPassword hasn't triggered the password reader
		// because snmpConfigCli is not present
		assertTrue(builder.isEmpty());

		// Set a new snmpConfigCli in MetricsHubCliService
		metricsHubCliService.snmpConfigCli = new SnmpConfigCli();

		// Make sure the StringBuilder is blank
		// This confirms that tryInteractiveSnmpPassword hasn't triggered the password reader
		// because snmpConfigCli is present but it doesn't define the username
		assertTrue(builder.isEmpty());

		// Set a username in snmpConfigCli
		metricsHubCliService.snmpConfigCli.setUsername("snmpUser");

		// Test tryInteractiveSnmpPassword method with the CliPasswordReader
		metricsHubCliService.tryInteractiveSnmpPassword(cliPasswordReader);

		// Assert that the captured password in the StringBuilder matches the expected value
		// This confirms that tryInteractiveSnmpPassword has triggered the password reader
		// because the username is present in snmpConfigCli but the password is null
		assertEquals(new String(PASSWORD), builder.toString());

		// Set a password in snmpConfigCli
		metricsHubCliService.snmpConfigCli.setPassword(PASSWORD);

		// Clear the StringBuilder
		builder.delete(0, PASSWORD.length);

		// Test tryInteractiveSnmpPassword method with the CliPasswordReader
		metricsHubCliService.tryInteractiveSnmpPassword(cliPasswordReader);

		// Ensure that the StringBuilder is empty after the method call
		// This confirms that tryInteractiveSnmpPassword hasn't triggered the password reader
		// because the both username and password are already present in snmpConfigCli
		assertTrue(builder.isEmpty());
	}

	@Test
	void testSnmpVersionConverter() {
		final SnmpVersionConverter snmpVersionConverter = new SnmpVersionConverter();

		// Check Snmp version conversion
		assertEquals(SnmpConfiguration.SnmpVersion.V3_MD5, snmpVersionConverter.convert("v3 md5"));
		assertEquals(SnmpConfiguration.SnmpVersion.V3_NO_AUTH, snmpVersionConverter.convert("v3 no auth"));
		assertEquals(SnmpConfiguration.SnmpVersion.V2C, snmpVersionConverter.convert("v2"));
		assertEquals(SnmpConfiguration.SnmpVersion.V1, snmpVersionConverter.convert("v1"));
	}

	@Test
	void testSnmpPrivacyConverter() {
		final SnmpPrivacyConverter snmpPrivacyConverter = new SnmpPrivacyConverter();

		// Check Snmp privacy conversion
		assertEquals(SnmpConfiguration.Privacy.DES, snmpPrivacyConverter.convert("des"));
		assertEquals(SnmpConfiguration.Privacy.AES, snmpPrivacyConverter.convert("aes"));
		assertEquals(SnmpConfiguration.Privacy.NO_ENCRYPTION, snmpPrivacyConverter.convert("none"));
		assertEquals(SnmpConfiguration.Privacy.NO_ENCRYPTION, snmpPrivacyConverter.convert("no"));
	}

	@Test
	void testSnmpConfigCliToProtocol() {
		// Create a SnmpConfigCli instance and set its data
		final SnmpConfigCli snmpConfigCli = new SnmpConfigCli();
		snmpConfigCli.setSnmpVersion(SnmpConfiguration.SnmpVersion.V1);
		snmpConfigCli.setPort(80);
		snmpConfigCli.setCommunity("community");
		snmpConfigCli.setPrivacy(SnmpConfiguration.Privacy.AES);
		snmpConfigCli.setTimeout(120L);
		snmpConfigCli.setPrivacyPassword(PASSWORD);

		// Create an SnmpConfiguration and call method toProtocol

		final SnmpConfiguration snmpConfiguration = snmpConfigCli.toProtocol("user1", PASSWORD);

		// Check the resulting snmp configuration
		assertNotNull(snmpConfiguration);
		assertEquals(SnmpConfiguration.SnmpVersion.V1, snmpConfiguration.getVersion());
		assertEquals("SNMP v1 (community)", snmpConfiguration.toString());
	}
}
