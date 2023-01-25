package com.sentrysoftware.matrix.connector.model.monitor.task.source.compute;

import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.*;
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
public class And extends Compute {

	private static final long serialVersionUID = 1L;

	private Integer column;
	private String value; // NOSONAR

	@Builder
	public And(String type, Integer index, Integer column, String value) {
		super(type);
		this.column = column;
		this.value = value;
	}


	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- column=", column);
		addNonNull(stringJoiner, "- value=", value);

		return stringJoiner.toString();
	}

	@Override
	public And copy() {
		return And
			.builder()
			.type(type)
			.column(column)
			.value(value)
			.build();
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		value = updater.apply(value);
	}
}
