package com.sentrysoftware.matrix.connector.model.monitor.job.source.compute;

import static com.sentrysoftware.matrix.common.helpers.StringHelper.addNonNull;

import java.util.StringJoiner;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.engine.strategy.source.compute.IComputeVisitor;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Substring extends Compute {

	private static final long serialVersionUID = 1959269892827970861L;

	private Integer column;
	private String start;
	private String length;

	@Builder
	public Substring(Integer index, Integer column, String start, String length) {
		super(index);
		this.column = column;
		this.start = start;
		this.length = length;
	}

	@Override
	public void accept(final IComputeVisitor computeVisitor) {
		computeVisitor.visit(this);
	}

	/**
	 * Copy the current instance
	 * 
	 * @return new {@link Substring} instance
	 */
	public Substring copy() {
		return Substring.builder()
				.column(column)
				.start(start)
				.length(length)
				.index(getIndex())
				.build();
	}

	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(HardwareConstants.NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- column=", column);
		addNonNull(stringJoiner, "- start=", start);
		addNonNull(stringJoiner, "- length=", length);

		return stringJoiner.toString();
	}
}
