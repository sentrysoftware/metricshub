package com.sentrysoftware.matrix.connector.model.identity.criterion;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public abstract class Snmp extends Criterion {

	private static final long serialVersionUID = 1L;

	private String oid;
	private String expectedResult;

	protected Snmp(String type, boolean forceSerialization, String oid, String expectedResult) {

		super(type, forceSerialization);
		this.oid = oid;
		this.expectedResult = expectedResult;
	}
}
