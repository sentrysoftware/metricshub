package com.sentrysoftware.matrix.strategy.utils;

import com.sentrysoftware.matrix.common.JobInfo;
import com.sentrysoftware.matrix.connector.model.monitor.task.Mapping;
import com.sentrysoftware.matrix.telemetry.Monitor;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static com.sentrysoftware.matrix.constants.Constants.HARDCODED_SOURCE;
import static com.sentrysoftware.matrix.constants.Constants.MY_CONNECTOR_1_NAME;
import static org.junit.jupiter.api.Assertions.assertEquals;

class MappingProcessorTest {

	@Test
	void testInterpretNonContextMapping() {
		final TelemetryManager telemetryManager = new TelemetryManager();

		final List<String> row = List.of(
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
				"1"
		);

		final MappingProcessor mappingProcessor = MappingProcessor
				.builder()
				.jobInfo(JobInfo.builder().connectorName(MY_CONNECTOR_1_NAME).build())
				.telemetryManager(telemetryManager)
				.mapping(Mapping
						.builder()
						.source(HARDCODED_SOURCE)
						.build()
				)
				.row(row)
				.build();

		// Value conversion tests, basic value and invalid value
		{
			final Map<String, String> expected = new LinkedHashMap<>();
			expected.put("testMebiByte2Byte", "1048576.0");
			expected.put("testMegaHertz2Hertz", "1000000.0");
			expected.put("testMegaBit2Bit", "1000000.0");
			expected.put("testPercent2Ratio", "0.1");
			expected.put("testValue", "10");
			expected.put("testInvalidValue", null);

			expected.put("testMegaHertz2Hertz", "1000000.0");
			expected.put("testMegaBit2Bit", "1000000.0");
			expected.put("testPercent2Ratio", "0.1");
			expected.put("testValue", "10");
			expected.put("testInvalidValue", null);

			final Map<String, String> keyValuePairs = Map.of(
					"testMebiByte2Byte", "mebibyte2byte(1)",
					"testMegaHertz2Hertz", "megahertz2hertz(1)",
					"testMegaBit2Bit", "megabit2bit(1)",
					"testPercent2Ratio", "percent2ratio(10)",
					"testValue", "10",
					"testInvalidValue", "percent2ratio(ten)"
			);

			assertEquals(expected, mappingProcessor.interpretNonContextMapping(keyValuePairs));
		}

		// Value conversion tests and basic value
		{
			final Map<String, String> expected = Map.of(
					"testMebiByte2Byte", "1048576.0",
					"testMegaHertz2Hertz", "1000000.0",
					"testMegaBit2Bit", "1000000.0",
					"testPercent2Ratio", "0.1",
					"testValue", "10"
			);

			final Map<String, String> keyValuePairs = Map.of(
					"testMebiByte2Byte", "mebibyte2byte($1)",
					"testMegaHertz2Hertz", "megahertz2hertz($2)",
					"testMegaBit2Bit", "megabit2bit($3)",
					"testPercent2Ratio", "percent2ratio($4)",
					"testValue", "$5"
			);

			assertEquals(expected, mappingProcessor.interpretNonContextMapping(keyValuePairs));
		}

		// Legacy conversion tests with correct values
		{
			final Map<String, String> expected = Map.of(
					"testLegacyFullDuplex", "1",
					"testLegacyPredictedFailure", "1",
					"testLegacyIntrusionStatus", "1",
					"testLegacyNeedsCleaning", "1",
					"testLegacyLinkStatus", "0",
					"testBoolean", "1"
			);

			final Map<String, String> keyValuePairs = Map.of(
					"testLegacyFullDuplex", "legacyfullduplex(1)",
					"testLegacyPredictedFailure", "legacypredictedfailure(1)",
					"testLegacyIntrusionStatus", "legacyintrusionstatus(1)",
					"testLegacyNeedsCleaning", "legacyneedscleaning(1)",
					"testLegacyLinkStatus", "legacylinkstatus(2)",
					"testBoolean", "boolean(true)"
			);

			assertEquals(expected, mappingProcessor.interpretNonContextMapping(keyValuePairs));
		}

		// Legacy conversion tests with column references
		{
			final Map<String, String> expected = Map.of(
					"testLegacyFullDuplex", "1",
					"testLegacyPredictedFailure", "1",
					"testLegacyIntrusionStatus", "1",
					"testLegacyNeedsCleaning", "1",
					"testLegacyLinkStatus", "0",
					"testBoolean", "1"
			);

			final Map<String, String> keyValuePairs = Map.of(
					"testLegacyFullDuplex", "legacyfullduplex($6)",
					"testLegacyPredictedFailure", "legacypredictedfailure($7)",
					"testLegacyIntrusionStatus", "legacyintrusionstatus($8)",
					"testLegacyNeedsCleaning", "legacyneedscleaning($9)",
					"testLegacyLinkStatus", "legacylinkstatus($10)",
					"testBoolean", "boolean($11)"
			);

			assertEquals(expected, mappingProcessor.interpretNonContextMapping(keyValuePairs));
		}

		// Legacy conversion tests with incorrect values
		{
			final Map<String, String> expected = new LinkedHashMap<>();
			expected.put("testLegacyFullDuplex", null);
			expected.put("testLegacyPredictedFailure", null);
			expected.put("testLegacyIntrusionStatus", null);
			expected.put("testLegacyNeedsCleaning", null);
			expected.put("testLegacyLinkStatus", null);
			expected.put("testBoolean", "0");

			final Map<String, String> keyValuePairs = Map.of(
					"testLegacyFullDuplex", "legacyfullduplex(invalid)",
					"testLegacyPredictedFailure", "legacypredictedfailure(invalid)",
					"testLegacyIntrusionStatus", "legacyintrusionstatus(invalid)",
					"testLegacyNeedsCleaning", "legacyneedscleaning(invalid)",
					"testLegacyLinkStatus", "legacylinkstatus(invalid)",
					"testBoolean", "boolean(invalid)"
			);

			assertEquals(expected, mappingProcessor.interpretNonContextMapping(keyValuePairs));
		}
	}

	@Test
	void testInterpretNonContextMappingLookup() {

		Monitor monitor = Monitor
				.builder()
				.attributes(
						Map.of(
								"id", "3",
								"controller_number", "2"
						)
				)
				.build();

		final TelemetryManager telemetryManager = TelemetryManager
				.builder()
				.monitors(Map.of("disk_controller", Map.of("monitor", monitor)))
				.build();

		final List<String> row = List.of("randomValue", "2");

		final MappingProcessor mappingProcessor = MappingProcessor
				.builder()
				.jobInfo(JobInfo.builder().connectorName(MY_CONNECTOR_1_NAME).build())
				.telemetryManager(telemetryManager)
				.mapping(Mapping
						.builder()
						.source(HARDCODED_SOURCE)
						.build()
				)
				.row(row)
				.build();

		final Map<String, String> expected = Map.of("hw.parent.id", "3");

		final Map<String, String> keyValuePairs = Map.of("hw.parent.id", "lookup(\"disk_controller\", \"id\", \"controller_number\", $2)");

		assertEquals(expected, mappingProcessor.interpretNonContextMapping(keyValuePairs));

		// Invalid lookup values tests

		final Map<String, String> expectedNull = new HashMap<>();
		expectedNull.put("hw.parent.id", null);

		final Map<String, String> keyValuePairsTooManyArguments = Map.of("hw.parent.id", "lookup(\"disk_controller\", \"id\", \"controller_number\", $2, \"extraValue\")");

		assertEquals(expectedNull, mappingProcessor.interpretNonContextMapping(keyValuePairsTooManyArguments));

		final Map<String, String> keyValuePairsInvalidFirstArgument = Map.of("hw.parent.id", "lookup(\"$11\", \"id\", \"controller_number\", $2)");

		assertEquals(expectedNull, mappingProcessor.interpretNonContextMapping(keyValuePairsInvalidFirstArgument));

		final Map<String, String> keyValuePairsInvalidSecondArgument = Map.of("hw.parent.id", "lookup(\"disk_controller\", $11, \"controller_number\", $2)");

		assertEquals(expectedNull, mappingProcessor.interpretNonContextMapping(keyValuePairsInvalidSecondArgument));

		final Map<String, String> keyValuePairsInvalidThirdArgument = Map.of("hw.parent.id", "lookup(\"disk_controller\", \"id\", $11, $2)");

		assertEquals(expectedNull, mappingProcessor.interpretNonContextMapping(keyValuePairsInvalidThirdArgument));

		final Map<String, String> keyValuePairsInvalidFourthArgument = Map.of("hw.parent.id", "lookup(\"disk_controller\", \"id\", \"controller_number\", $11)");

		assertEquals(expectedNull, mappingProcessor.interpretNonContextMapping(keyValuePairsInvalidFourthArgument));

		final Map<String, String> keyValuePairsMonitorTypeNotFound = Map.of("hw.parent.id", "lookup(\"enclosure\", \"id\", \"controller_number\", $2)");

		assertEquals(expectedNull, mappingProcessor.interpretNonContextMapping(keyValuePairsMonitorTypeNotFound));

		final Map<String, String> keyValuePairsMonitorWithAttributeValueNotFound = Map.of("hw.parent.id", "lookup(\"disk_controller\", \"id\", \"wrongAttribute\", $2)");

		assertEquals(expectedNull, mappingProcessor.interpretNonContextMapping(keyValuePairsMonitorWithAttributeValueNotFound));
	}
}