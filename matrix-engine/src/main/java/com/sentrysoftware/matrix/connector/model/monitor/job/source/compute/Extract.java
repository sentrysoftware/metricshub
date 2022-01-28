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
public class Extract extends Compute {

	private static final long serialVersionUID = 5738773914074029228L;

	private Integer column;
	private Integer subColumn;
	private String subSeparators;

	@Builder
	public Extract(Integer index, Integer column, Integer subColumn, String subSeparators) {
		super(index);
		this.column = column;
		this.subColumn = subColumn;
		this.subSeparators = subSeparators;
	}

	@Override
	public void accept(final IComputeVisitor computeVisitor) {
		computeVisitor.visit(this);
	}

	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(HardwareConstants.NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- column=", column);
		addNonNull(stringJoiner, "- subColumn=", subColumn);
		addNonNull(stringJoiner, "- subSeparators=", subSeparators);

		return stringJoiner.toString();
	}
}
