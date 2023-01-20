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
public class Replace extends Compute {

	private static final long serialVersionUID = 1L;

	private Integer column;
	private String existingValue;
	private String newValue;

	@Builder
	public Replace(String type, Integer column, String existingValue, String newValue) {
		super(type);
		this.column = column;
		this.existingValue = existingValue;
		this.newValue = newValue;
	}

	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- column=", column);
		addNonNull(stringJoiner, "- existingValue=", existingValue);
		addNonNull(stringJoiner, "- newValue=", newValue);

		return stringJoiner.toString();
	}

	@Override
	public Replace copy() {
		return Replace
			.builder()
			.type(type)
			.column(column)
			.existingValue(existingValue)
			.newValue(newValue)
			.build();
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		existingValue = updater.apply(existingValue);
		newValue = updater.apply(newValue);
	}
}
