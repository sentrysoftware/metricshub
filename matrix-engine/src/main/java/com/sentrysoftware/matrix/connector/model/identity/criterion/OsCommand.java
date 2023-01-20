package com.sentrysoftware.matrix.connector.model.identity.criterion;

import java.util.StringJoiner;

import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.NEW_LINE;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OsCommand extends Criterion {

	private static final long serialVersionUID = 1L;

	private String commandLine;
	private String errorMessage;
	private String expectedResult;
	private boolean executeLocally;
	private Long timeout;

	@Builder
	public OsCommand(
		String type,
		boolean forceSerialization,
		String commandLine,
		String errorMessage,
		String expectedResult,
		boolean executeLocally,
		Long timeout
	) {

		super(type, forceSerialization);
		this.commandLine = commandLine;
		this.errorMessage = errorMessage;
		this.expectedResult = expectedResult;
		this.executeLocally = executeLocally;
		this.timeout = timeout;
	}

	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(NEW_LINE);

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
