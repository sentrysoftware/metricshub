package com.sentrysoftware.matrix.engine.strategy.detection;

import java.util.ArrayList;
import java.util.List;

import com.sentrysoftware.matrix.connector.model.Connector;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder.Default;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TestedConnector {

	private Connector connector;
	@Default
	private List<CriterionTestResult> criterionTestResults = new ArrayList<>();

	public boolean isSuccess() {
		return !criterionTestResults.isEmpty() && criterionTestResults.stream().allMatch(CriterionTestResult::isSuccess);
	}
}
