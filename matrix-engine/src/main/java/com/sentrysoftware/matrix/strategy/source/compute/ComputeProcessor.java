package com.sentrysoftware.matrix.strategy.source.compute;

import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.COLUMN_PATTERN;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.COMMA;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.DEFAULT;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.DOUBLE_PATTERN;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.LOG_COMPUTE_KEY_SUFFIX_TEMPLATE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.TABLE_SEP;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.VERTICAL_BAR;

import com.sentrysoftware.matrix.common.helpers.StringHelper;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.ITranslationTable;
import com.sentrysoftware.matrix.connector.model.common.ReferenceTranslationTable;
import com.sentrysoftware.matrix.connector.model.common.TranslationTable;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.AbstractConcat;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Add;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.And;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.ArrayTranslate;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Awk;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Compute;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Convert;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Divide;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.DuplicateColumn;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.ExcludeMatchingLines;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Extract;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.ExtractPropertyFromWbemPath;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Json2Csv;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.KeepColumns;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.KeepOnlyMatchingLines;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.LeftConcat;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Multiply;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.PerBitTranslation;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Replace;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.RightConcat;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Substring;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Subtract;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Translate;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Xml2Csv;
import com.sentrysoftware.matrix.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.matrix.strategy.source.SourceTable;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;
import io.opentelemetry.instrumentation.annotations.SpanAttribute;
import io.opentelemetry.instrumentation.annotations.WithSpan;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Slf4j
public class ComputeProcessor implements IComputeProcessor {

	private TelemetryManager telemetryManager;
	private String connectorName;
	private MatsyaClientsExecutor matsyaClientsExecutor;
	private String sourceKey;
	private String hostname;
	private SourceTable sourceTable;
	private Integer index;
	private static final Map<Class<? extends Compute>, BiFunction<String, String, String>> MATH_FUNCTIONS_MAP;

	private static final Pattern NUMBER_PATTERN = Pattern.compile("\\d+");

	private static final Function<ComputeValue, String> GET_VALUE_FROM_ROW = computeValue -> {
		if (computeValue.getColumnIndex() < computeValue.getRow().size()) {
			return computeValue.getRow().get(computeValue.getColumnIndex());
		}
		log.warn("Cannot get value at index {} from the row {}.", computeValue.getColumnIndex(), computeValue.getRow());
		return null;
	};

	private static final Function<ComputeValue, String> GET_VALUE = ComputeValue::getValue;

	static {
		MATH_FUNCTIONS_MAP =
			Map.of(
				Add.class,
				(op1, op2) -> Double.toString(Double.parseDouble(op1) + Double.parseDouble(op2)),
				Subtract.class,
				(op1, op2) -> Double.toString(Double.parseDouble(op1) - Double.parseDouble(op2)),
				Multiply.class,
				(op1, op2) -> Double.toString(Double.parseDouble(op1) * Double.parseDouble(op2)),
				Divide.class,
				(op1, op2) -> {
					Double op2Value = Double.parseDouble(op2);
					if (op2Value != 0) {
						return Double.toString(Double.parseDouble(op1) / op2Value);
					}
					return null;
				}
			);
	}

	@Override
	@WithSpan("Compute ArrayTranslate Exec")
	public void process(@SpanAttribute("compute.definition") final ArrayTranslate arrayTranslate) {
		if (arrayTranslate == null) {
			log.warn(
				"Hostname {} - The Source (Array Translate) to visit is null, the array translate computation cannot be performed.",
				hostname
			);
			return;
		}

		final ITranslationTable translationTable = arrayTranslate.getTranslationTable();
		if (translationTable == null) {
			log.warn(
				"Hostname {} - Translation Table is null, the array translate computation cannot be performed.",
				hostname
			);
			return;
		}

		final Map<String, String> translations = findTranslations(translationTable);

		if (translations == null) {
			log.warn(
				"Hostname {} - The Translation Map is null, the array translate computation cannot be performed.",
				hostname
			);
			return;
		}

		final int column = arrayTranslate.getColumn();
		if (column < 1) {
			log.warn(
				"Hostname {} - The column number to translate cannot be < 1, the translate computation cannot be performed.",
				hostname
			);
			return;
		}

		final int columnIndex = column - 1;

		String arraySeparator = arrayTranslate.getArraySeparator();
		if (arraySeparator == null || VERTICAL_BAR.equals(arraySeparator)) {
			arraySeparator = "\\|";
		}

		String resultSeparator = arrayTranslate.getResultSeparator();
		if (resultSeparator == null) {
			resultSeparator = VERTICAL_BAR;
		}

		final String defaultTranslation = translations.get(DEFAULT);

		final List<List<String>> resultTable = new ArrayList<>();
		List<String> resultRow;

		for (List<String> row : sourceTable.getTable()) {
			if (columnIndex >= row.size()) {
				log.warn(
					"Hostname {} - The index of the column is {} but the row size is {}, the translate computation cannot be performed.",
					hostname,
					column,
					row.size()
				);

				return;
			}

			resultRow = new ArrayList<>(row);

			final String arrayValue = row.get(columnIndex);
			if (arrayValue != null) {
				final String[] splitArrayValue = arrayValue.split(arraySeparator);

				final String translatedArrayValue = Arrays
					.stream(splitArrayValue)
					.map(value -> translations.getOrDefault(value.toLowerCase(), defaultTranslation))
					.filter(value -> value != null && !value.isBlank())
					.collect(Collectors.joining(resultSeparator));

				resultRow.set(columnIndex, translatedArrayValue);
			}

			resultTable.add(resultRow);
		}

		sourceTable.setTable(resultTable);
		sourceTable.setRawData(SourceTable.tableToCsv(sourceTable.getTable(), TABLE_SEP, false));
	}

	@Override
	@WithSpan("Compute And Exec")
	public void process(@SpanAttribute("compute.definition") final And and) {
		if (and == null) {
			log.warn("Hostname {} - Compute Operation (And) is null, the table remains unchanged.", hostname);
			return;
		}

		String operand2 = and.getValue();

		if (and.getColumn() == null || operand2 == null) {
			log.warn(
				"Hostname {} - Arguments in Compute Operation (And) : {} are wrong, the table remains unchanged.",
				hostname,
				and
			);
			return;
		}

		int columnIndex = and.getColumn() - 1;

		if (columnIndex < 0) {
			log.warn(
				"Hostname {} - The index of the column to which apply the And operation cannot be < 1, the And computation cannot be performed.",
				hostname
			);
			return;
		}

		int colOperand2 = getColumnIndex(operand2);

		for (List<String> line : sourceTable.getTable()) {
			try {
				if (columnIndex < line.size()) {
					// Set the column value of the 'line' at 'columnIndex' to the result of a bitwise 'AND' operation
					// between the long representation of the value located at column index retrieved through 'line.get(columnIndex)' and either
					// 'operand2' or the value at another column index 'colOperand2' retrieved through 'line.get(colOperand2)' based on the condition.
					line.set(
						columnIndex,
						String.valueOf(
							(long) Double.parseDouble(line.get(columnIndex)) &
							(
								colOperand2 == -1
									? (long) Double.parseDouble(operand2)
									: (long) Double.parseDouble(line.get(colOperand2))
							)
						)
					);
				}
			} catch (NumberFormatException e) {
				log.warn("Hostname {} - Data is not correctly formatted.", hostname);
			}
		}

		sourceTable.setRawData(SourceTable.tableToCsv(sourceTable.getTable(), TABLE_SEP, false));
	}

	@Override
	@WithSpan("Compute Add Exec")
	public void process(@SpanAttribute("compute.definition") final Add add) {
		if (add == null) {
			log.warn("Hostname {} - Compute Operation (Add) is null, the table remains unchanged.", hostname);
			return;
		}

		Integer columnIndex = add.getColumn();
		String operand2 = add.getValue();

		if (columnIndex == null || operand2 == null) {
			log.warn(
				"Hostname {} - Arguments in Compute Operation (Add) : {} are wrong, the table remains unchanged.",
				hostname,
				add
			);
			return;
		}

		if (columnIndex < 1) {
			log.warn(
				"Hostname {} - The index of the column to add cannot be < 1, the addition computation cannot be performed.",
				hostname
			);
			return;
		}

		performMathematicalOperation(add, columnIndex, operand2);
	}

	@Override
	@WithSpan("Compute Awk Exec")
	public void process(@SpanAttribute("compute.definition") final Awk awk) {
		// TODO Auto-generated method stub
	}

	@Override
	@WithSpan("Compute Convert Exec")
	public void process(@SpanAttribute("compute.definition") final Convert convert) {
		// TODO Auto-generated method stub
	}

	@Override
	@WithSpan("Compute Divide Exec")
	public void process(@SpanAttribute("compute.definition") final Divide divide) {
		if (divide == null) {
			log.warn("Hostname {} - Compute Operation (Divide) is null, the table remains unchanged.", hostname);
			return;
		}

		if (divide.getColumn() == null || divide.getValue() == null) {
			log.warn(
				"Hostname {} - Arguments in Compute Operation (Divide) : {} are wrong, the table remains unchanged.",
				hostname,
				divide
			);
			return;
		}

		Integer columnIndex = divide.getColumn();
		String divideBy = divide.getValue();

		if (columnIndex < 1) {
			log.warn(
				"Hostname {} - The index of the column to divide cannot be < 1, the division computation cannot be performed.",
				hostname
			);
			return;
		}

		performMathematicalOperation(divide, columnIndex, divideBy);
	}

	@Override
	@WithSpan("Compute DuplicateColumn Exec")
	public void process(@SpanAttribute("compute.definition") final DuplicateColumn duplicateColumn) {
		// TODO Auto-generated method stub
	}

	@Override
	@WithSpan("Compute ExcludeMatchingLines Exec")
	public void process(@SpanAttribute("compute.definition") final ExcludeMatchingLines excludeMatchingLines) {
		// TODO Auto-generated method stub
	}

	@Override
	@WithSpan("Compute Extract Exec")
	public void process(@SpanAttribute("compute.definition") final Extract extract) {
		// TODO Auto-generated method stub
	}

	@Override
	@WithSpan("Compute ExtractPropertyFromWbemPath Exec")
	public void process(
		@SpanAttribute("compute.definition") final ExtractPropertyFromWbemPath extractPropertyFromWbemPath
	) {
		// TODO Auto-generated method stub
	}

	/**
	 * This method processes Json2Csv compute
	 * @param json2csv {@link Json2Csv} instance
	 */
	@Override
	@WithSpan("Compute Json2Csv Exec")
	public void process(@SpanAttribute("compute.definition") final Json2Csv json2csv) {
		if (json2csv == null) {
			log.warn("Hostname {} - Compute Operation (Json2CSV) is null, the table remains unchanged.", hostname);
			return;
		}

		try {
			final List<String> jsonToCsvProperties = new ArrayList<>(Arrays.asList(json2csv.getProperties().split(COMMA)));
			final String json2csvResult = matsyaClientsExecutor.executeJson2Csv(
				sourceTable.getRawData(),
				json2csv.getEntryKey(),
				jsonToCsvProperties,
				json2csv.getSeparator()
			);

			if (json2csvResult != null && !json2csvResult.isEmpty()) {
				sourceTable.setRawData(json2csvResult);
				sourceTable.setTable(SourceTable.csvToTable(json2csvResult, json2csv.getSeparator()));
			}
		} catch (Exception e) {
			logComputeError(
				connectorName,
				String.format(LOG_COMPUTE_KEY_SUFFIX_TEMPLATE, sourceKey, this.index),
				"Json2CSV",
				e,
				hostname
			);
		}
	}

	/**
	 * Log the given throwable
	 *
	 * @param connectorName The name of the connector defining the compute
	 * @param computeKey    The key of the compute
	 * @param context       Additional information about the operation
	 * @param throwable     The caught throwable to log
	 */
	private static void logComputeError(
		final String connectorName,
		final String computeKey,
		final String context,
		final Throwable throwable,
		final String hostname
	) {
		if (log.isErrorEnabled()) {
			log.error(
				"Hostname {} - Compute Operation [{}] has failed. Context [{}]. Connector [{}]. Errors:\n{}\n",
				hostname,
				computeKey,
				context,
				connectorName,
				StringHelper.getStackMessages(throwable)
			);
		}

		if (log.isDebugEnabled()) {
			log.debug(
				String.format(
					"Hostname %s - Compute Operation [%s] has failed. Context [%s]. Connector [%s]. Stack trace:",
					hostname,
					computeKey,
					context,
					connectorName
				),
				throwable
			);
		}
	}

	@Override
	@WithSpan("Compute KeepColumns Exec")
	public void process(@SpanAttribute("compute.definition") final KeepColumns keepColumns) {
		// TODO Auto-generated method stub
	}

	@Override
	@WithSpan("Compute KeepOnlyMatchingLines Exec")
	public void process(@SpanAttribute("compute.definition") final KeepOnlyMatchingLines keepOnlyMatchingLines) {
		// TODO Auto-generated method stub
	}

	@Override
	@WithSpan("Compute LeftConcat Exec")
	public void process(@SpanAttribute("compute.definition") final LeftConcat leftConcat) {
		processAbstractConcat(leftConcat);
	}

	@Override
	@WithSpan("Compute Multiply Exec")
	public void process(@SpanAttribute("compute.definition") final Multiply multiply) {
		if (multiply == null) {
			log.warn("Hostname {} - Compute Operation (Multiply) is null, the table remains unchanged.", hostname);
			return;
		}

		Integer columnIndex = multiply.getColumn();
		String operand2 = multiply.getValue();

		if (columnIndex == null || operand2 == null) {
			log.warn(
				"Hostname {} - Arguments in Compute Operation (Multiply) : {} are wrong, the table remains unchanged.",
				hostname,
				multiply
			);
			return;
		}

		if (columnIndex < 1) {
			log.warn(
				"Hostname {} - The index of the column to multiply cannot be < 1, the multiplication computation cannot be performed.",
				hostname
			);
			return;
		}

		performMathematicalOperation(multiply, columnIndex, operand2);
	}

	@Override
	@WithSpan("Compute PerBitTranslation Exec")
	public void process(@SpanAttribute("compute.definition") final PerBitTranslation perBitTranslation) {
		// TODO Auto-generated method stub
	}

	@Override
	@WithSpan("Compute Replace Exec")
	public void process(@SpanAttribute("compute.definition") final Replace replace) {
		// TODO Auto-generated method stub
	}

	@Override
	@WithSpan("Compute RightConcat Exec")
	public void process(@SpanAttribute("compute.definition") final RightConcat rightConcat) {
		processAbstractConcat(rightConcat);
	}

	@Override
	@WithSpan("Compute Subtract Exec")
	public void process(@SpanAttribute("compute.definition") final Subtract subtract) {
		if (subtract == null) {
			log.warn("Hostname {} - Compute Operation (Subtract) is null, the table remains unchanged.", hostname);
			return;
		}

		Integer columnIndex = subtract.getColumn();
		String operand2 = subtract.getValue();

		if (columnIndex == null || operand2 == null) {
			log.warn(
				"Hostname {} - Arguments in Compute Operation (Subtract) : {} are wrong, the table remains unchanged.",
				hostname,
				subtract
			);
			return;
		}

		if (columnIndex < 1) {
			log.warn(
				"Hostname {} - The index of the column to add cannot be < 1, the addition computation cannot be performed.",
				hostname
			);
			return;
		}

		performMathematicalOperation(subtract, columnIndex, operand2);
	}

	@Override
	@WithSpan("Compute Substring Exec")
	public void process(@SpanAttribute("compute.definition") final Substring substring) {
		if (!checkSubstring(substring)) {
			log.warn("Hostname {} - The substring {} is not valid, the table remains unchanged.", hostname, substring);
			return;
		}

		final String start = substring.getStart();
		final String length = substring.getLength();

		final Integer startColumnIndex = getColumnIndex(start);
		if (!checkValueAndColumnIndexConsistency(start, startColumnIndex)) {
			log.warn("Hostname {} - Inconsistent substring start value {}, the table remains unchanged.", hostname, start);
			return;
		}

		final Integer lengthColumnIndex = getColumnIndex(length);
		if (!checkValueAndColumnIndexConsistency(length, lengthColumnIndex)) {
			log.warn("Hostname {} - Inconsistent substring length value {}, the table remains unchanged.", hostname, length);
			return;
		}

		performSubstring(substring.getColumn() - 1, start, startColumnIndex, length, lengthColumnIndex);
	}

	/**
	 * Check value and column index consistency. At least we need one data available
	 *
	 * @param value              The string value as a number
	 * @param foreignColumnIndex The index of the column already extracted from a value expected as <em>Column($index)</em>
	 * @return <code>true</code> if data is consistent
	 */
	static boolean checkValueAndColumnIndexConsistency(final String value, final Integer foreignColumnIndex) {
		return foreignColumnIndex >= 0 || NUMBER_PATTERN.matcher(value).matches();
	}

	/**
	 * Check the given {@link Substring} instance
	 *
	 * @param substring The substring instance we wish to check
	 * @return true if the substring is valid
	 */
	static boolean checkSubstring(final Substring substring) {
		return substring != null && substring.getColumn() >= 1;
	}

	/**
	 * Perform a substring operation on the column identified by the given <code>columnIndex</code>
	 *
	 * @param columnIndex      The column number in the current {@link SourceTable}
	 * @param start            The begin index, inclusive and starts at 1
	 * @param startColumnIndex The column index, so that we extract the start index. If equals -1 then it is not used
	 * @param end              The ending index, exclusive
	 * @param endColumnIndex   The column index, so that we extract the length index. If equals -1 then it is not used
	 */
	void performSubstring(
		final int columnIndex,
		final String start,
		final int startColumnIndex,
		final String end,
		final int endColumnIndex
	) {
		final Function<ComputeValue, String> startFunction = getValueFunction(startColumnIndex);
		final Function<ComputeValue, String> endFunction = getValueFunction(endColumnIndex);

		sourceTable
			.getTable()
			.forEach(row -> {
				if (columnIndex < row.size()) {
					final String columnValue = row.get(columnIndex);

					final Integer beginIndex = transformToIntegerValue(
						startFunction.apply(new ComputeValue(row, startColumnIndex, start))
					);

					final Integer endIndex = transformToIntegerValue(
						endFunction.apply(new ComputeValue(row, endColumnIndex, end))
					);

					if (checkSubstringArguments(beginIndex, endIndex, columnValue.length())) {
						// No need to put endIndex -1 as the String substring end index is exclusive
						// PSL substr(1,3) is equivalent to Java String substring(0, 3)
						row.set(columnIndex, columnValue.substring(beginIndex - 1, endIndex));
						return;
					}
					log.warn(
						"Hostname {} - Substring arguments are not valid: start={}, end={}," +
						" startColumnIndex={}, endColumnIndex={}," +
						" computed beginIndex={}, computed endIndex={}," +
						" row={}, columnValue={}",
						hostname,
						start,
						end,
						startColumnIndex,
						endColumnIndex,
						beginIndex,
						endIndex,
						row,
						columnValue
					);
				}

				log.warn("Hostname {} - Cannot perform substring on row {} on column index {}", hostname, row, columnIndex);
			});

		sourceTable.setRawData(SourceTable.tableToCsv(sourceTable.getTable(), TABLE_SEP, false));
	}

	/**
	 * Check the substring argument to avoid the {@link StringIndexOutOfBoundsException}
	 *
	 * @param begin  Starts from 1
	 * @param end    The end index of the string
	 * @param length The length of the {@link String}
	 * @return <code>true</code> if a {@link String} substring can be performed
	 */
	static boolean checkSubstringArguments(final Integer begin, final Integer end, final int length) {
		return begin != null && end != null && (begin - 1) >= 0 && (begin - 1) <= end && end <= length;
	}

	/**
	 * Return the right {@link Function} based on the <code>foreignColumnIndex</code>
	 *
	 * @param foreignColumnIndex The index of the column we wish to check so that we choose the right function to return
	 * @return {@link Function} used to get the value
	 */
	static Function<ComputeValue, String> getValueFunction(final int foreignColumnIndex) {
		return (foreignColumnIndex >= 0) ? GET_VALUE_FROM_ROW : GET_VALUE;
	}

	/**
	 * Transform the given {@link String} value to an {@link Integer} value
	 *
	 * @param value The value we wish to parse
	 * @return {@link Integer} value
	 */
	static Integer transformToIntegerValue(final String value) {
		if (value != null && DOUBLE_PATTERN.matcher(value).matches()) {
			return (int) Double.parseDouble(value);
		}
		return null;
	}

	@AllArgsConstructor
	private static class ComputeValue {

		@Getter
		private final List<String> row;

		@Getter
		private final int columnIndex;

		@Getter
		private final String value;
	}

	@Override
	@WithSpan("Compute Translate Exec")
	public void process(@SpanAttribute("compute.definition") final Translate translate) {
		// TODO Auto-generated method stub
	}

	@Override
	@WithSpan("Compute Xml2Csv Exec")
	public void process(@SpanAttribute("compute.definition") final Xml2Csv xml2csv) {
		// TODO Auto-generated method stub
	}

	/**
	 * Perform a mathematical computation (add, subtract, multiply or divide) on a given column in the sourceTable
	 * Check if the operand2 is a reference to a column or a raw value
	 * @param computeOperation The compute operation must be one of : Add, Subtract, Multiply, Divide.
	 * @param column           Column to be changed
	 * @param operand2         Can be a reference to another column or a raw value
	 */
	private void performMathematicalOperation(
		final Compute computeOperation,
		final Integer column,
		final String operand2
	) {
		if (!MATH_FUNCTIONS_MAP.containsKey(computeOperation.getClass())) {
			log.warn("Hostname {} - The compute operation must be one of : Add, Subtract, Multiply, Divide.", hostname);
			return;
		}

		Integer columnIndex = column - 1;
		int operandByIndex = -1;

		Matcher matcher = COLUMN_PATTERN.matcher(operand2);
		if (matcher.matches()) {
			try {
				operandByIndex = Integer.parseInt(matcher.group(1)) - 1;
				if (operandByIndex < 0) {
					log.warn(
						"Hostname {} - The operand2 column index cannot be < 1, the {} computation cannot be performed, the table remains unchanged.",
						hostname,
						computeOperation.getClass()
					);
					return;
				}
			} catch (NumberFormatException e) {
				log.warn(
					"Hostname {} - NumberFormatException: {} is not a correct operand2 for {}, the table remains unchanged.",
					hostname,
					operand2,
					computeOperation
				);
				log.debug("Hostname {} - Stack trace:", hostname, e);
				return;
			}
		} else if (!DOUBLE_PATTERN.matcher(operand2).matches()) {
			log.warn("Hostname {} - operand2 is not a number: {}, the table remains unchanged.", hostname, operand2);
			return;
		}

		performMathComputeOnTable(computeOperation, columnIndex, operand2, operandByIndex);
	}

	/**
	 * Execute the computational operation (Add, Subtract, Divide or Multiply) on each row of the tableSource.
	 *
	 * @param computeOperation The {@link Compute} operation that should be performed.
	 * @param columnIndex      The index of the column on which the operation should be performed.
	 * @param operand2         The second operand of the operation.
	 * @param operand2Index    The column holding the value of the second operand in the {@link SourceTable}.
	 */
	private void performMathComputeOnTable(
		final Compute computeOperation,
		final Integer columnIndex,
		final String operand2,
		final int operand2Index
	) {
		for (List<String> line : sourceTable.getTable()) {
			performMathComputeOnLine(computeOperation, columnIndex, operand2, operand2Index, line);
		}

		sourceTable.setRawData(SourceTable.tableToCsv(sourceTable.getTable(), TABLE_SEP, false));
	}

	/**
	 * @param computeOperation The {@link Compute} operation that should be performed.
	 * @param columnIndex      The index of the column on which the operation should be performed.
	 * @param operand2         The second operand of the operation.
	 * @param operand2Index	   The column holding the value of the second operand in the {@link SourceTable}.
	 * @param line             The line that is being processed.
	 */
	private void performMathComputeOnLine(
		final Compute computeOperation,
		final Integer columnIndex,
		final String operand2,
		final int operand2Index,
		final List<String> line
	) {
		if (columnIndex < line.size()) {
			String operand1 = line.get(columnIndex);
			if (!operand1.isBlank()) {
				if (operand2Index != -1) {
					if (operand2Index < line.size()) {
						performMathComputeOnLine(computeOperation.getClass(), columnIndex, line, operand1, line.get(operand2Index));
					}
				} else {
					performMathComputeOnLine(computeOperation.getClass(), columnIndex, line, operand1, operand2);
				}
			}
		}
	}

	/**
	 * Given two operands, perform an addition, subtraction, multiplication or division
	 * and modify the given line on the given columnIndex.
	 *
	 * @param computeOperation The {@link Compute} operation that should be performed.
	 * @param columnIndex      The index of the column on which the operation should be performed.
	 * @param line             The row of the {@link SourceTable} that is being operated on.
	 * @param operand1         The first operand of the operation.
	 * @param operand2         The second operand of the operation.
	 */
	private void performMathComputeOnLine(
		final Class<? extends Compute> computeOperation,
		final Integer columnIndex,
		final List<String> line,
		final String operand1,
		final String operand2
	) {
		if (operand1.isBlank() || operand2.isBlank()) {
			return;
		}

		try {
			if (MATH_FUNCTIONS_MAP.containsKey(computeOperation)) {
				String resultFunction = MATH_FUNCTIONS_MAP.get(computeOperation).apply(operand1, operand2);
				if (resultFunction != null) {
					line.set(columnIndex, resultFunction);
				}
			}
		} catch (NumberFormatException e) {
			log.warn(
				"Hostname {} - There is a NumberFormatException on operand 1: {} or the operand 2: {}.",
				hostname,
				operand1,
				operand2
			);
			log.debug("Hostname {} - Stack trace:", hostname, e);
		}
	}

	/**
	 * Get the column index for the given value
	 *
	 * @param value The value we wish to parse
	 * @return {@link Integer} value or -1 if value is not in the column pattern format
	 */
	static Integer getColumnIndex(final String value) {
		final Matcher matcher = COLUMN_PATTERN.matcher(value);
		return matcher.matches() ? Integer.parseInt(matcher.group(1)) - 1 : -1;
	}

	/**
	 * Process the LeftConcat and RightConcat Computes
	 * @param abstractConcat
	 */
	private void processAbstractConcat(final AbstractConcat abstractConcat) {
		boolean firstChecks =
			abstractConcat != null &&
			abstractConcat.getValue() != null &&
			abstractConcat.getColumn() != null &&
			abstractConcat.getColumn() > 0 &&
			sourceTable != null &&
			sourceTable.getTable() != null &&
			!sourceTable.getTable().isEmpty();

		if (firstChecks) {
			// Case 1 : concatenation with an exiting column
			if (abstractConcat.getColumn() <= sourceTable.getTable().get(0).size()) {
				int columnIndex = abstractConcat.getColumn() - 1;
				String concatString = abstractConcat.getValue();

				// If abstractConcat.getValue() is like "$n",
				// we concat the column n instead of abstractConcat.getString()
				Matcher matcher = COLUMN_PATTERN.matcher(concatString);
				if (matcher.matches()) {
					int concatColumnIndex = Integer.parseInt(matcher.group(1)) - 1;
					if (concatColumnIndex < sourceTable.getTable().get(0).size()) {
						sourceTable.getTable().forEach(line -> concatColumns(line, columnIndex, concatColumnIndex, abstractConcat));
					}
				} else {
					sourceTable.getTable().forEach(line -> concatString(line, columnIndex, abstractConcat));

					// Serialize and deserialize
					// in case the String to concat contains a ';'
					// so that a new column is created.
					if (concatString.contains(TABLE_SEP)) {
						sourceTable.setTable(
							SourceTable.csvToTable(SourceTable.tableToCsv(sourceTable.getTable(), TABLE_SEP, false), TABLE_SEP)
						);
					}
				}
			} else if (abstractConcat.getColumn() == sourceTable.getTable().get(0).size() + 1) {
				// Case 2 : concatenation with non existing column

				// add at the end of the list (or at the beginning if the list is empty)
				sourceTable.getTable().forEach(line -> line.add(abstractConcat.getValue()));
			}
			sourceTable.setRawData(SourceTable.tableToCsv(sourceTable.getTable(), TABLE_SEP, false));
		}
	}

	/**
	 * Concatenates the values at <i>columnIndex</i> and <i>concatColumnIndex</i> on the given line,
	 * and stores the result at <i>columnIndex</i>.<br>
	 *
	 * Whether the value at <i>concatColumnIndex</i> goes to the left or to the right of the value at <i>columnIndex</i>
	 * depends on the type of the given {@link AbstractConcat}.
	 *
	 * @param line				The line on which the concatenation will be performed.
	 * @param columnIndex		The index of the column
	 *                          holding the value that should be concatenated to the value at <i>concatColumnIndex</i>.
	 *                          The result will be stored at <i>columnIndex</i>.
	 * @param concatColumnIndex	The index of the column
	 *                          holding the value that should be concatenated to the value at <i>columnIndex</i>.
	 * @param abstractConcat	The {@link AbstractConcat} used to determine
	 *                          whether the concatenation should be a left concatenation or a right concatenation.
	 */
	private void concatColumns(
		final List<String> line,
		final int columnIndex,
		final int concatColumnIndex,
		final AbstractConcat abstractConcat
	) {
		String result = abstractConcat instanceof LeftConcat
			? line.get(concatColumnIndex).concat(line.get(columnIndex))
			: line.get(columnIndex).concat(line.get(concatColumnIndex));

		line.set(columnIndex, result);
	}

	/**
	 * Concatenates the value at <i>columnIndex</i> on the given line
	 * with the given {@link AbstractConcat}'s <i>getString()</i> value,
	 * and stores the result at <i>columnIndex</i>.<br>
	 *
	 * Whether {@link AbstractConcat#toString()} ()} goes to the left or to the right of the value at <i>columnIndex</i>
	 * depends on the type of {@link AbstractConcat}.
	 *
	 * @param line				The line on which the concatenation will be performed.
	 * @param columnIndex		The index of the column
	 *                          holding the value that should be concatenated to {@link AbstractConcat#toString()} ()}.
	 *                          The result will be stored at <i>columnIndex</i>.
	 * @param abstractConcat	The {@link AbstractConcat} used to determine
	 *                          whether the concatenation should be a left concatenation or a right concatenation.
	 */
	private void concatString(final List<String> line, final int columnIndex, final AbstractConcat abstractConcat) {
		String result = abstractConcat instanceof LeftConcat
			? abstractConcat.getValue().concat(line.get(columnIndex))
			: line.get(columnIndex).concat(abstractConcat.getValue());

		line.set(columnIndex, result);
	}

	/**
	 * Find the translation map associated with the {@link ITranslationTable} in parameter.
	 * @param translationTable
	 * @return
	 */
	public Map<String, String> findTranslations(final ITranslationTable translation) {
		// In case of a ReferenceTranslationTable, we try to find its TranslationTable in the connector if the translations Map has not already been found.
		// In case of an InLineTranslationTable, the Map retrieved through translationTable.getTranslations()
		if (translation instanceof ReferenceTranslationTable referenceTranslationTable) {
			final Connector connector = telemetryManager.getConnectorStore().getStore().get(connectorName);
			if (connector != null && connector.getTranslations() != null) {
				final TranslationTable translationTable = connector
					.getTranslations()
					.get(referenceTranslationTable.getTableId());
				if (translationTable == null) {
					return null;
				}
				return translationTable.getTranslations();
			}
		}

		return ((TranslationTable) translation).getTranslations();
	}
}
