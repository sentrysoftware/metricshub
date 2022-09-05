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
public class Add extends Compute {

	private static final long serialVersionUID = 8539063845713915259L;

	private Integer column;
	// Number value or Column(n), hence the String type 
	private String add; // NOSONAR

	@Builder
	public Add(Integer index, Integer column, String add) {
		super(index);
		this.column = column;
		this.add = add;
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
		addNonNull(stringJoiner, "- add=", add);

		return stringJoiner.toString();
	}

	@Override
	public Add copy() {
		return Add
			.builder()
			.index(index)
			.column(column)
			.add(add)
			.build();
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		add = updater.apply(add);
	}

}
