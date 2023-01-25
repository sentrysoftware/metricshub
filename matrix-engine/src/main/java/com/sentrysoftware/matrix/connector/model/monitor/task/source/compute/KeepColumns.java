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
public class KeepColumns extends Compute {

	private static final long serialVersionUID = 1L;

	private List<Integer> columnNumbers = new ArrayList<>();

	@Builder
	public KeepColumns(String type, List<Integer> columnNumbers) {
		super(type);
		this.columnNumbers = columnNumbers == null ? new ArrayList<>() : columnNumbers;
	}

	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- columnNumbers=", columnNumbers);

		return stringJoiner.toString();
	}

	@Override
	public KeepColumns copy() {
		return KeepColumns
			.builder()
			.type(type)
			.columnNumbers(columnNumbers != null ? new ArrayList<>(columnNumbers) : null)
			.build();
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		// Not implemented because this class doesn't define string members
	}
}
