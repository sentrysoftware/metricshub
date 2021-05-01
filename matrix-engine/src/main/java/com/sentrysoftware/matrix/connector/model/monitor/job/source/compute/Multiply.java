package com.sentrysoftware.matrix.connector.model.monitor.job.source.compute;

import com.sentrysoftware.matrix.engine.strategy.source.compute.IComputeVisitor;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Multiply extends Compute {

	private static final long serialVersionUID = -2587218670274401808L;

	private Integer column;
	// Number value or Column(n), hence the String type 
	private String multiplyBy;

	@Builder
	public Multiply(Integer index, Integer column, String multiplyBy) {
		super(index);
		this.column = column;
		this.multiplyBy = multiplyBy;
	}

	@Override
	public void accept(final IComputeVisitor computeVisitor) {
		computeVisitor.visit(this);
	}

}
