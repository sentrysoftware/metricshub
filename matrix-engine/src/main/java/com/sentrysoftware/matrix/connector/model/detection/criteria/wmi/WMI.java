package com.sentrysoftware.matrix.connector.model.detection.criteria.wmi;

import com.sentrysoftware.matrix.connector.model.detection.criteria.WqlCriterion;
import com.sentrysoftware.matrix.engine.strategy.detection.CriterionTestResult;
import com.sentrysoftware.matrix.engine.strategy.detection.ICriterionVisitor;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class WMI extends WqlCriterion {

	private static final long serialVersionUID = -2278078347788456921L;

	@Override
	public CriterionTestResult accept(final ICriterionVisitor criterionVisitor) {
		return criterionVisitor.visit(this);
	}
}
