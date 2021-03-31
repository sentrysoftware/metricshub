package com.sentrysoftware.matrix.connector.model.detection.criteria.snmp;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SNMPGet extends SNMP {

	private static final long serialVersionUID = -8920718839930074126L;

	@Builder
	public SNMPGet(boolean forceSerialization, String oid, String expectedResult) {

		super(forceSerialization, oid, expectedResult);
	}

}
