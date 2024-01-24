package org.sentrysoftware.metricshub.engine.connector.model.identity.criterion;

import static com.fasterxml.jackson.annotation.Nulls.FAIL;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import org.sentrysoftware.metricshub.engine.connector.deserializer.custom.NonBlankDeserializer;
import org.sentrysoftware.metricshub.engine.strategy.detection.CriterionTestResult;
import org.sentrysoftware.metricshub.engine.strategy.detection.ICriterionProcessor;

/**
 * Represents a process detection criterion.
 * This criterion allows the detection of processes based on their command line.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProcessCriterion extends Criterion {

	private static final long serialVersionUID = 1L;

	/**
	 * The command line associated with the process criterion.
	 */
	@NonNull
	@JsonDeserialize(using = NonBlankDeserializer.class)
	@JsonSetter(nulls = FAIL)
	private String commandLine;

	/**
	 * Builder for constructing instances of {@link ProcessCriterion}.
	 *
	 * @param type                Type of the criterion.
	 * @param forceSerialization Flag indicating whether serialization should be forced.
	 * @param commandLine         The command line associated with the process criterion.
	 */
	@Builder
	@JsonCreator
	public ProcessCriterion(
		@JsonProperty("type") String type,
		@JsonProperty("forceSerialization") boolean forceSerialization,
		@JsonProperty(value = "commandLine", required = true) @NonNull String commandLine
	) {
		super(type, forceSerialization);
		this.commandLine = commandLine;
	}

	@Override
	public CriterionTestResult accept(ICriterionProcessor criterionProcessor) {
		return criterionProcessor.process(this);
	}
}
