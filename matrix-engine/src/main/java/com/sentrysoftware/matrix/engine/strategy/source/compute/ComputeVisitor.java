package com.sentrysoftware.matrix.engine.strategy.source.compute;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.TranslationTable;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Add;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.And;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.ArrayTranslate;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Awk;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Compute;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Convert;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Divide;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.DuplicateColumn;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.ExcludeMatchingLines;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Extract;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.ExtractPropertyFromWbemPath;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Json2CSV;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.KeepColumns;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.KeepOnlyMatchingLines;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.LeftConcat;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Multiply;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.PerBitTranslation;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Replace;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.RightConcat;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Substract;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Substring;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Translate;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.XML2CSV;
import com.sentrysoftware.matrix.engine.strategy.source.SourceTable;
import com.sentrysoftware.matrix.engine.strategy.utils.PslUtils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class ComputeVisitor implements IComputeVisitor {

	@Getter
	@Setter
	private SourceTable sourceTable;

	@Setter
	private Connector connector;

	@Override
	public void visit(final Add add) {
		if (add == null) {
			log.warn("Compute Operation (Add) is null, the table remains unchanged.");
			return;
		}

		Integer columnIndex = add.getColumn();
		String operand2 = add.getAdd();

		if (columnIndex == null || operand2 == null ) {
			log.warn("Arguments in Compute Operation (Add) : {} are wrong, the table remains unchanged.", add);
			return;
		}

		if (columnIndex < 1 ) {
			log.warn("The index of the column to add cannot be < 1, the Addition computation cannot be performed.");
			return;
		}

		performMathematicalOperation(add, columnIndex, operand2);
	}

	@Override
	public void visit(final ArrayTranslate arrayTranslate) {
		// Not implemented yet
	}

	@Override
	public void visit(final And and) {
		// Not implemented yet
	}

	@Override
	public void visit(final Awk awk) {
		// Not implemented yet
	}

	@Override
	public void visit(final Convert convert) {
		// Not implemented yet
	}

	@Override
	public void visit(final Divide divide) {
		if (divide == null) {
			log.warn("Compute Operation (Divide) is null, the table remains unchanged.");
			return;
		}

		if(divide.getColumn() == null || divide.getDivideBy() == null ) {
			log.warn("Arguments in Compute Operation (Divide) : {} are wrong, the table remains unchanged.", divide);
			return;
		}

		
		Integer columnIndex = divide.getColumn();
		String divideBy = divide.getDivideBy();

		if (columnIndex < 1) {
			log.warn("The index of the column to divide cannot be < 1, the divide computation cannot be performed.");
			return;
		}

		performMathematicalOperation(divide, columnIndex, divideBy);

	}

	@Override
	public void visit(final DuplicateColumn duplicateColumn) {

		if (duplicateColumn == null) {
			log.debug("DuplicateColumn object is null, the table remains unchanged.");
			return;
		}

		if (duplicateColumn.getColumn() == null || duplicateColumn.getColumn() == 0) {
			log.debug("The column index in DuplicateColumn cannot be null or 0, the table remains unchanged.");
			return;
		}

		// for each list in the list, duplicate the column of the given index  
		Integer columnIndex = duplicateColumn.getColumn() -1;

		for (List<String> elementList : sourceTable.getTable()) {
			if (columnIndex >= 0 && columnIndex < elementList.size()) {
				elementList.add(columnIndex, elementList.get(columnIndex));
			}
		}

	}

	@Override
	public void visit(final ExcludeMatchingLines excludeMatchingLines) {
		// Not implemented yet
	}

	@Override
	public void visit(final Extract extract) {
		// Not implemented yet
	}

	@Override
	public void visit(final ExtractPropertyFromWbemPath extractPropertyFromWbemPath) {
		// Not implemented yet
	}

	@Override
	public void visit(final Json2CSV json2csv) {
		// Not implemented yet
	}

	@Override
	public void visit(final KeepColumns keepColumns) {
		// Not implemented yet
	}

	@Override
	public void visit(final KeepOnlyMatchingLines keepOnlyMatchingLines) {

		if (
				keepOnlyMatchingLines != null
						&& keepOnlyMatchingLines.getColumn() != null
						&& keepOnlyMatchingLines.getColumn() > 0
						&& sourceTable != null
						&& sourceTable.getTable() != null
						&& !sourceTable.getTable().isEmpty()
						&& keepOnlyMatchingLines.getColumn() <= sourceTable.getTable().get(0).size()
		) {

			int columnIndex = keepOnlyMatchingLines.getColumn() - 1;

			String regexpPsl = keepOnlyMatchingLines.getRegExp();
			List<String> valueList = keepOnlyMatchingLines.getValueList();

			List<List<String>> table = sourceTable.getTable();

			// If there are both a regex and a valueList, both are applied, one after the other.
			if (regexpPsl != null && !regexpPsl.isEmpty()) {
				table = processRegexp(regexpPsl, table, columnIndex);
			}

			if (valueList != null && !valueList.isEmpty()) {
				table = processValueList(valueList, table, columnIndex);
			}

			sourceTable.setTable(table);
		}
	}

	private List<List<String>> processRegexp(String regexpPsl, List<List<String>> table, int columnIndex) {

		List<List<String>> sourceTableTmp = new ArrayList<>();
		Pattern regexpPattern = Pattern.compile(PslUtils.psl2JavaRegex(regexpPsl));
		for (List<String> line : table) {

			if (regexpPattern.matcher(line.get(columnIndex)).matches()) {
				sourceTableTmp.add(line);
			}
		}

		return sourceTableTmp;
	}

	private List<List<String>> processValueList(List<String> valueList, List<List<String>> table, int columnIndex) {

		List<List<String>> sourceTableTmp = new ArrayList<>();
		for(List<String> line : table) {

			if (valueList.contains(line.get(columnIndex))) {
				sourceTableTmp.add(line);
			}
		}

		return sourceTableTmp;
	}

	@Override
	public void visit(final LeftConcat leftConcat) {
		if (leftConcat != null && leftConcat.getString() != null && leftConcat.getColumn() != null && leftConcat.getColumn() > 0
				&& sourceTable != null && sourceTable.getTable() != null && !sourceTable.getTable().isEmpty()
				&& leftConcat.getColumn() <= sourceTable.getTable().get(0).size()) {
			int columnIndex = leftConcat.getColumn() - 1;
			String concatString = leftConcat.getString();

			// If leftConcat.getString() is like "Concat(n)", we concat the column n instead of leftConcat.getString() 
			if (Pattern.compile(HardwareConstants.COLUMN_REGEXP, Pattern.CASE_INSENSITIVE).matcher(concatString).matches()) {
				int leftColumnIndex = Integer.parseInt(concatString.substring(concatString.indexOf(HardwareConstants.OPENING_PARENTHESIS) + 1, 
						concatString.indexOf(HardwareConstants.CLOSING_PARENTHESIS))) - 1;

				if (leftColumnIndex < sourceTable.getTable().get(0).size()) {
					sourceTable.getTable()
							.stream()
							.forEach(column -> column.set(columnIndex, column.get(leftColumnIndex).concat(column.get(columnIndex))));
				}
			} else {
				sourceTable.getTable()
						.stream()
						.forEach(column -> column.set(columnIndex, leftConcat.getString().concat(column.get(columnIndex))));

				// Serialize and deserialize in case the String to concat contains a ';' so that a new column is created.
				if (concatString.contains(HardwareConstants.SEMICOLON)) {
					sourceTable.setTable(SourceTable.csvToTable(SourceTable.tableToCsv(sourceTable.getTable(), HardwareConstants.SEMICOLON), HardwareConstants.SEMICOLON));
				}
			}
		}
	}

	@Override
	public void visit(final Multiply multiply) {
		if (multiply == null) {
			log.warn("Compute Operation (Add) is null, the table remains unchanged.");
			return;
		}

		Integer columnIndex = multiply.getColumn();
		String operand2 = multiply.getMultiplyBy();
		
		if (columnIndex == null || operand2 == null ) {
			log.warn("Arguments in Compute Operation (Multiply) : {} are wrong, the table remains unchanged.", multiply);
			return;
		}

		if (columnIndex < 1 ) {
			log.warn("The index of the column to multiply cannot be < 1, the Multiplication computation cannot be performed.");
			return;
		}

		performMathematicalOperation(multiply, columnIndex, operand2);
	}

	@Override
	public void visit(final PerBitTranslation perBitTranslation) {
		// Not implemented yet
	}

	@Override
	public void visit(final Replace replace) {
		// Not implemented yet
	}

	@Override
	public void visit(final RightConcat rightConcat) {
		// Not implemented yet
	}

	@Override
	public void visit(final Substract substract) {
		// Not implemented yet
	}

	@Override
	public void visit(final Substring substring) {
		// Not implemented yet
	}

	@Override
	public void visit(final Translate translate) {

		if (translate == null) {
			log.warn("The Source (Translate) to visit is null, the translate computation cannot be performed.");
			return;
		}

		TranslationTable translationTable = translate.getTranslationTable();
		if (translationTable == null) {
			log.warn("TranslationTable is null, the translate computation cannont be performed.");
			return;
		}

		Map<String, String> translations = translationTable.getTranslations();
		if (translations == null) {
			log.warn("The Translation Map {} is null, the translate computation cannot be performed.",
					translationTable.getName());
			return;
		}

		Integer columnIndex = translate.getColumn() - 1;
		if (columnIndex < 0) {
			log.warn("The index of the column to translate cannot be < 1, the translate computation cannot be performed.");
			return;
		}

		for (List<String> line : sourceTable.getTable()) {

			if (columnIndex < line.size()) {
				String valueToBeReplaced = line.get(columnIndex);

				if (translations.containsKey(valueToBeReplaced)) {
					line.set(columnIndex, translations.get(valueToBeReplaced));
				} else {
					log.warn("The Translation Map {} does not contain the following value {}.",
							translationTable.getName(), valueToBeReplaced);
				}
			}
		}
	}

	@Override
	public void visit(final XML2CSV xml2csv) {
		// Not implemented yet
	}


	/**
	 * 
	 * @param computeOperation The compute operation must be one of : Add, Multiply, Divide.
	 * @param column column to be changed
	 * @param operand2 can be a reference to another column or a raw value
	 */
	private void performMathematicalOperation(final Compute computeOperation, Integer column, String operand2) {
		
		if (! MATH_FUNCTIONS_MAP.containsKey(computeOperation.getClass()))  {
			log.warn("The compute operation must be one of : Add, Multiply, Divide.");
			return;
		}
		
		Integer columnIndex = column - 1;
		int operandByIndex = -1;

		Matcher matcher = Pattern.compile(HardwareConstants.COLUMN_REGEXP, Pattern.CASE_INSENSITIVE).matcher(operand2);
		if (matcher.matches()) {
			try {
				operandByIndex = Integer.parseInt(matcher.group(1)) - 1;
				if (operandByIndex < 0) {
					log.warn("The operand2 column index cannot be < 1, the {} computation cannot be performed, the table remains unchanged.", computeOperation.getClass());
					return;
				}
			} catch (NumberFormatException e) {
				log.warn("NumberFormatException : {} is not a correct operand2, the table remains unchanged.", operand2, computeOperation);
				return;
			}

		} else if (!operand2.matches("\\d+")) {
			log.warn("operand2 is not a number: {}, the table remains unchanged.", operand2);
			return;
		}

		
		performMathComputeOnTable(computeOperation, columnIndex, operand2, operandByIndex);
	}

	private void performMathComputeOnTable(final Compute computeOperation, Integer columnIndex, String op2, int op2Index) {
		for (List<String> line : sourceTable.getTable()) {

			if (columnIndex < line.size()) {
				String op1 = line.get(columnIndex);

				if (op2Index != -1) {
					if (op2Index < line.size()) {
						performMathComputeOnLine(computeOperation.getClass(), columnIndex, line, op1, line.get(op2Index));
					}
				} else {

					performMathComputeOnLine(computeOperation.getClass(), columnIndex, line, op1, op2);

				}
			}
		}
	}
	
	private void performMathComputeOnLine(final Class<? extends Compute> computeOperation, final Integer columnIndex,
			final List<String> line, final String op1, final String op2) {
		try {
			if(MATH_FUNCTIONS_MAP.containsKey(computeOperation)) {
				String resultFunction = MATH_FUNCTIONS_MAP.get(computeOperation).apply(op1, op2);
				if ( resultFunction != null) {
					line.set(columnIndex, resultFunction);
				}
			}
		} catch (NumberFormatException e) {
			log.warn("There is a NumberFormatException on operand 1 : {} or the operand 2 {}", op1, op2);
		} 
	}


	private static Map<Class<? extends Compute>, BiFunction<String, String, String>> MATH_FUNCTIONS_MAP = new HashMap<>();

	static {
		MATH_FUNCTIONS_MAP.put(Add.class,
				(op1, op2) -> Integer.toString(Integer.parseInt(op1) + Integer.parseInt(op2)));
		MATH_FUNCTIONS_MAP.put(Multiply.class,
				(op1, op2) -> Integer.toString(Integer.parseInt(op1) * Integer.parseInt(op2)));
		MATH_FUNCTIONS_MAP.put(Divide.class,
				(op1, op2) -> {
					int op2Value = Integer.parseInt(op2);
					if(op2Value != 0) {
						return Integer.toString(Integer.parseInt(op1) / op2Value);
					}
					return null;
				});

	}
}
