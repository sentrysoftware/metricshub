package org.sentrysoftware.metricshub.engine.connector.model.identity.criterion;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.sentrysoftware.metricshub.engine.strategy.detection.CriterionTestResult;
import org.sentrysoftware.metricshub.engine.strategy.detection.ICriterionProcessor;

/**
 * Represents an IPMI (Intelligent Platform Management Interface) detection criterion.
 * IPMI is a standardized interface used by system administrators to manage and monitor server systems remotely.
 */
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class IpmiCriterion extends Criterion {

	private static final long serialVersionUID = 1L;

	/**
	 * Builder for constructing instances of {@link IpmiCriterion}.
	 *
	 * @param type                Type of the criterion.
	 * @param forceSerialization Flag indicating whether serialization should be forced.
	 */
	@Builder
	public IpmiCriterion(String type, boolean forceSerialization) {
		super(type, forceSerialization);
	}

	@Override
	public String toString() {
		return "- IPMI";
	}

	@Override
	public CriterionTestResult accept(ICriterionProcessor criterionProcessor) {
		return criterionProcessor.process(this);
	}
}
