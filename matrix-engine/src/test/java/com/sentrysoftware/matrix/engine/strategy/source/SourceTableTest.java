package com.sentrysoftware.matrix.engine.strategy.source;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

class SourceTableTest {

	@Test
	void testStringToTable() {
		final String csvTable = "\n1;A;i;\n\n2;B;ii;\n\n\n3;C;iii;\n\n";
		assertEquals(
				Stream.of(Arrays.asList("1", "A", "i"), Arrays.asList("2", "B", "ii"), Arrays.asList("3", "C", "iii"))
						.collect(Collectors.toList()),
						SourceTable.csvToTable(csvTable, ";"));

		assertNotNull(SourceTable.csvToTable(null, ";"));

		assertEquals(Collections.emptyList(), SourceTable.csvToTable("\n\n\n", ";"));

		final List<String> emptyCells = Arrays.asList("", "");
		assertEquals(Stream
				.of(emptyCells, emptyCells, emptyCells)
				.collect(Collectors.toList()),
				SourceTable.csvToTable("\n;;\n;;\n;;", ";"));

		assertEquals(Stream
				.of(Arrays.asList("", "a"),
						emptyCells,
						emptyCells)
				.collect(Collectors.toList()),
				SourceTable.csvToTable("\n;a;\n;;\n;;", ";"));
	}

	@Test
	void testLineToList() {
		assertEquals(Collections.emptyList(), SourceTable.lineToList(null, ";"));
		assertEquals(Collections.emptyList(), SourceTable.lineToList("", ";"));
		assertEquals(Arrays.asList(""), SourceTable.lineToList(";", ";"));
		assertEquals(Arrays.asList("", "", "", "", "", ""), SourceTable.lineToList(";;;;;;", ";"));
		assertEquals(Arrays.asList("","","a", "", "", ""), SourceTable.lineToList(";;a;;;;", ";"));
		assertEquals(Collections.emptyList(), SourceTable.lineToList("", ";"));

		// Test for line that ends without the ";" separator
		final List<String> list = Arrays.asList("a","b","c");
		assertEquals(list, SourceTable.lineToList("a;b;c", ";"));
		assertEquals(list, SourceTable.lineToList("a;b;c;", ";"));
	}

	@Test
	void testTableToCsv() {
		assertEquals("", SourceTable.tableToCsv(null, ";"));
		assertEquals("", SourceTable.tableToCsv(Collections.emptyList(), ";"));
		assertEquals(";;;;;;",
				SourceTable.tableToCsv(Collections.singletonList(Arrays.asList("", "", "", "", "", "")), ";"));
		assertEquals(";;;;;;\n;;;;;;",
				SourceTable.tableToCsv(
						Stream.of(
								Arrays.asList("", "", "", "", "", ""),
								Arrays.asList("", "", "", "", "", ""))
						.collect(Collectors.toList()),
						";"));
		assertEquals(";;a;;;;\n;;;a;;;",
				SourceTable.tableToCsv(
						Stream.of(
								Arrays.asList("", "", "a", "", "", ""),
								Arrays.asList("", "", "", "a", "", ""))
						.collect(Collectors.toList()),
						";"));
	}
}
