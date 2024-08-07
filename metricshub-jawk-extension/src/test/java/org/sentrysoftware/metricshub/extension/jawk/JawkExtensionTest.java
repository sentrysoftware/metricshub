package org.sentrysoftware.metricshub.extension.jawk;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sentrysoftware.metricshub.engine.configuration.HostConfiguration;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.JawkSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.Source;
import org.sentrysoftware.metricshub.engine.strategy.source.SourceProcessor;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;

@ExtendWith(MockitoExtension.class)
class JawkExtensionTest {

	@Test
	void testProcessSource() {
		SourceProcessor sourceProcessor = mock(SourceProcessor.class);
		final JawkSourceExtension jawkExtension = new JawkSourceExtension();
		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.hostConfiguration(HostConfiguration.builder().hostname("test-host").hostId("test-host").build())
			.build();
		final Source source = JawkSource
			.builder()
			.script(
				"""
					BEGIN {
					    OFS=";"
					    method = "get"
					    header = ""
					    source = getSource("systemIdDiscovery")
					    print source
					}
				"""
			)
			.build();
		assertDoesNotThrow(() -> jawkExtension.processSource(source, "connectorId", telemetryManager, sourceProcessor));
	}
}
