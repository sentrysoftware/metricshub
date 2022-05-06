package com.sentrysoftware.matrix.connector.model.detection.criteria.oscommand;

import java.util.StringJoiner;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.connector.model.detection.criteria.Criterion;
import com.sentrysoftware.matrix.engine.strategy.detection.CriterionTestResult;
import com.sentrysoftware.matrix.engine.strategy.detection.ICriterionVisitor;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OsCommand extends Criterion {

	private static final long serialVersionUID = -2984562425705831946L;

	private String commandLine;
	private String errorMessage;
	private String expectedResult;
	private boolean executeLocally;
	private Long timeout;

	@Builder
	public OsCommand(boolean forceSerialization, String commandLine, String errorMessage, String expectedResult,
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

	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(HardwareConstants.NEW_LINE);

		if (commandLine != null && !commandLine.isBlank()) {
			stringJoiner.add(new StringBuilder("- CommandLine: ").append(commandLine));
		}
		
		stringJoiner.add(new StringBuilder("- ExecuteLocally: ").append(executeLocally));
		
		if (expectedResult != null) {
			stringJoiner.add(new StringBuilder("- ExpectedResult: ").append(expectedResult));
		}
		
		if (timeout != null) {
			stringJoiner.add(new StringBuilder("- Timeout: ").append(timeout));
		}
		
		return stringJoiner.toString();
	}

}
