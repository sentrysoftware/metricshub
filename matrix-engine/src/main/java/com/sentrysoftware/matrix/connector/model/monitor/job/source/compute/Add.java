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
public class Add extends Compute {

	private static final long serialVersionUID = 8539063845713915259L;

	private Integer column;
	// Number value or Column(n), hence the String type 
	private String add;

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

}
