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
public class ExtractPropertyFromWbemPath extends Compute {

	private static final long serialVersionUID = 1L;

	private String property;
	private Integer column;

	@Builder
	public ExtractPropertyFromWbemPath(String type, String property, Integer column) {
		super(type);
		this.property = property;
		this.column = column;
	}

	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- column=", column);
		addNonNull(stringJoiner, "- property=", property);

		return stringJoiner.toString();
	}

	@Override
	public ExtractPropertyFromWbemPath copy() {
		return ExtractPropertyFromWbemPath
			.builder()
			.type(type)
			.property(property)
			.column(column)
			.build();
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		property = updater.apply(property);
	}
}
