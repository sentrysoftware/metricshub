package com.sentrysoftware.matrix.connector.model.monitor.task.source.compute;

import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.NEW_LINE;
import static com.sentrysoftware.matrix.common.helpers.StringHelper.addNonNull;

import java.util.StringJoiner;
import java.util.function.UnaryOperator;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ArrayTranslate extends Compute {

	private static final long serialVersionUID = 1L;

	private Integer column;
	private String translationTable;
	private String arraySeparator;
	private String resultSeparator;

	@Builder
	public ArrayTranslate(String type, Integer column, String translationTable, String arraySeparator,
			String resultSeparator) {
		super(type);
		this.column = column;
		this.translationTable = translationTable;
		this.arraySeparator = arraySeparator;
		this.resultSeparator = resultSeparator;
	}

	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- column=", column);
		addNonNull(stringJoiner, "- translationTable=", translationTable);
		addNonNull(stringJoiner, "- arraySeparator=", arraySeparator);
		addNonNull(stringJoiner, "- resultSeparator=", resultSeparator);

		return stringJoiner.toString();
	}

	@Override
	public ArrayTranslate copy() {
		return ArrayTranslate
			.builder()
			.type(type)
			.column(column)
			.translationTable(translationTable)
			.arraySeparator(arraySeparator)
			.resultSeparator(resultSeparator)
			.build();
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		arraySeparator = updater.apply(arraySeparator);
		resultSeparator = updater.apply(resultSeparator);
		translationTable = updater.apply(translationTable);
	}

}
