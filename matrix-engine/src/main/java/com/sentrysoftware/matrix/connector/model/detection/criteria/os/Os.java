package com.sentrysoftware.matrix.connector.model.detection.criteria.os;

import java.util.HashSet;
import java.util.Set;

import com.sentrysoftware.matrix.connector.model.common.OsType;
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
public class Os extends Criterion {

	private static final long serialVersionUID = -8982076836753923149L;

	private Set<OsType> keepOnly = new HashSet<>();
	private Set<OsType> exclude = new HashSet<>();

	@Builder
	public Os(boolean forceSerialization, Set<OsType> keepOnly, Set<OsType> exclude, int index) {
		super(forceSerialization, index);
		this.keepOnly = keepOnly;
		this.exclude = exclude;
	}

	@Override
	public CriterionTestResult accept(final ICriterionVisitor criterionVisitor) {
		return criterionVisitor.visit(this);
	}
}
