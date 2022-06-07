package com.sentrysoftware.matrix.connector.model.detection.criteria.wbem;

import com.sentrysoftware.matrix.connector.model.detection.criteria.WqlCriterion;
import com.sentrysoftware.matrix.engine.strategy.detection.CriterionTestResult;
import com.sentrysoftware.matrix.engine.strategy.detection.ICriterionVisitor;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Wbem extends WqlCriterion {

	private static final long serialVersionUID = -7417503756436261103L;

	@Builder
	public Wbem(boolean forceSerialization, String wbemQuery, String wbemNamespace, String expectedResult,
				String errorMessage, int index) {

		super(forceSerialization, wbemQuery, wbemNamespace, expectedResult, errorMessage, index);
	}

	@Override
	public CriterionTestResult accept(final ICriterionVisitor criterionVisitor) {
		return criterionVisitor.visit(this);
	}

	@Override
	public Wbem copy() {
		return Wbem
				.builder()
				.wbemQuery(getWbemQuery())
				.wbemNamespace(getWbemNamespace())
				.expectedResult(getExpectedResult())
				.errorMessage(getErrorMessage())
				.forceSerialization(isForceSerialization())
				.index(getIndex())
				.build();
	}

	@Override
	public String toString() {
		return super.toString();
	}
}