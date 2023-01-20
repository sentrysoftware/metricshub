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
public class Translate extends Compute {

	private static final long serialVersionUID = 1L;

	private Integer column;
	private String translationTable;

	@Builder
	public Translate(String type, Integer column, String translationTable) {
		super(type);
		this.column = column;
		this.translationTable = translationTable;
	}

	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- column=", column);
		addNonNull(stringJoiner, "- translationTable=", translationTable);

		return stringJoiner.toString();
	}

	@Override
	public Translate copy() {
		return Translate
			.builder()
			.type(type)
			.column(column)
			.translationTable(translationTable)
			.build();
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		translationTable = updater.apply(translationTable);
	}
}
