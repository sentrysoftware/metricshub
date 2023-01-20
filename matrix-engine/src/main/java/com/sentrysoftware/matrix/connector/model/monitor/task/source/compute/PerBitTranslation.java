package com.sentrysoftware.matrix.connector.model.monitor.task.source.compute;

import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.NEW_LINE;
import static com.sentrysoftware.matrix.common.helpers.StringHelper.addNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.UnaryOperator;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PerBitTranslation extends Compute {

	private static final long serialVersionUID = 1L;

	private Integer column;
	private List<Integer> bitList = new ArrayList<>();
	private String translationTable;

	@Builder
	public PerBitTranslation(String type, Integer column, List<Integer> bitList,
			String translationTable) {
		super(type);
		this.column = column;
		this.bitList = bitList == null ? new ArrayList<>() : bitList;
		this.translationTable = translationTable;
	}


	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- column=", column);
		addNonNull(stringJoiner, "- bitList=", bitList);
		addNonNull(stringJoiner, "- translationTable=", translationTable);

		return stringJoiner.toString();

	}

	@Override
	public PerBitTranslation copy() {
		return PerBitTranslation
			.builder()
			.type(type)
			.column(column)
			.bitList(new ArrayList<>(bitList))
			.translationTable(translationTable)
			.build();
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		translationTable = updater.apply(translationTable);
	}
}
