package com.sentrysoftware.matrix.connector.model.detection.criteria.oscommand;

import java.util.Map;

import com.sentrysoftware.matrix.connector.model.common.EmbeddedFile;
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
public class OSCommand extends Criterion {

	private static final long serialVersionUID = -2984562425705831946L;

	private String commandLine;
	private String errorMessage;
	private String expectedResult;
	private boolean executeLocally;
	private Long timeout;

	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	private Map<Integer, EmbeddedFile> embeddedFiles;

	@Builder
	public OSCommand(boolean forceSerialization, String commandLine, String errorMessage, String expectedResult,
			boolean executeLocally, Long timeout, int index) {

		super(forceSerialization, index);
		this.commandLine = commandLine;
		this.errorMessage = errorMessage;
		this.expectedResult = expectedResult;
		this.executeLocally = executeLocally;
		this.timeout = timeout;
	}

	@Override
	public CriterionTestResult accept(final ICriterionVisitor criterionVisitor) {
		return criterionVisitor.visit(this);
	}
	
}
