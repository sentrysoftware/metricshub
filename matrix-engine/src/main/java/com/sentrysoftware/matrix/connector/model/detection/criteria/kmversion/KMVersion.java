package com.sentrysoftware.matrix.connector.model.detection.criteria.kmversion;

import com.sentrysoftware.matrix.connector.model.detection.criteria.Criterion;
import com.sentrysoftware.matrix.engine.strategy.detection.CriterionTestResult;
import com.sentrysoftware.matrix.engine.strategy.detection.ICriterionVisitor;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class KMVersion extends Criterion {

	private static final long serialVersionUID = -5818089198751116637L;

	private String version;

	@Builder
	public KMVersion(boolean forceSerialization, String version, int index) {
		super(forceSerialization, index);
		this.version = version;
	}

	@Override
	public CriterionTestResult accept(final ICriterionVisitor criterionVisitor) {
		return criterionVisitor.visit(this);
	}
}
