package com.sentrysoftware.matrix.telemetry;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.ALTERNATE_COLUMN_SEPARATOR;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.NEW_LINE;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SourceTable {

	private String rawData;
	private List<List<String>> table;
	private List<String> headers;
	private ReentrantLock forceSerializationLock;

	/**
	 * Transform the {@link List} table to a {@link String} representation
	 * [[a1,b1,c2],[a1,b1,c1]]
	 * =>
	 * a1,b1,c1,
	 * a2,b2,c2,
	 *
	 * @param table            The table result we wish to parse
	 * @param separator        The cells separator on each line
	 * @param replaceSeparator Whether we should replace the separator by comma
	 * @return {@link String} value
	 */
	public static String tableToCsv(final List<List<String>> table, final String separator,
									final boolean replaceSeparator) {
		if (table != null) {
			return table
					.stream()
					.filter(Objects::nonNull)
					.map(line -> replaceSeparator
							? line
							.stream()
							.map(val -> val.replace(separator, ALTERNATE_COLUMN_SEPARATOR))
							.collect(Collectors.toList())
							: line)
					.map(line -> String.join(separator, line) + separator)
					.collect(Collectors.joining(NEW_LINE));
		}

		return "";
	}
}
