package com.sentrysoftware.matrix.connector.model.detection.criteria.snmp;

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
public class SNMPGetNext extends SNMP {

	private static final long serialVersionUID = -3577780284142096293L;

	@Builder
	public SNMPGetNext(boolean forceSerialization, String oid, String expectedResult, int index) {

		super(forceSerialization, oid, expectedResult, index);
	}

	@Override
	public CriterionTestResult accept(final ICriterionVisitor criterionVisitor) {
		return criterionVisitor.visit(this);
	}
}
