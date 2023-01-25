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
public class DuplicateColumn extends Compute {

	private static final long serialVersionUID = 1L;

	private Integer column;

	@Builder
	public DuplicateColumn(String type, Integer column) {
		super(type);
		this.column = column;
	}

	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- column=", column);

		return stringJoiner.toString();
	}

	@Override
	public DuplicateColumn copy() {
		return DuplicateColumn
			.builder()
			.type(type)
			.column(column)
			.build();
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		// Not implemented because this class doesn't define any string member
	}
}
