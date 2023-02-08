package com.sentrysoftware.matrix.connector.model.identity.criterion;

import java.util.StringJoiner;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sentrysoftware.matrix.connector.deserializer.custom.NonBlankDeserializer;
import com.sentrysoftware.matrix.connector.deserializer.custom.TimeoutDeserializer;

import static com.fasterxml.jackson.annotation.Nulls.FAIL;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.NEW_LINE;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OsCommand extends Criterion {

	private static final long serialVersionUID = 1L;

	@NonNull
	@JsonSetter(nulls = FAIL)
	@JsonDeserialize(using = NonBlankDeserializer.class)
	private String commandLine;
	private String errorMessage;
	private String expectedResult;
	private boolean executeLocally;
	@JsonDeserialize(using = TimeoutDeserializer.class)
	private Long timeout;

	@Builder
	@JsonCreator
	public OsCommand(
			@JsonProperty("type") String type,
			@JsonProperty("forceSerialization") boolean forceSerialization,
			@JsonProperty(value = "commandLine", required = true) @NonNull String commandLine,
			@JsonProperty("errorMessage") String errorMessage,
			@JsonProperty("expectedResult") String expectedResult,
			@JsonProperty("executeLocally") boolean executeLocally,
			@JsonProperty("timeout") Long timeout) {

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