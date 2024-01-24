package org.sentrysoftware.metricshub.engine.connector.model.identity.criterion;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.sentrysoftware.metricshub.engine.strategy.detection.CriterionTestResult;
import org.sentrysoftware.metricshub.engine.strategy.detection.ICriterionProcessor;

/**
 * Represents a detection criterion based on Wbem queries.
 * Extends the abstract class {@link WqlCriterion} and inherits from {@link Criterion}.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class WbemCriterion extends WqlCriterion {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs a new {@code WbemCriterion} instance using the provided parameters.
	 *
	 * @param type               The type of the criterion.
	 * @param forceSerialization Flag indicating whether serialization should be forced.
	 * @param query              The Wbem query for the criterion.
	 * @param namespace          The namespace for the Wbem query.
	 * @param expectedResult     The expected result of the criterion.
	 * @param errorMessage       The error message associated with the criterion.
	 */
	@JsonCreator
	@Builder
	public WbemCriterion(
		@JsonProperty("type") String type,
		@JsonProperty("forceSerialization") boolean forceSerialization,
		@JsonProperty(value = "query", required = true) @NonNull String query,
		@JsonProperty(value = "namespace") String namespace,
		@JsonProperty(value = "expectedResult") String expectedResult,
		@JsonProperty(value = "errorMessage") String errorMessage
	) {
		super(type, forceSerialization, query, namespace, expectedResult, errorMessage);
	}

	@Override
	public WbemCriterion copy() {
		return WbemCriterion
			.builder()
			.query(getQuery())
			.namespace(getNamespace())
			.expectedResult(getExpectedResult())
			.errorMessage(getErrorMessage())
			.forceSerialization(isForceSerialization())
			.build();
	}

	@Override
	public String toString() {
		return super.toString();
	}

	@Override
	public CriterionTestResult accept(ICriterionProcessor criterionProcessor) {
		return criterionProcessor.process(this);
	}
}
