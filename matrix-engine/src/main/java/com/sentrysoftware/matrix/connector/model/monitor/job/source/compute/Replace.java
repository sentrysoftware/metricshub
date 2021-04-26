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
public class Replace extends Compute {

	private static final long serialVersionUID = -1177932638215228955L;

	private Integer column;
	private String replace;
	private String replaceBy;

	@Builder
	public Replace(Integer index, Integer column, String replace, String replaceBy) {
		super(index);
		this.column = column;
		this.replace = replace;
		this.replaceBy = replaceBy;
	}

	@Override
	public void accept(final IComputeVisitor computeVisitor) {
		computeVisitor.visit(this);
	}

}
