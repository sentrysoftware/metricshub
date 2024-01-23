package org.sentrysoftware.metricshub.engine.connector.model.identity.criterion;

import static com.fasterxml.jackson.annotation.Nulls.FAIL;
import static com.fasterxml.jackson.annotation.Nulls.SKIP;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.NEW_LINE;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.StringJoiner;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.sentrysoftware.metricshub.engine.connector.deserializer.custom.BooleanDeserializer;
import org.sentrysoftware.metricshub.engine.connector.deserializer.custom.NonBlankDeserializer;
import org.sentrysoftware.metricshub.engine.connector.deserializer.custom.TimeoutDeserializer;
import org.sentrysoftware.metricshub.engine.strategy.detection.CriterionTestResult;
import org.sentrysoftware.metricshub.engine.strategy.detection.ICriterionProcessor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OsCommandCriterion extends Criterion {

	private static final long serialVersionUID = 1L;

	@NonNull
	@JsonSetter(nulls = FAIL)
	@JsonDeserialize(using = NonBlankDeserializer.class)
	private String commandLine;

	private String errorMessage;
	private String expectedResult;

	@JsonDeserialize(using = BooleanDeserializer.class)
	@JsonSetter(nulls = SKIP)
	private Boolean executeLocally = false;

	@JsonDeserialize(using = TimeoutDeserializer.class)
	private Long timeout;

	@Builder
	@JsonCreator
	public OsCommandCriterion(
		@JsonProperty("type") String type,
		@JsonProperty("forceSerialization") boolean forceSerialization,
		@JsonProperty(value = "commandLine", required = true) @NonNull String commandLine,
		@JsonProperty("errorMessage") String errorMessage,
		@JsonProperty("expectedResult") String expectedResult,
		@JsonProperty("executeLocally") Boolean executeLocally,
		@JsonProperty("timeout") Long timeout
	) {
		super(type, forceSerialization);
		this.commandLine = commandLine;
		this.errorMessage = errorMessage;
		this.expectedResult = expectedResult;
		this.executeLocally = executeLocally != null && executeLocally;
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

	@Override
	public CriterionTestResult accept(ICriterionProcessor criterionProcessor) {
		return criterionProcessor.process(this);
	}
}
