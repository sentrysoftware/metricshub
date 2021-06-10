package com.sentrysoftware.matrix.connector.model.detection.criteria.wmi;

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
public class WMI extends Criterion {

	private static final long serialVersionUID = -2278078347788456921L;
	private String wbemQuery;
	private String wbemNamespace;
	private String expectedResult;
	private String errorMessage;

	@Builder
	public WMI(boolean forceSerialization, String wbemQuery, String wbemNamespace, String expectedResult, String errorMessage, int index) {

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
