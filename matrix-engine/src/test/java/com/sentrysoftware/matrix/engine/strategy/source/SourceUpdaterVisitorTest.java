package com.sentrysoftware.matrix.engine.strategy.source;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.EmbeddedFile;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.http.EntryConcatMethod;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.http.HTTPSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.ipmi.IPMI;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.oscommand.OSCommandSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.reference.ReferenceSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.reference.StaticSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetTableSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.tablejoin.TableJoinSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.tableunion.TableUnionSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.telnet.TelnetInteractiveSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.ucs.UCSSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.wbem.WBEMSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.wmi.WMISource;
import com.sentrysoftware.matrix.engine.strategy.StrategyConfig;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoring;

@ExtendWith(MockitoExtension.class)
class SourceUpdaterVisitorTest {

	private static final String AUTHENTICATION_TOKEN_FIELD = "authenticationToken";
	private static final String ENCLOSURE_DISCOVERY_SOURCE_2_KEY = "Enclosure.Discovery.Source(2)";
	private static final String ENCLOSURE_DISCOVERY_SOURCE_1_KEY = "Enclosure.Discovery.Source(1)";
	private static final String ENCLOSURE_DEVICE_ID = "1.1";
	private static final String VALUE_TABLE = "enclosure.collect.source(1)";
	private static final String DEVICE_ID = "deviceId";
	private static final String EMBEDDED_FILE_CODE_CONTENT_1 = "showplatform -v,13,14";
	private static final String EMBEDDED_FILE_CODE_CONTENT_2 = "showenvironment,7,8";
	private static final String VALUE_VAL1 = "val1";

	@Mock
	private ISourceVisitor sourceVisitor;

	@Mock
	private Connector connector;

	@Mock
	private Monitor monitor;

	@Mock
	private StrategyConfig strategyConfig;

	@Mock
	private HostMonitoring hostMonitoring;

	@InjectMocks
	private SourceUpdaterVisitor sourceUpdaterVisitor;

	private static Map<String, String> metadata = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
	
	private static EmbeddedFile embeddedFile1 = EmbeddedFile.builder().content(EMBEDDED_FILE_CODE_CONTENT_1).build();
	private static EmbeddedFile embeddedFile2 = EmbeddedFile.builder().content(EMBEDDED_FILE_CODE_CONTENT_2).build();
	
	@BeforeAll
	public static void setUp() {

		metadata.put(DEVICE_ID, ENCLOSURE_DEVICE_ID);

	}

	@Test
	void testVisitHTTPSource() {
		doReturn(SourceTable.empty()).when(sourceVisitor).visit(any(HTTPSource.class));
		assertEquals(SourceTable.empty(), new SourceUpdaterVisitor(sourceVisitor, connector, monitor, strategyConfig).visit(HTTPSource.builder().build()));

		final SNMPGetSource snmpGetSource = SNMPGetSource.builder().oid("1.2.3.4.5.6.%Fan.Collect.DeviceID").build();
		doReturn(metadata).when(monitor).getMetadata();
		final List<List<String>> resultSnmp = Collections.singletonList(Collections.singletonList(VALUE_VAL1));
		final SourceTable expectedSnmp = SourceTable.builder().table(resultSnmp).build();
		doReturn(expectedSnmp).when(sourceVisitor).visit(any(SNMPGetSource.class));
		assertEquals(expectedSnmp, new SourceUpdaterVisitor(sourceVisitor, connector, monitor, strategyConfig).visit(snmpGetSource));

		HTTPSource httpSource = HTTPSource.builder()
				.executeForEachEntryOf("enclosure.collect.source(1)")
				.url("urlprefix_%Entry.Column(1)%_urlmidsection_%Entry.Column(2)%_urlsuffix")
				.build();

		List<List<String>> table = Arrays.asList(
				Arrays.asList("val1", "val2", "val3"),
				Arrays.asList("a1", "b1", "c1"));

		SourceTable sourceTable = SourceTable.builder()
				.table(table)
				.build();

		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		doReturn(sourceTable).when(hostMonitoring).getSourceTableByKey(VALUE_TABLE);

		String expectedResult = "expectedVal1expectedVal2";

		SourceTable expected1 = SourceTable.builder().rawData("expectedVal1").build();
		SourceTable expected2 = SourceTable.builder().rawData("expectedVal2").build();
		doReturn(expected1, expected2).when(sourceVisitor).visit(any(HTTPSource.class));
		SourceTable result = new SourceUpdaterVisitor(sourceVisitor, connector, monitor, strategyConfig).visit(httpSource);
		assertEquals(expectedResult, result.getRawData());

		httpSource.setEntryConcatMethod(EntryConcatMethod.LIST);
		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		doReturn(sourceTable).when(hostMonitoring).getSourceTableByKey(VALUE_TABLE);
		doReturn(expected1, expected2).when(sourceVisitor).visit(any(HTTPSource.class));
		result = new SourceUpdaterVisitor(sourceVisitor, connector, monitor, strategyConfig).visit(httpSource);
		assertEquals(expectedResult, result.getRawData());

		httpSource.setEntryConcatMethod(EntryConcatMethod.JSON_ARRAY);
		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		doReturn(sourceTable).when(hostMonitoring).getSourceTableByKey(VALUE_TABLE);
		doReturn(expected1, expected2).when(sourceVisitor).visit(any(HTTPSource.class));
		result = new SourceUpdaterVisitor(sourceVisitor, connector, monitor, strategyConfig).visit(httpSource);
		expectedResult = "expectedVal1,\n" +
				"expectedVal2";
		assertEquals(expectedResult, result.getRawData());

		httpSource.setEntryConcatMethod(EntryConcatMethod.JSON_ARRAY_EXTENDED);
		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		doReturn(sourceTable).when(hostMonitoring).getSourceTableByKey(VALUE_TABLE);
		doReturn(expected1, expected2).when(sourceVisitor).visit(any(HTTPSource.class));
		result = new SourceUpdaterVisitor(sourceVisitor, connector, monitor, strategyConfig).visit(httpSource);
		expectedResult = "{\n" +
				"\"Entry\":{\n" +
				"\"Full\":\"val1,val2,val3\",\n" +
				"\"Column(1)\":\"val1\",\n" +
				"\"Column(2)\":\"val2\",\n" +
				"\"Column(3)\":\"val3\",\n" +
				"\"Value\":expectedVal1\n" +
				"}\n" +
				"},\n" +
				"{\n" +
				"\"Entry\":{\n" +
				"\"Full\":\"a1,b1,c1\",\n" +
				"\"Column(1)\":\"a1\",\n" +
				"\"Column(2)\":\"b1\",\n" +
				"\"Column(3)\":\"c1\",\n" +
				"\"Value\":expectedVal2\n" +
				"}\n" +
				"}";

		assertEquals(expectedResult, result.getRawData());

		httpSource.setEntryConcatMethod(EntryConcatMethod.CUSTOM);
		httpSource.setEntryConcatStart("EntryConcatStart_");
		httpSource.setEntryConcatEnd("_EntryConcatEnd\n");
		doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
		doReturn(sourceTable).when(hostMonitoring).getSourceTableByKey(VALUE_TABLE);
		doReturn(expected1, expected2).when(sourceVisitor).visit(any(HTTPSource.class));
		result = new SourceUpdaterVisitor(sourceVisitor, connector, monitor, strategyConfig).visit(httpSource);
		expectedResult = "EntryConcatStart_expectedVal1_EntryConcatEnd\n" +
				"EntryConcatStart_expectedVal2_EntryConcatEnd\n";
		assertEquals(expectedResult, result.getRawData());
	}

	@Test
	void testReplaceDynamicEntry() {
		List<String> row = Arrays.asList("val1", "val2", "val3");
		var key1 = "/endpoint/%entry.column(1)%/%entry.column(2)%";
		assertEquals("/endpoint/val1/val2", SourceUpdaterVisitor.replaceDynamicEntry(key1, row));

		var key2 = "{\n"
				+ "    \"id\" : \"%entry.column(1)%\",\n"
				+ "    \"name\" : \"%entry.column(2)%\"\n"
				+ "}";
		var expected =  "{\n"
				+ "    \"id\" : \"val1\",\n"
				+ "    \"name\" : \"val2\"\n"
				+ "}";

		assertEquals(expected, SourceUpdaterVisitor.replaceDynamicEntry(key2, row));

		var key3 =  "%entry.column(1)%";
		assertEquals("val1", SourceUpdaterVisitor.replaceDynamicEntry(key3, row));
	}

	@Test
	void testVisitIPMI() {
		doReturn(SourceTable.empty()).when(sourceVisitor).visit(any(IPMI.class));
		assertEquals(SourceTable.empty(), new SourceUpdaterVisitor(sourceVisitor, connector, monitor, strategyConfig).visit(IPMI.builder().build()));
	}

	@Test
	void testVisitOSCommandSource() {
		doReturn(SourceTable.empty()).when(sourceVisitor).visit(any(OSCommandSource.class));
		assertEquals(SourceTable.empty(), new SourceUpdaterVisitor(sourceVisitor, connector, monitor, strategyConfig).visit(OSCommandSource.builder().commandLine("cmd --version").build()));
	}

	@Test
	void testVisitReferenceSource() {
		doReturn(SourceTable.empty()).when(sourceVisitor).visit(any(ReferenceSource.class));
		assertEquals(SourceTable.empty(), new SourceUpdaterVisitor(sourceVisitor, connector, monitor, strategyConfig).visit(ReferenceSource.builder().build()));

		ReferenceSource referenceSource = ReferenceSource.builder().reference(VALUE_TABLE).build();
		final List<List<String>> result = Collections.singletonList(Collections.singletonList(VALUE_VAL1));
		final SourceTable expected = SourceTable.builder().table(result).build();

		final SNMPGetSource snmpGetSource = SNMPGetSource.builder().oid("1.2.3.4.5.6.%Fan.Collect.DeviceID").build();
		doReturn(metadata).when(monitor).getMetadata();
		final List<List<String>> resultSnmp = Collections.singletonList(Collections.singletonList(VALUE_VAL1));
		final SourceTable expectedSnmp = SourceTable.builder().table(resultSnmp).build();
		doReturn(expectedSnmp).when(sourceVisitor).visit(any(SNMPGetSource.class));
		assertEquals(expectedSnmp, new SourceUpdaterVisitor(sourceVisitor, connector, monitor, strategyConfig).visit(snmpGetSource));

		doReturn(expected).when(sourceVisitor).visit(any(ReferenceSource.class));
		assertEquals(expected, new SourceUpdaterVisitor(sourceVisitor, connector, monitor, strategyConfig).visit(referenceSource));
	}

	@Test
	void testVisitStaticSource() {
		doReturn(SourceTable.empty()).when(sourceVisitor).visit(any(StaticSource.class));
		assertEquals(SourceTable.empty(), new SourceUpdaterVisitor(sourceVisitor, connector, monitor, strategyConfig).visit(StaticSource.builder().build()));

		StaticSource staticSource = StaticSource.builder().staticValue(VALUE_VAL1).build();
		final List<List<String>> resultTable = Collections.singletonList(Collections.singletonList(VALUE_VAL1));
		final SourceTable expected = SourceTable.builder().table(resultTable).build();
		doReturn(expected).when(sourceVisitor).visit(any(StaticSource.class));
		assertEquals(expected, new SourceUpdaterVisitor(sourceVisitor, connector, monitor, strategyConfig).visit(staticSource));
	}

	@Test
	void testVisitSNMPGetSource() {
		{
			doReturn(SourceTable.empty()).when(sourceVisitor).visit(any(SNMPGetSource.class));
			assertEquals(SourceTable.empty(), new SourceUpdaterVisitor(sourceVisitor, connector, null, strategyConfig).visit(SNMPGetSource.builder().oid("1.2.3.4.5.6").build()));
		}

		{

			final SNMPGetSource snmpGetSource = SNMPGetSource.builder().oid("1.2.3.4.5.6.%Fan.Collect.DeviceID").build();
			doReturn(metadata).when(monitor).getMetadata();
			final List<List<String>> result = Collections.singletonList(Collections.singletonList(VALUE_VAL1));
			final SourceTable expected = SourceTable.builder().table(result).build();
			doReturn(expected).when(sourceVisitor).visit(any(SNMPGetSource.class));
			assertEquals(expected, new SourceUpdaterVisitor(sourceVisitor, connector, monitor, strategyConfig).visit(snmpGetSource));
		}
	}

	@Test
	void testVisitSNMPGetTableSource() {
		{
			doReturn(SourceTable.empty()).when(sourceVisitor).visit(any(SNMPGetTableSource.class));
			assertEquals(SourceTable.empty(), new SourceUpdaterVisitor(sourceVisitor, connector, null, strategyConfig).visit(SNMPGetTableSource.builder().oid("1.2.3.4.5.6").build()));
		}

		{

			final SNMPGetTableSource snmpGetTableSource = SNMPGetTableSource.builder().oid("1.2.3.4.5.6.%Fan.Collect.DeviceID%").build();
			doReturn(metadata).when(monitor).getMetadata();
			final List<List<String>> result = Arrays.asList(Arrays.asList("val1, val2"), Arrays.asList("val3", "val4"));
			final SourceTable expected = SourceTable.builder().table(result).build();
			doReturn(expected).when(sourceVisitor).visit(any(SNMPGetTableSource.class));
			// Update the test when you implement SourceVisitor.visit(SNMPGetSource snmpGetSource)
			assertEquals(expected, new SourceUpdaterVisitor(sourceVisitor, connector, monitor, strategyConfig).visit(snmpGetTableSource));
		}
	}

	@Test
	void testVisitTableJoinSource() {
		doReturn(SourceTable.empty()).when(sourceVisitor).visit(any(TableJoinSource.class));
		assertEquals(SourceTable.empty(), new SourceUpdaterVisitor(sourceVisitor, connector, monitor, strategyConfig).visit(TableJoinSource.builder().build()));
	}

	@Test
	void testVisitTableUnionSource() {
		doReturn(SourceTable.empty()).when(sourceVisitor).visit(any(TableUnionSource.class));
		assertEquals(SourceTable.empty(), new SourceUpdaterVisitor(sourceVisitor, connector, monitor, strategyConfig).visit(TableUnionSource.builder().build()));
	}

	@Test
	void testVisitTelnetInteractiveSource() {
		doReturn(SourceTable.empty()).when(sourceVisitor).visit(any(TelnetInteractiveSource.class));
		assertEquals(SourceTable.empty(), new SourceUpdaterVisitor(sourceVisitor, connector, monitor, strategyConfig).visit(TelnetInteractiveSource.builder().build()));
	}

	@Test
	void testVisitUCSSource() {
		doReturn(SourceTable.empty()).when(sourceVisitor).visit(any(UCSSource.class));
		assertEquals(SourceTable.empty(), new SourceUpdaterVisitor(sourceVisitor, connector, monitor, strategyConfig).visit(UCSSource.builder().build()));
	}

	@Test
	void testVisitWBEMSource() {
		doReturn(SourceTable.empty()).when(sourceVisitor).visit(any(WBEMSource.class));
		assertEquals(SourceTable.empty(), new SourceUpdaterVisitor(sourceVisitor, connector, monitor, strategyConfig).visit(WBEMSource.builder().build()));
	}

	@Test
	void testVisitWMISource() {
		doReturn(SourceTable.empty()).when(sourceVisitor).visit(any(WMISource.class));
		assertEquals(SourceTable.empty(), new SourceUpdaterVisitor(sourceVisitor, connector, monitor, strategyConfig).visit(WMISource.builder().build()));
	}

	@Test
	void testReplaceDeviceIdInSNMPOid() {

		{
			// No monitor no replacement
			final SNMPGetTableSource snmpGetTableSource = buildSNMPGetTableSource("1.3.6.1.4.1.674.10893.1.20.140.1.1.4.%Enclosure.Collect.DeviceID%");
			sourceUpdaterVisitor.replaceDeviceIdInSNMPOid(snmpGetTableSource, null);

			assertEquals("1.3.6.1.4.1.674.10893.1.20.140.1.1.4.%Enclosure.Collect.DeviceID%", snmpGetTableSource.getOid());
		}

		{
			// No replacement
			doReturn(metadata).when(monitor).getMetadata();
			final SNMPGetTableSource snmpGetTableSource = buildSNMPGetTableSource("1.3.6.1.4.1.674.10893.1.20.140.1.1.4.");
			sourceUpdaterVisitor.replaceDeviceIdInSNMPOid(snmpGetTableSource, monitor);

			assertEquals("1.3.6.1.4.1.674.10893.1.20.140.1.1.4.", snmpGetTableSource.getOid());
		}

		{
			doReturn(metadata).when(monitor).getMetadata();
			final SNMPGetTableSource snmpGetTableSource = buildSNMPGetTableSource("1.3.6.%Enclosure.Collect.DeviceID%.4.1.674.10893.1.20.140.1.1.4.%Enclosure.Collect.DeviceID%");
			sourceUpdaterVisitor.replaceDeviceIdInSNMPOid(snmpGetTableSource, monitor);

			assertEquals("1.3.6."+ ENCLOSURE_DEVICE_ID +".4.1.674.10893.1.20.140.1.1.4." + ENCLOSURE_DEVICE_ID, snmpGetTableSource.getOid());
		}

	}

	@Test
	void testReplaceEmbeddedFilesInOsCommandLine() {

		{
			// No embedded file in the connector
			final OSCommandSource osCommandSource = buildOSCommandSource("pw system version %EmbeddedFile(1)% and %EmbeddedFile(2)%");
			doReturn(Collections.emptyMap()).when(connector).getEmbeddedFiles();
			sourceUpdaterVisitor.replaceEmbeddedFilesInOsCommandLine(osCommandSource, connector);

			assertEquals("pw system version %EmbeddedFile(1)% and %EmbeddedFile(2)%", osCommandSource.getCommandLine());
		}

		{
			// No replacements
			final OSCommandSource osCommandSource = buildOSCommandSource("pw system version");
			doReturn(Map.of(1, embeddedFile1, 2, embeddedFile2)).when(connector).getEmbeddedFiles();
			sourceUpdaterVisitor.replaceEmbeddedFilesInOsCommandLine(osCommandSource, connector);

			assertEquals("pw system version", osCommandSource.getCommandLine());
		}

		{
			final OSCommandSource osCommandSource = buildOSCommandSource("pw system version %EmbeddedFile(1)% and %EmbeddedFile(2)%");
			doReturn(Map.of(1, embeddedFile1, 2, embeddedFile2)).when(connector).getEmbeddedFiles();
			sourceUpdaterVisitor.replaceEmbeddedFilesInOsCommandLine(osCommandSource, connector);

			assertEquals("pw system version "+ EMBEDDED_FILE_CODE_CONTENT_1 + " and " + EMBEDDED_FILE_CODE_CONTENT_2, osCommandSource.getCommandLine());
		}

	}

	@Test
	void testGetValueFromForeignSource() {

		{ 
			assertNull(sourceUpdaterVisitor.getValueFromForeignSource(ENCLOSURE_DISCOVERY_SOURCE_2_KEY,
					null,
					AUTHENTICATION_TOKEN_FIELD));
			assertEquals("", sourceUpdaterVisitor.getValueFromForeignSource(ENCLOSURE_DISCOVERY_SOURCE_2_KEY,
					"",
					AUTHENTICATION_TOKEN_FIELD));
		}

		{
			// No foreign source table
			doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();

			String value = sourceUpdaterVisitor.getValueFromForeignSource(ENCLOSURE_DISCOVERY_SOURCE_2_KEY,
					ENCLOSURE_DISCOVERY_SOURCE_1_KEY,
					AUTHENTICATION_TOKEN_FIELD);
			assertNull(value);
		}

		{
			// Foreign source table empty
			doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
			doReturn(SourceTable.empty()).when(hostMonitoring).getSourceTableByKey(ENCLOSURE_DISCOVERY_SOURCE_1_KEY);

			String value = sourceUpdaterVisitor.getValueFromForeignSource(ENCLOSURE_DISCOVERY_SOURCE_2_KEY,
					ENCLOSURE_DISCOVERY_SOURCE_1_KEY,
					AUTHENTICATION_TOKEN_FIELD);
			assertNull(value);
		}

		{
			// Foreign source table not empty but null list table
			doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
			doReturn(SourceTable.builder().table(null).build()).when(hostMonitoring).getSourceTableByKey(ENCLOSURE_DISCOVERY_SOURCE_1_KEY);

			String value = sourceUpdaterVisitor.getValueFromForeignSource(ENCLOSURE_DISCOVERY_SOURCE_2_KEY,
					ENCLOSURE_DISCOVERY_SOURCE_1_KEY,
					AUTHENTICATION_TOKEN_FIELD);
			assertNull(value);
		}

		{
			// Foreign source table not empty but empty first line
			doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
			doReturn(SourceTable.builder().table(List.of(Collections.emptyList(), List.of("val1", "val2"))).build()).when(hostMonitoring).getSourceTableByKey(ENCLOSURE_DISCOVERY_SOURCE_1_KEY);

			String value = sourceUpdaterVisitor.getValueFromForeignSource(ENCLOSURE_DISCOVERY_SOURCE_2_KEY,
					ENCLOSURE_DISCOVERY_SOURCE_1_KEY,
					AUTHENTICATION_TOKEN_FIELD);
			assertNull(value);
		}

		{
			// Foreign source table not empty but null first line
			doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
			doReturn(SourceTable.builder().table(Arrays.asList((List<String>) null)).build()).when(hostMonitoring).getSourceTableByKey(ENCLOSURE_DISCOVERY_SOURCE_1_KEY);

			String value = sourceUpdaterVisitor.getValueFromForeignSource(ENCLOSURE_DISCOVERY_SOURCE_2_KEY,
					ENCLOSURE_DISCOVERY_SOURCE_1_KEY,
					AUTHENTICATION_TOKEN_FIELD);
			assertNull(value);
		}
	
		{
			// Foreign source table present via the list table
			doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
			doReturn(SourceTable.builder().table(List.of(List.of("token", "unwanted", "unwanted"))).build()).when(hostMonitoring).getSourceTableByKey(ENCLOSURE_DISCOVERY_SOURCE_1_KEY);

			String value = sourceUpdaterVisitor.getValueFromForeignSource(ENCLOSURE_DISCOVERY_SOURCE_2_KEY,
					ENCLOSURE_DISCOVERY_SOURCE_1_KEY,
					AUTHENTICATION_TOKEN_FIELD);
			assertEquals("token", value);
		}

		{
			// Foreign source table present via the raw data table
			doReturn(hostMonitoring).when(strategyConfig).getHostMonitoring();
			doReturn(SourceTable.builder().rawData("token;unwanted;unwanted;").build()).when(hostMonitoring).getSourceTableByKey(ENCLOSURE_DISCOVERY_SOURCE_1_KEY);

			String value = sourceUpdaterVisitor.getValueFromForeignSource(ENCLOSURE_DISCOVERY_SOURCE_2_KEY,
					ENCLOSURE_DISCOVERY_SOURCE_1_KEY,
					AUTHENTICATION_TOKEN_FIELD);
			assertEquals("token", value);
		}
	}

	private static OSCommandSource buildOSCommandSource(final String commandLine) {
		return OSCommandSource
				.builder()
				.commandLine(commandLine)
				.key(VALUE_TABLE)
				.computes(Collections.emptyList())
				.build();
	}

	private static SNMPGetTableSource buildSNMPGetTableSource(final String oid) {
		return SNMPGetTableSource
				.builder()
				.oid(oid)
				.key(VALUE_TABLE)
				.computes(Collections.emptyList())
				.build();
	}

}
