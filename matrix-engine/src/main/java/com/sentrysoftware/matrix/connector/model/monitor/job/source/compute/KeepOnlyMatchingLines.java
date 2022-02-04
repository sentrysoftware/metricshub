package com.sentrysoftware.matrix.connector.model.monitor.job.source.compute;

import java.util.Set;

import com.sentrysoftware.matrix.engine.strategy.source.compute.IComputeVisitor;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class KeepOnlyMatchingLines extends AbstractMatchingLines {

	private static final long serialVersionUID = 5853378552607445344L;

	@Builder
	public KeepOnlyMatchingLines(Integer index, Integer column, String regExp, Set<String> valueSet) {
		super(index, column, regExp, valueSet);
	}

	@Override
	public void accept(final IComputeVisitor computeVisitor) {
		computeVisitor.visit(this);
	}

	@Override
	public String toString() {
		return super.toString();
	}
}
