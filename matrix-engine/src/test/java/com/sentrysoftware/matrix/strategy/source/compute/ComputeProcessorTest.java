package com.sentrysoftware.matrix.strategy.source.compute;

import static com.sentrysoftware.matrix.constants.Constants.EMPTY;
import static com.sentrysoftware.matrix.constants.Constants.SINGLE_SPACE;
import static com.sentrysoftware.matrix.constants.Constants.VALUE_VAL1;
import static com.sentrysoftware.matrix.constants.Constants.VALUE_VAL2;
import static com.sentrysoftware.matrix.constants.Constants.VALUE_VAL3;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Add;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.And;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Divide;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.LeftConcat;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Multiply;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.RightConcat;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Subtract;
import com.sentrysoftware.matrix.strategy.source.SourceTable;

class ComputeProcessorTest {

	SourceTable sourceTable;
	ComputeProcessor computeProcessor;

	private static final String DOLLAR_1 = "$1";
	private static final String UNDERSCORE_DOLLAR_1 = "_$1";
	private static final String DOLLAR_3 = "$3";
	private static final String DOLLAR_4 = "$4";
	private static final String DOLLAR_13 = "$13";
	private static final String ZERO = "0";
	private static final String ZERO_POINT_ZERO = "0.0";
	private static final String ONE = "1";
	private static final String TWO = "2";
	private static final String FOUR_POINT_ZERO = "4.0";
	private static final String FIVE = "5";
	private static final String TEN = "10";
	private static final String FOURTEEN = "14";
	private static final String THIRTY = "30";
	private static final String THIRTY_SIX = "36";
	private static final String FORTY_ONE = "41";
	private static final String TWO_HUNDRED = "200";
	private static final String TWO_HUNDRED_AND_FIFTY_FOUR = "254";
	private static final String TWO_HUNDRED_AND_FIFTY_FIVE = "255";
	private static final String FIVE_HUNDRED = "500";
	private static final String ONE_THOUSAND_FIVE_HUNDRED = "1500";
	private static final String ANOTHER_NEW_COLUMN = "AnotherNew.Column";
	private static final String ID1 = "ID1";
	private static final String ID2 = "ID2";
	private static final String ID3 = "ID3";
	private static final String PREFIX_ID1 = "prefix_ID1";
	private static final String PREFIX_ID2 = "prefix_ID2";
	private static final String PREFIX_ID3 = "prefix_ID3";
	private static final String ID1_SUFFIX = "ID1_suffix";
	private static final String ID2_SUFFIX = "ID2_suffix";
	private static final String ID3_SUFFIX = "ID3_suffix";
	private static final String FOO = "FOO";
	private static final String MANUFACTURER1 = "MANUFACTURER1";
	private static final String MANUFACTURER2 = "MANUFACTURER2";
	private static final String MANUFACTURER3 = "MANUFACTURER3";
	private static final String PREFIX_MANUFACTURER1 = "prefix_MANUFACTURER1";
	private static final String PREFIX_MANUFACTURER2 = "prefix_MANUFACTURER2";
	private static final String PREFIX_MANUFACTURER3 = "prefix_MANUFACTURER3";
	private static final String MANUFACTURER1_SUFFIX = "MANUFACTURER1_suffix";
	private static final String MANUFACTURER2_SUFFIX = "MANUFACTURER2_suffix";
	private static final String MANUFACTURER3_SUFFIX = "MANUFACTURER3_suffix";
	private static final String NAME1 = "NAME1";
	private static final String NAME2 = "NAME2";
	private static final String NAME3 = "NAME3";
	private static final String NEW_COMMA_COLUMN = "new,Column";
	private static final String NEW_COMMA_DOLLAR_4 = "new,$4";
	private static final String NUMBER_OF_DISKS1 = "NUMBER_OF_DISKS1";
	private static final String NUMBER_OF_DISKS2 = "NUMBER_OF_DISKS2";
	private static final String NUMBER_OF_DISKS3 = "NUMBER_OF_DISKS3";
	private static final String PREFIX = "prefix_";
	private static final String SUFFIX = "_suffix";

	private static final List<String> LINE_1 = Arrays.asList(ID1, NAME1, MANUFACTURER1, NUMBER_OF_DISKS1);
	private static final List<String> LINE_2 = Arrays.asList(ID2, NAME2, MANUFACTURER2, NUMBER_OF_DISKS2);
	private static final List<String> LINE_3 = Arrays.asList(ID3, NAME3, MANUFACTURER3, NUMBER_OF_DISKS3);

	private static final List<String> LINE_1_RESULT_LEFT = new ArrayList<>(Arrays.asList(ID1, NAME1, PREFIX_MANUFACTURER1, NUMBER_OF_DISKS1));
	private static final List<String> LINE_2_RESULT_LEFT = new ArrayList<>(Arrays.asList(ID2, NAME2, PREFIX_MANUFACTURER2, NUMBER_OF_DISKS2));
	private static final List<String> LINE_3_RESULT_LEFT = new ArrayList<>(Arrays.asList(ID3, NAME3, PREFIX_MANUFACTURER3, NUMBER_OF_DISKS3));

	private static final List<String> LINE_1_RESULT_RIGHT = new ArrayList<>(Arrays.asList(ID1, NAME1, MANUFACTURER1_SUFFIX, NUMBER_OF_DISKS1));
	private static final List<String> LINE_2_RESULT_RIGHT = new ArrayList<>(Arrays.asList(ID2, NAME2, MANUFACTURER2_SUFFIX, NUMBER_OF_DISKS2));
	private static final List<String> LINE_3_RESULT_RIGHT = new ArrayList<>(Arrays.asList(ID3, NAME3, MANUFACTURER3_SUFFIX, NUMBER_OF_DISKS3));

	private static final List<String> LINE_1_ONE_COLUMN = new ArrayList<>(Collections.singletonList(ID1));
	private static final List<String> LINE_2_ONE_COLUMN = new ArrayList<>(Collections.singletonList(ID2));
	private static final List<String> LINE_3_ONE_COLUMN = new ArrayList<>(Collections.singletonList(ID3));

	@BeforeEach
	void setUp() {
		computeProcessor = new ComputeProcessor();
		sourceTable = new SourceTable();
		computeProcessor.setSourceTable(sourceTable);
	}

	private void initializeSourceTable() {
		sourceTable.getTable().clear();
		sourceTable.getTable().add(new ArrayList<>(LINE_1));
		sourceTable.getTable().add(new ArrayList<>(LINE_2));
		sourceTable.getTable().add(new ArrayList<>(LINE_3));
	}

	@Test
	void testProcessAdd() {
		List<List<String>> table = Arrays.asList(
			Arrays.asList(ID1, FIVE_HUNDRED, TWO, VALUE_VAL1),
			Arrays.asList(ID2, ONE_THOUSAND_FIVE_HUNDRED, FIVE, VALUE_VAL2),
			Arrays.asList(ID1, TWO_HUNDRED, TWO, VALUE_VAL3));

		sourceTable.setTable(table);

		computeProcessor.process((Add) null);
		assertEquals(table, sourceTable.getTable());

		Add addition = Add.builder().column(-1).value(EMPTY).build();
		computeProcessor.process(addition);
		assertEquals(table, sourceTable.getTable());

		addition = Add.builder().column(1).value(DOLLAR_13).build();
		computeProcessor.process(addition);
		assertEquals(table, sourceTable.getTable());

		addition = Add.builder().column(1).value(ZERO).build();
		computeProcessor.process(addition);
		assertEquals(table, sourceTable.getTable());

		List<List<String>> result = Arrays.asList(
			Arrays.asList(ID1, "502.0", TWO, VALUE_VAL1),
			Arrays.asList(ID2, "1505.0", FIVE, VALUE_VAL2),
			Arrays.asList(ID1, "202.0", TWO, VALUE_VAL3));

		Add addColumn = Add.builder().column(2).value(DOLLAR_3).build();
		computeProcessor.process(addColumn);
		assertEquals(result, sourceTable.getTable());

		addColumn = Add.builder().column(2).value(DOLLAR_1).build();
		computeProcessor.process(addColumn);
		assertEquals(result, sourceTable.getTable());

		addColumn = Add.builder().column(2).value(ID1).build();
		computeProcessor.process(addColumn);
		assertEquals(result, sourceTable.getTable());

		Add addValue = Add.builder().column(2).value(TEN).build();
		computeProcessor.process(addValue);
		assertEquals(Arrays.asList(
			Arrays.asList(ID1, "512.0", TWO, VALUE_VAL1),
			Arrays.asList(ID2, "1515.0", FIVE, VALUE_VAL2),
			Arrays.asList(ID1, "212.0", TWO, VALUE_VAL3)),
			sourceTable.getTable());

		Add emptyAdd = Add.builder().column(4).value(FIVE).build();
		table = Arrays.asList(
			Arrays.asList(ID1, FIVE_HUNDRED, TWO, EMPTY),
			Arrays.asList(ID2, ONE_THOUSAND_FIVE_HUNDRED, FIVE, EMPTY),
			Arrays.asList(ID1, TWO_HUNDRED, TWO, EMPTY));
		sourceTable.setTable(table);
		computeProcessor.process(emptyAdd);
		assertEquals(table, sourceTable.getTable());

		emptyAdd = Add.builder().column(2).value(DOLLAR_4).build();
		computeProcessor.process(emptyAdd);
		assertEquals(table, sourceTable.getTable());
	}

	@Test
	void testProcessDivide() {
		List<List<String>> table = Arrays.asList(
			Arrays.asList(ID1, FIVE_HUNDRED, TWO, VALUE_VAL1),
			Arrays.asList(ID2, ONE_THOUSAND_FIVE_HUNDRED,FIVE, VALUE_VAL2),
			Arrays.asList(ID1, TWO_HUNDRED, TWO, VALUE_VAL3));

		sourceTable.setTable(table);

		computeProcessor.process((Divide) null);
		assertEquals(table, sourceTable.getTable());

		Divide divide = Divide.builder().column(-1).value(EMPTY).build();
		computeProcessor.process(divide);
		assertEquals(table, sourceTable.getTable());

		divide = Divide.builder().column(2).value(ONE).build();
		computeProcessor.process(divide);
		assertEquals(table, sourceTable.getTable());

		divide = Divide.builder().column(2).value(DOLLAR_13).build();
		computeProcessor.process(divide);
		assertEquals(table, sourceTable.getTable());

		divide = Divide.builder().column(2).value(ZERO).build();
		computeProcessor.process(divide);
		assertEquals(table, sourceTable.getTable());

		List<List<String>> result1 = Arrays.asList(
			Arrays.asList(ID1, "250.0", TWO, VALUE_VAL1),
			Arrays.asList(ID2, "300.0", FIVE, VALUE_VAL2),
			Arrays.asList(ID1, "100.0", TWO, VALUE_VAL3));

		Divide valueColumn = Divide.builder().column(2).value(DOLLAR_3).build();
		computeProcessor.process(valueColumn);
		assertEquals(result1, sourceTable.getTable());

		valueColumn = Divide.builder().column(2).value(DOLLAR_1).build();
		computeProcessor.process(valueColumn);
		assertEquals(result1, sourceTable.getTable());

		valueColumn = Divide.builder().column(2).value(ID1).build();
		computeProcessor.process(valueColumn);
		assertEquals(result1, sourceTable.getTable());

		List<List<String>> result2 = Arrays.asList(
			Arrays.asList(ID1, "25.0", TWO, VALUE_VAL1),
			Arrays.asList(ID2, "30.0", FIVE, VALUE_VAL2),
			Arrays.asList(ID1, "10.0", TWO, VALUE_VAL3));

		Divide valueValue = Divide.builder().column(2).value(TEN).build();
		computeProcessor.process(valueValue);
		assertEquals(result2, sourceTable.getTable());

		valueValue = Divide.builder().column(2).value(ZERO).build();
		computeProcessor.process(valueValue);
		assertEquals(result2, sourceTable.getTable());
	}

	@Test
	void testProcessMultiply() {
		SourceTable sourceTable = new SourceTable();
		ComputeProcessor computeProcessor = new ComputeProcessor();
		computeProcessor.setSourceTable(sourceTable);

		List<List<String>> table = Arrays.asList(
			Arrays.asList(ID1, FIVE_HUNDRED, TWO, VALUE_VAL1),
			Arrays.asList(ID2, ONE_THOUSAND_FIVE_HUNDRED,FIVE, VALUE_VAL2),
			Arrays.asList(ID1, TWO_HUNDRED, TWO, VALUE_VAL3));

		sourceTable.setTable(table);

		computeProcessor.process((Multiply) null);
		assertEquals(table, sourceTable.getTable());

		Multiply multiply = Multiply.builder().column(-1).value(EMPTY).build();
		computeProcessor.process(multiply);
		assertEquals(table, sourceTable.getTable());

		multiply = Multiply.builder().column(2).value(ONE).build();
		computeProcessor.process(multiply);
		assertEquals(table, sourceTable.getTable());

		multiply = Multiply.builder().column(2).value(DOLLAR_13).build();
		computeProcessor.process(multiply);
		assertEquals(table, sourceTable.getTable());

		List<List<String>> result = Arrays.asList(
			Arrays.asList(ID1, "1000.0", TWO, VALUE_VAL1),
			Arrays.asList(ID2, "7500.0", FIVE, VALUE_VAL2),
			Arrays.asList(ID1, "400.0", TWO, VALUE_VAL3));

		Multiply multiByColumn = Multiply.builder().column(2).value(DOLLAR_3).build();
		computeProcessor.process(multiByColumn);
		assertEquals(result, sourceTable.getTable());

		multiByColumn = Multiply.builder().column(2).value(DOLLAR_1).build();
		computeProcessor.process(multiByColumn);
		assertEquals(result, sourceTable.getTable());

		multiByColumn = Multiply.builder().column(2).value(ID1).build();
		computeProcessor.process(multiByColumn);
		assertEquals(result, sourceTable.getTable());

		Multiply valueValue = Multiply.builder().column(2).value(TEN).build();
		computeProcessor.process(valueValue);
		assertEquals(Arrays.asList(
			Arrays.asList(ID1, "10000.0", TWO, VALUE_VAL1),
			Arrays.asList(ID2, "75000.0", FIVE, VALUE_VAL2),
			Arrays.asList(ID1, "4000.0", TWO, VALUE_VAL3)),
			sourceTable.getTable());

		valueValue = Multiply.builder().column(2).value(ZERO).build();
		computeProcessor.process(valueValue);
		assertEquals(Arrays.asList(
			Arrays.asList(ID1, ZERO_POINT_ZERO, TWO, VALUE_VAL1),
			Arrays.asList(ID2, ZERO_POINT_ZERO, FIVE, VALUE_VAL2),
			Arrays.asList(ID1, ZERO_POINT_ZERO, TWO, VALUE_VAL3)),
			sourceTable.getTable());
	}

	@Test
	void testProcessSubtract() {
		List<List<String>> table = Arrays.asList(
			Arrays.asList(ID1, FIVE_HUNDRED, TWO, VALUE_VAL1),
			Arrays.asList(ID2, ONE_THOUSAND_FIVE_HUNDRED,FIVE, VALUE_VAL2),
			Arrays.asList(ID1, TWO_HUNDRED, TWO, VALUE_VAL3));

		sourceTable.setTable(table);

		computeProcessor.process((Subtract) null);
		assertEquals(table, sourceTable.getTable());

		Subtract substract = Subtract.builder().column(-1).value(EMPTY).build();
		computeProcessor.process(substract);
		assertEquals(table, sourceTable.getTable());

		substract = Subtract.builder().column(1).value(DOLLAR_13).build();
		computeProcessor.process(substract);
		assertEquals(table, sourceTable.getTable());

		substract = Subtract.builder().column(1).value(ZERO).build();
		computeProcessor.process(substract);
		assertEquals(table, sourceTable.getTable());

		List<List<String>> result = Arrays.asList(
			Arrays.asList(ID1, "498.0", TWO, VALUE_VAL1),
			Arrays.asList(ID2, "1495.0", FIVE, VALUE_VAL2),
			Arrays.asList(ID1, "198.0", TWO, VALUE_VAL3));

		Subtract substractColumn = Subtract.builder().column(2).value(DOLLAR_3).build();
		computeProcessor.process(substractColumn);
		assertEquals(result, sourceTable.getTable());

		substractColumn = Subtract.builder().column(2).value(DOLLAR_1).build();
		computeProcessor.process(substractColumn);
		assertEquals(result, sourceTable.getTable());

		substractColumn = Subtract.builder().column(2).value(ID1).build();
		computeProcessor.process(substractColumn);
		assertEquals(result, sourceTable.getTable());

		Subtract substractValue = Subtract.builder().column(2).value(TEN).build();
		computeProcessor.process(substractValue);
		assertEquals(Arrays.asList(
			Arrays.asList(ID1, "488.0", TWO, VALUE_VAL1),
			Arrays.asList(ID2, "1485.0", FIVE, VALUE_VAL2),
			Arrays.asList(ID1, "188.0", TWO, VALUE_VAL3)),
			sourceTable.getTable());
	}

	@Test
	void testPerformMathComputeOnLine() {
		List<List<String>> table = Collections.singletonList(Arrays.asList(SINGLE_SPACE, FOO, FOUR_POINT_ZERO));
		sourceTable.setTable(table);

		// column index > row size
		Divide divide = Divide
			.builder()
			.value(TWO)
			.column(4)
			.build();
		computeProcessor.process(divide);
		assertEquals(table, sourceTable.getTable());

		// column index <= row size, op1 is blank
		divide.setColumn(1);
		computeProcessor.process(divide);
		assertEquals(table, sourceTable.getTable());

		// column index <= row size, op1 is not blank, op2Index == -1
		table.get(0).set(0, "8.0");
		List<List<String>> result = Collections.singletonList(Arrays.asList(FOUR_POINT_ZERO, FOO, FOUR_POINT_ZERO));
		computeProcessor.process(divide);
		assertEquals(result, sourceTable.getTable());

		// column index <= row size, op1 is not blank, op2Index != -1, op2Index >= row size
		divide.setValue(DOLLAR_4);
		computeProcessor.process(divide);
		assertEquals(result, sourceTable.getTable());

		// column index <= row size, op1 is not blank, op2Index != -1, op2Index < row size
		divide.setValue(DOLLAR_3);
		result = Collections.singletonList(Arrays.asList("1.0", FOO, FOUR_POINT_ZERO));
		computeProcessor.process(divide);
		assertEquals(result, sourceTable.getTable());
	}

	@Test
	void testProcessAnd() {

		SourceTable sourceTable = new SourceTable();
		ComputeProcessor computeProcessor = new ComputeProcessor();
		computeProcessor.setSourceTable(sourceTable);

		List<List<String>> table = Arrays.asList(
			Arrays.asList(ID1, NAME1, MANUFACTURER1, ONE),	// 0000 0001
			Arrays.asList(ID2, NAME2, MANUFACTURER2, FOURTEEN),	// 0000 1110
			Arrays.asList(ID3, NAME3, MANUFACTURER3, TWO_HUNDRED_AND_FIFTY_FIVE));	// 1111 1110

		List<List<String>> tableResult = Arrays.asList(
			Arrays.asList(ID1, NAME1, MANUFACTURER1, ONE),
			Arrays.asList(ID2, NAME2, MANUFACTURER2, FOURTEEN),
			Arrays.asList(ID3, NAME3, MANUFACTURER3, TWO_HUNDRED_AND_FIFTY_FIVE));

		sourceTable.setTable(table);

		// test null source to visit
		computeProcessor.process((And) null);
		assertEquals(tableResult, sourceTable.getTable());

		// test TranslationTable is null
		And and = And.builder().column(0).value(ONE).build();	// and : 0000 0001
		computeProcessor.process(and);
		assertEquals(tableResult, sourceTable.getTable());

		// test column value is not an integer
		and.setColumn(3);
		computeProcessor.process(and);
		assertEquals(tableResult, sourceTable.getTable());

		// tests OK
		and.setColumn(4);
		tableResult = Arrays.asList(
			Arrays.asList(ID1, NAME1, MANUFACTURER1, ONE),
			Arrays.asList(ID2, NAME2, MANUFACTURER2, ZERO),
			Arrays.asList(ID3, NAME3, MANUFACTURER3, ONE));
		computeProcessor.process(and);
		assertEquals(tableResult, sourceTable.getTable());

		table = Arrays.asList(
			Arrays.asList(ID1, NAME1, MANUFACTURER1, ONE),	// 0000 0001
			Arrays.asList(ID2, NAME2, MANUFACTURER2, FOURTEEN),	// 0000 1110
			Arrays.asList(ID3, NAME3, MANUFACTURER3, TWO_HUNDRED_AND_FIFTY_FIVE));	// 1111 1110
		and.setValue(THIRTY);											// and:0001 1110

		sourceTable.setTable(table);

		tableResult = Arrays.asList(
			Arrays.asList(ID1, NAME1, MANUFACTURER1, ZERO),	// 0000 0001
			Arrays.asList(ID2, NAME2, MANUFACTURER2, FOURTEEN),	// 0000 1110
			Arrays.asList(ID3, NAME3, MANUFACTURER3, THIRTY));	// 0001 1110
		computeProcessor.process(and);
		assertEquals(tableResult, sourceTable.getTable());

		// test with column
		table = Arrays.asList(
			Arrays.asList(ID1, NAME1, TWO_HUNDRED_AND_FIFTY_FOUR, ONE),		// 1111 1110 & 0000 0001
			Arrays.asList(ID2, NAME2, THIRTY_SIX, FOURTEEN),		// 0010 0100 & 0000 1110
			Arrays.asList(ID3, NAME3, FORTY_ONE, TWO_HUNDRED_AND_FIFTY_FIVE));	// 0010 1001 & 1111 1111
		and.setValue(DOLLAR_3);

		sourceTable.setTable(table);

		tableResult = Arrays.asList(
			Arrays.asList(ID1, NAME1, TWO_HUNDRED_AND_FIFTY_FOUR, ZERO),	// 0000 0000
			Arrays.asList(ID2, NAME2, THIRTY_SIX, "4"),	// 0000 0100
			Arrays.asList(ID3, NAME3, FORTY_ONE, FORTY_ONE));	// 0010 1001
		computeProcessor.process(and);
		assertEquals(tableResult, sourceTable.getTable());
	}

	@Test
	void testProcessLeftConcat() {
		initializeSourceTable();

		// Test with empty LeftConcat
		LeftConcat leftConcat = new LeftConcat();

		computeProcessor.process(leftConcat);

		List<List<String>> table = sourceTable.getTable();

		assertEquals(LINE_1, table.get(0));
		assertEquals(LINE_2, table.get(1));
		assertEquals(LINE_3, table.get(2));

		// Test with LeftConcat without Value
		leftConcat.setColumn(3);

		computeProcessor.process(leftConcat);

		assertEquals(LINE_1, table.get(0));
		assertEquals(LINE_2, table.get(1));
		assertEquals(LINE_3, table.get(2));

		// Test with correct LeftConcat
		leftConcat.setValue(PREFIX);

		computeProcessor.process(leftConcat);

		assertEquals(LINE_1_RESULT_LEFT, table.get(0));
		assertEquals(LINE_2_RESULT_LEFT, table.get(1));
		assertEquals(LINE_3_RESULT_LEFT, table.get(2));

		// empty lines in table => add new column
		sourceTable.getTable().clear();
		sourceTable.getTable().add(new ArrayList<>());
		sourceTable.getTable().add(new ArrayList<>());
		sourceTable.getTable().add(new ArrayList<>());
		leftConcat.setColumn(1);
		leftConcat.setValue(FOO);
		computeProcessor.process(leftConcat);
		assertEquals(new ArrayList<>(Arrays.asList(FOO)), table.get(0));
		assertEquals(new ArrayList<>(Arrays.asList(FOO)), table.get(1));
		assertEquals(new ArrayList<>(Arrays.asList(FOO)), table.get(2));

	}

	@Test
	void testProcessLeftConcatOneColumn() {
		sourceTable.getTable().add(new ArrayList<>(LINE_1_ONE_COLUMN));
		sourceTable.getTable().add(new ArrayList<>(LINE_2_ONE_COLUMN));
		sourceTable.getTable().add(new ArrayList<>(LINE_3_ONE_COLUMN));

		LeftConcat leftConcat = LeftConcat.builder().column(1).value(PREFIX).build();

		computeProcessor.process(leftConcat);

		List<List<String>> table = sourceTable.getTable();

		assertEquals(new ArrayList<>(Collections.singletonList(PREFIX_ID1)), table.get(0));
		assertEquals(new ArrayList<>(Collections.singletonList(PREFIX_ID2)), table.get(1));
		assertEquals(new ArrayList<>(Collections.singletonList(PREFIX_ID3)), table.get(2));
	}

	@Test
	void testProcessLeftConcatColumn() {
		initializeSourceTable();

		LeftConcat leftConcat = LeftConcat.builder().column(3).value(DOLLAR_1).build();

		computeProcessor.process(leftConcat);

		List<List<String>> table = sourceTable.getTable();

		assertEquals(new ArrayList<>(Arrays.asList(ID1, NAME1, "ID1MANUFACTURER1", NUMBER_OF_DISKS1)), table.get(0));
		assertEquals(new ArrayList<>(Arrays.asList(ID2, NAME2, "ID2MANUFACTURER2", NUMBER_OF_DISKS2)), table.get(1));
		assertEquals(new ArrayList<>(Arrays.asList(ID3, NAME3, "ID3MANUFACTURER3", NUMBER_OF_DISKS3)), table.get(2));
	}

	@Test
	void testProcessLeftConcatNotColumn1() {
		initializeSourceTable();

		LeftConcat leftConcat = LeftConcat.builder().column(3).value("$1_").build();

		computeProcessor.process(leftConcat);

		List<List<String>> table = sourceTable.getTable();

		assertEquals(new ArrayList<>(Arrays.asList(ID1, NAME1, "$1_MANUFACTURER1", NUMBER_OF_DISKS1)), table.get(0));
		assertEquals(new ArrayList<>(Arrays.asList(ID2, NAME2, "$1_MANUFACTURER2", NUMBER_OF_DISKS2)), table.get(1));
		assertEquals(new ArrayList<>(Arrays.asList(ID3, NAME3, "$1_MANUFACTURER3", NUMBER_OF_DISKS3)), table.get(2));
	}

	@Test
	void testProcessLeftConcatNotColumn2() {
		initializeSourceTable();

		LeftConcat leftConcat = LeftConcat.builder().column(3).value(UNDERSCORE_DOLLAR_1).build();

		computeProcessor.process(leftConcat);

		List<List<String>> table = sourceTable.getTable();

		assertEquals(new ArrayList<>(Arrays.asList(ID1, NAME1, "_$1MANUFACTURER1", NUMBER_OF_DISKS1)), table.get(0));
		assertEquals(new ArrayList<>(Arrays.asList(ID2, NAME2, "_$1MANUFACTURER2", NUMBER_OF_DISKS2)), table.get(1));
		assertEquals(new ArrayList<>(Arrays.asList(ID3, NAME3, "_$1MANUFACTURER3", NUMBER_OF_DISKS3)), table.get(2));
	}

	@Test
	void testProcessLeftConcatNewColumn() {
		initializeSourceTable();

		LeftConcat leftConcat = LeftConcat.builder().column(3).value("new,Column;prefix_").build();

		computeProcessor.process(leftConcat);

		List<List<String>> table = sourceTable.getTable();

		assertEquals(new ArrayList<>(Arrays.asList(ID1, NAME1, NEW_COMMA_COLUMN, PREFIX_MANUFACTURER1, NUMBER_OF_DISKS1)), table.get(0));
		assertEquals(new ArrayList<>(Arrays.asList(ID2, NAME2, NEW_COMMA_COLUMN, PREFIX_MANUFACTURER2, NUMBER_OF_DISKS2)), table.get(1));
		assertEquals(new ArrayList<>(Arrays.asList(ID3, NAME3, NEW_COMMA_COLUMN, PREFIX_MANUFACTURER3, NUMBER_OF_DISKS3)), table.get(2));
	}

	@Test
	void testProcessLeftConcatTwoNewColumns() {
		initializeSourceTable();

		LeftConcat leftConcat = LeftConcat.builder().column(1).value("new,$4;AnotherNew.Column;prefix_").build();

		computeProcessor.process(leftConcat);

		List<List<String>> table = sourceTable.getTable();

		assertEquals(new ArrayList<>(Arrays.asList(NEW_COMMA_DOLLAR_4, ANOTHER_NEW_COLUMN, PREFIX_ID1, NAME1, MANUFACTURER1, NUMBER_OF_DISKS1)), table.get(0));
		assertEquals(new ArrayList<>(Arrays.asList(NEW_COMMA_DOLLAR_4, ANOTHER_NEW_COLUMN, PREFIX_ID2, NAME2, MANUFACTURER2, NUMBER_OF_DISKS2)), table.get(1));
		assertEquals(new ArrayList<>(Arrays.asList(NEW_COMMA_DOLLAR_4, ANOTHER_NEW_COLUMN, PREFIX_ID3, NAME3, MANUFACTURER3, NUMBER_OF_DISKS3)), table.get(2));
	}

	@Test
	void testProcessRightConcat() {
		initializeSourceTable();

		// Test with empty RightConcat
		RightConcat rightConcat = new RightConcat();

		computeProcessor.process(rightConcat);

		List<List<String>> table = sourceTable.getTable();

		assertEquals(LINE_1, table.get(0));
		assertEquals(LINE_2, table.get(1));
		assertEquals(LINE_3, table.get(2));

		// Test with RightConcat without Value
		rightConcat.setColumn(3);

		computeProcessor.process(rightConcat);

		assertEquals(LINE_1, table.get(0));
		assertEquals(LINE_2, table.get(1));
		assertEquals(LINE_3, table.get(2));

		// Test with correct RightConcat
		rightConcat.setValue(SUFFIX);

		computeProcessor.process(rightConcat);

		assertEquals(LINE_1_RESULT_RIGHT, table.get(0));
		assertEquals(LINE_2_RESULT_RIGHT, table.get(1));
		assertEquals(LINE_3_RESULT_RIGHT, table.get(2));
		// index = size + 1 => add new column
		rightConcat.setColumn(5);
		rightConcat.setValue(FOO);
		computeProcessor.process(rightConcat);
		List<List<String>> expected = Arrays.asList(LINE_1_RESULT_RIGHT, LINE_2_RESULT_RIGHT, LINE_3_RESULT_RIGHT);
		expected.get(0).add(FOO);
		expected.get(1).add(FOO);
		expected.get(2).add(FOO);
		assertEquals(expected, table);

		// index > size + 1  => out of bounds nothing changed
		rightConcat.setColumn(15);
		rightConcat.setValue(FOO);
		computeProcessor.process(rightConcat);
		assertEquals(expected, table);
	}

	@Test
	void testProcessRightConcatOneColumn() {
		sourceTable.getTable().add(new ArrayList<>(LINE_1_ONE_COLUMN));
		sourceTable.getTable().add(new ArrayList<>(LINE_2_ONE_COLUMN));
		sourceTable.getTable().add(new ArrayList<>(LINE_3_ONE_COLUMN));

		RightConcat rightConcat = RightConcat.builder().column(1).value(SUFFIX).build();

		computeProcessor.process(rightConcat);

		List<List<String>> table = sourceTable.getTable();

		assertEquals(new ArrayList<>(Collections.singletonList(ID1_SUFFIX)), table.get(0));
		assertEquals(new ArrayList<>(Collections.singletonList(ID2_SUFFIX)), table.get(1));
		assertEquals(new ArrayList<>(Collections.singletonList(ID3_SUFFIX)), table.get(2));
	}

	@Test
	void testProcessRightConcatColumn() {
		initializeSourceTable();

		RightConcat rightConcat = RightConcat.builder().column(3).value(DOLLAR_1).build();

		computeProcessor.process(rightConcat);

		List<List<String>> table = sourceTable.getTable();

		assertEquals(new ArrayList<>(Arrays.asList(ID1, NAME1, "MANUFACTURER1ID1", NUMBER_OF_DISKS1)), table.get(0));
		assertEquals(new ArrayList<>(Arrays.asList(ID2, NAME2, "MANUFACTURER2ID2", NUMBER_OF_DISKS2)), table.get(1));
		assertEquals(new ArrayList<>(Arrays.asList(ID3, NAME3, "MANUFACTURER3ID3", NUMBER_OF_DISKS3)), table.get(2));
	}

	@Test
	void testProcessRightConcatNotColumn1() {
		initializeSourceTable();

		RightConcat rightConcat = RightConcat.builder().column(3).value(UNDERSCORE_DOLLAR_1).build();

		computeProcessor.process(rightConcat);

		List<List<String>> table = sourceTable.getTable();

		assertEquals(new ArrayList<>(Arrays.asList(ID1, NAME1, "MANUFACTURER1_$1", NUMBER_OF_DISKS1)), table.get(0));
		assertEquals(new ArrayList<>(Arrays.asList(ID2, NAME2, "MANUFACTURER2_$1", NUMBER_OF_DISKS2)), table.get(1));
		assertEquals(new ArrayList<>(Arrays.asList(ID3, NAME3, "MANUFACTURER3_$1", NUMBER_OF_DISKS3)), table.get(2));
	}

	@Test
	void testProcessRightConcatNewColumn() {
		initializeSourceTable();

		RightConcat rightConcat = RightConcat.builder().column(3).value("_suffix;new,Column").build();

		computeProcessor.process(rightConcat);

		List<List<String>> table = sourceTable.getTable();

		assertEquals(new ArrayList<>(Arrays.asList(ID1, NAME1, MANUFACTURER1_SUFFIX, NEW_COMMA_COLUMN, NUMBER_OF_DISKS1)), table.get(0));
		assertEquals(new ArrayList<>(Arrays.asList(ID2, NAME2, MANUFACTURER2_SUFFIX, NEW_COMMA_COLUMN, NUMBER_OF_DISKS2)), table.get(1));
		assertEquals(new ArrayList<>(Arrays.asList(ID3, NAME3, MANUFACTURER3_SUFFIX, NEW_COMMA_COLUMN, NUMBER_OF_DISKS3)), table.get(2));
	}

	@Test
	void testProcessRightConcatTwoNewColumns() {
		initializeSourceTable();

		RightConcat rightConcat = RightConcat.builder().column(1).value("_suffix;new,$4;AnotherNew.Column").build();

		computeProcessor.process(rightConcat);

		List<List<String>> table = sourceTable.getTable();

		assertEquals(new ArrayList<>(Arrays.asList(ID1_SUFFIX, NEW_COMMA_DOLLAR_4, ANOTHER_NEW_COLUMN, NAME1, MANUFACTURER1, NUMBER_OF_DISKS1)), table.get(0));
		assertEquals(new ArrayList<>(Arrays.asList(ID2_SUFFIX, NEW_COMMA_DOLLAR_4, ANOTHER_NEW_COLUMN, NAME2, MANUFACTURER2, NUMBER_OF_DISKS2)), table.get(1));
		assertEquals(new ArrayList<>(Arrays.asList(ID3_SUFFIX, NEW_COMMA_DOLLAR_4, ANOTHER_NEW_COLUMN, NAME3, MANUFACTURER3, NUMBER_OF_DISKS3)), table.get(2));
	}

	@Test
	void testProcessRightConcatNoOperation() {
		initializeSourceTable();

		// RightConcat is null
		computeProcessor.setSourceTable(SourceTable.empty());
		computeProcessor.process((RightConcat) null);
		assertNotNull(computeProcessor.getSourceTable().getTable());
		assertTrue(computeProcessor.getSourceTable().getTable().isEmpty());

		// RightConcat is not null, RightConcat.getColumn() is not null, RightConcat.getValue() is not null,
		// RightConcat.getColumn() <= 0
		RightConcat rightConcat = RightConcat.builder().value(SUFFIX).column(0).build();
		computeProcessor.process(rightConcat);
		assertTrue(computeProcessor.getSourceTable().getTable().isEmpty());

		// RightConcat is not null, RightConcat.getColumn() is not null, RightConcat.getValue() is not null,
		// RightConcat.getColumn() > 0,
		// computeProcessor.getSourceTable() is null
		rightConcat.setColumn(1);
		computeProcessor.setSourceTable(null);
		computeProcessor.process(rightConcat);
		assertNull(computeProcessor.getSourceTable());

		// RightConcat is not null, RightConcat.getColumn() is not null, RightConcat.getValue() is not null,
		// RightConcat.getColumn() > 0,
		// computeProcessor.getSourceTable() is not null, computeProcessor.getSourceTable().getTable() is null
		computeProcessor.setSourceTable(SourceTable.builder().table(null).build());
		computeProcessor.process(rightConcat);
		assertNull(computeProcessor.getSourceTable().getTable());

		// RightConcat is not null, RightConcat.getColumn() is not null, RightConcat.getValue() is not null,
		// RightConcat.getColumn() > 0,
		// computeProcessor.getSourceTable() is not null, computeProcessor.getSourceTable().getTable() is not null,
		// computeProcessor.getSourceTable().getTable().isEmpty()
		computeProcessor.setSourceTable(SourceTable.empty());
		computeProcessor.process(rightConcat);
		assertTrue(computeProcessor.getSourceTable().getTable().isEmpty());

		// RightConcat is not null, RightConcat.getColumn() is not null, RightConcat.getValue() is not null,
		// RightConcat.getColumn() > 0,
		// computeProcessor.getSourceTable() is not null, computeProcessor.getSourceTable().getTable() is not null,
		// computeProcessor.getSourceTable().getTable() is not empty,
		// RightConcat.getColumn() > sourceTable.getTable().get(0).size()
		computeProcessor.setSourceTable(
			SourceTable
			.builder()
			.table(
				Collections.singletonList(
					Collections.singletonList(FOO)
					)
				)
			.build());
		rightConcat.setColumn(5);
		computeProcessor.process(rightConcat);
		assertEquals(1, computeProcessor.getSourceTable().getTable().size());

		// RightConcat is not null, RightConcat.getColumn() is not null, RightConcat.getValue() is not null,
		// RightConcat.getColumn() > 0,
		// computeProcessor.getSourceTable() is not null, computeProcessor.getSourceTable().getTable() is not null,
		// computeProcessor.getSourceTable().getTable() is not empty,
		// RightConcat.getColumn() <= sourceTable.getTable().get(0).size(),
		// matcher.matches, concatColumnIndex < sourceTable.getTable().get(0).size()
		rightConcat.setColumn(1);
		rightConcat.setValue("$2");
		computeProcessor.process(rightConcat);
		assertEquals(1, computeProcessor.getSourceTable().getTable().size());
	}
}
