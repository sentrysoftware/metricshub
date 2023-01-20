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
public class Substring extends Compute {

	private static final long serialVersionUID = 1L;

	private Integer column;
	private String start;
	private String length;

	@Builder
	public Substring(String type, Integer column, String start, String length) {
		super(type);
		this.column = column;
		this.start = start;
		this.length = length;
	}

	/**
	 * Copy the current instance
	 * 
	 * @return new {@link Substring} instance
	 */
	public Substring copy() {
		return Substring.builder()
				.type(type)
				.column(column)
				.start(start)
				.length(length)
				.build();
	}

	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- column=", column);
		addNonNull(stringJoiner, "- start=", start);
		addNonNull(stringJoiner, "- length=", length);

		return stringJoiner.toString();
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		start = updater.apply(start);
		length = updater.apply(length);
	}
}
