package com.sentrysoftware.matrix.connector.model.detection.criteria.snmp;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SNMPGetNext extends SNMP {

	private static final long serialVersionUID = -3577780284142096293L;

	@Builder
	public SNMPGetNext(boolean forceSerialization, String oid, String expectedResult) {

		super(forceSerialization, oid, expectedResult);
	}

}
