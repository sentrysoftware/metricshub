package com.sentrysoftware.matrix.connector.model.detection.criteria.process;

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
public class Process extends Criterion {

	private static final long serialVersionUID = 4418210555494869095L;

	private String processCommandLine;

	@Builder
	public Process(boolean forceSerialization, String processCommandLine, int index) {

		super(forceSerialization, index);
		this.processCommandLine = processCommandLine;
	}

	@Override
	public CriterionTestResult accept(final ICriterionVisitor criterionVisitor) {
		return criterionVisitor.visit(this);
	}
	
}
