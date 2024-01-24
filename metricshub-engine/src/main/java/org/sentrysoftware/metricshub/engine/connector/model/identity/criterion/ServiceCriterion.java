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
import org.sentrysoftware.metricshub.engine.connector.deserializer.custom.NonBlankDeserializer;
import org.sentrysoftware.metricshub.engine.strategy.detection.CriterionTestResult;
import org.sentrysoftware.metricshub.engine.strategy.detection.ICriterionProcessor;

/**
 * Represents a detection criterion based on a service. This criterion checks whether the specified service is present.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ServiceCriterion extends Criterion {

	private static final long serialVersionUID = 1L;

	/**
	 * The name of the service for the criterion.
	 */
	@NonNull
	@JsonDeserialize(using = NonBlankDeserializer.class)
	@JsonSetter(nulls = FAIL)
	private String name;

	/**
	 * Constructor to create an instance of {@link ServiceCriterion} using a builder.
	 *
	 * @param type                Type of the criterion.
	 * @param forceSerialization Flag indicating whether serialization should be forced.
	 * @param name                The name of the service for the criterion.
	 */
	@Builder
	@JsonCreator
	public ServiceCriterion(
		@JsonProperty("type") String type,
		@JsonProperty("forceSerialization") boolean forceSerialization,
		@JsonProperty(value = "name", required = true) @NonNull String name
	) {
		super(type, forceSerialization);
		this.name = name;
	}

	@Override
	public String toString() {
		return "- Service: " + name;
	}

	@Override
	public CriterionTestResult accept(ICriterionProcessor criterionProcessor) {
		return criterionProcessor.process(this);
	}
}
