package org.sentrysoftware.metricshub.engine.strategy.detection;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sentrysoftware.metricshub.engine.connector.model.Connector;

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
