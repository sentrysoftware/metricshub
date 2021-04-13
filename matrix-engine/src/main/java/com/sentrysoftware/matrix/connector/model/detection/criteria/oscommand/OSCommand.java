package com.sentrysoftware.matrix.connector.model.detection.criteria.oscommand;

import com.sentrysoftware.matrix.connector.model.detection.criteria.Criterion;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OSCommand extends Criterion {

	private static final long serialVersionUID = -2984562425705831946L;

	private String commandLine;
	private String errorMessage;
	private String expectedResult;
	private boolean executeLocally;
	private Long timeout;

	@Builder
	public OSCommand(boolean forceSerialization, String commandLine, String errorMessage, String expectedResult,
			boolean executeLocally, Long timeout) {

		super(forceSerialization);
		this.commandLine = commandLine;
		this.errorMessage = errorMessage;
		this.expectedResult = expectedResult;
		this.executeLocally = executeLocally;
		this.timeout = timeout;
	}

	
}
