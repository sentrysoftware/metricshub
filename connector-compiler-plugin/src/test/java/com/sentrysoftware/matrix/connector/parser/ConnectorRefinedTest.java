package com.sentrysoftware.matrix.connector.parser;

import com.sentrysoftware.matrix.connector.model.common.TranslationTable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConnectorRefinedTest {

	private ConnectorRefined connectorRefined;

	private static final String DELL_OPEN_MANAGE_HDFS =
		"src/it/success/hardware-connectors/src/main/hdf/MS_HW_DellOpenManage.hdfs";

	private static final String FOO = "FOO";
	private static final String BAR = "BAR";

	@BeforeEach
	void setup() {

		connectorRefined = new ConnectorRefined();
	}

	@Test
	void testLoad() {

		assertDoesNotThrow(() -> connectorRefined.load(DELL_OPEN_MANAGE_HDFS));

		// Testing processTranslationTables()

		Set<String> translationTableNames = new HashSet<>(Arrays.asList(
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
			"PowerSupplyStatusInformationTranslationTable"));

		assertEquals(translationTableNames, connectorRefined.getTranslationTables().keySet());
		TranslationTable genericStatusTranslationTable = connectorRefined
			.getTranslationTables()
			.get("GenericStatusTranslationTable");
		assertNotNull(genericStatusTranslationTable);
		Map<String, String> genericStatusTranslationTableTranslations = genericStatusTranslationTable.getTranslations();
		assertNotNull(genericStatusTranslationTableTranslations);
		assertEquals(5, genericStatusTranslationTableTranslations.size());
		assertEquals(
			Map.of(
				"3", "OK",
				"4", "WARN",
				"5", "ALARM",
				"6", "ALARM",
				"default", "UNKNOWN"),
			genericStatusTranslationTableTranslations
		);
	}

	@Test
	void testGetProblemList() {

		assertEquals(0, connectorRefined.getProblemList().length);
	}

	@Test
	void testGet() {

		assertNull(connectorRefined.get(FOO));
	}

	@Test
	void testGetOrDefault() {

		assertEquals(BAR, connectorRefined.getOrDefault(FOO, BAR));
	}

	@Test
	void testPut() {

		assertNull(connectorRefined.get(FOO));
		connectorRefined.put(FOO, BAR);
		assertEquals(BAR, connectorRefined.get(FOO));
	}

	@Test
	void testEntrySet() {

		assertTrue(connectorRefined.entrySet().isEmpty());
	}

	@Test
	void testGetCompiledFilenameKO() {

		assertEquals("null.connector", ConnectorRefined.getCompiledFilename(null));
		assertEquals("invalid.connector", ConnectorRefined.getCompiledFilename(FOO));
	}
}