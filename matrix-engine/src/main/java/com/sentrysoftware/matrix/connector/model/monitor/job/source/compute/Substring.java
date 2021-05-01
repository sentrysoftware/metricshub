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
public class Substring extends Compute {

	private static final long serialVersionUID = 1959269892827970861L;

	private Integer column;
	private Integer start;
	private Integer end;

	@Builder
	public Substring(Integer index, Integer column, Integer start, Integer end) {
		super(index);
		this.column = column;
		this.start = start;
		this.end = end;
	}

	@Override
	public void accept(final IComputeVisitor computeVisitor) {
		computeVisitor.visit(this);
	}

}
