package com.sentrysoftware.matrix.engine.strategy.source;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.COMMA;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.EMPTY;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.NEW_LINE;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SourceTable {

	private String rawData;

	@Default
	private List<List<String>> table = new ArrayList<>();
	
	@Default
	private List<String> headers = new ArrayList<>();

	/**
	 * Transform the {@link List} table to a {@link String} representation
	 * [[a1,b1,c2],[a1,b1,c1]]
	 *  =>
	 * a1,b1,c1,
	 * a2,b2,c2,
	 *
	 * @param table            The table result we wish to parse
	 * @param separator        The cells separator on each line
	 * @param replaceSeparator Whether we should replace the separator by comma 
	 * @return {@link String} value
	 */
	public static String tableToCsv(final List<List<String>> table, final String separator, final boolean replaceSeparator) {
		if (table != null) {
			return table
					.stream()
					.filter(Objects::nonNull)
					.map(line -> replaceSeparator ?  
							line.stream().map(val -> val.replace(separator, COMMA)).collect(Collectors.toList()) : line)
					.map(line -> String.join(separator, line) + separator)
					.collect(Collectors.joining(NEW_LINE));
		}

		return EMPTY;
	}

	/**
	 * Return the List representation of the CSV String table :
	 * a1,b1,c1,
	 * a2,b2,c2,
	 * =>
	 * [[a1,b1,c2],[a1,b1,c1]]
	 * 
	 * @param csvTable 
	 *             The CSV table we wish to parse
	 * @param separator 
	 *             The cells separator
	 * @return {@link List} of {@link List} table
	 */
	public static List<List<String>> csvToTable(final String csvTable, final String separator) {
		if (csvTable != null) {
			return Stream
					.of(csvTable.split("\n"))
					.map(line -> lineToList(line, separator))
					.filter(line -> !line.isEmpty())
					.collect(Collectors.toList());
		}
		return new ArrayList<>();
	}


	/**
	 * Transform a line to a list
	 * a1,b1,c1, => [ a1, b1, c1 ]
	 * @param line 
	 *             The CSV line we wish to parse 
	 * @param separator 
	 *             The cells serparator
	 * @return {@link List} of {@link String}
	 */
	 public static List<String> lineToList(String line, final String separator) {
		if (line != null && !line.isEmpty()) {
			// Make sure the line ends with the separator
			line = !line.endsWith(separator) ? line + separator : line;

			// Make sure we don't change the integrity of the line with the split in case of empty cells
			final String[] split = line.split(separator, -1);
			return Stream
					.of(split)
					.limit(split.length - 1L)
					.collect(Collectors.toList());
		}
		return new ArrayList<>();
	}

	/**
	 * @return Empty {@link SourceTable} instance
	 */
	public static SourceTable empty() {
		return SourceTable.builder().build();
	}

}
