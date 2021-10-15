package com.sentrysoftware.matrix.connector.model.detection.criteria.sshinteractive;

import java.util.ArrayList;
import java.util.List;

import com.sentrysoftware.matrix.connector.model.common.sshinteractive.step.Step;
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
public class SshInteractive extends Criterion {

	private static final long serialVersionUID = 8279611415738510282L;

	private Integer port;
	private String expectedResult;
	private List<Step> steps = new ArrayList<>();

	@Builder
	public SshInteractive(boolean forceSerialization, Integer port, String expectedResult, List<Step> steps, int index) {

		super(forceSerialization, index);
		this.port = port;
		this.expectedResult = expectedResult;
		this.steps = steps;
	}

	@Override
	public CriterionTestResult accept(final ICriterionVisitor criterionVisitor) {
		return criterionVisitor.visit(this);
	}
}
