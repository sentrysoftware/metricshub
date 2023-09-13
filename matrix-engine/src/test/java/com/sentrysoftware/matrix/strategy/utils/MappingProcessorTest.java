package com.sentrysoftware.matrix.strategy.utils;

import static com.sentrysoftware.matrix.constants.Constants.HARDCODED_SOURCE;
import static com.sentrysoftware.matrix.constants.Constants.MY_CONNECTOR_1_NAME;
import static com.sentrysoftware.matrix.constants.Constants.SOURCE_REF_KEY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.common.JobInfo;
import com.sentrysoftware.matrix.connector.model.monitor.task.Mapping;
import com.sentrysoftware.matrix.strategy.source.SourceTable;
import com.sentrysoftware.matrix.telemetry.HostProperties;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;

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
			.jobInfo(JobInfo.builder().connectorName(MY_CONNECTOR_1_NAME).build())
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
			.jobInfo(JobInfo.builder().connectorName(MY_CONNECTOR_1_NAME).build())
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
			.jobInfo(JobInfo.builder().connectorName(MY_CONNECTOR_1_NAME).build())
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

	@Test
	void testInterpretNonContextMapping() {
		final HostProperties hostProperties = HostProperties
				.builder()
				.build();

			hostProperties.getConnectorNamespace(MY_CONNECTOR_1_NAME);

			final TelemetryManager telemetryManager = TelemetryManager
				.builder()
				.hostProperties(hostProperties)
				.build();

			List<String> row = List.of(
				"1",
				"1",
				"1",
				"10",
				"10",
				"1",
				"1",
				"1",
				"1",
				"2",
				"1");

			final MappingProcessor mappingProcessor = MappingProcessor
				.builder()
				.jobInfo(JobInfo.builder().connectorName(MY_CONNECTOR_1_NAME).build())
				.telemetryManager(telemetryManager)
				.mapping(
						Mapping
								.builder()
								.source(HARDCODED_SOURCE)
								.build()
				)
				.row(row)
				.build();

		// Value conversion tests and basic value
		{
			final Map<String, String> expected = Map.of(
				"testMebiByte2Byte", "1048576.0",
				"testMegaHertz2Hertz", "1000000.0",
				"testMegaBit2Bit", "1000000.0",
				"testPercent2Ratio", "0.1",
				"testValue", "10");

			final Map<String, String> keyValuePairs = Map.of(
				"testMebiByte2Byte", "mebibyte2byte(1)",
				"testMegaHertz2Hertz", "megahertz2hertz(1)",
				"testMegaBit2Bit", "megabit2bit(1)",
				"testPercent2Ratio", "percent2ratio(10)",
				"testValue", "10");

			assertEquals(expected, mappingProcessor.interpretNonContextMapping(keyValuePairs));
		}

		// Value conversion tests and basic value
		{
			final Map<String, String> expected = Map.of(
				"testMebiByte2Byte", "1048576.0",
				"testMegaHertz2Hertz", "1000000.0",
				"testMegaBit2Bit", "1000000.0",
				"testPercent2Ratio", "0.1",
				"testValue", "10");

			final Map<String, String> keyValuePairs = Map.of(
				"testMebiByte2Byte", "mebibyte2byte($1)",
				"testMegaHertz2Hertz", "megahertz2hertz($2)",
				"testMegaBit2Bit", "megabit2bit($3)",
				"testPercent2Ratio", "percent2ratio($4)",
				"testValue", "$5");

			assertEquals(expected, mappingProcessor.interpretNonContextMapping(keyValuePairs));
		}

		// Legacy conversion tests with correct values
		{
			final Map<String, String> expected = Map.of(
				"test1", "1",
				"test2", "1",
				"test3", "1",
				"test4", "1",
				"test5", "0",
				"test6", "1");
			
			final Map<String, String> keyValuePairs = Map.of(
				"test1", "legacyfullduplex(1)",
				"test2", "legacypredictedfailure(1)",
				"test3", "legacyintrusionstatus(1)",
				"test4", "legacyneedscleaning(1)",
				"test5", "legacylinkstatus(2)",
				"test6", "boolean(true)");

			assertEquals(expected, mappingProcessor.interpretNonContextMapping(keyValuePairs));
		}

		// Legacy conversion tests with column references
		{
			final Map<String, String> expected = Map.of(
				"test1", "1",
				"test2", "1",
				"test3", "1",
				"test4", "1",
				"test5", "0",
				"test6", "1");

			final Map<String, String> keyValuePairs = Map.of(
				"test1", "legacyfullduplex($6)",
				"test2", "legacypredictedfailure($7)",
				"test3", "legacyintrusionstatus($8)",
				"test4", "legacyneedscleaning($9)",
				"test5", "legacylinkstatus($10)",
				"test6", "boolean($11)");

			assertEquals(expected, mappingProcessor.interpretNonContextMapping(keyValuePairs));
		}

		// Legacy conversion tests with incorrect values
		{
			final Map<String, String> expected = Map.of(
				"test1", null,
				"test2", null,
				"test3", null,
				"test4", null,
				"test5", null,
				"test6", "0");

			final Map<String, String> keyValuePairs = Map.of(
				"test1", "legacyfullduplex(invalid)",
				"test2", "legacypredictedfailure(invalid)",
				"test3", "legacyintrusionstatus(invalid)",
				"test4", "legacyneedscleaning(invalid)",
				"test5", "legacylinkstatus(invalid)",
				"test6", "boolean(invalid)");

			assertEquals(expected, mappingProcessor.interpretNonContextMapping(keyValuePairs));
		}

		// Lookup conversion tests
	}
}