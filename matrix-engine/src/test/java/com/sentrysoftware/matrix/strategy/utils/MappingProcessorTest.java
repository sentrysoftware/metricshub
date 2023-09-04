package com.sentrysoftware.matrix.strategy.utils;

import com.sentrysoftware.matrix.connector.model.monitor.task.Mapping;
import com.sentrysoftware.matrix.strategy.source.SourceTable;
import com.sentrysoftware.matrix.telemetry.HostProperties;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static com.sentrysoftware.matrix.constants.Constants.HARDCODED_SOURCE;
import static com.sentrysoftware.matrix.constants.Constants.MY_CONNECTOR_1_NAME;
import static com.sentrysoftware.matrix.constants.Constants.SOURCE_REF_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MappingProcessorTest {

	@Test
	void testLookupSourceTableFromHardcodedSource() {
		final HostProperties hostProperties = HostProperties
			.builder()
			.build();

		hostProperties.getConnectorNamespace(MY_CONNECTOR_1_NAME);

		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.hostProperties(hostProperties)
			.build();

		final MappingProcessor mappingProcessor = MappingProcessor
			.builder()
			.connectorId(MY_CONNECTOR_1_NAME)
			.telemetryManager(telemetryManager)
			.mapping(
					Mapping
							.builder()
							.source(HARDCODED_SOURCE)
							.build()
			)
			.build();

		Optional<SourceTable> sourceTableOpt = mappingProcessor.lookupSourceTable();
		assertTrue(sourceTableOpt.isPresent());

		assertEquals(HARDCODED_SOURCE, sourceTableOpt.get().getTable().get(0).get(0));
	}

	@Test
	void testLookupSourceTableFromReferencedSource() {
		final HostProperties hostProperties = HostProperties
			.builder()
			.build();

		final SourceTable expected = SourceTable
			.builder()
			.table(List.of(List.of("value")))
			.build();

		hostProperties
			.getConnectorNamespace(MY_CONNECTOR_1_NAME)
			.addSourceTable(SOURCE_REF_KEY, expected);

		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.hostProperties(hostProperties)
			.build();

		final MappingProcessor mappingProcessor = MappingProcessor
			.builder()
			.connectorId(MY_CONNECTOR_1_NAME)
			.telemetryManager(telemetryManager)
			.mapping(
					Mapping
							.builder()
							.source(SOURCE_REF_KEY)
							.build()
			)
			.build();

		Optional<SourceTable> sourceTableOpt = mappingProcessor.lookupSourceTable();
		assertTrue(sourceTableOpt.isPresent());

		assertEquals(expected, sourceTableOpt.get());
	}

	@Test
	void testLookupSourceTableFromReferencedSourceNotFound() {
		final HostProperties hostProperties = HostProperties
			.builder()
			.build();

		hostProperties.getConnectorNamespace(MY_CONNECTOR_1_NAME);

		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.hostProperties(hostProperties)
			.build();

		final MappingProcessor mappingProcessor = MappingProcessor
			.builder()
			.connectorId(MY_CONNECTOR_1_NAME)
			.telemetryManager(telemetryManager)
			.mapping(
					Mapping
						.builder()
						.source(SOURCE_REF_KEY)
						.build()
			)
			.build();

		assertTrue(mappingProcessor.lookupSourceTable().isEmpty());
	}
}