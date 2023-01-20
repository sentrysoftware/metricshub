package com.sentrysoftware.matrix.connector.model.identity.criterion;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Wbem extends WqlCriterion {

	private static final long serialVersionUID = 1L;

	@Builder
	public Wbem(
		String type,
		boolean forceSerialization,
		String query,
		String namespace,
		String expectedResult,
		String errorMessage
	) {

		super(type, forceSerialization, query, namespace, expectedResult, errorMessage);
	}


	@Override
	public Wbem copy() {
		return Wbem
			.builder()
			.query(getQuery())
			.namespace(getNamespace())
			.expectedResult(getExpectedResult())
			.errorMessage(getErrorMessage())
			.forceSerialization(isForceSerialization())
			.build();
	}

	@Override
	public String toString() {
		return super.toString();
	}
}
