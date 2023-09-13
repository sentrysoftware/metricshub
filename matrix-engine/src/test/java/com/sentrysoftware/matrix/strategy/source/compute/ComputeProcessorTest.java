package com.sentrysoftware.matrix.strategy.source.compute;

import static com.sentrysoftware.matrix.constants.Constants.EMPTY;
import static com.sentrysoftware.matrix.constants.Constants.SINGLE_SPACE;
import static com.sentrysoftware.matrix.constants.Constants.VALUE_VAL1;
import static com.sentrysoftware.matrix.constants.Constants.VALUE_VAL2;
import static com.sentrysoftware.matrix.constants.Constants.VALUE_VAL3;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Add;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.And;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Divide;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Multiply;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.PerBitTranslation;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Subtract;
import com.sentrysoftware.matrix.strategy.source.SourceTable;

class ComputeProcessorTest {

	private static final String DOLLAR_1 = "$1";
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
	private static final String ID1 = "ID1";
	private static final String ID2 = "ID2";
	private static final String ID3 = "ID3";
	private static final String FOO = "FOO";
	private static final String MANUFACTURER1 = "MANUFACTURER1";
	private static final String MANUFACTURER2 = "MANUFACTURER2";
	private static final String MANUFACTURER3 = "MANUFACTURER3";
	private static final String NAME1 = "NAME1";
	private static final String NAME2 = "NAME2";
	private static final String NAME3 = "NAME3";

	@Test
	void testProcessAdd() {
		SourceTable sourceTable = new SourceTable();
		ComputeProcessor computeProcessor = new ComputeProcessor();
		computeProcessor.setSourceTable(sourceTable);

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
		SourceTable sourceTable = new SourceTable();
		ComputeProcessor computeProcessor = new ComputeProcessor();
		computeProcessor.setSourceTable(sourceTable);

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
		SourceTable sourceTable = new SourceTable();
		ComputeProcessor computeProcessor = new ComputeProcessor();
		computeProcessor.setSourceTable(sourceTable);

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

		SourceTable sourceTable = new SourceTable();
		ComputeProcessor computeProcessor = new ComputeProcessor();
		computeProcessor.setSourceTable(sourceTable);

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
}
