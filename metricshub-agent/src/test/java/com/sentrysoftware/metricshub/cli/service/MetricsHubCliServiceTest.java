package com.sentrysoftware.metricshub.cli.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.sentrysoftware.metricshub.cli.helper.StringBuilderWriter;
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
}
