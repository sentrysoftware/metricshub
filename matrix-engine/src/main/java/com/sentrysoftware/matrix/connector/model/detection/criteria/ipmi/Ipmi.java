package com.sentrysoftware.matrix.connector.model.detection.criteria.ipmi;

import com.sentrysoftware.matrix.connector.model.detection.criteria.Criterion;
import com.sentrysoftware.matrix.engine.strategy.detection.CriterionTestResult;
import com.sentrysoftware.matrix.engine.strategy.detection.ICriterionVisitor;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Ipmi extends Criterion {

	private static final long serialVersionUID = 3276866736810038056L;

	@Builder
	public Ipmi(boolean forceSerialization, int index) {

		super(forceSerialization, index);
	}

	@Override
	public CriterionTestResult accept(final ICriterionVisitor criterionVisitor) {
		return criterionVisitor.visit(this);
	}

	@Override
	public String toString() {
		return "- IPMI";
	}
}
