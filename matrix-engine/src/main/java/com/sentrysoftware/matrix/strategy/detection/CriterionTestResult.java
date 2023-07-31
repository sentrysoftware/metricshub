package com.sentrysoftware.matrix.strategy.detection;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CriterionTestResult {

	private String result;

	private boolean success;

	private String message;

	private Throwable exception;

}
