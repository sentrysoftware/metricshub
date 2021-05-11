package com.sentrysoftware.matrix.engine.strategy.source.compute;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.TranslationTable;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.AbstractMatchingLines;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@AllArgsConstructor
@NoArgsConstructor
@Slf4j
public class ComputeVisitor implements IComputeVisitor {

	private static final Pattern COLUMN_PATTERN =  Pattern.compile(HardwareConstants.COLUMN_REGEXP, Pattern.CASE_INSENSITIVE);

	@Getter
	@Setter
	private SourceTable sourceTable;

	@Setter
	private Connector connector;

	private static final Map<Class<? extends Compute>, BiFunction<String, String, String>> MATH_FUNCTIONS_MAP;

	private static final BiFunction<String, Map<String, String>, String> PER_BIT_MATCHES_TRANSLATION_FUNCTION = (str, translations) -> translations.get(
			HardwareConstants.OPENING_PARENTHESIS + str + HardwareConstants.COMMA + HardwareConstants.ONE + HardwareConstants.CLOSING_PARENTHESIS);

	private static final BiFunction<String, Map<String, String>, String> PER_BIT_NOT_MATCHES_TRANSLATION_FUNCTION = (str, translations) -> translations.get(
			HardwareConstants.OPENING_PARENTHESIS + str + HardwareConstants.COMMA + HardwareConstants.ZERO +  HardwareConstants.CLOSING_PARENTHESIS);

	private static final BiFunction<String, Map<String, String>, String> TRANSLATION_FUNCTION = (str, translations) -> translations.get(str);

	static {
		final Map<Class<? extends Compute>, BiFunction<String, String, String>> map = new HashMap<>();
		map.put(Add.class,
				(op1, op2) -> Integer.toString(Integer.parseInt(op1) + Integer.parseInt(op2)));
		map.put(Multiply.class,
				(op1, op2) -> Integer.toString(Integer.parseInt(op1) * Integer.parseInt(op2)));
		map.put(Divide.class,
				(op1, op2) -> {
					int op2Value = Integer.parseInt(op2);
					if(op2Value != 0) {
						return Integer.toString(Integer.parseInt(op1) / op2Value);
					}
					return null;
				});
		MATH_FUNCTIONS_MAP = Collections.unmodifiableMap(map);

	}

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
			log.warn("The index of the column to add cannot be < 1, the addition computation cannot be performed.");
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
			log.warn("The index of the column to divide cannot be < 1, the division computation cannot be performed.");
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
		int columnIndex = duplicateColumn.getColumn() -1;

		for (List<String> elementList : sourceTable.getTable()) {
			if (columnIndex >= 0 && columnIndex < elementList.size()) {
				elementList.add(columnIndex, elementList.get(columnIndex));
			}
		}

	}

	@Override
	public void visit(final ExcludeMatchingLines excludeMatchingLines) {

		processAbstractMatchingLines(excludeMatchingLines);
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

		processAbstractMatchingLines(keepOnlyMatchingLines);
	}

	/**
	 * Updates the {@link SourceTable}
	 * by keeping or removing lines
	 * according to the definition of the given {@link AbstractMatchingLines}.
	 *
	 * @param abstractMatchingLines	The {@link AbstractMatchingLines}
	 *                              describing the rules
	 *                              regarding which lines should be kept or removed in/from the {@link SourceTable}.
	 */
	private void processAbstractMatchingLines(AbstractMatchingLines abstractMatchingLines) {

		if (abstractMatchingLines != null
				&& abstractMatchingLines.getColumn() != null
				&& abstractMatchingLines.getColumn() > 0
				&& sourceTable != null
				&& sourceTable.getTable() != null
				&& !sourceTable.getTable().isEmpty()
				&& abstractMatchingLines.getColumn() <= sourceTable.getTable().get(0).size()) {

			int columnIndex = abstractMatchingLines.getColumn() - 1;

			String pslRegexp = abstractMatchingLines.getRegExp();
			List<String> valueList = abstractMatchingLines.getValueList();

			List<List<String>> table = sourceTable.getTable();

			// If there are both a regex and a valueList, both are applied, one after the other.
			if (pslRegexp != null && !pslRegexp.isEmpty()) {

				table = filterTable(table, columnIndex, getPredicate(pslRegexp, abstractMatchingLines));
			}

			if (valueList != null && !valueList.isEmpty()) {

				table = filterTable(table, columnIndex, getPredicate(valueList, abstractMatchingLines));
			}

			sourceTable.setTable(table);
		}
	}

	/**
	 * @param pslRegexp				The PSL regular expression used to filter the lines in the {@link SourceTable}.
	 * @param abstractMatchingLines	The {@link AbstractMatchingLines}
	 *                              describing the rules
	 *                              regarding which lines should be kept or removed in/from the {@link SourceTable}.
	 *
	 * @return						A {@link Predicate},
	 * 								based on the given regular expression
	 * 								and the concrete type of the given {@link AbstractMatchingLines},
	 * 								that can be used to filter the lines in the {@link SourceTable}.
	 */
	private Predicate<String> getPredicate(String pslRegexp, AbstractMatchingLines abstractMatchingLines) {

		Pattern pattern = Pattern.compile(PslUtils.psl2JavaRegex(pslRegexp));

		return abstractMatchingLines instanceof KeepOnlyMatchingLines
			? value -> pattern.matcher(value).matches()
			: value -> !pattern.matcher(value).matches();
	}

	/**
	 * @param valueList				The list of values used to filter the lines in the {@link SourceTable}.
	 * @param abstractMatchingLines	The {@link AbstractMatchingLines}
	 *                              describing the rules
	 *                              regarding which lines should be kept or removed in/from the {@link SourceTable}.
	 *
	 * @return						A {@link Predicate},
	 * 								based on the given list of values
	 * 								and the concrete type of the given {@link AbstractMatchingLines},
	 * 								that can be used to filter the lines in the {@link SourceTable}.
	 */
	private Predicate<String> getPredicate(List<String> valueList, AbstractMatchingLines abstractMatchingLines) {

		return abstractMatchingLines instanceof KeepOnlyMatchingLines
			? valueList::contains
			: value -> !valueList.contains(value);
	}

	/**
	 * @param table			The table that is being filtered.
	 * @param columnIndex	The index of the column
	 *                      whose values should evaluate to true against the given {@link Predicate}.
	 * @param predicate		The {@link Predicate} against which
	 *                      each value at the given column in the resulting table
	 *                      must evaluate to true.
	 *
	 * @return				A new table
	 * 						having just the rows of the given table
	 * 						for which values at the given column evaluate to true against the given {@link Predicate}.
	 */
	private List<List<String>> filterTable(List<List<String>> table, int columnIndex, Predicate<String> predicate) {

		List<List<String>> sourceTableTmp = new ArrayList<>();
		for (List<String> line : table) {

			if (predicate.test(line.get(columnIndex))) {
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

			// If leftConcat.getString() is like "Column(n)", we concat the column n instead of leftConcat.getString() 
			if (COLUMN_PATTERN.matcher(concatString).matches()) {
				int leftColumnIndex = Integer.parseInt(concatString.substring(concatString.indexOf(HardwareConstants.OPENING_PARENTHESIS) + 1, 
						concatString.indexOf(HardwareConstants.CLOSING_PARENTHESIS))) - 1;

				if (leftColumnIndex < sourceTable.getTable().get(0).size()) {
					sourceTable.getTable()
							.forEach(column -> column.set(columnIndex, column.get(leftColumnIndex).concat(column.get(columnIndex))));
				}
			} else {
				sourceTable.getTable()
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
			log.warn("Compute Operation (Multiply) is null, the table remains unchanged.");
			return;
		}

		Integer columnIndex = multiply.getColumn();
		String operand2 = multiply.getMultiplyBy();
		
		if (columnIndex == null || operand2 == null ) {
			log.warn("Arguments in Compute Operation (Multiply) : {} are wrong, the table remains unchanged.", multiply);
			return;
		}

		if (columnIndex < 1 ) {
			log.warn("The index of the column to multiply cannot be < 1, the multiplication computation cannot be performed.");
			return;
		}

		performMathematicalOperation(multiply, columnIndex, operand2);
	}

	@Override
	public void visit(final PerBitTranslation perBitTranslation) {

		if (!perBitTtranslationCheck(perBitTranslation)) {
			return;
		}

		Map<String, String> translations = perBitTranslation.getBitTranslationTable().getTranslations();
		Integer columnIndex = perBitTranslation.getColumn() - 1;
		List<Integer> bitList = perBitTranslation.getBitList();

		String newValue;

		for (List<String> line : sourceTable.getTable()) {

			if (columnIndex < line.size()) {

				Integer valueToBeReplacedInt;

				try {
					valueToBeReplacedInt = Integer.parseInt(line.get(columnIndex));
				} catch (NumberFormatException e) {
					log.warn("Data is not correctly formatted.");
					return;
				}

				List<String> columnResult = new ArrayList<>();

				for (Integer bit : bitList) {
					if (((int) Math.pow(2, bit) & valueToBeReplacedInt) != 0) {
						newValue = translate(bit.toString(), translations, PER_BIT_MATCHES_TRANSLATION_FUNCTION);
						if (newValue != null) {
							columnResult.add(newValue);
						}
					} else {
						newValue = translate(bit.toString(), translations, PER_BIT_NOT_MATCHES_TRANSLATION_FUNCTION);
						if (newValue != null) {
							columnResult.add(newValue);
						}
					}
				}

				if (!columnResult.isEmpty()) {
					String separator = HardwareConstants.WHITE_SPACE + HardwareConstants.DASH + HardwareConstants.WHITE_SPACE;

					line.set(columnIndex, columnResult
							.stream()
							.map(value -> String.join(separator, value))
							.collect(Collectors.joining(separator)));
				}
			}
		}
	}

	/**
	 * PerBitTtranslation visit check.
	 * @param perBitTranslation
	 * @return
	 */
	private boolean perBitTtranslationCheck(final PerBitTranslation perBitTranslation) {
		if (perBitTranslation == null) {
			log.warn("The Source (PerBitTranslation) to visit is null, the PerBitTranslation computation cannot be performed.");
			return false;
		}

		TranslationTable bitTranslationTable = perBitTranslation.getBitTranslationTable();
		if (bitTranslationTable == null) {
			log.warn("TranslationTable is null, the PerBitTranslation computation cannont be performed.");
			return false;
		}

		Map<String, String> translations = bitTranslationTable.getTranslations();
		if (translations == null) {
			log.warn("The Translation Map {} is null, the PerBitTranslation computation cannot be performed.",
					bitTranslationTable.getName());
			return false;
		}

		Integer columnIndex = perBitTranslation.getColumn() - 1;
		if (columnIndex < 0) {
			log.warn("The index of the column to translate cannot be < 1, the PerBitTranslation computation cannot be performed.");
			return false;
		}

		List<Integer> bitList = perBitTranslation.getBitList();
		if (bitList == null) {
			log.warn("BitList is null, the PerBitTranslation computation cannont be performed.");
			return false;
		}

		return true;
	}

	/**
	 * Translates the valueTotranslate using the translationMap in the translationFunction.
	 * @param valueTotranslate
	 * @param translationMap
	 * @param translationFunction
	 * @return
	 */
	private String translate(final String valueTotranslate, final Map<String, String> translationMap, final BiFunction<String, Map<String, String>, String> translationFunction) {
		return translationFunction.apply(valueTotranslate, translationMap);
	}

	@Override
	public void visit(final Replace replace) {
		if (replace == null) {
			log.warn("Compute Operation (Replace) is null, the table remains unchanged.");
			return;
		}

		Integer columnToReplace = replace.getColumn();
		String strToReplace = replace.getReplace();
		String replacement = replace.getReplaceBy();

		if (columnToReplace == null || strToReplace == null || replacement == null) {
			log.warn("Arguments in Compute Operation (Replace) : {} are wrong, the table remains unchanged.", replace);
			return;
		}

		if (columnToReplace < 1) {
			log.warn("The index of the column to replace cannot be < 1, the replacement computation cannot be performed.");
			return;
		}

		int columnIndex = columnToReplace - 1;

		// If replacement is like "Column(n)", we replace the strToReplace by the content of the column n.
		if (COLUMN_PATTERN.matcher(replacement).matches()) {
			int replacementColumnIndex = Integer.parseInt(replacement.substring(
					replacement.indexOf(HardwareConstants.OPENING_PARENTHESIS) + 1, 
					replacement.indexOf(HardwareConstants.CLOSING_PARENTHESIS))) - 1;

			if (replacementColumnIndex < sourceTable.getTable().get(0).size()) {
				sourceTable.getTable()
				.forEach(column -> column.set(
						columnIndex, 
						column.get(columnIndex).replace(strToReplace, column.get(replacementColumnIndex)))
						);
			}
		} else {
			sourceTable.getTable()
			.forEach(column -> column.set(columnIndex, column.get(columnIndex).replace(strToReplace, replacement)));
		}

		sourceTable.setTable(SourceTable.csvToTable(SourceTable.tableToCsv(sourceTable.getTable(), HardwareConstants.SEMICOLON), HardwareConstants.SEMICOLON));
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

		int columnIndex = translate.getColumn() - 1;
		if (columnIndex < 0) {
			log.warn("The index of the column to translate cannot be < 1, the translate computation cannot be performed.");
			return;
		}

		for (List<String> line : sourceTable.getTable()) {

			if (columnIndex < line.size()) {
				String valueToBeReplaced = line.get(columnIndex);
				String newValue = translate(valueToBeReplaced, translations, TRANSLATION_FUNCTION);

				if (newValue != null) {
					line.set(columnIndex, newValue);
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
	 * Perform a mathematical computation (add, multiply or divide) on a given column in the sourcTable
	 * Check if the operand2 is a reference to a column or a raw value 
	 * @param computeOperation The compute operation must be one of : Add, Multiply, Divide.
	 * @param column column to be changed
	 * @param operand2 can be a reference to another column or a raw value
	 */
	private void performMathematicalOperation(final Compute computeOperation, Integer column, String operand2) {
		
		if (!MATH_FUNCTIONS_MAP.containsKey(computeOperation.getClass())) {
			log.warn("The compute operation must be one of : Add, Multiply, Divide.");
			return;
		}

		Integer columnIndex = column - 1;
		int operandByIndex = -1;

		Matcher matcher = COLUMN_PATTERN.matcher(operand2);
		if (matcher.matches()) {
			try {
				operandByIndex = Integer.parseInt(matcher.group(1)) - 1;
				if (operandByIndex < 0) {
					log.warn("The operand2 column index cannot be < 1, the {} computation cannot be performed, the table remains unchanged.", computeOperation.getClass());
					return;
				}
			} catch (NumberFormatException e) {
				log.warn("NumberFormatException : {} is not a correct operand2, the table remains unchanged.", operand2, computeOperation);
				log.warn("Exception : ", e);
				return;
			}

		} else if (!operand2.matches("\\d+")) {
			log.warn("operand2 is not a number: {}, the table remains unchanged.", operand2);
			return;
		}

		
		performMathComputeOnTable(computeOperation, columnIndex, operand2, operandByIndex);
	}

	/**
	 * Execute the computational operation (Divide, Add or Multiply) on each row of the tableSource
	 * @param computeOperation 
	 * @param columnIndex
	 * @param op2
	 * @param op2Index
	 */
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

	/**
	 * Given two operands, perform an addition, multiplication or division and modify the given line on the given columnIndex
	 * @param computeOperation
	 * @param columnIndex
	 * @param line
	 * @param op1
	 * @param op2
	 */
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
			log.warn("Exception : ", e);
		} 
	}
}
