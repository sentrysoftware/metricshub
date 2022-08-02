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
public class And extends Compute {

	private static final long serialVersionUID = -1026653374377611321L;

	private Integer column;
	private String and; // NOSONAR

	@Builder
	public And(Integer index, Integer column, String and) {
		super(index);
		this.column = column;
		this.and = and;
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
		addNonNull(stringJoiner, "- and=", and);

		return stringJoiner.toString();
	}

	@Override
	public And copy() {
		return And
			.builder()
			.index(index)
			.column(column)
			.and(and)
			.build();
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		and = updater.apply(and);
	}
}
