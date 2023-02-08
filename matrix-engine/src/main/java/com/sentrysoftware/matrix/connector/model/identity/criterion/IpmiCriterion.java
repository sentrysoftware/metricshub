package com.sentrysoftware.matrix.connector.model.identity.criterion;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class IpmiCriterion extends Criterion {

	private static final long serialVersionUID = 1L;

	@Builder
	public IpmiCriterion(String type, boolean forceSerialization) {

		super(type, forceSerialization);
	}

	@Override
	public String toString() {
		return "- IPMI";
	}
}
