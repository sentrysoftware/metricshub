package com.sentrysoftware.matrix.connector.model.identity.criterion;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SnmpGet extends Snmp {

	private static final long serialVersionUID = 1L;

	@Builder
	public SnmpGet(String type, boolean forceSerialization, String oid, String expectedResult) {

		super(type, forceSerialization, oid, expectedResult);
	}

}
