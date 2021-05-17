package com.sentrysoftware.matrix.connector.model.monitor.job.source.compute;

import com.sentrysoftware.matrix.engine.strategy.source.compute.IComputeVisitor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class KeepOnlyMatchingLines extends AbstractMatchingLines {

	private static final long serialVersionUID = 5853378552607445344L;

	@Builder
	public KeepOnlyMatchingLines(Integer index, Integer column, String regExp, List<String> valueList) {
		super(index, column, regExp, valueList);
	}

	@Override
	public void accept(final IComputeVisitor computeVisitor) {
		computeVisitor.visit(this);
	}
}
