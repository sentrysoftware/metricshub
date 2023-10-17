package com.sentrysoftware.metricshub.engine.strategy.detection;

import com.sentrysoftware.metricshub.engine.connector.model.Connector;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ConnectorTestResult {

	private Connector connector;

	@Default
	private List<CriterionTestResult> criterionTestResults = new ArrayList<>();

	/**
	 * Whether the connector's criteria are successfully executed or not
	 *
	 * @return boolean value
	 */
	public boolean isSuccess() {
		return !criterionTestResults.isEmpty() && criterionTestResults.stream().allMatch(CriterionTestResult::isSuccess);
	}
}
