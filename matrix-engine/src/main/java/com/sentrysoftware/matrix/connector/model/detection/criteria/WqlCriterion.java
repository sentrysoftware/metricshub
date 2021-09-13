package com.sentrysoftware.matrix.connector.model.detection.criteria;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class WqlCriterion extends Criterion {

	private static final long serialVersionUID = 1L;

	private String wbemQuery;
	private String wbemNamespace;
	private String expectedResult;
	private String errorMessage;

	protected WqlCriterion(boolean forceSerialization, String wbemQuery, String wbemNamespace, String expectedResult,
				String errorMessage, int index) {

		super(forceSerialization, index);
		this.wbemQuery = wbemQuery;
		this.wbemNamespace = wbemNamespace;
		this.expectedResult = expectedResult;
		this.errorMessage = errorMessage;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("- WQL Query: ")
				.append(wbemQuery)
				.append("\n- Namespace: ")
				.append(wbemNamespace);
		if (expectedResult != null && !expectedResult.isBlank()) {
			sb.append("\n- Expected Result: ").append(expectedResult);
		}
		return sb.toString();
	}

	public abstract WqlCriterion copy();
}
