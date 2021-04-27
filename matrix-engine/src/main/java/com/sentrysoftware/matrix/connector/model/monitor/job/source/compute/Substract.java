package com.sentrysoftware.matrix.connector.model.monitor.job.source.compute;

import com.sentrysoftware.matrix.engine.strategy.source.compute.IComputeVisitor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Substract extends Compute {

	private static final long serialVersionUID = -832322604772227972L;

	private Integer column;
	// Number value or Column(n), hence the String type 
	private String substract;

	@Override
	public void accept(final IComputeVisitor computeVisitor) {
		computeVisitor.visit(this);
	}
}
