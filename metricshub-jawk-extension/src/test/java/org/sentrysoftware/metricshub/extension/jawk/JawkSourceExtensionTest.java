package org.sentrysoftware.metricshub.extension.jawk;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.sentrysoftware.metricshub.engine.configuration.HostConfiguration;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.HttpSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.IpmiSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.JawkSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.SnmpGetSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.SnmpTableSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.Source;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.WbemSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.WmiSource;
import org.sentrysoftware.metricshub.engine.strategy.source.SourceProcessor;
import org.sentrysoftware.metricshub.engine.strategy.source.SourceTable;
import org.sentrysoftware.metricshub.engine.telemetry.HostProperties;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;

@ExtendWith(MockitoExtension.class)
class JawkSourceExtensionTest {

	private static final String RAW_DATA_RESULT_1 = "{\"myResult\": \"rawData result 1\"}";
	private static final String RAW_DATA_RESULT_2 = "{\"myResult\": \"rawData result 2\"}";
	private static final String RAW_DATA_RESULT_3 = "{\"myResult\": \"rawData result 3\"}";
	private static final String DATA_RESULT_1 = "rawData result 1";
	private static final String DATA_RESULT_2 = "rawData result 2";
	private static final String DATA_RESULT_3 = "rawData result 3";

	@Mock
	private SourceProcessor sourceProcessorMock;

	@Test
	void testProcessSource() {
		final JawkSourceExtension jawkExtension = new JawkSourceExtension();
		final TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.hostConfiguration(HostConfiguration.builder().hostname("test-host").hostId("test-host").build())
			.build();

		final List<String> line1 = Arrays.asList("FOO", "1", "2", "3");
		final List<String> line2 = Arrays.asList("BAR", "10", "20", "30");
		final List<String> line3 = Arrays.asList("BAZ", "100", "200", "300");
		final List<List<String>> tableOne = Arrays.asList(line1, line2, line3);
		final SourceTable sourceTableOne = SourceTable
			.builder()
			.table(tableOne)
			.rawData(SourceTable.tableToCsv(tableOne, ";", false))
			.build();

		final HostProperties hostProperties = HostProperties.builder().build();

		hostProperties
			.getConnectorNamespace("connectorId")
			.addSourceTable("${source::monitors.system.discovery.sources.source_one}", sourceTableOne);

		telemetryManager.setHostProperties(hostProperties);

		// Http request & Json2CSV
		final Source source = JawkSource
			.builder()
			.type("Jawk")
			.input("${source::monitors.system.discovery.sources.source_one}")
			.script(
				"""
					BEGIN {
					    FS=";"
					    OFS=";"
				    }

				    {
					    requestArguments["method"] = "get"
					    requestArguments["path"] = "/ConfigurationManager/v1/objects/storages/" $1
					    requestArguments["resultContent"] = "body"
					    json2csvArguments["jsonSource"] = executeHttpRequest(requestArguments)
					    json2csvArguments["entryKey"] = "/"
					    json2csvArguments["properties"] = "myResult"
					    json2csvArguments["separator"] = ";"
					    print json2csv(json2csvArguments)
					}
				"""
			)
			.build();

		final SourceTable sourceTableResult1 = SourceTable.builder().rawData(RAW_DATA_RESULT_1).build();
		final SourceTable sourceTableResult2 = SourceTable.builder().rawData(RAW_DATA_RESULT_2).build();
		final SourceTable sourceTableResult3 = SourceTable.builder().rawData(RAW_DATA_RESULT_3).build();

		when(sourceProcessorMock.process(any(HttpSource.class)))
			.thenReturn(sourceTableResult1, sourceTableResult2, sourceTableResult3);

		assertEquals(
			Arrays.asList(
				Arrays.asList("/", DATA_RESULT_1),
				Arrays.asList("/", DATA_RESULT_2),
				Arrays.asList("/", DATA_RESULT_3)
			),
			jawkExtension.processSource(source, "connectorId", telemetryManager, sourceProcessorMock).getTable()
		);

		// WBEM Request
		((JawkSource) source).setScript(
				"""
					BEGIN {
					    FS=";"
					    OFS=";"
				    }

				    {
				    	requestArguments["query"] = "myQuery"
				    	requestArguments["namespace"] = "myNamespace"
					    print executeWbemRequest(requestArguments)
					}
				"""
			);
		((JawkSource) source).setInput("input test");
		doReturn(
			SourceTable.builder().rawData("result1;result2").table(SourceTable.csvToTable("result1;result2", ";")).build()
		)
			.when(sourceProcessorMock)
			.process(any(WbemSource.class));
		assertEquals(
			Collections.singletonList(Arrays.asList("result1", "result2")),
			jawkExtension.processSource(source, "connectorId", telemetryManager, sourceProcessorMock).getTable()
		);

		// WMI Request
		((JawkSource) source).setScript(
				"""
					BEGIN {
					    FS=";"
					    OFS=";"
				    }

				    {
				    	requestArguments["query"] = "myQuery"
				    	requestArguments["namespace"] = "myNamespace"
					    print executeWmiRequest(requestArguments)
					}
				"""
			);
		doReturn(
			SourceTable.builder().rawData("result1;result2").table(SourceTable.csvToTable("result1;result2", ";")).build()
		)
			.when(sourceProcessorMock)
			.process(any(WmiSource.class));
		assertEquals(
			Collections.singletonList(Arrays.asList("result1", "result2")),
			jawkExtension.processSource(source, "connectorId", telemetryManager, sourceProcessorMock).getTable()
		);

		// SNMP Get Request
		((JawkSource) source).setScript(
				"""
					BEGIN {
					    FS=";"
					    OFS=";"
				    }

				    {
				    	requestArguments["oid"] = "1.2.3.4.5.6"
					    print executeSnmpGet(requestArguments)
					}
				"""
			);
		doReturn(
			SourceTable.builder().rawData("result1;result2").table(SourceTable.csvToTable("result1;result2", ";")).build()
		)
			.when(sourceProcessorMock)
			.process(any(SnmpGetSource.class));

		assertEquals(
			Collections.singletonList(Arrays.asList("result1", "result2")),
			jawkExtension.processSource(source, "connectorId", telemetryManager, sourceProcessorMock).getTable()
		);

		// SNMP Table Request
		((JawkSource) source).setScript(
				"""
					BEGIN {
					    FS=";"
					    OFS=";"
				    }

				    {
				    	requestArguments["oid"] = "1.2.3.4.5.6"
				    	requestArguments["selectColumns"] = "1"
					    print executeSnmpTable(requestArguments)
					}
				"""
			);
		doReturn(
			SourceTable.builder().rawData("result1;result2").table(SourceTable.csvToTable("result1;result2", ";")).build()
		)
			.when(sourceProcessorMock)
			.process(any(SnmpTableSource.class));
		assertEquals(
			Collections.singletonList(Arrays.asList("result1", "result2")),
			jawkExtension.processSource(source, "connectorId", telemetryManager, sourceProcessorMock).getTable()
		);
	}

	@Test
	void testIsValidSource() {
		final JawkSourceExtension jawkExtension = new JawkSourceExtension();
		assertFalse(jawkExtension.isValidSource(new IpmiSource()));
		assertTrue(jawkExtension.isValidSource(new JawkSource()));
	}
}
