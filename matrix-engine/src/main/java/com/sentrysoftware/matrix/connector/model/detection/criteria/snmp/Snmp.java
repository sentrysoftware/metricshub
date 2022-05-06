package com.sentrysoftware.matrix.connector.model.detection.criteria.snmp;

import com.sentrysoftware.matrix.connector.model.detection.criteria.Criterion;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public abstract class Snmp extends Criterion {

	private static final long serialVersionUID = -7948517909514524108L;

	private String oid;
	private String expectedResult;

	protected Snmp(boolean forceSerialization, String oid, String expectedResult, int index) {

		super(forceSerialization, index);
		this.oid = oid;
		this.expectedResult = expectedResult;
	}
}
