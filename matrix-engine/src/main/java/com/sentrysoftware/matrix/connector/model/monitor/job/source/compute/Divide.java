package com.sentrysoftware.matrix.connector.model.monitor.job.source.compute;

import static com.sentrysoftware.matrix.common.helpers.StringHelper.addNonNull;

import java.util.StringJoiner;
import java.util.function.UnaryOperator;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.engine.strategy.source.compute.IComputeVisitor;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Divide extends Compute {

	private static final long serialVersionUID = 3909226699144193542L;

	private Integer column;
	// Number value or Column(n), hence the String type 
	private String divideBy;

	@Builder
	public Divide(Integer index, Integer column, String divideBy) {
		super(index);
		this.column = column;
		this.divideBy = divideBy;
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
		addNonNull(stringJoiner, "- divideBy=", divideBy);

		return stringJoiner.toString();
	}

	@Override
	public Divide copy() {
		return Divide
			.builder()
			.index(index)
			.column(column)
			.divideBy(divideBy)
			.build();
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		divideBy = updater.apply(divideBy);
	}
}
