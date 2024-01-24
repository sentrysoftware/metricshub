package org.sentrysoftware.metricshub.engine.connector.model.identity.criterion;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.sentrysoftware.metricshub.engine.strategy.detection.CriterionTestResult;
import org.sentrysoftware.metricshub.engine.strategy.detection.ICriterionProcessor;

/**
 * Represents a detection criterion based on product requirements, including engine and Knowledge Module (KM) versions.
 * This criterion checks whether the specified engine and KM versions match the requirements.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductRequirementsCriterion extends Criterion {

	private static final long serialVersionUID = 1L;

	/**
	 * The required engine version for the criterion.
	 */
	private String engineVersion;
	/**
	 * The required Knowledge Module (KM) version for the criterion.
	 */
	private String kmVersion;

	/**
	 * Constructor to create an instance of {@link ProductRequirementsCriterion} using a builder.
	 *
	 * @param type                Type of the criterion.
	 * @param forceSerialization Flag indicating whether serialization should be forced.
	 * @param engineVersion       The required engine version for the criterion.
	 * @param kmVersion           The required Knowledge Module (KM) version for the criterion.
	 */
	@Builder
	public ProductRequirementsCriterion(String type, boolean forceSerialization, String engineVersion, String kmVersion) {
		super(type, forceSerialization);
		this.engineVersion = engineVersion;
		this.kmVersion = kmVersion;
	}

	@Override
	public CriterionTestResult accept(ICriterionProcessor criterionProcessor) {
		return criterionProcessor.process(this);
	}
}
