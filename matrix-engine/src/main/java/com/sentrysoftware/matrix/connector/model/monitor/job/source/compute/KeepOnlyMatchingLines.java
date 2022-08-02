package com.sentrysoftware.matrix.connector.model.monitor.job.source.compute;

import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

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

	@Override
	public KeepOnlyMatchingLines copy() {
		return KeepOnlyMatchingLines
			.builder()
			.index(index)
			.column(column)
			.regExp(regExp)
			.valueSet(valueSet == null ? null :
				valueSet
					.stream()
					.collect(Collectors
						.toCollection(() -> new TreeSet<>(String.CASE_INSENSITIVE_ORDER))
					)
			)
			.build();

	}
}
