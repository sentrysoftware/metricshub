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
public class Substract extends Compute {

	private static final long serialVersionUID = -832322604772227972L;

	private Integer column;
	// Number value or Column(n), hence the String type 
	private String substract;

	@Builder
	public Substract(Integer index, Integer column, String substract) {
		super(index);
		this.column = column;
		this.substract = substract;
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
		addNonNull(stringJoiner, "- substract=", substract);

		return stringJoiner.toString();
	}
}
