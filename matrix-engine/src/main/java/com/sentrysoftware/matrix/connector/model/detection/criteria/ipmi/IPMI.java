package com.sentrysoftware.matrix.connector.model.detection.criteria.ipmi;

import com.sentrysoftware.matrix.connector.model.detection.criteria.Criteria;

import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
public class IPMI extends Criteria {

	private static final long serialVersionUID = 3276866736810038056L;

	@Builder
	public IPMI(boolean forceSerialization) {

		super(forceSerialization);
	}

}
