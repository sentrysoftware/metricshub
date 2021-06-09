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
	private String errorMessage;

	@Builder
	public WBEM(boolean forceSerialization, String wbemQuery, String wbemNamespace, String expectedResult,
				String errorMessage, int index) {

		super(forceSerialization, index);
		this.wbemQuery = wbemQuery;
		this.wbemNamespace = wbemNamespace;
		this.expectedResult = expectedResult;
		this.errorMessage = errorMessage;
	}

	@Override
	public CriterionTestResult accept(final ICriterionVisitor criterionVisitor) {
		return criterionVisitor.visit(this);
	}
}
