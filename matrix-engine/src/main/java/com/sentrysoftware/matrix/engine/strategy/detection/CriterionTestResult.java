package com.sentrysoftware.matrix.engine.strategy.detection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CriterionTestResult {
	private static final CriterionTestResult EMPTY = CriterionTestResult.builder().build();

	private String result;
	private boolean success;
	private String message;

	public static CriterionTestResult empty() {
		return EMPTY;
	}
}
