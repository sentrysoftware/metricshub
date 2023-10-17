package com.sentrysoftware.metricshub.engine.connector.model.identity.criterion;

import com.sentrysoftware.metricshub.engine.strategy.detection.CriterionTestResult;
import com.sentrysoftware.metricshub.engine.strategy.detection.ICriterionProcessor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductRequirementsCriterion extends Criterion {

	private static final long serialVersionUID = 1L;

	private String engineVersion;
	private String kmVersion;

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
