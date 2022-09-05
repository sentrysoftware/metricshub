package com.sentrysoftware.matrix.connector.model.monitor.job.source.compute;

import com.sentrysoftware.matrix.engine.strategy.source.compute.IComputeVisitor;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class LeftConcat extends AbstractConcat {

	private static final long serialVersionUID = -7237305051553135699L;

	@Builder
	public LeftConcat(Integer index, Integer column, String string) {
		super(index, column, string);
	}

	@Override
	public void accept(final IComputeVisitor computeVisitor) {
		computeVisitor.visit(this);
	}

	@Override
	public String toString() {
		return super.toString();
	}

	@Override
	public LeftConcat copy() {
		return LeftConcat
			.builder()
			.index(index)
			.column(column)
			.string(string)
			.build();
	}

}
