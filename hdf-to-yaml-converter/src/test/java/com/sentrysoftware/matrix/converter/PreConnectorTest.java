package com.sentrysoftware.matrix.converter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.OptionalInt;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.converter.PreConnector.ConnectorEntry;

class PreConnectorTest {

	private PreConnector preConnector;

	static final String DELL_OPEN_MANAGE_HDFS = "src/test/resources/hdf/MS_HW_DellOpenManage.hdfs";

	private static final String FOO = "FOO";
	private static final String BAR = "BAR";

	private static final int IS_BEFORE = -1;
	private static final int IS_EQUAL = 0;
	private static final int IS_AFTER = 1;

	@BeforeEach
	void setup() {

		preConnector = new PreConnector();
	}

	@Test
	void testLoad() {

		assertDoesNotThrow(() -> preConnector.load(DELL_OPEN_MANAGE_HDFS));

		// Testing processTranslationTables()

		Set<String> translationTableNames = new HashSet<>(
			Arrays.asList(
				"BatteryStatusInformationTranslationTable",
				"ProcessorStatusInformationTranslationTable",
				"EnvironmentStatusTranslationTable",
				"RACTypeTranslationTable",
				"GenericStatusTranslationTable",
				"GenericStatusInformationTranslationTable",
				"MemoryTypeTranslationTable",
				"cpuFamilyTranslationTable",
				"EnvironmentStatusInformationTranslationTable",
				"PowerSupplyTypeTranslationTable",
				"PowerSupplyStatusInformationTranslationTable"
			)
		);

		assertEquals(translationTableNames, preConnector.getTranslationTables().keySet());
		final Map<String, String> genericStatusTranslationTable = preConnector
			.getTranslationTables()
			.get("GenericStatusTranslationTable");
		assertNotNull(genericStatusTranslationTable);
		assertNotNull(genericStatusTranslationTable);
		assertEquals(5, genericStatusTranslationTable.size());
		assertEquals(
			Map.of(
				"3", "OK",
				"4", "WARN",
				"5", "ALARM",
				"6", "ALARM",
				"Default", "UNKNOWN"
			),
			genericStatusTranslationTable
		);
	}

	@Test
	void testGetProblemList() {

		assertEquals(0, preConnector.getProblemList().size());
	}

	@Test
	void testGet() {

		assertNull(preConnector.get(FOO));
	}

	@Test
	void testGetOrDefault() {

		assertEquals(BAR, preConnector.getOrDefault(FOO, BAR));
	}

	@Test
	void testPut() {

		assertNull(preConnector.get(FOO));
		preConnector.put(FOO, BAR);
		assertEquals(BAR, preConnector.get(FOO));
	}

	@Test
	void testEntrySet() {

		assertTrue(preConnector.entrySet().isEmpty());
	}

	@Test
	void testSortCodeMap() {

		assertNull(PreConnector.sortCodeMap(null));
		assertEquals(Collections.emptyMap(), PreConnector.sortCodeMap(Collections.emptyMap()));

		final Map<String, String> codeMap = new LinkedHashMap<>();
		codeMap.put("hdf.displayname", "HP StorageWorks EVA (SSSU) - Patrol Agent on Windows");
		codeMap.put("Detection.Criteria(2).Type", "TelnetInteractive");
		codeMap.put("Detection.Criteria(2).Step(1).Type", "Sleep");
		codeMap.put("Detection.Criteria(2).Step(1).Duration", "1");
		codeMap.put("Detection.Criteria(2).Step(2).TelnetOnly", "1");
		codeMap.put("Detection.Criteria(2).ForceSerialization", "1");
		codeMap.put("Detection.Criteria(2).Step(2).Type", "WaitFor");
		codeMap.put("Detection.Criteria(1).Type", "TelnetInteractive");
		codeMap.put("Detection.Criteria(1).Step(1).Type", "Sleep");
		codeMap.put("Detection.Criteria(1).Step(1).Duration", "1");
		codeMap.put("Detection.Criteria(1).Step(2).Type", "WaitFor");
		codeMap.put("Detection.Criteria(1).Step(2).TelnetOnly", "1");
		codeMap.put("Detection.Criteria(1).ForceSerialization", "1");
		codeMap.put("Enclosure.Collect.Source(1)", "%Enclosure.Discovery.Source(1)%");
		codeMap.put("Enclosure.Collect.ValueTable", "%Enclosure.Collect.Source(1)%");
		codeMap.put("Enclosure.Collect.Status", "ValueTable.Column(1)");
		codeMap.put("Enclosure.Discovery.Source(1).Compute(1).Type", "Awk");
		codeMap.put("Enclosure.Discovery.Source(1).Compute(1).AwkScript", "EmbeddedFile(2)");
		codeMap.put("Enclosure.Discovery.Source(1).Type", "OsCommand");
		codeMap.put("Enclosure.Discovery.Source(1).ExecuteLocally", "1");
		codeMap.put("Enclosure.Discovery.InstanceTable", "%Enclosure.Discovery.Source(1)%");
		codeMap.put("Enclosure.Discovery.Instance.DeviceID", "HPBladeChassis");
		codeMap.put("PhysicalDisk.Collect.Source(1).Type", "TelnetInteractive");
		codeMap.put("PhysicalDisk.Collect.Source(1).Step(1).Type", "Sleep");
		codeMap.put("PhysicalDisk.Collect.Type", "MonoInstance");
		codeMap.put("PhysicalDisk.Collect.Source(1).Step(1).Duration", "1");
		codeMap.put("PhysicalDisk.Collect.Source(1).Separators", ":");
		codeMap.put("PhysicalDisk.Collect.Source(2).Type", "TelnetInteractive");
		codeMap.put("PhysicalDisk.Collect.Source(2).Step(1).Type", "Sleep");
		codeMap.put("PhysicalDisk.Collect.Source(2).Step(1).Duration", "1");
		codeMap.put("PhysicalDisk.Collect.Source(2).Separators", ":");
		codeMap.put("PhysicalDisk.Collect.Source(1).Step(2).Type", "WaitFor");
		codeMap.put("PhysicalDisk.Collect.Source(1).Step(2).TelnetOnly", "1");
		codeMap.put("PhysicalDisk.Collect.Source(2).Step(2).Type", "WaitFor");
		codeMap.put("PhysicalDisk.Collect.Source(2).Step(2).TelnetOnly", "1");

		final Map<String, String> expected = new LinkedHashMap<>();
		expected.put("hdf.displayname", "HP StorageWorks EVA (SSSU) - Patrol Agent on Windows");
		expected.put("Detection.Criteria(1).Type", "TelnetInteractive");
		expected.put("Detection.Criteria(1).ForceSerialization", "1");
		expected.put("Detection.Criteria(1).Step(1).Type", "Sleep");
		expected.put("Detection.Criteria(1).Step(1).Duration", "1");
		expected.put("Detection.Criteria(1).Step(2).Type", "WaitFor");
		expected.put("Detection.Criteria(1).Step(2).TelnetOnly", "1");
		expected.put("Detection.Criteria(2).Type", "TelnetInteractive");
		expected.put("Detection.Criteria(2).ForceSerialization", "1");
		expected.put("Detection.Criteria(2).Step(1).Type", "Sleep");
		expected.put("Detection.Criteria(2).Step(1).Duration", "1");
		expected.put("Detection.Criteria(2).Step(2).Type", "WaitFor");
		expected.put("Detection.Criteria(2).Step(2).TelnetOnly", "1");
		expected.put("Enclosure.Discovery.Source(1).Type", "OsCommand");
		expected.put("Enclosure.Discovery.Source(1).ExecuteLocally", "1");
		expected.put("Enclosure.Discovery.Source(1).Compute(1).Type", "Awk");
		expected.put("Enclosure.Discovery.Source(1).Compute(1).AwkScript", "EmbeddedFile(2)");
		expected.put("Enclosure.Discovery.InstanceTable", "%Enclosure.Discovery.Source(1)%");
		expected.put("Enclosure.Discovery.Instance.DeviceID", "HPBladeChassis");
		expected.put("Enclosure.Collect.Source(1)", "%Enclosure.Discovery.Source(1)%");
		expected.put("Enclosure.Collect.ValueTable", "%Enclosure.Collect.Source(1)%");
		expected.put("Enclosure.Collect.Status", "ValueTable.Column(1)");
		expected.put("PhysicalDisk.Collect.Type", "MonoInstance");
		expected.put("PhysicalDisk.Collect.Source(1).Type", "TelnetInteractive");
		expected.put("PhysicalDisk.Collect.Source(1).Separators", ":");
		expected.put("PhysicalDisk.Collect.Source(1).Step(1).Type", "Sleep");
		expected.put("PhysicalDisk.Collect.Source(1).Step(1).Duration", "1");
		expected.put("PhysicalDisk.Collect.Source(1).Step(2).Type", "WaitFor");
		expected.put("PhysicalDisk.Collect.Source(1).Step(2).TelnetOnly", "1");
		expected.put("PhysicalDisk.Collect.Source(2).Type", "TelnetInteractive");
		expected.put("PhysicalDisk.Collect.Source(2).Separators", ":");
		expected.put("PhysicalDisk.Collect.Source(2).Step(1).Type", "Sleep");
		expected.put("PhysicalDisk.Collect.Source(2).Step(1).Duration", "1");
		expected.put("PhysicalDisk.Collect.Source(2).Step(2).Type", "WaitFor");
		expected.put("PhysicalDisk.Collect.Source(2).Step(2).TelnetOnly", "1");

		assertEquals(expected, PreConnector.sortCodeMap(codeMap));
	}

	@Test
	void testCompareCodeMap() {

		// check connector entries null
		assertEquals(IS_EQUAL, PreConnector.compareCodeMap(null, null));
		assertEquals(IS_BEFORE, PreConnector.compareCodeMap(new ConnectorEntry(0, "hdf.displayname", "test"), null));
		assertEquals(IS_AFTER, PreConnector.compareCodeMap(null, new ConnectorEntry(0, "hdf.displayname", "test")));

		// compare hdf
		assertEquals(IS_BEFORE,
			PreConnector.compareCodeMap(
				new ConnectorEntry(0, "hdf.DisplayName", "HP StorageWorks EVA (SSSU) - Patrol Agent on Windows"),
				new ConnectorEntry(1, "hdf.TypicalPlatform", "HP StorageWorks EVA")
			)
		);

		assertEquals(IS_AFTER,
			PreConnector.compareCodeMap(
				new ConnectorEntry(1, "hdf.TypicalPlatform", "HP StorageWorks EVA"),
				new ConnectorEntry(0, "hdf.DisplayName", "HP StorageWorks EVA (SSSU) - Patrol Agent on Windows")
			)
		);

		assertEquals(IS_BEFORE,
			PreConnector.compareCodeMap(
				new ConnectorEntry(0, "hdf.DisplayName", "HP StorageWorks EVA (SSSU) - Patrol Agent on Windows"),
				new ConnectorEntry(0, "detection.criteria(1).type", "Process")
			)
		);

		assertEquals(IS_AFTER,
			PreConnector.compareCodeMap(
				new ConnectorEntry(0, "Enclosure.Discovery.Source(1).Type", "TelnetInteractive"),
				new ConnectorEntry(0, "hdf.DisplayName", "HP StorageWorks EVA (SSSU) - Patrol Agent on Windows")
			)
		);
	}

	@Test
	void testCompareDetectionConnectorEntries() {

		// check if Detection
		assertEquals(
			OptionalInt.empty(), 
			PreConnector.compareDetectionConnectorEntries(
				new ConnectorEntry(0, "hdf.DisplayName", "HP StorageWorks EVA (SSSU) - Patrol Agent on Windows"),
				new ConnectorEntry(0, "hdf.TypicalPlatform", "HP StorageWorks EVA")
			)
		);

		assertEquals(
			OptionalInt.of(IS_BEFORE), 
			PreConnector.compareDetectionConnectorEntries(
				new ConnectorEntry(0, "detection.criteria(1).type", "OS"),
				new ConnectorEntry(0, "hdf.TypicalPlatform", "HP StorageWorks EVA")
			)
		);

		assertEquals(
			OptionalInt.of(IS_AFTER), 
			PreConnector.compareDetectionConnectorEntries(
				new ConnectorEntry(0, "hdf.DisplayName", "HP StorageWorks EVA (SSSU) - Patrol Agent on Windows"),
				new ConnectorEntry(0, "detection.criteria(1).type", "OS")
			)
		);

		// compare criteria index first
		assertEquals(
			OptionalInt.of(IS_BEFORE), 
			PreConnector.compareDetectionConnectorEntries(
				new ConnectorEntry(0, "detection.criteria(1).type", "OS"),
				new ConnectorEntry(0, "Detection.Criteria(2).Type", "TelnetInteractive")
			)
		);

		assertEquals(
			OptionalInt.of(IS_AFTER), 
			PreConnector.compareDetectionConnectorEntries(
				new ConnectorEntry(0, "Detection.Criteria(2).Type", "TelnetInteractive"),
				new ConnectorEntry(0, "detection.criteria(1).type", "OS")
			)
		);

		// compare criteria type before
		assertEquals(
			OptionalInt.of(IS_BEFORE), 
			PreConnector.compareDetectionConnectorEntries(
				new ConnectorEntry(0, "detection.criteria(1).type", "OSCommand"),
				new ConnectorEntry(0, "detection.criteria(1).CommandLine", "echo %OS%")
			)
		);

		assertEquals(
			OptionalInt.of(IS_AFTER), 
			PreConnector.compareDetectionConnectorEntries(
				new ConnectorEntry(0, "detection.criteria(1).CommandLine", "echo %OS%"),
				new ConnectorEntry(0, "detection.criteria(1).type", "OSCommand")
			)
		);

		// compare parameter values
		assertEquals(
			OptionalInt.of(IS_BEFORE), 
			PreConnector.compareDetectionConnectorEntries(
				new ConnectorEntry(0, "detection.criteria(1).CommandLine", "echo %OS%"),
				new ConnectorEntry(0, "detection.criteria(2).CommandLine", "echo %OS%")
			)
		);

		assertEquals(
			OptionalInt.of(IS_AFTER), 
			PreConnector.compareDetectionConnectorEntries(
				new ConnectorEntry(0, "detection.criteria(2).CommandLine", "echo %OS%"),
				new ConnectorEntry(0, "detection.criteria(1).CommandLine", "echo %OS%")
			)
		);

		assertEquals(
			OptionalInt.of(IS_BEFORE), 
			PreConnector.compareDetectionConnectorEntries(
				new ConnectorEntry(2, "detection.criteria(1).CommandLine", "echo %OS%"),
				new ConnectorEntry(3, "detection.criteria(1).Timeout", "30")
			)
		);

		assertEquals(
			OptionalInt.of(IS_AFTER), 
			PreConnector.compareDetectionConnectorEntries(
				new ConnectorEntry(3, "detection.criteria(1).Timeout", "30"),
				new ConnectorEntry(2, "detection.criteria(1).CommandLine", "echo %OS%")
			)
		);

		// compare steps
		assertEquals(
			OptionalInt.of(IS_BEFORE), 
			PreConnector.compareDetectionConnectorEntries(
				new ConnectorEntry(0, "detection.criteria(1).type", "TelnetInteractive"),
				new ConnectorEntry(0, "Detection.Criteria(1).Step(1).Type", "Sleep")
			)
		);

		assertEquals(
			OptionalInt.of(IS_AFTER), 
			PreConnector.compareDetectionConnectorEntries(
				new ConnectorEntry(0, "Detection.Criteria(1).Step(1).Type", "Sleep"),
				new ConnectorEntry(0, "detection.criteria(1).type", "TelnetInteractive")
			)
		);

		assertEquals(
			OptionalInt.of(IS_BEFORE), 
			PreConnector.compareDetectionConnectorEntries(
				new ConnectorEntry(0, "detection.criteria(1).ExpectedResult", "HP.* BladeSystem Onboard Administrator"),
				new ConnectorEntry(0, "Detection.Criteria(1).Step(1).Type", "Sleep")
			)
		);

		assertEquals(
			OptionalInt.of(IS_AFTER), 
			PreConnector.compareDetectionConnectorEntries(
				new ConnectorEntry(0, "Detection.Criteria(1).Step(1).Type", "Sleep"),
				new ConnectorEntry(0, "detection.criteria(1).ExpectedResult", "HP.* BladeSystem Onboard Administrator")
			)
		);

		assertEquals(
			OptionalInt.of(IS_BEFORE), 
			PreConnector.compareDetectionConnectorEntries(
				new ConnectorEntry(0, "Detection.Criteria(1).Step(1).Type", "Sleep"),
				new ConnectorEntry(0, "Detection.Criteria(1).Step(2).Type", "Sleep")
			)
		);

		assertEquals(
			OptionalInt.of(IS_AFTER),
			PreConnector.compareDetectionConnectorEntries(
				new ConnectorEntry(0, "Detection.Criteria(1).Step(2).Type", "Sleep"),
				new ConnectorEntry(0, "Detection.Criteria(1).Step(1).Type", "Sleep")
			)
		);

		assertEquals(
			OptionalInt.of(IS_BEFORE), 
			PreConnector.compareDetectionConnectorEntries(
				new ConnectorEntry(0, "Detection.Criteria(1).Step(1).Type", "Sleep"),
				new ConnectorEntry(0, "Detection.Criteria(1).Step(1).Duration", "1")
			)
		);

		assertEquals(
			OptionalInt.of(IS_AFTER), 
			PreConnector.compareDetectionConnectorEntries(
				new ConnectorEntry(0, "Detection.Criteria(1).Step(1).Duration", "1"),
				new ConnectorEntry(0, "Detection.Criteria(1).Step(1).Type", "Sleep")
			)
		);

		assertEquals(
			OptionalInt.of(IS_BEFORE), 
			PreConnector.compareDetectionConnectorEntries(
				new ConnectorEntry(0, "Detection.Criteria(1).Step(1).Duration", "1"),
				new ConnectorEntry(0, "Detection.Criteria(1).Step(2).Duration", "1")
			)
		);

		assertEquals(
			OptionalInt.of(IS_AFTER),
			PreConnector.compareDetectionConnectorEntries(
				new ConnectorEntry(0, "Detection.Criteria(1).Step(2).Duration", "1"),
				new ConnectorEntry(0, "Detection.Criteria(1).Step(1).Duration", "1")
			)
		);

		assertEquals(
			OptionalInt.of(IS_BEFORE), 
			PreConnector.compareDetectionConnectorEntries(
				new ConnectorEntry(1, "Detection.Criteria(1).Step(1).TelnetOnly", "1"),
				new ConnectorEntry(2, "Detection.Criteria(1).Step(1).TimeOut", "20")
			)
		);

		assertEquals(
			OptionalInt.of(IS_AFTER),
			PreConnector.compareDetectionConnectorEntries(
				new ConnectorEntry(2, "Detection.Criteria(1).Step(1).TimeOut", "20"),
				new ConnectorEntry(1, "Detection.Criteria(1).Step(1).TelnetOnly", "1")
			)
		);
	}

	@Test
	void testCompareSourceConnectorEntries() { // NOSONAR

		// compare Discovery before Collect
		assertEquals(IS_BEFORE,
			PreConnector.compareCodeMap(
				new ConnectorEntry(0, "Enclosure.Discovery.Source(1).Type", "TelnetInteractive"),
				new ConnectorEntry(0, "Battery.Collect.Source(1).Type", "TelnetInteractive")
			)
		);

		assertEquals(IS_AFTER,
			PreConnector.compareCodeMap(
				new ConnectorEntry(0, "Battery.Collect.Source(1).Type", "TelnetInteractive"),
				new ConnectorEntry(0, "Enclosure.Discovery.Source(1).Type", "TelnetInteractive")
			)
		);

		// compare other sources like instances
		assertEquals(
			IS_BEFORE, 
			PreConnector.compareSourceConnectorEntries(
				new ConnectorEntry(10, "Enclosure.Collect.ValueTable", "%Enclosure.Collect.Source(1)%"),
				new ConnectorEntry(15, "Blade.Collect.Type", "MultiInstance")
			)
		);

		assertEquals(
			IS_AFTER,
			PreConnector.compareSourceConnectorEntries(
				new ConnectorEntry(15, "Blade.Collect.Type", "MultiInstance"),
				new ConnectorEntry(10, "Enclosure.Collect.ValueTable", "%Enclosure.Collect.Source(1)%")
			)
		);

		// compare instance type before source
		assertEquals(
			IS_BEFORE, 
			PreConnector.compareSourceConnectorEntries(
				new ConnectorEntry(0, "Blade.Collect.Type", "MultiInstance"),
				new ConnectorEntry(0, "Blade.Collect.Source(1).Type", "HTTP")
			)
		);

		assertEquals(
			IS_AFTER,
			PreConnector.compareSourceConnectorEntries(
				new ConnectorEntry(0, "Blade.Collect.Source(1).Type", "HTTP"),
				new ConnectorEntry(0, "Blade.Collect.Type", "MultiInstance")
			)
		);

		// compare value  after source
		assertEquals(
			IS_BEFORE, 
			PreConnector.compareSourceConnectorEntries(
				new ConnectorEntry(0, "Blade.Collect.Source(1).Type", "HTTP"),
				new ConnectorEntry(0, "Blade.Collect.ValueTable", "%Enclosure.Collect.Source(1)%")
			)
		);

		assertEquals(
			IS_AFTER,
			PreConnector.compareSourceConnectorEntries(
				new ConnectorEntry(0, "Blade.Collect.ValueTable", "%Enclosure.Collect.Source(1)%"),
				new ConnectorEntry(0, "Blade.Collect.Source(1).Type", "HTTP")
			)
		);

		// compare source

		// compare source index
		assertEquals(
			IS_BEFORE, 
			PreConnector.compareSourceConnectorEntries(
				new ConnectorEntry(0, "PowerSupply.Collect.Source(1).Type", "HTTP"),
				new ConnectorEntry(0, "PowerSupply.Collect.Source(2).Type", "HTTP")
			)
		);

		assertEquals(
			IS_AFTER,
			PreConnector.compareSourceConnectorEntries(
				new ConnectorEntry(0, "PowerSupply.Collect.Source(2).Type", "HTTP"),
				new ConnectorEntry(0, "PowerSupply.Collect.Source(1).Type", "HTTP")
			)
		);

		assertEquals(
			IS_BEFORE, 
			PreConnector.compareSourceConnectorEntries(
				new ConnectorEntry(0, "PowerSupply.Collect.Source(1).Method", "GET"),
				new ConnectorEntry(0, "PowerSupply.Collect.Source(2).Method", "GET")
			)
		);

		assertEquals(
			IS_AFTER,
			PreConnector.compareSourceConnectorEntries(
				new ConnectorEntry(0, "PowerSupply.Collect.Source(2).Method", "GET"),
				new ConnectorEntry(0, "PowerSupply.Collect.Source(1).Method", "GET")
			)
		);

		assertEquals(
			IS_BEFORE, 
			PreConnector.compareSourceConnectorEntries(
				new ConnectorEntry(0, "Enclosure.Collect.Source(1).Step(3).Type", "Sleep"),
				new ConnectorEntry(0, "Enclosure.Collect.Source(2).Step(1).Type", "Sleep")
			)
		);

		assertEquals(
			IS_AFTER,
			PreConnector.compareSourceConnectorEntries(
				new ConnectorEntry(0, "Enclosure.Collect.Source(2).Step(1).Type", "Sleep"),
				new ConnectorEntry(0, "Enclosure.Collect.Source(1).Step(3).Type", "Sleep")
			)
		);

		assertEquals(
			IS_BEFORE, 
			PreConnector.compareSourceConnectorEntries(
				new ConnectorEntry(0, "Enclosure.Collect.Source(1).Compute(3).Type", "Translate"),
				new ConnectorEntry(0, "Enclosure.Collect.Source(2).Compute(1).Type", "Translate")
			)
		);

		assertEquals(
			IS_AFTER,
			PreConnector.compareSourceConnectorEntries(
				new ConnectorEntry(0, "Enclosure.Collect.Source(2).Compute(1).Type", "Translate"),
				new ConnectorEntry(0, "Enclosure.Collect.Source(1).Compute(3).Type", "Translate")
			)
		);

		// compare source reference over parameter
		assertEquals(
			IS_BEFORE, 
			PreConnector.compareSourceConnectorEntries(
				new ConnectorEntry(0, "PowerSupply.Collect.Source(1)", "%Enclosure.Collect.Source(1)%"),
				new ConnectorEntry(0, "PowerSupply.Collect.Source(1).Method", "GET")
			)
		);

		assertEquals(
			IS_AFTER,
			PreConnector.compareSourceConnectorEntries(
				new ConnectorEntry(0, "PowerSupply.Collect.Source(1).Method", "GET"),
				new ConnectorEntry(0, "PowerSupply.Collect.Source(1)", "%Enclosure.Collect.Source(1)%")
			)
		);

		// compare source reference over type
		assertEquals(
			IS_BEFORE, 
			PreConnector.compareSourceConnectorEntries(
				new ConnectorEntry(0, "PowerSupply.Collect.Source(1)", "%Enclosure.Collect.Source(1)%"),
				new ConnectorEntry(0, "PowerSupply.Collect.Source(1).Type", "HTTP")
			)
		);

		assertEquals(
			IS_AFTER,
			PreConnector.compareSourceConnectorEntries(
				new ConnectorEntry(0, "PowerSupply.Collect.Source(1).Type", "HTTP"),
				new ConnectorEntry(0, "PowerSupply.Collect.Source(1)", "%Enclosure.Collect.Source(1)%")
			)
		);

		// compare source type over parameter
		assertEquals(
			IS_BEFORE, 
			PreConnector.compareSourceConnectorEntries(
				new ConnectorEntry(0, "PowerSupply.Collect.Source(1).Type", "HTTP"),
				new ConnectorEntry(0, "PowerSupply.Collect.Source(1).Method", "GET")
			)
		);

		assertEquals(
			IS_AFTER,
			PreConnector.compareSourceConnectorEntries(
				new ConnectorEntry(0, "PowerSupply.Collect.Source(1).Method", "GET"),
				new ConnectorEntry(0, "PowerSupply.Collect.Source(1).Type", "HTTP")
			)
		);

		// compare source parameters
		assertEquals(
			IS_BEFORE, 
			PreConnector.compareSourceConnectorEntries(
				new ConnectorEntry(2, "PowerSupply.Collect.Source(1).Method", "GET"),
				new ConnectorEntry(4, "PowerSupply.Collect.Source(1).ResultContent", "Body")
			)
		);

		assertEquals(
			IS_AFTER,
			PreConnector.compareSourceConnectorEntries(
				new ConnectorEntry(4, "PowerSupply.Collect.Source(1).ResultContent", "Body"),
				new ConnectorEntry(2, "PowerSupply.Collect.Source(1).Method", "GET")
			)
		);

		// compare source over step
		assertEquals(
			IS_BEFORE, 
			PreConnector.compareSourceConnectorEntries(
				new ConnectorEntry(0, "Enclosure.Collect.Source(1).Timeout", "1"),
				new ConnectorEntry(0, "Enclosure.Collect.Source(1).Step(1).Type", "Sleep")
			)
		);

		assertEquals(
			IS_AFTER,
			PreConnector.compareSourceConnectorEntries(
				new ConnectorEntry(0, "Enclosure.Collect.Source(1).Step(1).Type", "Sleep"),
				new ConnectorEntry(0, "Enclosure.Collect.Source(1).Timeout", "1")
			)
		);

		// compare source over compute
		assertEquals(
			IS_BEFORE, 
			PreConnector.compareSourceConnectorEntries(
				new ConnectorEntry(0, "Enclosure.Collect.Source(1).Timeout", "1"),
				new ConnectorEntry(0, "Enclosure.Collect.Source(1).Compute(1).Type", "Awk")
			)
		);

		assertEquals(
			IS_AFTER,
			PreConnector.compareSourceConnectorEntries(
				new ConnectorEntry(0, "Enclosure.Collect.Source(1).Compute(1).Type", "Awk"),
				new ConnectorEntry(0, "Enclosure.Collect.Source(1).Timeout", "1")
			)
		);

		// compare steps

		// compare step index
		assertEquals(
			IS_BEFORE, 
			PreConnector.compareSourceConnectorEntries(
				new ConnectorEntry(0, "Enclosure.Collect.Source(1).Step(1).Type", "Sleep"),
				new ConnectorEntry(0, "Enclosure.Collect.Source(1).Step(2).Type", "Sleep")
			)
		);

		assertEquals(
			IS_AFTER,
			PreConnector.compareSourceConnectorEntries(
				new ConnectorEntry(0, "Enclosure.Collect.Source(1).Step(2).Type", "Sleep"),
				new ConnectorEntry(0, "Enclosure.Collect.Source(1).Step(1).Type", "Sleep")
			)
		);

		assertEquals(
			IS_BEFORE, 
			PreConnector.compareSourceConnectorEntries(
				new ConnectorEntry(0, "Enclosure.Collect.Source(1).Step(2).Type", "Sleep"),
				new ConnectorEntry(0, "Enclosure.Collect.Source(1).Compute(1).Type", "Translate")
			)
		);

		assertEquals(
			IS_AFTER,
			PreConnector.compareSourceConnectorEntries(
				new ConnectorEntry(0, "Enclosure.Collect.Source(1).Compute(1).Type", "Translate"),
				new ConnectorEntry(0, "Enclosure.Collect.Source(1).Step(2).Type", "Sleep")
			)
		);

		// compare step type
		assertEquals(
			IS_BEFORE, 
			PreConnector.compareSourceConnectorEntries(
				new ConnectorEntry(0, "Enclosure.Collect.Source(1).Step(1).Type", "Sleep"),
				new ConnectorEntry(0, "Enclosure.Collect.Source(1).Step(1).Duration", "1")
			)
		);

		assertEquals(
			IS_AFTER,
			PreConnector.compareSourceConnectorEntries(
				new ConnectorEntry(0, "Enclosure.Collect.Source(1).Step(1).Duration", "1"),
				new ConnectorEntry(0, "Enclosure.Collect.Source(1).Step(1).Type", "Sleep")
			)
		);

		// compare steps parameters
		assertEquals(
			IS_BEFORE, 
			PreConnector.compareSourceConnectorEntries(
				new ConnectorEntry(10, "Enclosure.Collect.Source(1).Step(1).TimeOut", "1"),
				new ConnectorEntry(11, "Enclosure.Collect.Source(1).Step(1).TelnetOnly", "1")
			)
		);

		assertEquals(
			IS_AFTER,
			PreConnector.compareSourceConnectorEntries(
				new ConnectorEntry(11, "Enclosure.Collect.Source(1).Step(1).TelnetOnly", "1"),
				new ConnectorEntry(10, "Enclosure.Collect.Source(1).Step(1).TimeOut", "1")
			)
		);

		// compare step over compute
		assertEquals(
			IS_BEFORE, 
			PreConnector.compareSourceConnectorEntries(
				new ConnectorEntry(0, "Enclosure.Collect.Source(1).Step(2).Text", "ogin:"),
				new ConnectorEntry(0, "Enclosure.Collect.Source(1).Compute(1).Type", "Awk")
			)
		);

		assertEquals(
			IS_AFTER,
			PreConnector.compareSourceConnectorEntries(
				new ConnectorEntry(0, "Enclosure.Collect.Source(1).Compute(1).Type", "Awk"),
				new ConnectorEntry(0, "Enclosure.Collect.Source(1).Step(2).Text", "ogin:")
			)
		);

		// compare compute

		// compare compute index
		assertEquals(
			IS_BEFORE, 
			PreConnector.compareSourceConnectorEntries(
				new ConnectorEntry(0, "Enclosure.Collect.Source(1).Compute(1).Type", "Awk"),
				new ConnectorEntry(0, "Enclosure.Collect.Source(1).Compute(2).Type", "Awk")
			)
		);

		assertEquals(
			IS_AFTER,
			PreConnector.compareSourceConnectorEntries(
				new ConnectorEntry(0, "Enclosure.Collect.Source(1).Compute(2).Type", "Awk"),
				new ConnectorEntry(0, "Enclosure.Collect.Source(1).Compute(1).Type", "Awk")
			)
		);

		// compare compute type over parameter
		assertEquals(
			IS_BEFORE, 
			PreConnector.compareSourceConnectorEntries(
				new ConnectorEntry(0, "Enclosure.Collect.Source(1).Compute(1).Type", "Awk"),
				new ConnectorEntry(0, "Enclosure.Collect.Source(1).Compute(1).Separators", ":")
			)
		);

		assertEquals(
			IS_AFTER,
			PreConnector.compareSourceConnectorEntries(
				new ConnectorEntry(0, "Enclosure.Collect.Source(1).Compute(1).Separators", ":"),
				new ConnectorEntry(0, "Enclosure.Collect.Source(1).Compute(1).Type", "Awk")
			)
		);

		// compare compute parameters
		assertEquals(
			IS_BEFORE, 
			PreConnector.compareSourceConnectorEntries(
				new ConnectorEntry(11, "Enclosure.Collect.Source(1).Compute(1).KeepOnlyRegExp", "MSHW;"),
				new ConnectorEntry(12, "Enclosure.Collect.Source(1).Compute(1).Separators", ":")
			)
		);

		assertEquals(
			IS_AFTER,
			PreConnector.compareSourceConnectorEntries(
				new ConnectorEntry(12, "Enclosure.Collect.Source(1).Compute(1).Separators", ":"),
				new ConnectorEntry(11, "Enclosure.Collect.Source(1).Compute(1).KeepOnlyRegExp", "MSHW;")
			)
		);
	}

	@Test
	void testConnectorEntry() { // NOSONAR
		{
			final ConnectorEntry connectorEntry = new ConnectorEntry(0, "hdf.displayname", "test");
			assertEquals(0, connectorEntry.getOriginalIndex());
			assertEquals("hdf.displayname", connectorEntry.getKey());
			assertEquals("test", connectorEntry.getValue());
			assertEquals("hdf", connectorEntry.extractEntryType());
			assertTrue(connectorEntry.hasHdf());
			assertFalse(connectorEntry.hasType());
			assertFalse(connectorEntry.hasDiscovery());
			assertFalse(connectorEntry.hasCollect());
			assertFalse(connectorEntry.hasSourceReference());
			assertEquals(OptionalInt.empty(), connectorEntry.extractDetectionIndex());
			assertEquals(OptionalInt.empty(), connectorEntry.extractDetectionStepIndex());
			assertEquals(OptionalInt.empty(), connectorEntry.extractSourceIndex());
			assertEquals(OptionalInt.empty(), connectorEntry.extractSourceStepIndex());
			assertEquals(OptionalInt.empty(), connectorEntry.extractComputeIndex());
		}
		{
			final ConnectorEntry connectorEntry = new ConnectorEntry(8, "Detection.Criteria(1).Type", "TelnetInteractive");
			assertEquals(8, connectorEntry.getOriginalIndex());
			assertEquals("Detection.Criteria(1).Type", connectorEntry.getKey());
			assertEquals("TelnetInteractive", connectorEntry.getValue());
			assertEquals("detection", connectorEntry.extractEntryType());
			assertFalse(connectorEntry.hasHdf());
			assertTrue(connectorEntry.hasType());
			assertFalse(connectorEntry.hasDiscovery());
			assertFalse(connectorEntry.hasCollect());
			assertFalse(connectorEntry.hasSourceReference());
			assertEquals(OptionalInt.of(1), connectorEntry.extractDetectionIndex());
			assertEquals(OptionalInt.empty(), connectorEntry.extractDetectionStepIndex());
			assertEquals(OptionalInt.empty(), connectorEntry.extractSourceIndex());
			assertEquals(OptionalInt.empty(), connectorEntry.extractSourceStepIndex());
			assertEquals(OptionalInt.empty(), connectorEntry.extractComputeIndex());
		}
		{
			final ConnectorEntry connectorEntry = new ConnectorEntry(9, "Detection.Criteria(1).ForceSerialization", "1");
			assertEquals(9, connectorEntry.getOriginalIndex());
			assertEquals("Detection.Criteria(1).ForceSerialization", connectorEntry.getKey());
			assertEquals("1", connectorEntry.getValue());
			assertEquals("detection", connectorEntry.extractEntryType());
			assertFalse(connectorEntry.hasHdf());
			assertFalse(connectorEntry.hasType());
			assertFalse(connectorEntry.hasDiscovery());
			assertFalse(connectorEntry.hasCollect());
			assertFalse(connectorEntry.hasSourceReference());
			assertEquals(OptionalInt.of(1), connectorEntry.extractDetectionIndex());
			assertEquals(OptionalInt.empty(), connectorEntry.extractDetectionStepIndex());
			assertEquals(OptionalInt.empty(), connectorEntry.extractSourceIndex());
			assertEquals(OptionalInt.empty(), connectorEntry.extractSourceStepIndex());
			assertEquals(OptionalInt.empty(), connectorEntry.extractComputeIndex());
		}
		{
			final ConnectorEntry connectorEntry = new ConnectorEntry(11, "Detection.Criteria(1).Step(2).Type", "WaitFor");
			assertEquals(11, connectorEntry.getOriginalIndex());
			assertEquals("Detection.Criteria(1).Step(2).Type", connectorEntry.getKey());
			assertEquals("WaitFor", connectorEntry.getValue());
			assertEquals("detection", connectorEntry.extractEntryType());
			assertFalse(connectorEntry.hasHdf());
			assertTrue(connectorEntry.hasType());
			assertFalse(connectorEntry.hasDiscovery());
			assertFalse(connectorEntry.hasCollect());
			assertFalse(connectorEntry.hasSourceReference());
			assertEquals(OptionalInt.of(1), connectorEntry.extractDetectionIndex());
			assertEquals(OptionalInt.of(2), connectorEntry.extractDetectionStepIndex());
			assertEquals(OptionalInt.empty(), connectorEntry.extractSourceIndex());
			assertEquals(OptionalInt.empty(), connectorEntry.extractSourceStepIndex());
			assertEquals(OptionalInt.empty(), connectorEntry.extractComputeIndex());
		}
		{
			final ConnectorEntry connectorEntry = new ConnectorEntry(12, "Detection.Criteria(1).Step(2).TelnetOnly", "1");
			assertEquals(12, connectorEntry.getOriginalIndex());
			assertEquals("Detection.Criteria(1).Step(2).TelnetOnly", connectorEntry.getKey());
			assertEquals("1", connectorEntry.getValue());
			assertEquals("detection", connectorEntry.extractEntryType());
			assertFalse(connectorEntry.hasHdf());
			assertFalse(connectorEntry.hasType());
			assertFalse(connectorEntry.hasDiscovery());
			assertFalse(connectorEntry.hasCollect());
			assertFalse(connectorEntry.hasSourceReference());
			assertEquals(OptionalInt.of(1), connectorEntry.extractDetectionIndex());
			assertEquals(OptionalInt.of(2), connectorEntry.extractDetectionStepIndex());
			assertEquals(OptionalInt.empty(), connectorEntry.extractSourceIndex());
			assertEquals(OptionalInt.empty(), connectorEntry.extractSourceStepIndex());
			assertEquals(OptionalInt.empty(), connectorEntry.extractComputeIndex());
		}
		{
			final ConnectorEntry connectorEntry = new ConnectorEntry(31, "LogicalDisk.Collect.Source(1).Type", "OsCommand");
			assertEquals(31, connectorEntry.getOriginalIndex());
			assertEquals("LogicalDisk.Collect.Source(1).Type", connectorEntry.getKey());
			assertEquals("OsCommand", connectorEntry.getValue());
			assertEquals("logicaldisk", connectorEntry.extractEntryType());
			assertFalse(connectorEntry.hasHdf());
			assertTrue(connectorEntry.hasType());
			assertFalse(connectorEntry.hasDiscovery());
			assertTrue(connectorEntry.hasCollect());
			assertFalse(connectorEntry.hasSourceReference());
			assertEquals(OptionalInt.empty(), connectorEntry.extractDetectionIndex());
			assertEquals(OptionalInt.empty(), connectorEntry.extractDetectionStepIndex());
			assertEquals(OptionalInt.of(1), connectorEntry.extractSourceIndex());
			assertEquals(OptionalInt.empty(), connectorEntry.extractSourceStepIndex());
			assertEquals(OptionalInt.empty(), connectorEntry.extractComputeIndex());
		}
		{
			final ConnectorEntry connectorEntry = new ConnectorEntry(31, "LogicalDisk.Collect.Source(1).Type", "OsCommand");
			assertEquals(31, connectorEntry.getOriginalIndex());
			assertEquals("LogicalDisk.Collect.Source(1).Type", connectorEntry.getKey());
			assertEquals("OsCommand", connectorEntry.getValue());
			assertEquals("logicaldisk", connectorEntry.extractEntryType());
			assertFalse(connectorEntry.hasHdf());
			assertTrue(connectorEntry.hasType());
			assertFalse(connectorEntry.hasDiscovery());
			assertTrue(connectorEntry.hasCollect());
			assertFalse(connectorEntry.hasSourceReference());
			assertEquals(OptionalInt.empty(), connectorEntry.extractDetectionIndex());
			assertEquals(OptionalInt.empty(), connectorEntry.extractDetectionStepIndex());
			assertEquals(OptionalInt.of(1), connectorEntry.extractSourceIndex());
			assertEquals(OptionalInt.empty(), connectorEntry.extractSourceStepIndex());
			assertEquals(OptionalInt.empty(), connectorEntry.extractComputeIndex());
		}
		{
			final ConnectorEntry connectorEntry = new ConnectorEntry(22, "Enclosure.Collect.Source(1)", "%Enclosure.Discovery.Source(1)%");
			assertEquals(22, connectorEntry.getOriginalIndex());
			assertEquals("Enclosure.Collect.Source(1)", connectorEntry.getKey());
			assertEquals("%Enclosure.Discovery.Source(1)%", connectorEntry.getValue());
			assertEquals("enclosure", connectorEntry.extractEntryType());
			assertFalse(connectorEntry.hasHdf());
			assertFalse(connectorEntry.hasType());
			assertFalse(connectorEntry.hasDiscovery());
			assertTrue(connectorEntry.hasCollect());
			assertTrue(connectorEntry.hasSourceReference());
			assertEquals(OptionalInt.empty(), connectorEntry.extractDetectionIndex());
			assertEquals(OptionalInt.empty(), connectorEntry.extractDetectionStepIndex());
			assertEquals(OptionalInt.of(1), connectorEntry.extractSourceIndex());
			assertEquals(OptionalInt.empty(), connectorEntry.extractSourceStepIndex());
			assertEquals(OptionalInt.empty(), connectorEntry.extractComputeIndex());
		}
		{
			final ConnectorEntry connectorEntry = new ConnectorEntry(41, "Battery.Collect.Source(1).Step(2).Type", "Sleep");
			assertEquals(41, connectorEntry.getOriginalIndex());
			assertEquals("Battery.Collect.Source(1).Step(2).Type", connectorEntry.getKey());
			assertEquals("Sleep", connectorEntry.getValue());
			assertEquals("battery", connectorEntry.extractEntryType());
			assertFalse(connectorEntry.hasHdf());
			assertFalse(connectorEntry.hasDiscovery());
			assertTrue(connectorEntry.hasCollect());
			assertTrue(connectorEntry.hasType());
			assertFalse(connectorEntry.hasSourceReference());
			assertEquals(OptionalInt.empty(), connectorEntry.extractDetectionIndex());
			assertEquals(OptionalInt.empty(), connectorEntry.extractDetectionStepIndex());
			assertEquals(OptionalInt.of(1), connectorEntry.extractSourceIndex());
			assertEquals(OptionalInt.of(2), connectorEntry.extractSourceStepIndex());
			assertEquals(OptionalInt.empty(), connectorEntry.extractComputeIndex());
		}
		{
			final ConnectorEntry connectorEntry = new ConnectorEntry(42, "Battery.Collect.Source(1).Step(2).Duration", "1");
			assertEquals(42, connectorEntry.getOriginalIndex());
			assertEquals("Battery.Collect.Source(1).Step(2).Duration", connectorEntry.getKey());
			assertEquals("battery", connectorEntry.extractEntryType());
			assertEquals("1", connectorEntry.getValue());
			assertFalse(connectorEntry.hasHdf());
			assertFalse(connectorEntry.hasType());
			assertFalse(connectorEntry.hasDiscovery());
			assertTrue(connectorEntry.hasCollect());
			assertFalse(connectorEntry.hasSourceReference());
			assertEquals(OptionalInt.empty(), connectorEntry.extractDetectionIndex());
			assertEquals(OptionalInt.empty(), connectorEntry.extractDetectionStepIndex());
			assertEquals(OptionalInt.of(1), connectorEntry.extractSourceIndex());
			assertEquals(OptionalInt.of(2), connectorEntry.extractSourceStepIndex());
			assertEquals(OptionalInt.empty(), connectorEntry.extractComputeIndex());
		}
		{
			final ConnectorEntry connectorEntry = new ConnectorEntry(51, "PhysicalDisk.Discovery.Source(3).Compute(1).Type", "RightConcat");
			assertEquals(51, connectorEntry.getOriginalIndex());
			assertEquals("PhysicalDisk.Discovery.Source(3).Compute(1).Type", connectorEntry.getKey());
			assertEquals("RightConcat", connectorEntry.getValue());
			assertEquals("physicaldisk", connectorEntry.extractEntryType());
			assertFalse(connectorEntry.hasHdf());
			assertTrue(connectorEntry.hasType());
			assertTrue(connectorEntry.hasDiscovery());
			assertFalse(connectorEntry.hasCollect());
			assertFalse(connectorEntry.hasSourceReference());
			assertEquals(OptionalInt.empty(), connectorEntry.extractDetectionIndex());
			assertEquals(OptionalInt.empty(), connectorEntry.extractDetectionStepIndex());
			assertEquals(OptionalInt.of(3), connectorEntry.extractSourceIndex());
			assertEquals(OptionalInt.empty(), connectorEntry.extractSourceStepIndex());
			assertEquals(OptionalInt.of(1), connectorEntry.extractComputeIndex());
		}
		{
			final ConnectorEntry connectorEntry = new ConnectorEntry(52, "PhysicalDisk.Discovery.Source(3).Compute(1).Column", "2");
			assertEquals(52, connectorEntry.getOriginalIndex());
			assertEquals("PhysicalDisk.Discovery.Source(3).Compute(1).Column", connectorEntry.getKey());
			assertEquals("physicaldisk", connectorEntry.extractEntryType());
			assertEquals("2", connectorEntry.getValue());
			assertFalse(connectorEntry.hasHdf());
			assertFalse(connectorEntry.hasType());
			assertTrue(connectorEntry.hasDiscovery());
			assertFalse(connectorEntry.hasCollect());
			assertFalse(connectorEntry.hasSourceReference());
			assertEquals(OptionalInt.empty(), connectorEntry.extractDetectionIndex());
			assertEquals(OptionalInt.empty(), connectorEntry.extractDetectionStepIndex());
			assertEquals(OptionalInt.of(3), connectorEntry.extractSourceIndex());
			assertEquals(OptionalInt.empty(), connectorEntry.extractSourceStepIndex());
			assertEquals(OptionalInt.of(1), connectorEntry.extractComputeIndex());
		}
	}

	@Test
	void testProcessCode() {
		{
			final PreConnector preConnector = new PreConnector();
			preConnector.processCode("Detection.Criteria(1).Step(9).Type=\"SendText\"\n"
				+ "Detection.Criteria(1).Step(9).Text=\"exit\\n\"");
			final Map<String, String> codeMap = preConnector.getCodeMap();
			assertEquals("SendText", codeMap.get("detection.criteria(1).step(9).type"));
			assertEquals("exit\n", codeMap.get("detection.criteria(1).step(9).text"));
		}
		{
			final PreConnector preConnector = new PreConnector();
			preConnector.processCode("PhysicalDisk.Discovery.Source(1).Compute(3).SubSeparators=\" \\t\"");
			final Map<String, String> codeMap = preConnector.getCodeMap();
			assertEquals(" \t", codeMap.get("physicaldisk.discovery.source(1).compute(3).subseparators"));
		}
		{
			final PreConnector preConnector = new PreConnector();
			preConnector.processCode("PhysicalDisk.Collect.Source(1).CommandLine=\"/bin/echo \"\"select path %PhysicalDisk.Collect.DeviceID%;wait;information;wait;infolog\"\"|/usr/bin/stm -c\\n/bin/echo \"\"select path %PhysicalDisk.Collect.DeviceID%;wait;veroptions execctrl iterations 1 behavior errorcount 10 testcoverage mincoverage gentactlog no reporterrors reportwarnings queries querynondes;verify;wait;currdevstatus\"\"|/usr/bin/stm -c\"");
			final Map<String, String> codeMap = preConnector.getCodeMap();
			final String expected = "/bin/echo \"select path %PhysicalDisk.Collect.DeviceID%;wait;information;wait;infolog\"|/usr/bin/stm -c\n"
				+ "/bin/echo \"select path %PhysicalDisk.Collect.DeviceID%;wait;veroptions execctrl iterations 1 behavior errorcount 10 testcoverage mincoverage gentactlog no reporterrors reportwarnings queries querynondes;verify;wait;currdevstatus\"|/usr/bin/stm -c";
			assertEquals(expected, codeMap.get("physicaldisk.collect.source(1).commandline"));
		}
	}

	@Test
	void testProcessDefineDirectives() {
		{
			// Simple use case
			final String rawCode = "#define _IsiStatusCommand /usr/bin/isi status -w\n"
					+ "Enclosure.Discovery.Source(2).CommandLine=\"/bin/zsh -c \"\"%{SUDO:/usr/bin/isi} _IsiStatusCommand \"\" \"";

			final PreConnector preConnector = new PreConnector();
			preConnector.processDefineDirectives(rawCode);
			final Map<String, String> actual = preConnector.getConstants();
			final Map<String, String> expected = Map.of("_IsiStatusCommand", "/usr/bin/isi status -w");

			assertEquals(expected, actual);
		}
		{

			// $1 is extracted correctly
			final String rawCode = "#define _IsiStatusCommand /usr/bin/isi($1) status -w\n"
					+ "Enclosure.Discovery.Source(2).CommandLine=\"/bin/zsh -c \"\"%{SUDO:/usr/bin/isi} _IsiStatusCommand \"\" \"";

			final PreConnector preConnector = new PreConnector();
			preConnector.processDefineDirectives(rawCode);
			final Map<String, String> actual = preConnector.getConstants();
			final Map<String, String> expected = Map.of("_IsiStatusCommand", "/usr/bin/isi($1) status -w");

			assertEquals(expected, actual);
		}
		{

			// (\\w+) is extracted
			final String rawCode = "#define _IsiStatusCommand /usr/bin/isi(\\w+) status -w\n"
					+ "Enclosure.Discovery.Source(2).CommandLine=\"/bin/zsh -c \"\"%{SUDO:/usr/bin/isi} _IsiStatusCommand \"\" \"";

			final PreConnector preConnector = new PreConnector();
			preConnector.processDefineDirectives(rawCode);
			final Map<String, String> actual = preConnector.getConstants();
			final Map<String, String> expected = Map.of("_IsiStatusCommand", "/usr/bin/isi(\\w+) status -w");

			assertEquals(expected, actual);
		}
	}

	@Test
	void testProcessComments() {
		{
			final String rawCode = """
					// Comment to be ignored
					
					// Comment1
					// Comment2
					key=value
					""";
			final PreConnector preConnector = new PreConnector();
			String rawCodeResult = preConnector.processComments(rawCode);
			assertEquals("key=value",rawCodeResult.trim());
			assertEquals(Map.of("key", List.of("Comment1", "Comment2")), preConnector.getComments());
		}

		{
			final String rawCode = """
					// Comment to be ignored
					
					/* Comment1 */
					// Comment2
					key=value
					""";
			final PreConnector preConnector = new PreConnector();
			String rawCodeResult = preConnector.processComments(rawCode);
			assertEquals("key=value",rawCodeResult.trim());
			assertEquals(Map.of("key", List.of("Comment1", "Comment2")), preConnector.getComments());
		}

		{
			final String rawCode = """
					// Comment to be ignored
					
					/* 
					 * Comment1
					 * Comment2
					 */
					// Comment3
					key=value
					""";
			final PreConnector preConnector = new PreConnector();
			String rawCodeResult = preConnector.processComments(rawCode);
			assertEquals("key=value",rawCodeResult.trim());
			assertEquals(Map.of("key", List.of("", "Comment1", "Comment2", "", "Comment3")), preConnector.getComments());
		}
	}

	@Test
	void testProcessIncludeDirectives() {
		final String rawCode = """
				#include Parent1Connector.hhdf
				#include Parent2Connector.hhdf
				""";

		final PreConnector preConnector = new PreConnector();
		preConnector.processIncludeDirectives(rawCode);

		// Check the order
		assertEquals(
			List.of("Parent1Connector-header", "Parent2Connector-header"),
			preConnector.getExtendedConnectors().stream().toList()
		);
	}

}