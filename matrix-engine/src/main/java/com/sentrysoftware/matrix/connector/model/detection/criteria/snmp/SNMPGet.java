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
public class SNMPGet extends SNMP {

	private static final long serialVersionUID = -8920718839930074126L;

	@Builder
	public SNMPGet(boolean forceSerialization, String oid, String expectedResult, int index) {

		super(forceSerialization, oid, expectedResult, index);
	}

	@Override
	public CriterionTestResult accept(final ICriterionVisitor criterionVisitor) {
		return criterionVisitor.visit(this);
	}
}
