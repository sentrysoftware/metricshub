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

}
