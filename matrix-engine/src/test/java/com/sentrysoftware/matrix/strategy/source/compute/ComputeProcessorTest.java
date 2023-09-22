package com.sentrysoftware.matrix.strategy.source.compute;

import static com.sentrysoftware.matrix.constants.Constants.EMPTY;
import static com.sentrysoftware.matrix.constants.Constants.LOCALHOST;
import static com.sentrysoftware.matrix.constants.Constants.SINGLE_SPACE;
import static com.sentrysoftware.matrix.constants.Constants.VALUE_VAL1;
import static com.sentrysoftware.matrix.constants.Constants.VALUE_VAL2;
import static com.sentrysoftware.matrix.constants.Constants.VALUE_VAL3;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;

import com.sentrysoftware.matrix.common.helpers.ResourceHelper;
import com.sentrysoftware.matrix.configuration.HostConfiguration;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.ConnectorStore;
import com.sentrysoftware.matrix.connector.model.common.DeviceKind;
import com.sentrysoftware.matrix.connector.model.common.EmbeddedFile;
import com.sentrysoftware.matrix.connector.model.common.ReferenceTranslationTable;
import com.sentrysoftware.matrix.connector.model.common.TranslationTable;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Add;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.And;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.ArrayTranslate;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Awk;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Divide;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Json2Csv;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.LeftConcat;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Multiply;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.RightConcat;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Substring;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Subtract;
import com.sentrysoftware.matrix.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.matrix.strategy.source.SourceTable;
import com.sentrysoftware.matrix.strategy.utils.EmbeddedFileHelper;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.apache.groovy.util.Maps;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ComputeProcessorTest {

	private SourceTable sourceTable;

	@InjectMocks
	private ComputeProcessor computeProcessor;

	@Spy
	private MatsyaClientsExecutor matsyaClientsExecutorMock;

	@Mock
	private ConnectorStore connectorStoreMock;

	private static final String DOLLAR_1 = "$1";
	private static final String UNDERSCORE_DOLLAR_1 = "_$1";
	private static final String DOLLAR_3 = "$3";
	private static final String DOLLAR_4 = "$4";
	private static final String DOLLAR_13 = "$13";
	private static final String ZERO = "0";
	private static final String ZERO_POINT_ZERO = "0.0";
	private static final String ONE = "1";
	private static final String ONE_TWO_THREE = "1, 2, 3";
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
	private static final String BAR = "BAR";
	private static final String BAZ = "BAZ";
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
	private static final String TYPE1 = "TYPE1";
	private static final String TYPE2 = "TYPE2";
	private static final String TYPE3 = "TYPE3";

	private static final List<String> LINE_1 = Arrays.asList(ID1, NAME1, MANUFACTURER1, NUMBER_OF_DISKS1);
	private static final List<String> LINE_2 = Arrays.asList(ID2, NAME2, MANUFACTURER2, NUMBER_OF_DISKS2);
	private static final List<String> LINE_3 = Arrays.asList(ID3, NAME3, MANUFACTURER3, NUMBER_OF_DISKS3);

	private static final List<String> LINE_1_RESULT_LEFT = new ArrayList<>(
		Arrays.asList(ID1, NAME1, PREFIX_MANUFACTURER1, NUMBER_OF_DISKS1)
	);
	private static final List<String> LINE_2_RESULT_LEFT = new ArrayList<>(
		Arrays.asList(ID2, NAME2, PREFIX_MANUFACTURER2, NUMBER_OF_DISKS2)
	);
	private static final List<String> LINE_3_RESULT_LEFT = new ArrayList<>(
		Arrays.asList(ID3, NAME3, PREFIX_MANUFACTURER3, NUMBER_OF_DISKS3)
	);

	private static final List<String> LINE_1_RESULT_RIGHT = new ArrayList<>(
		Arrays.asList(ID1, NAME1, MANUFACTURER1_SUFFIX, NUMBER_OF_DISKS1)
	);
	private static final List<String> LINE_2_RESULT_RIGHT = new ArrayList<>(
		Arrays.asList(ID2, NAME2, MANUFACTURER2_SUFFIX, NUMBER_OF_DISKS2)
	);
	private static final List<String> LINE_3_RESULT_RIGHT = new ArrayList<>(
		Arrays.asList(ID3, NAME3, MANUFACTURER3_SUFFIX, NUMBER_OF_DISKS3)
	);

	private static final List<String> LINE_1_ONE_COLUMN = new ArrayList<>(Collections.singletonList(ID1));
	private static final List<String> LINE_2_ONE_COLUMN = new ArrayList<>(Collections.singletonList(ID2));
	private static final List<String> LINE_3_ONE_COLUMN = new ArrayList<>(Collections.singletonList(ID3));

	private static final String TABLE_SEP = ";";

	private TelemetryManager telemetryManager;

	@BeforeEach
	void setUp() {
		computeProcessor = new ComputeProcessor();
		sourceTable = new SourceTable();
		computeProcessor.setSourceTable(sourceTable);
		computeProcessor.setHostname(LOCALHOST);
		telemetryManager =
			TelemetryManager
				.builder()
				.hostConfiguration(
					HostConfiguration.builder().hostname(LOCALHOST).hostId(LOCALHOST).hostType(DeviceKind.WINDOWS).build()
				)
				.build();
		matsyaClientsExecutorMock.setTelemetryManager(telemetryManager);
		computeProcessor.setMatsyaClientsExecutor(matsyaClientsExecutorMock);
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
			Arrays.asList(ID1, TWO_HUNDRED, TWO, VALUE_VAL3)
		);

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

		final List<List<String>> result = Arrays.asList(
			Arrays.asList(ID1, "502.0", TWO, VALUE_VAL1),
			Arrays.asList(ID2, "1505.0", FIVE, VALUE_VAL2),
			Arrays.asList(ID1, "202.0", TWO, VALUE_VAL3)
		);

		Add addColumn = Add.builder().column(2).value(DOLLAR_3).build();
		computeProcessor.process(addColumn);
		assertEquals(result, sourceTable.getTable());

		addColumn = Add.builder().column(2).value(DOLLAR_1).build();
		computeProcessor.process(addColumn);
		assertEquals(result, sourceTable.getTable());

		addColumn = Add.builder().column(2).value(ID1).build();
		computeProcessor.process(addColumn);
		assertEquals(result, sourceTable.getTable());

		final Add addValue = Add.builder().column(2).value(TEN).build();
		computeProcessor.process(addValue);
		assertEquals(
			Arrays.asList(
				Arrays.asList(ID1, "512.0", TWO, VALUE_VAL1),
				Arrays.asList(ID2, "1515.0", FIVE, VALUE_VAL2),
				Arrays.asList(ID1, "212.0", TWO, VALUE_VAL3)
			),
			sourceTable.getTable()
		);

		Add emptyAdd = Add.builder().column(4).value(FIVE).build();
		table =
			Arrays.asList(
				Arrays.asList(ID1, FIVE_HUNDRED, TWO, EMPTY),
				Arrays.asList(ID2, ONE_THOUSAND_FIVE_HUNDRED, FIVE, EMPTY),
				Arrays.asList(ID1, TWO_HUNDRED, TWO, EMPTY)
			);
		sourceTable.setTable(table);
		computeProcessor.process(emptyAdd);
		assertEquals(table, sourceTable.getTable());

		emptyAdd = Add.builder().column(2).value(DOLLAR_4).build();
		computeProcessor.process(emptyAdd);
		assertEquals(table, sourceTable.getTable());
	}

	@Test
	void testProcessDivide() {
		final List<List<String>> table = Arrays.asList(
			Arrays.asList(ID1, FIVE_HUNDRED, TWO, VALUE_VAL1),
			Arrays.asList(ID2, ONE_THOUSAND_FIVE_HUNDRED, FIVE, VALUE_VAL2),
			Arrays.asList(ID1, TWO_HUNDRED, TWO, VALUE_VAL3)
		);

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

		final List<List<String>> result1 = Arrays.asList(
			Arrays.asList(ID1, "250.0", TWO, VALUE_VAL1),
			Arrays.asList(ID2, "300.0", FIVE, VALUE_VAL2),
			Arrays.asList(ID1, "100.0", TWO, VALUE_VAL3)
		);

		Divide valueColumn = Divide.builder().column(2).value(DOLLAR_3).build();
		computeProcessor.process(valueColumn);
		assertEquals(result1, sourceTable.getTable());

		valueColumn = Divide.builder().column(2).value(DOLLAR_1).build();
		computeProcessor.process(valueColumn);
		assertEquals(result1, sourceTable.getTable());

		valueColumn = Divide.builder().column(2).value(ID1).build();
		computeProcessor.process(valueColumn);
		assertEquals(result1, sourceTable.getTable());

		final List<List<String>> result2 = Arrays.asList(
			Arrays.asList(ID1, "25.0", TWO, VALUE_VAL1),
			Arrays.asList(ID2, "30.0", FIVE, VALUE_VAL2),
			Arrays.asList(ID1, "10.0", TWO, VALUE_VAL3)
		);

		Divide valueValue = Divide.builder().column(2).value(TEN).build();
		computeProcessor.process(valueValue);
		assertEquals(result2, sourceTable.getTable());

		valueValue = Divide.builder().column(2).value(ZERO).build();
		computeProcessor.process(valueValue);
		assertEquals(result2, sourceTable.getTable());
	}

	@Test
	void testProcessMultiply() {
		final List<List<String>> table = Arrays.asList(
			Arrays.asList(ID1, FIVE_HUNDRED, TWO, VALUE_VAL1),
			Arrays.asList(ID2, ONE_THOUSAND_FIVE_HUNDRED, FIVE, VALUE_VAL2),
			Arrays.asList(ID1, TWO_HUNDRED, TWO, VALUE_VAL3)
		);

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

		final List<List<String>> result = Arrays.asList(
			Arrays.asList(ID1, "1000.0", TWO, VALUE_VAL1),
			Arrays.asList(ID2, "7500.0", FIVE, VALUE_VAL2),
			Arrays.asList(ID1, "400.0", TWO, VALUE_VAL3)
		);

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
		assertEquals(
			Arrays.asList(
				Arrays.asList(ID1, "10000.0", TWO, VALUE_VAL1),
				Arrays.asList(ID2, "75000.0", FIVE, VALUE_VAL2),
				Arrays.asList(ID1, "4000.0", TWO, VALUE_VAL3)
			),
			sourceTable.getTable()
		);

		valueValue = Multiply.builder().column(2).value(ZERO).build();
		computeProcessor.process(valueValue);
		assertEquals(
			Arrays.asList(
				Arrays.asList(ID1, ZERO_POINT_ZERO, TWO, VALUE_VAL1),
				Arrays.asList(ID2, ZERO_POINT_ZERO, FIVE, VALUE_VAL2),
				Arrays.asList(ID1, ZERO_POINT_ZERO, TWO, VALUE_VAL3)
			),
			sourceTable.getTable()
		);
	}

	@Test
	void testProcessSubtract() {
		final List<List<String>> table = Arrays.asList(
			Arrays.asList(ID1, FIVE_HUNDRED, TWO, VALUE_VAL1),
			Arrays.asList(ID2, ONE_THOUSAND_FIVE_HUNDRED, FIVE, VALUE_VAL2),
			Arrays.asList(ID1, TWO_HUNDRED, TWO, VALUE_VAL3)
		);

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

		final List<List<String>> result = Arrays.asList(
			Arrays.asList(ID1, "498.0", TWO, VALUE_VAL1),
			Arrays.asList(ID2, "1495.0", FIVE, VALUE_VAL2),
			Arrays.asList(ID1, "198.0", TWO, VALUE_VAL3)
		);

		Subtract substractColumn = Subtract.builder().column(2).value(DOLLAR_3).build();
		computeProcessor.process(substractColumn);
		assertEquals(result, sourceTable.getTable());

		substractColumn = Subtract.builder().column(2).value(DOLLAR_1).build();
		computeProcessor.process(substractColumn);
		assertEquals(result, sourceTable.getTable());

		substractColumn = Subtract.builder().column(2).value(ID1).build();
		computeProcessor.process(substractColumn);
		assertEquals(result, sourceTable.getTable());

		final Subtract substractValue = Subtract.builder().column(2).value(TEN).build();
		computeProcessor.process(substractValue);
		assertEquals(
			Arrays.asList(
				Arrays.asList(ID1, "488.0", TWO, VALUE_VAL1),
				Arrays.asList(ID2, "1485.0", FIVE, VALUE_VAL2),
				Arrays.asList(ID1, "188.0", TWO, VALUE_VAL3)
			),
			sourceTable.getTable()
		);
	}

	@Test
	void testPerformMathComputeOnLine() {
		final List<List<String>> table = Collections.singletonList(Arrays.asList(SINGLE_SPACE, FOO, FOUR_POINT_ZERO));
		sourceTable.setTable(table);

		// column index > row size
		final Divide divide = Divide.builder().value(TWO).column(4).build();
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
		List<List<String>> table = Arrays.asList(
			Arrays.asList(ID1, NAME1, MANUFACTURER1, ONE), // 0000 0001
			Arrays.asList(ID2, NAME2, MANUFACTURER2, FOURTEEN), // 0000 1110
			Arrays.asList(ID3, NAME3, MANUFACTURER3, TWO_HUNDRED_AND_FIFTY_FIVE)
		); // 1111 1110

		List<List<String>> tableResult = Arrays.asList(
			Arrays.asList(ID1, NAME1, MANUFACTURER1, ONE),
			Arrays.asList(ID2, NAME2, MANUFACTURER2, FOURTEEN),
			Arrays.asList(ID3, NAME3, MANUFACTURER3, TWO_HUNDRED_AND_FIFTY_FIVE)
		);

		sourceTable.setTable(table);

		// test null source to process
		computeProcessor.process((And) null);
		assertEquals(tableResult, sourceTable.getTable());

		// test TranslationTable is null
		final And and = And.builder().column(0).value(ONE).build(); // and : 0000 0001
		computeProcessor.process(and);
		assertEquals(tableResult, sourceTable.getTable());

		// test column value is not an integer
		and.setColumn(3);
		computeProcessor.process(and);
		assertEquals(tableResult, sourceTable.getTable());

		// tests OK
		and.setColumn(4);
		tableResult =
			Arrays.asList(
				Arrays.asList(ID1, NAME1, MANUFACTURER1, ONE),
				Arrays.asList(ID2, NAME2, MANUFACTURER2, ZERO),
				Arrays.asList(ID3, NAME3, MANUFACTURER3, ONE)
			);
		computeProcessor.process(and);
		assertEquals(tableResult, sourceTable.getTable());

		table =
			Arrays.asList(
				Arrays.asList(ID1, NAME1, MANUFACTURER1, ONE), // 0000 0001
				Arrays.asList(ID2, NAME2, MANUFACTURER2, FOURTEEN), // 0000 1110
				Arrays.asList(ID3, NAME3, MANUFACTURER3, TWO_HUNDRED_AND_FIFTY_FIVE)
			); // 1111 1110
		and.setValue(THIRTY); // and:0001 1110

		sourceTable.setTable(table);

		tableResult =
			Arrays.asList(
				Arrays.asList(ID1, NAME1, MANUFACTURER1, ZERO), // 0000 0001
				Arrays.asList(ID2, NAME2, MANUFACTURER2, FOURTEEN), // 0000 1110
				Arrays.asList(ID3, NAME3, MANUFACTURER3, THIRTY)
			); // 0001 1110
		computeProcessor.process(and);
		assertEquals(tableResult, sourceTable.getTable());

		// test with column
		table =
			Arrays.asList(
				Arrays.asList(ID1, NAME1, TWO_HUNDRED_AND_FIFTY_FOUR, ONE), // 1111 1110 & 0000 0001
				Arrays.asList(ID2, NAME2, THIRTY_SIX, FOURTEEN), // 0010 0100 & 0000 1110
				Arrays.asList(ID3, NAME3, FORTY_ONE, TWO_HUNDRED_AND_FIFTY_FIVE)
			); // 0010 1001 & 1111 1111
		and.setValue(DOLLAR_3);

		sourceTable.setTable(table);

		tableResult =
			Arrays.asList(
				Arrays.asList(ID1, NAME1, TWO_HUNDRED_AND_FIFTY_FOUR, ZERO), // 0000 0000
				Arrays.asList(ID2, NAME2, THIRTY_SIX, "4"), // 0000 0100
				Arrays.asList(ID3, NAME3, FORTY_ONE, FORTY_ONE)
			); // 0010 1001
		computeProcessor.process(and);
		assertEquals(tableResult, sourceTable.getTable());
	}

	@Test
	void testProcessLeftConcat() {
		initializeSourceTable();

		// Test with empty LeftConcat
		final LeftConcat leftConcat = new LeftConcat();

		computeProcessor.process(leftConcat);

		final List<List<String>> table = sourceTable.getTable();

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

		final LeftConcat leftConcat = LeftConcat.builder().column(1).value(PREFIX).build();

		computeProcessor.process(leftConcat);

		final List<List<String>> table = sourceTable.getTable();

		assertEquals(new ArrayList<>(Collections.singletonList(PREFIX_ID1)), table.get(0));
		assertEquals(new ArrayList<>(Collections.singletonList(PREFIX_ID2)), table.get(1));
		assertEquals(new ArrayList<>(Collections.singletonList(PREFIX_ID3)), table.get(2));
	}

	@Test
	void testProcessLeftConcatColumn() {
		initializeSourceTable();

		final LeftConcat leftConcat = LeftConcat.builder().column(3).value(DOLLAR_1).build();

		computeProcessor.process(leftConcat);

		final List<List<String>> table = sourceTable.getTable();

		assertEquals(new ArrayList<>(Arrays.asList(ID1, NAME1, "ID1MANUFACTURER1", NUMBER_OF_DISKS1)), table.get(0));
		assertEquals(new ArrayList<>(Arrays.asList(ID2, NAME2, "ID2MANUFACTURER2", NUMBER_OF_DISKS2)), table.get(1));
		assertEquals(new ArrayList<>(Arrays.asList(ID3, NAME3, "ID3MANUFACTURER3", NUMBER_OF_DISKS3)), table.get(2));
	}

	@Test
	void testProcessLeftConcatNotColumn1() {
		initializeSourceTable();

		final LeftConcat leftConcat = LeftConcat.builder().column(3).value("$1_").build();

		computeProcessor.process(leftConcat);

		final List<List<String>> table = sourceTable.getTable();

		assertEquals(new ArrayList<>(Arrays.asList(ID1, NAME1, "$1_MANUFACTURER1", NUMBER_OF_DISKS1)), table.get(0));
		assertEquals(new ArrayList<>(Arrays.asList(ID2, NAME2, "$1_MANUFACTURER2", NUMBER_OF_DISKS2)), table.get(1));
		assertEquals(new ArrayList<>(Arrays.asList(ID3, NAME3, "$1_MANUFACTURER3", NUMBER_OF_DISKS3)), table.get(2));
	}

	@Test
	void testProcessLeftConcatNotColumn2() {
		initializeSourceTable();

		final LeftConcat leftConcat = LeftConcat.builder().column(3).value(UNDERSCORE_DOLLAR_1).build();

		computeProcessor.process(leftConcat);

		final List<List<String>> table = sourceTable.getTable();

		assertEquals(new ArrayList<>(Arrays.asList(ID1, NAME1, "_$1MANUFACTURER1", NUMBER_OF_DISKS1)), table.get(0));
		assertEquals(new ArrayList<>(Arrays.asList(ID2, NAME2, "_$1MANUFACTURER2", NUMBER_OF_DISKS2)), table.get(1));
		assertEquals(new ArrayList<>(Arrays.asList(ID3, NAME3, "_$1MANUFACTURER3", NUMBER_OF_DISKS3)), table.get(2));
	}

	@Test
	void testProcessLeftConcatNewColumn() {
		initializeSourceTable();

		final LeftConcat leftConcat = LeftConcat.builder().column(3).value("new,Column;prefix_").build();

		computeProcessor.process(leftConcat);

		final List<List<String>> table = sourceTable.getTable();

		assertEquals(
			new ArrayList<>(Arrays.asList(ID1, NAME1, NEW_COMMA_COLUMN, PREFIX_MANUFACTURER1, NUMBER_OF_DISKS1)),
			table.get(0)
		);
		assertEquals(
			new ArrayList<>(Arrays.asList(ID2, NAME2, NEW_COMMA_COLUMN, PREFIX_MANUFACTURER2, NUMBER_OF_DISKS2)),
			table.get(1)
		);
		assertEquals(
			new ArrayList<>(Arrays.asList(ID3, NAME3, NEW_COMMA_COLUMN, PREFIX_MANUFACTURER3, NUMBER_OF_DISKS3)),
			table.get(2)
		);
	}

	@Test
	void testProcessLeftConcatTwoNewColumns() {
		initializeSourceTable();

		final LeftConcat leftConcat = LeftConcat.builder().column(1).value("new,$4;AnotherNew.Column;prefix_").build();

		computeProcessor.process(leftConcat);

		final List<List<String>> table = sourceTable.getTable();

		assertEquals(
			new ArrayList<>(
				Arrays.asList(NEW_COMMA_DOLLAR_4, ANOTHER_NEW_COLUMN, PREFIX_ID1, NAME1, MANUFACTURER1, NUMBER_OF_DISKS1)
			),
			table.get(0)
		);
		assertEquals(
			new ArrayList<>(
				Arrays.asList(NEW_COMMA_DOLLAR_4, ANOTHER_NEW_COLUMN, PREFIX_ID2, NAME2, MANUFACTURER2, NUMBER_OF_DISKS2)
			),
			table.get(1)
		);
		assertEquals(
			new ArrayList<>(
				Arrays.asList(NEW_COMMA_DOLLAR_4, ANOTHER_NEW_COLUMN, PREFIX_ID3, NAME3, MANUFACTURER3, NUMBER_OF_DISKS3)
			),
			table.get(2)
		);
	}

	@Test
	void testProcessRightConcat() {
		initializeSourceTable();

		// Test with empty RightConcat
		final RightConcat rightConcat = new RightConcat();

		computeProcessor.process(rightConcat);

		final List<List<String>> table = sourceTable.getTable();

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
		final List<List<String>> expected = Arrays.asList(LINE_1_RESULT_RIGHT, LINE_2_RESULT_RIGHT, LINE_3_RESULT_RIGHT);
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

		final RightConcat rightConcat = RightConcat.builder().column(1).value(SUFFIX).build();

		computeProcessor.process(rightConcat);

		final List<List<String>> table = sourceTable.getTable();

		assertEquals(new ArrayList<>(Collections.singletonList(ID1_SUFFIX)), table.get(0));
		assertEquals(new ArrayList<>(Collections.singletonList(ID2_SUFFIX)), table.get(1));
		assertEquals(new ArrayList<>(Collections.singletonList(ID3_SUFFIX)), table.get(2));
	}

	@Test
	void testProcessRightConcatColumn() {
		initializeSourceTable();

		final RightConcat rightConcat = RightConcat.builder().column(3).value(DOLLAR_1).build();

		computeProcessor.process(rightConcat);

		final List<List<String>> table = sourceTable.getTable();

		assertEquals(new ArrayList<>(Arrays.asList(ID1, NAME1, "MANUFACTURER1ID1", NUMBER_OF_DISKS1)), table.get(0));
		assertEquals(new ArrayList<>(Arrays.asList(ID2, NAME2, "MANUFACTURER2ID2", NUMBER_OF_DISKS2)), table.get(1));
		assertEquals(new ArrayList<>(Arrays.asList(ID3, NAME3, "MANUFACTURER3ID3", NUMBER_OF_DISKS3)), table.get(2));
	}

	@Test
	void testProcessRightConcatNotColumn1() {
		initializeSourceTable();

		final RightConcat rightConcat = RightConcat.builder().column(3).value(UNDERSCORE_DOLLAR_1).build();

		computeProcessor.process(rightConcat);

		final List<List<String>> table = sourceTable.getTable();

		assertEquals(new ArrayList<>(Arrays.asList(ID1, NAME1, "MANUFACTURER1_$1", NUMBER_OF_DISKS1)), table.get(0));
		assertEquals(new ArrayList<>(Arrays.asList(ID2, NAME2, "MANUFACTURER2_$1", NUMBER_OF_DISKS2)), table.get(1));
		assertEquals(new ArrayList<>(Arrays.asList(ID3, NAME3, "MANUFACTURER3_$1", NUMBER_OF_DISKS3)), table.get(2));
	}

	@Test
	void testProcessRightConcatNewColumn() {
		initializeSourceTable();

		final RightConcat rightConcat = RightConcat.builder().column(3).value("_suffix;new,Column").build();

		computeProcessor.process(rightConcat);

		final List<List<String>> table = sourceTable.getTable();

		assertEquals(
			new ArrayList<>(Arrays.asList(ID1, NAME1, MANUFACTURER1_SUFFIX, NEW_COMMA_COLUMN, NUMBER_OF_DISKS1)),
			table.get(0)
		);
		assertEquals(
			new ArrayList<>(Arrays.asList(ID2, NAME2, MANUFACTURER2_SUFFIX, NEW_COMMA_COLUMN, NUMBER_OF_DISKS2)),
			table.get(1)
		);
		assertEquals(
			new ArrayList<>(Arrays.asList(ID3, NAME3, MANUFACTURER3_SUFFIX, NEW_COMMA_COLUMN, NUMBER_OF_DISKS3)),
			table.get(2)
		);
	}

	@Test
	void testProcessRightConcatTwoNewColumns() {
		initializeSourceTable();

		final RightConcat rightConcat = RightConcat.builder().column(1).value("_suffix;new,$4;AnotherNew.Column").build();

		computeProcessor.process(rightConcat);

		final List<List<String>> table = sourceTable.getTable();

		assertEquals(
			new ArrayList<>(
				Arrays.asList(ID1_SUFFIX, NEW_COMMA_DOLLAR_4, ANOTHER_NEW_COLUMN, NAME1, MANUFACTURER1, NUMBER_OF_DISKS1)
			),
			table.get(0)
		);
		assertEquals(
			new ArrayList<>(
				Arrays.asList(ID2_SUFFIX, NEW_COMMA_DOLLAR_4, ANOTHER_NEW_COLUMN, NAME2, MANUFACTURER2, NUMBER_OF_DISKS2)
			),
			table.get(1)
		);
		assertEquals(
			new ArrayList<>(
				Arrays.asList(ID3_SUFFIX, NEW_COMMA_DOLLAR_4, ANOTHER_NEW_COLUMN, NAME3, MANUFACTURER3, NUMBER_OF_DISKS3)
			),
			table.get(2)
		);
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
		final RightConcat rightConcat = RightConcat.builder().value(SUFFIX).column(0).build();
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
			SourceTable.builder().table(Collections.singletonList(Collections.singletonList(FOO))).build()
		);
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

	@Test
	void testJson2Csv() {
		// Retrieve the Json file and extract its content as String
		final String rawData = ResourceHelper
			.getResourceAsString("/test-files/compute/json2Csv/json2CsvSample.json", ComputeProcessor.class)
			.replaceAll("\\s", "");

		// Set the extracted rawData in the source table
		sourceTable.setRawData(rawData);

		// Check the case of a null {@link Json2Csv} instance
		Json2Csv jsonToCsv = null;
		computeProcessor.process(jsonToCsv);
		assertEquals(rawData, sourceTable.getRawData());

		// Check the case of an empty {@link Json2Csv} instance
		jsonToCsv = Json2Csv.builder().build();
		computeProcessor.process(jsonToCsv);
		assertEquals(rawData, sourceTable.getRawData());

		// Check the case of a non-null {@link Json2Csv} instance
		jsonToCsv =
			Json2Csv.builder().entryKey("/monitors").separator(";").properties("id,name,monitorType,hostId").build();

		computeProcessor.process(jsonToCsv);

		final String expectedRawDataResult =
			"/monitors[0];enclosure-1;enclosure-1;ENCLOSURE;hostId;\n" +
			"/monitors[1];enclosure-2;enclosure-2;ENCLOSURE;hostId;\n";
		assertEquals(expectedRawDataResult, sourceTable.getRawData());
	}

	@Test
	void visitSubstringNOK() {
		final List<List<String>> table = Arrays.asList(
			Arrays.asList("ID1", "Dell+1"),
			Arrays.asList("ID2", "Dell+33"),
			Arrays.asList("ID3", "Dell+xyz")
		);

		sourceTable.setTable(table);
		computeProcessor.process((Substring) null);
		assertEquals(table, sourceTable.getTable());

		computeProcessor.process(Substring.builder().column(-1).start("-1").length("-1").build());
		assertEquals(table, sourceTable.getTable());

		computeProcessor.process(Substring.builder().column(2).start("TOTO").length("4").build());
		assertEquals(table, sourceTable.getTable());

		computeProcessor.process(Substring.builder().column(2).start("1").length("TOTO").build());
		assertEquals(table, sourceTable.getTable());
	}

	@Test
	void testCheckSubstring() {
		assertTrue(ComputeProcessor.checkSubstring(Substring.builder().column(2).start("1").length("4").build()));
		assertFalse(ComputeProcessor.checkSubstring(Substring.builder().column(-2).start("1").length("4").build()));
		assertFalse(ComputeProcessor.checkSubstring(Substring.builder().column(-1).start("1").length("4").build()));
		assertFalse(ComputeProcessor.checkSubstring(Substring.builder().column(-1).start("-1").length("-1").build()));
		assertFalse(ComputeProcessor.checkSubstring(null));
	}

	@Test
	void testProcessSubstring() {
		final List<List<String>> table = Arrays.asList(
			Arrays.asList("ID1", "Dell+1"),
			Arrays.asList("ID2", "Dell+33"),
			Arrays.asList("ID3", "Dell+xyz")
		);

		sourceTable.setTable(table);

		final Substring substring = Substring.builder().column(2).start("1").length("4").build();

		computeProcessor.process(substring);

		final List<List<String>> expected = Arrays.asList(
			Arrays.asList("ID1", "Dell"),
			Arrays.asList("ID2", "Dell"),
			Arrays.asList("ID3", "Dell")
		);
		assertEquals(expected, sourceTable.getTable());
	}

	@Test
	void testProcessSubstringViaColumn() {
		final List<List<String>> table = Arrays.asList(
			Arrays.asList("ID1", "Dell+1", "4"),
			Arrays.asList("ID2", "Dell+33", "4"),
			Arrays.asList("ID3", "Dell+xyz", "4")
		);

		sourceTable.setTable(table);

		final Substring substring = Substring.builder().column(2).start("1").length("$3").build();

		computeProcessor.process(substring);

		final List<List<String>> expected = Arrays.asList(
			Arrays.asList("ID1", "Dell", "4"),
			Arrays.asList("ID2", "Dell", "4"),
			Arrays.asList("ID3", "Dell", "4")
		);
		assertEquals(expected, sourceTable.getTable());
	}

	@Test
	void testPerformSubstringWrongBeginIndex() {
		final List<List<String>> table = Arrays.asList(
			Arrays.asList("ID1", "Dell+1"),
			Arrays.asList("ID2", "Dell+33"),
			Arrays.asList("ID3", "Dell+xyz")
		);

		sourceTable.setTable(table);

		computeProcessor.performSubstring(1, "Column(4)", 3, "4", -1);

		assertEquals(table, sourceTable.getTable());
	}

	@Test
	void testPerformSubstringWrongColumnIndex() {
		final List<List<String>> table = Arrays.asList(
			Arrays.asList("ID1", "Dell+1"),
			Arrays.asList("ID2", "Dell+33"),
			Arrays.asList("ID3", "Dell+xyz")
		);

		sourceTable.setTable(table);

		computeProcessor.performSubstring(3, "1", -1, "4", -1);

		assertEquals(table, sourceTable.getTable());
	}

	@Test
	void testPerformSubstring() {
		final List<List<String>> table = Arrays.asList(
			Arrays.asList("ID1", "Dell+1"),
			Arrays.asList("ID2", "Dell+33"),
			Arrays.asList("ID3", "Dell+xyz")
		);

		sourceTable.setTable(table);

		computeProcessor.performSubstring(1, "1", -1, "4", -1);

		final List<List<String>> expected = Arrays.asList(
			Arrays.asList("ID1", "Dell"),
			Arrays.asList("ID2", "Dell"),
			Arrays.asList("ID3", "Dell")
		);
		assertEquals(expected, sourceTable.getTable());
	}

	@Test
	void testCheckSubstringArguments() {
		assertTrue(ComputeProcessor.checkSubstringArguments(1, 3, 3));

		//noinspection ConstantConditions
		assertFalse(ComputeProcessor.checkSubstringArguments(null, 3, 3));

		//noinspection ConstantConditions
		assertFalse(ComputeProcessor.checkSubstringArguments(1, null, 3));

		assertFalse(ComputeProcessor.checkSubstringArguments(0, 3, 3));
		assertFalse(ComputeProcessor.checkSubstringArguments(2, 0, 3));
		assertFalse(ComputeProcessor.checkSubstringArguments(1, 4, 3));
	}

	@Test
	void testTransformToIntegerValue() {
		assertNull(ComputeProcessor.transformToIntegerValue(null));
		assertNull(ComputeProcessor.transformToIntegerValue("a"));
		assertEquals(1, ComputeProcessor.transformToIntegerValue("1"));
	}

	@Test
	void testGetValueFunction() {
		assertNotNull(ComputeProcessor.getValueFunction(-1));
		assertNotNull(ComputeProcessor.getValueFunction(0));
	}

	@Test
	void testCheckValueAndColumnIndexConsistency() {
		assertTrue(ComputeProcessor.checkValueAndColumnIndexConsistency("1", -1));
		assertFalse(ComputeProcessor.checkValueAndColumnIndexConsistency("Column(0)", -1));
		assertTrue(ComputeProcessor.checkValueAndColumnIndexConsistency("Column(1)", 0));
		assertTrue(ComputeProcessor.checkValueAndColumnIndexConsistency("1", 1));
	}

	@Test
	void testGetColumnIndex() {
		assertEquals(1, ComputeProcessor.getColumnIndex(" $2 "));
		assertEquals(-1, ComputeProcessor.getColumnIndex("2"));
	}

	@Test
	void testProcessArrayTranslate() throws IOException {
		List<List<String>> table = Arrays.asList(
			Arrays.asList(ID1, null, TYPE1),
			Arrays.asList(ID2, null, TYPE2),
			Arrays.asList(ID3, null, TYPE3)
		);

		List<List<String>> result = Arrays.asList(
			Arrays.asList(ID1, "TRANSLATED_STATUS11|TRANSLATED_STATUS12|TRANSLATED_STATUS13", TYPE1),
			Arrays.asList(ID2, "NO_VALUE|TRANSLATED_STATUS22", TYPE2),
			Arrays.asList(ID3, "TRANSLATED_STATUS31", TYPE3)
		);

		final Map<String, String> translations = Maps.of(
			"",
			"NO_VALUE",
			"status11",
			"TRANSLATED_STATUS11",
			"status12",
			"TRANSLATED_STATUS12",
			"status13",
			"TRANSLATED_STATUS13",
			"status22",
			"TRANSLATED_STATUS22", // No translation for STATUS22
			"status31",
			"TRANSLATED_STATUS31"
		);
		final String translationTableName = "translationTableName";
		final TranslationTable connectorTranslationTable = TranslationTable.builder().translations(translations).build();
		final String connectorName = "connectorName";
		final Connector connector = Connector
			.builder()
			.translations(Collections.singletonMap(translationTableName, connectorTranslationTable))
			.build();

		Map<String, Connector> store = Maps.of(connectorName, connector);

		final TelemetryManager telemetryManager = TelemetryManager.builder().connectorStore(connectorStoreMock).build();

		doReturn(store).when(connectorStoreMock).getStore();
		computeProcessor.setConnectorName(connectorName);
		computeProcessor.setTelemetryManager(telemetryManager);

		sourceTable.setTable(table);

		// ArrayTranslate is null
		computeProcessor.process((ArrayTranslate) null);
		assertEquals(table, sourceTable.getTable());

		// ArrayTranslate is not null, translationTable is null
		ArrayTranslate arrayTranslate = new ArrayTranslate();
		computeProcessor.process(arrayTranslate);
		assertEquals(table, sourceTable.getTable());

		// ArrayTranslate is not null, translationTable is not null, translations is not null,
		// column < 1
		ReferenceTranslationTable translationTable = new ReferenceTranslationTable("");
		arrayTranslate.setTranslationTable(translationTable);
		arrayTranslate.setColumn(0);
		computeProcessor.process(arrayTranslate);
		assertEquals(table, sourceTable.getTable());

		// ArrayTranslate is not null, translationTable is not null, translations is not null,
		// column >= 1, arraySeparator is null, resultSeparator is null, columnIndex >= row size
		arrayTranslate.setColumn(4);
		computeProcessor.process(arrayTranslate);
		assertEquals(table, sourceTable.getTable());

		// ArrayTranslate is not null, translationTable is not null, translations is not null,
		// column >= 1, arraySeparator is not null, resultSeparator is not null, columnIndex >= row size
		arrayTranslate.setArraySeparator(",");
		arrayTranslate.setResultSeparator("|");
		computeProcessor.process(arrayTranslate);
		assertEquals(table, sourceTable.getTable());

		// ArrayTranslate is not null, translationTable is not null, translations is not null,
		// column >= 1, arraySeparator is not null, resultSeparator is not null, columnIndex < row size,
		// arrayValue is null
		arrayTranslate.setColumn(2);
		computeProcessor.process(arrayTranslate);
		assertEquals(table, sourceTable.getTable());

		// Test ReferenceTranslationTable OK
		table =
			Arrays.asList(
				Arrays.asList(ID1, "STATUS11,STATUS12,STATUS13", TYPE1),
				Arrays.asList(ID2, ",STATUS22,STATUS23,", TYPE2),
				Arrays.asList(ID3, "STATUS31", TYPE3)
			);

		sourceTable.setTable(table);

		translationTable = new ReferenceTranslationTable("${translation::translationTableName}");
		arrayTranslate.setTranslationTable(translationTable);

		computeProcessor.process(arrayTranslate);
		assertEquals(result, sourceTable.getTable());

		// Test ReferenceTranslationTable OK
		sourceTable.setTable(
			Arrays.asList(
				Arrays.asList(ID1, "STATUS11,STATUS12,STATUS13", TYPE1),
				Arrays.asList(ID2, "STATUS22,STATUS23,", TYPE2),
				Arrays.asList(ID3, "STATUS31", TYPE3)
			)
		);

		result =
			Arrays.asList(
				Arrays.asList(ID1, "TRANSLATED_STATUS11|TRANSLATED_STATUS12|TRANSLATED_STATUS13", TYPE1),
				Arrays.asList(ID2, "TRANSLATED_STATUS22", TYPE2),
				Arrays.asList(ID3, "TRANSLATED_STATUS31", TYPE3)
			);

		final TranslationTable referenceTranslationTable = TranslationTable
			.builder()
			.translations(
				Map.of(
					"status11",
					"TRANSLATED_STATUS11",
					"status12",
					"TRANSLATED_STATUS12",
					"status13",
					"TRANSLATED_STATUS13",
					"status22",
					"TRANSLATED_STATUS22",
					"status31",
					"TRANSLATED_STATUS31"
				)
			)
			.build();
		arrayTranslate.setTranslationTable(referenceTranslationTable);

		computeProcessor.process(arrayTranslate);
		assertEquals(result, sourceTable.getTable());
	}

	@Test
	void testProcessAwk() throws Exception {
		List<List<String>> table = Arrays.asList(LINE_1, LINE_2, LINE_3);

		sourceTable.setTable(table);
		Awk awkNull = null;
		computeProcessor.process(awkNull);
		assertEquals(table, sourceTable.getTable());

		sourceTable.setTable(null);
		sourceTable.setRawData(null);
		final String embeddedFileName = "${file::embeddedFile-1}";
		Awk awkOK = Awk
			.builder()
			.script(embeddedFileName)
			.keep("^" + FOO)
			.exclude("^" + BAR)
			.separators(TABLE_SEP)
			.selectColumns(ONE_TWO_THREE)
			.build();
		final Map<String, EmbeddedFile> embeddedFileMap = Collections.singletonMap(
			embeddedFileName,
			EmbeddedFile.builder().content(BAZ).build()
		);

		doReturn(
			"FOO;ID1;NAME1;MANUFACTURER1;NUMBER_OF_DISKS1\nBAR;ID2;NAME2;MANUFACTURER2;NUMBER_OF_DISKS2\nBAZ;ID3;NAME3;MANUFACTURER3;NUMBER_OF_DISKS3"
		)
			.when(matsyaClientsExecutorMock)
			.executeAwkScript(any(), any());
		try (final MockedStatic<EmbeddedFileHelper> mockedEmbeddedFileHelper = mockStatic(EmbeddedFileHelper.class)) {
			mockedEmbeddedFileHelper
				.when(() -> EmbeddedFileHelper.findEmbeddedFiles(embeddedFileName))
				.thenReturn(embeddedFileMap);
			computeProcessor.process(awkOK);
			String expectedRawData = "FOO;ID1;NAME1;";
			List<List<String>> expectedTable = Arrays.asList(Arrays.asList(FOO, ID1, NAME1));
			assertEquals(expectedTable, sourceTable.getTable());
			assertEquals(expectedRawData, sourceTable.getRawData());
		}

		final List<List<String>> osCommandResultTable = List.of(List.of("OS command result"));
		sourceTable.setTable(osCommandResultTable);
		sourceTable.setRawData(null);
		awkOK =
			Awk
				.builder()
				.script(embeddedFileName)
				.keep("^" + FOO)
				.exclude("^" + BAR)
				.separators(TABLE_SEP)
				.selectColumns(ONE_TWO_THREE)
				.build();
		doReturn(null).when(matsyaClientsExecutorMock).executeAwkScript(any(), any());

		try (final MockedStatic<EmbeddedFileHelper> mockedEmbeddedFileHelper = mockStatic(EmbeddedFileHelper.class)) {
			mockedEmbeddedFileHelper
				.when(() -> EmbeddedFileHelper.findEmbeddedFiles(embeddedFileName))
				.thenReturn(embeddedFileMap);
			computeProcessor.process(awkOK);
			assertEquals(Collections.emptyList(), sourceTable.getTable());
		}

		sourceTable.setTable(osCommandResultTable);
		sourceTable.setRawData(null);
		awkOK =
			Awk
				.builder()
				.script(embeddedFileName)
				.keep("^" + FOO)
				.exclude("^" + BAR)
				.separators(TABLE_SEP)
				.selectColumns(ONE_TWO_THREE)
				.build();
		doReturn(EMPTY).when(matsyaClientsExecutorMock).executeAwkScript(any(), any());
		try (final MockedStatic<EmbeddedFileHelper> mockedEmbeddedFileHelper = mockStatic(EmbeddedFileHelper.class)) {
			mockedEmbeddedFileHelper
				.when(() -> EmbeddedFileHelper.findEmbeddedFiles(embeddedFileName))
				.thenReturn(embeddedFileMap);
			computeProcessor.process(awkOK);
			assertEquals(Collections.emptyList(), sourceTable.getTable());
		}

		sourceTable.setRawData(null);
		sourceTable.setTable(table);
		doReturn(SourceTable.tableToCsv(table, TABLE_SEP, true))
			.when(matsyaClientsExecutorMock)
			.executeAwkScript(any(), any());
		try (final MockedStatic<EmbeddedFileHelper> mockedEmbeddedFileHelper = mockStatic(EmbeddedFileHelper.class)) {
			mockedEmbeddedFileHelper
				.when(() -> EmbeddedFileHelper.findEmbeddedFiles(embeddedFileName))
				.thenReturn(embeddedFileMap);
			computeProcessor.process(
				Awk
					.builder()
					.script(embeddedFileName)
					.exclude(ID1)
					.keep(ID2)
					.separators(TABLE_SEP)
					.selectColumns("2, 3")
					.build()
			);
			assertEquals("NAME2;MANUFACTURER2;", sourceTable.getRawData());
			assertEquals(Arrays.asList(Arrays.asList(NAME2, MANUFACTURER2)), sourceTable.getTable());
		}
	}
}
