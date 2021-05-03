package com.sentrysoftware.matrix.engine.strategy.source.compute;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.TranslationTable;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.DuplicateColumn;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.KeepOnlyMatchingLines;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.LeftConcat;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Translate;
import com.sentrysoftware.matrix.engine.strategy.source.SourceTable;

class ComputeVisitorTest {

	private ComputeVisitor computeVisitor;
	private SourceTable sourceTable;

	private static final String FOO = "FOO";
	private static final String BAR = "BAR";
	private static final String BAZ = "BAZ";

	private static final List<String> LINE_1 = Arrays.asList("ID1", "NAME1", "MANUFACTURER1", "NUMBER_OF_DISKS1");
	private static final List<String> LINE_2 = Arrays.asList("ID2", "NAME2", "MANUFACTURER2", "NUMBER_OF_DISKS2");
	private static final List<String> LINE_3 = Arrays.asList("ID3", "NAME3", "MANUFACTURER3", "NUMBER_OF_DISKS3");

	private static final List<String> LINE_1_RESULT = new ArrayList<>(Arrays.asList("ID1", "NAME1", "prefix_MANUFACTURER1", "NUMBER_OF_DISKS1"));
	private static final List<String> LINE_2_RESULT = new ArrayList<>(Arrays.asList("ID2", "NAME2", "prefix_MANUFACTURER2", "NUMBER_OF_DISKS2"));
	private static final List<String> LINE_3_RESULT = new ArrayList<>(Arrays.asList("ID3", "NAME3", "prefix_MANUFACTURER3", "NUMBER_OF_DISKS3"));

	private static final List<String> LINE_1_ONE_COLUMN = new ArrayList<>(Arrays.asList("ID1"));
	private static final List<String> LINE_2_ONE_COLUMN = new ArrayList<>(Arrays.asList("ID2"));
	private static final List<String> LINE_3_ONE_COLUMN = new ArrayList<>(Arrays.asList("ID3"));

	private static final List<String> LINE_1_ONE_COLUMN_RESULT = new ArrayList<>(Arrays.asList("prefix_ID1"));
	private static final List<String> LINE_2_ONE_COLUMN_RESULT = new ArrayList<>(Arrays.asList("prefix_ID2"));
	private static final List<String> LINE_3_ONE_COLUMN_RESULT = new ArrayList<>(Arrays.asList("prefix_ID3"));

	private static final List<String> LINE_1_RESULT_COLUMN_1 = new ArrayList<>(Arrays.asList("ID1", "NAME1", "ID1MANUFACTURER1", "NUMBER_OF_DISKS1"));
	private static final List<String> LINE_2_RESULT_COLUMN_1 = new ArrayList<>(Arrays.asList("ID2", "NAME2", "ID2MANUFACTURER2", "NUMBER_OF_DISKS2"));
	private static final List<String> LINE_3_RESULT_COLUMN_1 = new ArrayList<>(Arrays.asList("ID3", "NAME3", "ID3MANUFACTURER3", "NUMBER_OF_DISKS3"));

	private static final List<String> LINE_1_RESULT_NOT_COLUMN_1 = new ArrayList<>(Arrays.asList("ID1", "NAME1", "Column(1)_MANUFACTURER1", "NUMBER_OF_DISKS1"));
	private static final List<String> LINE_2_RESULT_NOT_COLUMN_1 = new ArrayList<>(Arrays.asList("ID2", "NAME2", "Column(1)_MANUFACTURER2", "NUMBER_OF_DISKS2"));
	private static final List<String> LINE_3_RESULT_NOT_COLUMN_1 = new ArrayList<>(Arrays.asList("ID3", "NAME3", "Column(1)_MANUFACTURER3", "NUMBER_OF_DISKS3"));

	private static final List<String> LINE_1_RESULT_NOT_COLUMN_2 = new ArrayList<>(Arrays.asList("ID1", "NAME1", "_Column(1)MANUFACTURER1", "NUMBER_OF_DISKS1"));
	private static final List<String> LINE_2_RESULT_NOT_COLUMN_2 = new ArrayList<>(Arrays.asList("ID2", "NAME2", "_Column(1)MANUFACTURER2", "NUMBER_OF_DISKS2"));
	private static final List<String> LINE_3_RESULT_NOT_COLUMN_2 = new ArrayList<>(Arrays.asList("ID3", "NAME3", "_Column(1)MANUFACTURER3", "NUMBER_OF_DISKS3"));

	private static final List<String> LINE_1_RESULT_NEW_COLUMN = new ArrayList<>(Arrays.asList("ID1", "NAME1", "new,Column", "prefix_MANUFACTURER1", "NUMBER_OF_DISKS1"));
	private static final List<String> LINE_2_RESULT_NEW_COLUMN = new ArrayList<>(Arrays.asList("ID2", "NAME2", "new,Column", "prefix_MANUFACTURER2", "NUMBER_OF_DISKS2"));
	private static final List<String> LINE_3_RESULT_NEW_COLUMN = new ArrayList<>(Arrays.asList("ID3", "NAME3", "new,Column", "prefix_MANUFACTURER3", "NUMBER_OF_DISKS3"));

	private static final List<String> LINE_1_RESULT_TWO_NEW_COLUMNS = new ArrayList<>(Arrays.asList("new,Column(4)", "AnotherNew.Column", "prefix_ID1", "NAME1", "MANUFACTURER1", "NUMBER_OF_DISKS1"));
	private static final List<String> LINE_2_RESULT_TWO_NEW_COLUMNS = new ArrayList<>(Arrays.asList("new,Column(4)", "AnotherNew.Column", "prefix_ID2", "NAME2", "MANUFACTURER2", "NUMBER_OF_DISKS2"));
	private static final List<String> LINE_3_RESULT_TWO_NEW_COLUMNS = new ArrayList<>(Arrays.asList("new,Column(4)", "AnotherNew.Column", "prefix_ID3", "NAME3", "MANUFACTURER3", "NUMBER_OF_DISKS3"));

	@BeforeEach
	void setUp() {
		computeVisitor = new ComputeVisitor();
		sourceTable = new SourceTable();
		computeVisitor.setSourceTable(sourceTable);
		computeVisitor.setConnector(Connector.builder().build());
	}

	@Test
	void visitKeepOnlyMatchingLinesNoOperation() {

		// KeepOnlyMatchingLines is null
		computeVisitor.setSourceTable(SourceTable.empty());
		computeVisitor.visit((KeepOnlyMatchingLines) null);
		assertNotNull(computeVisitor.getSourceTable().getTable());
		assertTrue(computeVisitor.getSourceTable().getTable().isEmpty());

		// KeepOnlyMatchingLines is not null, keepOnlyMatchingLines.getColumn() is null
		KeepOnlyMatchingLines keepOnlyMatchingLines = KeepOnlyMatchingLines.builder().build();
		computeVisitor.visit(keepOnlyMatchingLines);
		assertTrue(computeVisitor.getSourceTable().getTable().isEmpty());

		// KeepOnlyMatchingLines is not null, keepOnlyMatchingLines.getColumn() is not null,
		// keepOnlyMatchingLines.getColumn() <= 0
		keepOnlyMatchingLines.setColumn(0);
		computeVisitor.visit(keepOnlyMatchingLines);
		assertTrue(computeVisitor.getSourceTable().getTable().isEmpty());

		// KeepOnlyMatchingLines is not null, keepOnlyMatchingLines.getColumn() is not null,
		// keepOnlyMatchingLines.getColumn() > 0,
		// computeVisitor.getSourceTable() is null
		keepOnlyMatchingLines.setColumn(1);
		computeVisitor.setSourceTable(null);
		computeVisitor.visit(keepOnlyMatchingLines);
		assertNull(computeVisitor.getSourceTable());

		// KeepOnlyMatchingLines is not null, keepOnlyMatchingLines.getColumn() is not null,
		// keepOnlyMatchingLines.getColumn() > 0,
		// computeVisitor.getSourceTable() is not null, computeVisitor.getSourceTable().getTable() is null
		computeVisitor.setSourceTable(SourceTable.builder().table(null).build());
		computeVisitor.visit(keepOnlyMatchingLines);
		assertNull(computeVisitor.getSourceTable().getTable());

		// KeepOnlyMatchingLines is not null, keepOnlyMatchingLines.getColumn() is not null,
		// keepOnlyMatchingLines.getColumn() > 0,
		// computeVisitor.getSourceTable() is not null, computeVisitor.getSourceTable().getTable() is not null,
		// computeVisitor.getSourceTable().getTable().isEmpty()
		computeVisitor.setSourceTable(SourceTable.empty());
		computeVisitor.visit(keepOnlyMatchingLines);
		assertTrue(computeVisitor.getSourceTable().getTable().isEmpty());

		// KeepOnlyMatchingLines is not null, keepOnlyMatchingLines.getColumn() is not null,
		// keepOnlyMatchingLines.getColumn() > 0,
		// computeVisitor.getSourceTable() is not null, computeVisitor.getSourceTable().getTable() is not null,
		// computeVisitor.getSourceTable().getTable() is not empty,
		// keepOnlyMatchingLines.getColumn() > sourceTable.getTable().get(0).size()
		computeVisitor.setSourceTable(
				SourceTable
						.builder()
						.table(
								Collections.singletonList(
										Collections.singletonList(FOO)
								)
						)
						.build());
		keepOnlyMatchingLines.setColumn(2);
		computeVisitor.visit(keepOnlyMatchingLines);
		assertEquals(1, computeVisitor.getSourceTable().getTable().size());
	}

	@Test
	void visitKeepOnlyMatchingLines() {

		List<String> line1 = Arrays.asList(FOO, "1", "2", "3");
		List<String> line2 = Arrays.asList(BAR, "10", "20", "30");
		List<String> line3 = Arrays.asList(BAZ, "100", "200", "300");
		List<List<String>> table = Arrays.asList(line1, line2, line3);

		computeVisitor.setSourceTable(
				SourceTable
						.builder()
						.table(table)
						.build());

		// regexp is null, valueList is null
		KeepOnlyMatchingLines keepOnlyMatchingLines = KeepOnlyMatchingLines
				.builder()
				.column(1)
				.regExp(null)
				.valueList(null)
				.build();
		computeVisitor.visit(keepOnlyMatchingLines);
		assertEquals(table, computeVisitor.getSourceTable().getTable());

		// regexp is empty, valueList is null
		keepOnlyMatchingLines.setRegExp("");
		computeVisitor.visit(keepOnlyMatchingLines);
		assertEquals(table, computeVisitor.getSourceTable().getTable());

		// regexp is empty, valueList is empty
		keepOnlyMatchingLines.setValueList(Collections.emptyList());
		computeVisitor.visit(keepOnlyMatchingLines);
		assertEquals(table, computeVisitor.getSourceTable().getTable());

		// regex is not null, not empty
		keepOnlyMatchingLines.setRegExp("^B.*");
		computeVisitor.visit(keepOnlyMatchingLines);
		assertNotEquals(table, computeVisitor.getSourceTable().getTable());
		List<List<String>> resultTable = computeVisitor.getSourceTable().getTable();
		assertNotNull(resultTable);
		assertEquals(2, resultTable.size());
		assertEquals(line2, resultTable.get(0));
		assertEquals(line3, resultTable.get(1));

		// regex is null,
		// valueList is not null, not empty
		computeVisitor.getSourceTable().setTable(table);
		keepOnlyMatchingLines.setRegExp(null);
		keepOnlyMatchingLines.setValueList(Arrays.asList("3", "300"));
		keepOnlyMatchingLines.setColumn(4);
		computeVisitor.visit(keepOnlyMatchingLines);
		assertNotEquals(table, computeVisitor.getSourceTable().getTable());
		resultTable = computeVisitor.getSourceTable().getTable();
		assertNotNull(resultTable);
		assertEquals(2, resultTable.size());
		assertEquals(line1, resultTable.get(0));
		assertEquals(line3, resultTable.get(1));

		// regex is not null, not empty
		// valueList is not null, not empty
		computeVisitor.getSourceTable().setTable(table);
		keepOnlyMatchingLines.setColumn(1);
		keepOnlyMatchingLines.setRegExp("^B.*"); // Applying only the regex would match line2 and line3
		keepOnlyMatchingLines.setValueList(Arrays.asList("FOO", "BAR", "BAB")); // Applying only the valueList would match line1 and line2
		computeVisitor.visit(keepOnlyMatchingLines);
		assertNotEquals(table, computeVisitor.getSourceTable().getTable());
		resultTable = computeVisitor.getSourceTable().getTable();
		assertNotNull(resultTable);
		assertEquals(1, resultTable.size()); // Applying both the regex and the valueList matches only line2
		assertEquals(line2, resultTable.get(0));
	}

	@Test
	void testLeftConcatVisit() {
		initializeSourceTable();

		// Test with empty LeftConcat
		LeftConcat leftConcat = new LeftConcat();

		computeVisitor.visit(leftConcat);

		List<List<String>> table = sourceTable.getTable();

		assertEquals(LINE_1, table.get(0));
		assertEquals(LINE_2, table.get(1));
		assertEquals(LINE_3, table.get(2));

		// Test with LeftConcat without String
		leftConcat.setColumn(3);

		computeVisitor.visit(leftConcat);

		assertEquals(LINE_1, table.get(0));
		assertEquals(LINE_2, table.get(1));
		assertEquals(LINE_3, table.get(2));

		// Test with LeftConcat without Column
		leftConcat.setColumn(null);
		leftConcat.setString("prefix_");

		computeVisitor.visit(leftConcat);

		assertEquals(LINE_1, table.get(0));
		assertEquals(LINE_2, table.get(1));
		assertEquals(LINE_3, table.get(2));

		// Test with correct LeftConcat
		leftConcat.setColumn(3);

		computeVisitor.visit(leftConcat);

		assertEquals(LINE_1_RESULT, table.get(0));
		assertEquals(LINE_2_RESULT, table.get(1));
		assertEquals(LINE_3_RESULT, table.get(2));
	}

	@Test
	void testLeftConcatVisitOneColumn() {
		sourceTable.getTable().add(new ArrayList<>(LINE_1_ONE_COLUMN));
		sourceTable.getTable().add(new ArrayList<>(LINE_2_ONE_COLUMN));
		sourceTable.getTable().add(new ArrayList<>(LINE_3_ONE_COLUMN));

		LeftConcat leftConcat = new LeftConcat(1, 1, "prefix_");

		computeVisitor.visit(leftConcat);

		List<List<String>> table = sourceTable.getTable();

		assertEquals(LINE_1_ONE_COLUMN_RESULT, table.get(0));
		assertEquals(LINE_2_ONE_COLUMN_RESULT, table.get(1));
		assertEquals(LINE_3_ONE_COLUMN_RESULT, table.get(2));
	}

	@Test
	void testLeftConcatVisitColumn() {
		initializeSourceTable();

		LeftConcat leftConcat = new LeftConcat(1, 3, "Column(1)");

		computeVisitor.visit(leftConcat);

		List<List<String>> table = sourceTable.getTable();

		assertEquals(LINE_1_RESULT_COLUMN_1, table.get(0));
		assertEquals(LINE_2_RESULT_COLUMN_1, table.get(1));
		assertEquals(LINE_3_RESULT_COLUMN_1, table.get(2));
	}

	@Test
	void testLeftConcatVisitNotColumn1() {
		initializeSourceTable();

		LeftConcat leftConcat = new LeftConcat(1, 3, "Column(1)_");

		computeVisitor.visit(leftConcat);

		List<List<String>> table = sourceTable.getTable();

		assertEquals(LINE_1_RESULT_NOT_COLUMN_1, table.get(0));
		assertEquals(LINE_2_RESULT_NOT_COLUMN_1, table.get(1));
		assertEquals(LINE_3_RESULT_NOT_COLUMN_1, table.get(2));
	}

	@Test
	void testLeftConcatVisitNotColumn2() {
		initializeSourceTable();

		LeftConcat leftConcat = new LeftConcat(1, 3, "_Column(1)");

		computeVisitor.visit(leftConcat);

		List<List<String>> table = sourceTable.getTable();

		assertEquals(LINE_1_RESULT_NOT_COLUMN_2, table.get(0));
		assertEquals(LINE_2_RESULT_NOT_COLUMN_2, table.get(1));
		assertEquals(LINE_3_RESULT_NOT_COLUMN_2, table.get(2));
	}

	@Test
	void testLeftConcatVisitNewColumn() {
		initializeSourceTable();

		LeftConcat leftConcat = new LeftConcat(1, 3, "new,Column;prefix_");

		computeVisitor.visit(leftConcat);

		List<List<String>> table = sourceTable.getTable();

		assertEquals(LINE_1_RESULT_NEW_COLUMN, table.get(0));
		assertEquals(LINE_2_RESULT_NEW_COLUMN, table.get(1));
		assertEquals(LINE_3_RESULT_NEW_COLUMN, table.get(2));
	}

	@Test
	void testLeftConcatVisitTwoNewColumns() {
		initializeSourceTable();

		LeftConcat leftConcat = new LeftConcat(1, 1, "new,Column(4);AnotherNew.Column;prefix_");

		computeVisitor.visit(leftConcat);

		List<List<String>> table = sourceTable.getTable();

		assertEquals(LINE_1_RESULT_TWO_NEW_COLUMNS, table.get(0));
		assertEquals(LINE_2_RESULT_TWO_NEW_COLUMNS, table.get(1));
		assertEquals(LINE_3_RESULT_TWO_NEW_COLUMNS, table.get(2));
	}

	@Test
	void testDuplicateColumn() {
		sourceTable.getTable().add(new ArrayList<>(LINE_1));
		// test null arg
		DuplicateColumn dupColumnNull = null;
		computeVisitor.visit(dupColumnNull);
		assertEquals(Arrays.asList(Arrays.asList("ID1", "NAME1", "MANUFACTURER1", "NUMBER_OF_DISKS1")), sourceTable.getTable());

		// test out of bounds
		DuplicateColumn dupColumn = new DuplicateColumn(1, 0);
		computeVisitor.visit(dupColumn);
		assertEquals(Arrays.asList(Arrays.asList("ID1", "NAME1", "MANUFACTURER1", "NUMBER_OF_DISKS1")), sourceTable.getTable());
		
		dupColumn = new DuplicateColumn(10, 10);
		computeVisitor.visit(dupColumn);
		assertEquals(Arrays.asList(Arrays.asList("ID1", "NAME1", "MANUFACTURER1", "NUMBER_OF_DISKS1")), sourceTable.getTable());
		
		// test actual index
		dupColumn = new DuplicateColumn(1, 1);
		computeVisitor.visit(dupColumn);
		assertEquals(Arrays.asList(Arrays.asList("ID1", "ID1", "NAME1", "MANUFACTURER1", "NUMBER_OF_DISKS1")), sourceTable.getTable());
		
		dupColumn = new DuplicateColumn(2, 2);
		computeVisitor.visit(dupColumn);
		assertEquals(Arrays.asList(Arrays.asList("ID1", "ID1",  "ID1", "NAME1", "MANUFACTURER1", "NUMBER_OF_DISKS1")), sourceTable.getTable());
		
		dupColumn = new DuplicateColumn(3, 6);
		computeVisitor.visit(dupColumn);
		assertEquals(Arrays.asList(Arrays.asList("ID1", "ID1", "ID1", "NAME1", "MANUFACTURER1", "NUMBER_OF_DISKS1", "NUMBER_OF_DISKS1")), sourceTable.getTable());
		
		// test multiple lines
		initializeSourceTable();
		

		dupColumn = new DuplicateColumn(13, 3);
		computeVisitor.visit(dupColumn);
		assertEquals(Arrays.asList(
				Arrays.asList("ID1", "NAME1", "MANUFACTURER1", "MANUFACTURER1", "NUMBER_OF_DISKS1"), 
				Arrays.asList("ID2", "NAME2", "MANUFACTURER2", "MANUFACTURER2", "NUMBER_OF_DISKS2"),
				Arrays.asList("ID3", "NAME3", "MANUFACTURER3", "MANUFACTURER3", "NUMBER_OF_DISKS3")),
				sourceTable.getTable());

		dupColumn = new DuplicateColumn(13, 7);
		computeVisitor.visit(dupColumn);
		assertEquals(Arrays.asList(
				Arrays.asList("ID1", "NAME1", "MANUFACTURER1", "MANUFACTURER1", "NUMBER_OF_DISKS1"), 
				Arrays.asList("ID2", "NAME2", "MANUFACTURER2", "MANUFACTURER2", "NUMBER_OF_DISKS2"),
				Arrays.asList("ID3", "NAME3", "MANUFACTURER3", "MANUFACTURER3", "NUMBER_OF_DISKS3")),
				sourceTable.getTable());
		

		dupColumn = new DuplicateColumn(13, null);
		computeVisitor.visit(dupColumn);
		assertEquals(Arrays.asList(
				Arrays.asList("ID1", "NAME1", "MANUFACTURER1", "MANUFACTURER1", "NUMBER_OF_DISKS1"), 
				Arrays.asList("ID2", "NAME2", "MANUFACTURER2", "MANUFACTURER2", "NUMBER_OF_DISKS2"),
				Arrays.asList("ID3", "NAME3", "MANUFACTURER3", "MANUFACTURER3", "NUMBER_OF_DISKS3")),
				sourceTable.getTable());


		dupColumn = new DuplicateColumn(13, 0);
		computeVisitor.visit(dupColumn);
		assertEquals(Arrays.asList(
				Arrays.asList("ID1", "NAME1", "MANUFACTURER1", "MANUFACTURER1", "NUMBER_OF_DISKS1"), 
				Arrays.asList("ID2", "NAME2", "MANUFACTURER2", "MANUFACTURER2", "NUMBER_OF_DISKS2"),
				Arrays.asList("ID3", "NAME3", "MANUFACTURER3", "MANUFACTURER3", "NUMBER_OF_DISKS3")),
				sourceTable.getTable());
	}

	private void initializeSourceTable() {
		sourceTable.getTable().clear();
		sourceTable.getTable().add(new ArrayList<>(LINE_1));
		sourceTable.getTable().add(new ArrayList<>(LINE_2));
		sourceTable.getTable().add(new ArrayList<>(LINE_3));
	}



	@Test
	void testTranslation() {
		
		final Map<String, String> translationMap = Stream.of(new String[][] { 
			{ "NAME1", "NAME1_resolved" }, { "NAME2", "NAME2_resolved" }, { "NAME3", "NAME3_resolved" }, 
			{ "ID1", "ID1_resolved" }, { "ID2", "ID2_resolved" }, { "ID3", "ID3_resolved" }, 
			{ "NUMBER_OF_DISKS1", "NUMBER_OF_DISKS1_resolved" }, { "NUMBER_OF_DISKS2", "NUMBER_OF_DISKS2_resolved" }, { "NUMBER_OF_DISKS3", "NUMBER_OF_DISKS3_resolved" }, 
			})
				.collect(Collectors.toMap(data -> data[0], data -> data[1]));

		// test null source to visit
		initializeSourceTable();
		Translate translateNull = null;
		computeVisitor.visit(translateNull);
		assertEquals(Arrays.asList(
				Arrays.asList("ID1", "NAME1", "MANUFACTURER1", "NUMBER_OF_DISKS1"), 
				Arrays.asList("ID2", "NAME2", "MANUFACTURER2", "NUMBER_OF_DISKS2"),
				Arrays.asList("ID3", "NAME3", "MANUFACTURER3", "NUMBER_OF_DISKS3")),
				sourceTable.getTable());

		// test TranslationTable is null
		initializeSourceTable();
		Translate translate = Translate.builder().column(0).index(0).build();
		computeVisitor.visit(translate);
		assertEquals(Arrays.asList(
				Arrays.asList("ID1", "NAME1", "MANUFACTURER1", "NUMBER_OF_DISKS1"), 
				Arrays.asList("ID2", "NAME2", "MANUFACTURER2", "NUMBER_OF_DISKS2"),
				Arrays.asList("ID3", "NAME3", "MANUFACTURER3", "NUMBER_OF_DISKS3")),
				sourceTable.getTable());

		initializeSourceTable();
		translate = Translate.builder().column(0).index(0).translationTable(TranslationTable.builder().name("TR1").build()).build();
		computeVisitor.visit(translate);
		assertEquals(Arrays.asList(
				Arrays.asList("ID1", "NAME1", "MANUFACTURER1", "NUMBER_OF_DISKS1"), 
				Arrays.asList("ID2", "NAME2", "MANUFACTURER2", "NUMBER_OF_DISKS2"),
				Arrays.asList("ID3", "NAME3", "MANUFACTURER3", "NUMBER_OF_DISKS3")),
				sourceTable.getTable());

		// test index out of bounds
		initializeSourceTable();
		translate = Translate.builder().column(0).index(0).translationTable(TranslationTable.builder().name("TR1").translations(translationMap ).build()).build();
		computeVisitor.visit(translate);
		assertEquals(Arrays.asList(
				Arrays.asList("ID1", "NAME1", "MANUFACTURER1", "NUMBER_OF_DISKS1"), 
				Arrays.asList("ID2", "NAME2", "MANUFACTURER2", "NUMBER_OF_DISKS2"),
				Arrays.asList("ID3", "NAME3", "MANUFACTURER3", "NUMBER_OF_DISKS3")),
				sourceTable.getTable());

		initializeSourceTable();
		translate = Translate.builder().column(10).index(10).translationTable(TranslationTable.builder().name("TR1").translations(translationMap ).build()).build();
		computeVisitor.visit(translate);
		assertEquals(Arrays.asList(
				Arrays.asList("ID1", "NAME1", "MANUFACTURER1", "NUMBER_OF_DISKS1"), 
				Arrays.asList("ID2", "NAME2", "MANUFACTURER2", "NUMBER_OF_DISKS2"),
				Arrays.asList("ID3", "NAME3", "MANUFACTURER3", "NUMBER_OF_DISKS3")),
				sourceTable.getTable());

		// test 1st index
		initializeSourceTable();
		translate = Translate.builder().column(1).index(1).translationTable(TranslationTable.builder().name("TR1").translations(translationMap ).build()).build();
		computeVisitor.visit(translate);
		assertEquals(Arrays.asList(
				Arrays.asList("ID1_resolved", "NAME1", "MANUFACTURER1", "NUMBER_OF_DISKS1"), 
				Arrays.asList("ID2_resolved", "NAME2", "MANUFACTURER2", "NUMBER_OF_DISKS2"),
				Arrays.asList("ID3_resolved", "NAME3", "MANUFACTURER3", "NUMBER_OF_DISKS3")),
				sourceTable.getTable());

		// test intermediate index
		initializeSourceTable();
		translate = Translate.builder().column(2).index(2).translationTable(TranslationTable.builder().name("TR1").translations(translationMap ).build()).build();
		computeVisitor.visit(translate);
		assertEquals(Arrays.asList(
				Arrays.asList("ID1", "NAME1_resolved", "MANUFACTURER1", "NUMBER_OF_DISKS1"), 
				Arrays.asList("ID2", "NAME2_resolved", "MANUFACTURER2", "NUMBER_OF_DISKS2"),
				Arrays.asList("ID3", "NAME3_resolved", "MANUFACTURER3", "NUMBER_OF_DISKS3")),
				sourceTable.getTable());

		// test last index
		initializeSourceTable();
		translate = Translate.builder().column(4).index(2).translationTable(TranslationTable.builder().name("TR1").translations(translationMap ).build()).build();
		computeVisitor.visit(translate);
		assertEquals(Arrays.asList(
				Arrays.asList("ID1", "NAME1", "MANUFACTURER1", "NUMBER_OF_DISKS1_resolved"), 
				Arrays.asList("ID2", "NAME2", "MANUFACTURER2", "NUMBER_OF_DISKS2_resolved"),
				Arrays.asList("ID3", "NAME3", "MANUFACTURER3", "NUMBER_OF_DISKS3_resolved")),
				sourceTable.getTable());

		// test unknown value
		initializeSourceTable();
		sourceTable.getTable().add(new ArrayList<>(Arrays.asList("ID", "NAME", "MANUFACTURER", "NUMBER_OF_DISKS")));
		translate = Translate.builder().column(1).index(2).translationTable(TranslationTable.builder().name("TR1").translations(translationMap ).build()).build();
		computeVisitor.visit(translate);
		assertEquals(Arrays.asList(
				Arrays.asList("ID1_resolved", "NAME1", "MANUFACTURER1", "NUMBER_OF_DISKS1"), 
				Arrays.asList("ID2_resolved", "NAME2", "MANUFACTURER2", "NUMBER_OF_DISKS2"),
				Arrays.asList("ID3_resolved", "NAME3", "MANUFACTURER3", "NUMBER_OF_DISKS3"),
				Arrays.asList("ID", "NAME", "MANUFACTURER", "NUMBER_OF_DISKS")),
				sourceTable.getTable());
		
	}
	

}
