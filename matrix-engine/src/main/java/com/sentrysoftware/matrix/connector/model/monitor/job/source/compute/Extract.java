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

}
