package com.sentrysoftware.matrix.common.helpers;

import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.EMPTY;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.N_A;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SEMICOLON;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.WHITE_SPACE;

public class TextTableHelper {

	private TextTableHelper() {

	}

	private static final int PADDING_SIZE = 1;
	private static final String NEW_LINE = "\n";
	private static final String TABLE_JOINT_SYMBOL = "+";
	private static final String TABLE_VALUE_SPLIT_SYMBOL = "|";
	private static final String TABLE_BORDER_SYMBOL = "-";

	public static String generateTextTable(List<List<String>> rows) {

		if (rows == null || rows.isEmpty()) {
			return EMPTY;
		}

		List<String> firstRow = rows.get(0);
		Assert.notNull(firstRow, "firstRow cannot be null.");

		List<TableHeader> headers = IntStream
			.range(1, firstRow.size() + 1)
			.mapToObj(index -> new TableHeader(String.format("Column %d", index), TextDataType.STRING))
			.collect(Collectors.toList());

		return generateTextTable(headers, rows);
	}

	public static String generateTextTable(String semiColonSeparatedColumns, List<List<String>> rows) {

		return (semiColonSeparatedColumns == null || semiColonSeparatedColumns.isBlank())
		? generateTextTable(rows)
		: generateTextTable(semiColonSeparatedColumns.split(SEMICOLON), rows);
	}

	public static String generateTextTable(String[] columns, List<List<String>> rows) {

		if (columns == null || columns.length == 0) {
			return generateTextTable(rows);
		}

		List<TableHeader> headers = Arrays
			.stream(columns)
			.map(columnName -> new TableHeader(columnName, TextDataType.STRING))
			.collect(Collectors.toList());

		return generateTextTable(headers, rows);
	}

	public static String generateTextTable(Collection<String> columns, List<List<String>> rows) {

		if (columns == null || columns.isEmpty()) {
			return generateTextTable(rows);
		}

		List<TableHeader> headers = columns
			.stream()
			.map(columnName -> new TableHeader(columnName, TextDataType.STRING))
			.collect(Collectors.toList());

		return generateTextTable(headers, rows);
	}

	/**
	 * Create a text table for the given <code>headers</code> and the
	 * corresponding <code>rows</code>.<br>
	 * Each row is designed as a {@link List} of {@link String} values.
	 * <ul>
	 *    <li>This method expects non-null <code>headers</code> {@link List} and non-null <code>rows</code> {@link List}</li>
	 *    <li><code>headers</code> {@link List} cannot contain null values</li>
	 *    <li>The row {@link List} elements order is important and must match the same order as on the <code>headers</code> {@link List}</li>
	 *    <li>A <code>null</code> row is ignored</li>
	 *    <li>A row can be empty or having a size less than the <code>headers</code> size</li>
	 *    <li>Extra cells in a row are ignored</li>
	 * </ul>
	 *
	 * @param headers {@link List} of columns we wish to insert in the text table
	 * @param rows {@link List} of {@link List} elements
	 * @return Text table
	 */
	public static String generateTextTable(final List<TableHeader> headers, List<List<String>> rows) {

		checkArguments(headers, rows);

		rows = cleanRows(rows, headers.size());

		final StringBuilder stringBuilder = new StringBuilder();

		final Map<Integer, Integer> columnMaxWidthMapping = getMaximumWidthOfTable(headers, rows);

		// Open the table with a new row line
		createRowLine(stringBuilder, headers.size(), columnMaxWidthMapping);
		stringBuilder.append(NEW_LINE);

		// Create the header line
		for (int headerIndex = 0; headerIndex < headers.size(); headerIndex++) {

			fillCell(stringBuilder, headers.get(headerIndex).getTitle(), headerIndex, columnMaxWidthMapping,
				headers.get(headerIndex).getType());
		}
		stringBuilder.append(NEW_LINE);

		// Create a new row line to separate the headers and the rows
		createRowLine(stringBuilder, headers.size(), columnMaxWidthMapping);

		// Loop over each row to append them in the current table
		for (List<String> row : rows) {

			stringBuilder.append(NEW_LINE);

			// Append each cell the row
			for (int cellIndex = 0; cellIndex < row.size(); cellIndex++) {

				fillCell(stringBuilder, row.get(cellIndex), cellIndex, columnMaxWidthMapping,
					headers.get(cellIndex).getType());
			}
		}
		stringBuilder.append(NEW_LINE);

		// Close the table with a new row line
		createRowLine(stringBuilder, headers.size(), columnMaxWidthMapping);

		return stringBuilder.toString();
	}

	/**
	 * Remove null rows clean each row
	 * @param rows we wish to clean
	 * @param headersSize the size of the headers
	 * @return new cleaned rows
	 */
	private static List<List<String>> cleanRows(final List<List<String>> rows, final int headersSize) {

		return rows
			.stream()
			.filter(Objects::nonNull)
			.map(row -> cleanRow(row, headersSize))
			.collect(Collectors.toList());
	}

	/**
	 * Clean the given row based on the given <code>headersSize</code>
	 * <ul>
	 *  <li>Replace null cells by {@link HardwareConstants#N_A}</li>
	 *  <li>Create missing cells with {@link HardwareConstants#N_A}</li>
	 *  <li>Remove extra cells</li>
	 * </ul>
	 * @param row we wish to clean
	 * @param headersSize the size of the headers
	 * @return new cleaned row
	 */
	private static List<String> cleanRow(final List<String> row, final int headersSize) {

		ArrayList<String> result = new ArrayList<>(row);

		// Replace null cells
		// noinspection ResultOfMethodCallIgnored
		Collections.replaceAll(result, null, N_A);

		// Create missing cells
		if (result.size() < headersSize) {

			return Stream
				.concat(result.stream(), Stream.generate(() -> N_A).limit((long) headersSize - result.size()))
				.collect(Collectors.toList());

		} else if (result.size() > headersSize) {

			// Remove extra cells
			return result
				.stream()
				.limit(headersSize)
				.collect(Collectors.toList());
		}

		return result;
	}

	private static void checkArguments(final List<TableHeader> headers, final List<List<String>> rows) {

		Assert.notNull(headers, "headers cannot be null.");
		Assert.notNull(rows, "rows cannot be null.");

		final int indexOfNull = headers.indexOf(null);
		Assert.isTrue(indexOfNull == -1, () -> String.format("Header at index '%d' cannot be null.", indexOfNull));

//		// Checking all rows are the same size
//		int commonRowSize = -1;
//		for (List<String> row : rows) {
//
//			Assert.notNull(row, "row cannot be null.");
//
//			if (commonRowSize == -1) {
//
//				commonRowSize = row.size();
//				continue;
//			}
//
//			Assert.isTrue(row.size() == commonRowSize,
//				String.format("The following row was expected to have %d column(s): %s", commonRowSize, row));
//		}
	}

	/**
	 * A row line is filled with {@link #TABLE_BORDER_SYMBOL} and joined with the  {@link #TABLE_JOINT_SYMBOL}. <br>
	 * E.g: +---------+---------------+--------+
	 * @param stringBuilder the {@link StringBuilder} we wish to update
	 * @param headersSize the number of columns
	 * @param columnMaxWidthMapping maximum size for each column
	 */
	private static void createRowLine(final StringBuilder stringBuilder, final int headersSize,
									  Map<Integer, Integer> columnMaxWidthMapping) {

		for (int i = 0; i < headersSize; i++) {

			// First start with the table joint e.g. '+' character
			if (i == 0) {
				stringBuilder.append(TABLE_JOINT_SYMBOL);
			}

			// Then append the table border character using the max column size + the
			// padding twice since the values will be prefixed and suffixed with the padding
			// size
			stringBuilder.append(TABLE_BORDER_SYMBOL.repeat(columnMaxWidthMapping.get(i) + PADDING_SIZE * 2));

			// Finally, append the table joint
			stringBuilder.append(TABLE_JOINT_SYMBOL);
		}
	}

	/**
	 * Get the maximum width for each column by comparing the header lengths and row cells lengths.
	 * @param headers {@link List} of columns we wish to compare
	 * @param rows {@link List} of {@link List} elements
	 * @return {@link Map} where each column index defines maximum length.
	 */
	private static Map<Integer, Integer> getMaximumWidthOfTable(final List<TableHeader> headers,
																final List<List<String>> rows) {

		final Map<Integer, Integer> columnMaxWidthMapping = new HashMap<>();

		// Initialize the map with header sizes as max lengths
		for (int columnIndex = 0; columnIndex < headers.size(); columnIndex++) {
			columnMaxWidthMapping.put(columnIndex, headers.get(columnIndex).getTitle().length());
		}

		// Loop over all the row cells and determine the maximum size for each column index
		for (final List<String> row : rows) {

			for (int columnIndex = 0; columnIndex < row.size(); columnIndex++) {

				if (row.get(columnIndex).length() > columnMaxWidthMapping.get(columnIndex)) {
					columnMaxWidthMapping.put(columnIndex, row.get(columnIndex).length());
				}
			}
		}

		return columnMaxWidthMapping;
	}

	/**
	 * The optimum cell padding is used for cells with data length less than the maximum column length.
	 * @param cellIndex the index of the cells. i.e. the column index
	 * @param dataLength the length of the cell data
	 * @param columnMaxWidthMapping max column lengths
	 * @return a cell padding size to append before or after the data value
	 */
	private static int getOptimumCellPadding(final int cellIndex, int dataLength,
											 final Map<Integer, Integer> columnMaxWidthMapping) {

		int cellPaddingSize = PADDING_SIZE;

		if (dataLength < columnMaxWidthMapping.get(cellIndex)) {
			cellPaddingSize = PADDING_SIZE + columnMaxWidthMapping.get(cellIndex) - dataLength;
		}

		return cellPaddingSize;
	}

	/**
	 * Simply append white spaces on the given <code>stringBuilder</code>
	 * @param stringBuilder {@link StringBuilder} we wish to update
	 * @param times number of append operations we wish to perform
	 */
	private static void fillSpace(final StringBuilder stringBuilder, final int times) {

		stringBuilder.append(WHITE_SPACE.repeat(times));
	}

	/**
	 * Fill a cell data with its left and right paddings so that it is well displayed
	 * @param stringBuilder {@link StringBuilder} we wish to update
	 * @param cell {@link String} data value
	 * @param cellIndex column index
	 * @param columnMaxWidthMapping maximum size for each column
	 * @param textDataType the type of the data (possible types are in {@link TextDataType})
	 */
	private static void fillCell(final StringBuilder stringBuilder, final String cell, final int cellIndex,
								 final Map<Integer, Integer> columnMaxWidthMapping, TextDataType textDataType) {

		final int cellPaddingSize = getOptimumCellPadding(cellIndex, cell.length(), columnMaxWidthMapping);

		// Open the row, if we are on the first cell then append the split symbol. E.g. '|'
		if (cellIndex == 0) {
			stringBuilder.append(TABLE_VALUE_SPLIT_SYMBOL);
		}

		int rightPadding = PADDING_SIZE;
		int leftPadding = PADDING_SIZE;

		if (TextDataType.STRING.equals(textDataType)) {
			rightPadding = cellPaddingSize;
		} else if (TextDataType.NUMBER.equals(textDataType)) {
			leftPadding = cellPaddingSize;
		} else {
			throw new IllegalStateException(String.format("Unsupported TextDataType: %s", textDataType.name()));
		}

		// Append left padding
		fillSpace(stringBuilder, leftPadding);

		// Append the value
		stringBuilder.append(cell);

		// Append right padding
		fillSpace(stringBuilder, rightPadding);

		// Close the row
		stringBuilder.append(TABLE_VALUE_SPLIT_SYMBOL);
	}
}
