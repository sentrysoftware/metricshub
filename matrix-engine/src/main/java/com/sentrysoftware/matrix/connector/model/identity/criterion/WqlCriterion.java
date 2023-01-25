package com.sentrysoftware.matrix.connector.model.identity.criterion;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class WqlCriterion extends Criterion {

	private static final long serialVersionUID = 1L;

	private String query;
	private String namespace = "root/cimv2";
	private String expectedResult;
	private String errorMessage;

	protected WqlCriterion(
		String type,
		boolean forceSerialization,
		String query,
		String namespace,
		String expectedResult,
		String errorMessage
	) {

		super(type, forceSerialization);
		this.query = query;
		this.namespace = namespace;
		this.expectedResult = expectedResult;
		this.errorMessage = errorMessage;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("- WQL Query: ")
				.append(query)
				.append("\n- Namespace: ")
				.append(namespace);
		if (expectedResult != null && !expectedResult.isBlank()) {
			sb.append("\n- Expected Result: ").append(expectedResult);
		}
		return sb.toString();
	}

	public abstract WqlCriterion copy();
}
