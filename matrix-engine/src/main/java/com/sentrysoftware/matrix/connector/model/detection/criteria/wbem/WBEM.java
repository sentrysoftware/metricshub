package com.sentrysoftware.matrix.connector.model.detection.criteria.wbem;

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
public class WBEM extends Criterion {

	private static final long serialVersionUID = -7417503756436261103L;

	private String wbemQuery;
	private String wbemNamespace;
	private String expectedResult;

	@Builder
	public WBEM(boolean forceSerialization, String wbemQuery, String wbemNamespace, String expectedResult, int index) {

		super(forceSerialization, index);
		this.wbemQuery = wbemQuery;
		this.wbemNamespace = wbemNamespace;
		this.expectedResult = expectedResult;
	}

	@Override
	public CriterionTestResult accept(final ICriterionVisitor criterionVisitor) {
		return criterionVisitor.visit(this);
	}
}
