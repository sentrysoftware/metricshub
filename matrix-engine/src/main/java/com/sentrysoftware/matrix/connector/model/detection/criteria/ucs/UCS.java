package com.sentrysoftware.matrix.connector.model.detection.criteria.ucs;

import com.sentrysoftware.matrix.connector.model.detection.criteria.Criterion;
import com.sentrysoftware.matrix.engine.strategy.detection.CriterionTestResult;
import com.sentrysoftware.matrix.engine.strategy.detection.ICriterionVisitor;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class UCS extends Criterion {

	private static final long serialVersionUID = 3035383624414379693L;

	private String query;
	private String errorMessage;
	private String expectedResult;

	@Builder
	public UCS(boolean forceSerialization, String query, String errorMessage, String expectedResult, int index) {

		super(forceSerialization, index);
		this.query = query;
		this.errorMessage = errorMessage;
		this.expectedResult = expectedResult;
	}

	@Override
	public CriterionTestResult accept(final ICriterionVisitor criterionVisitor) {
		return criterionVisitor.visit(this);
	}
}
