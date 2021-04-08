package com.sentrysoftware.matrix.connector.model.detection.criteria.snmp;

import com.sentrysoftware.matrix.connector.model.detection.criteria.Criterion;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class SNMP extends Criterion {

	private static final long serialVersionUID = -7948517909514524108L;

	private String oid;
	private String expectedResult;

	protected SNMP(boolean forceSerialization, String oid, String expectedResult) {

		super(forceSerialization);
		this.oid = oid;
		this.expectedResult = expectedResult;
	}
}
