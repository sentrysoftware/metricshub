package com.sentrysoftware.matrix.engine.strategy.source.compute;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.LeftConcat;
import com.sentrysoftware.matrix.engine.strategy.source.SourceTable;

public class ComputeVisitorTest {

	ComputeVisitor computeVisitor;
	SourceTable sourceTable;

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
	}

	@Test
	void testLeftConcatVisit() {
		sourceTable.getTable().add(new ArrayList<>(LINE_1));
		sourceTable.getTable().add(new ArrayList<>(LINE_2));
		sourceTable.getTable().add(new ArrayList<>(LINE_3));

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

		LeftConcat leftConcat = new LeftConcat(1, "prefix_");

		computeVisitor.visit(leftConcat);

		List<List<String>> table = sourceTable.getTable();

		assertEquals(LINE_1_ONE_COLUMN_RESULT, table.get(0));
		assertEquals(LINE_2_ONE_COLUMN_RESULT, table.get(1));
		assertEquals(LINE_3_ONE_COLUMN_RESULT, table.get(2));
	}

	@Test
	void testLeftConcatVisitColumn() {
		sourceTable.getTable().add(new ArrayList<>(LINE_1));
		sourceTable.getTable().add(new ArrayList<>(LINE_2));
		sourceTable.getTable().add(new ArrayList<>(LINE_3));

		LeftConcat leftConcat = new LeftConcat(3, "Column(1)");

		computeVisitor.visit(leftConcat);

		List<List<String>> table = sourceTable.getTable();

		assertEquals(LINE_1_RESULT_COLUMN_1, table.get(0));
		assertEquals(LINE_2_RESULT_COLUMN_1, table.get(1));
		assertEquals(LINE_3_RESULT_COLUMN_1, table.get(2));
	}

	@Test
	void testLeftConcatVisitNotColumn1() {
		sourceTable.getTable().add(new ArrayList<>(LINE_1));
		sourceTable.getTable().add(new ArrayList<>(LINE_2));
		sourceTable.getTable().add(new ArrayList<>(LINE_3));

		LeftConcat leftConcat = new LeftConcat(3, "Column(1)_");

		computeVisitor.visit(leftConcat);

		List<List<String>> table = sourceTable.getTable();

		assertEquals(LINE_1_RESULT_NOT_COLUMN_1, table.get(0));
		assertEquals(LINE_2_RESULT_NOT_COLUMN_1, table.get(1));
		assertEquals(LINE_3_RESULT_NOT_COLUMN_1, table.get(2));
	}

	@Test
	void testLeftConcatVisitNotColumn2() {
		sourceTable.getTable().add(new ArrayList<>(LINE_1));
		sourceTable.getTable().add(new ArrayList<>(LINE_2));
		sourceTable.getTable().add(new ArrayList<>(LINE_3));

		LeftConcat leftConcat = new LeftConcat(3, "_Column(1)");

		computeVisitor.visit(leftConcat);

		List<List<String>> table = sourceTable.getTable();

		assertEquals(LINE_1_RESULT_NOT_COLUMN_2, table.get(0));
		assertEquals(LINE_2_RESULT_NOT_COLUMN_2, table.get(1));
		assertEquals(LINE_3_RESULT_NOT_COLUMN_2, table.get(2));
	}

	@Test
	void testLeftConcatVisitNewColumn() {
		sourceTable.getTable().add(new ArrayList<>(LINE_1));
		sourceTable.getTable().add(new ArrayList<>(LINE_2));
		sourceTable.getTable().add(new ArrayList<>(LINE_3));

		LeftConcat leftConcat = new LeftConcat(3, "new,Column;prefix_");

		computeVisitor.visit(leftConcat);

		List<List<String>> table = sourceTable.getTable();

		assertEquals(LINE_1_RESULT_NEW_COLUMN, table.get(0));
		assertEquals(LINE_2_RESULT_NEW_COLUMN, table.get(1));
		assertEquals(LINE_3_RESULT_NEW_COLUMN, table.get(2));
	}

	@Test
	void testLeftConcatVisitTwoNewColumns() {
		sourceTable.getTable().add(new ArrayList<>(LINE_1));
		sourceTable.getTable().add(new ArrayList<>(LINE_2));
		sourceTable.getTable().add(new ArrayList<>(LINE_3));

		LeftConcat leftConcat = new LeftConcat(1, "new,Column(4);AnotherNew.Column;prefix_");

		computeVisitor.visit(leftConcat);

		List<List<String>> table = sourceTable.getTable();

		assertEquals(LINE_1_RESULT_TWO_NEW_COLUMNS, table.get(0));
		assertEquals(LINE_2_RESULT_TWO_NEW_COLUMNS, table.get(1));
		assertEquals(LINE_3_RESULT_TWO_NEW_COLUMNS, table.get(2));
	}
}
