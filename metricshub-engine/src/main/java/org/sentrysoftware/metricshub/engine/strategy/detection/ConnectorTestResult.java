package org.sentrysoftware.metricshub.engine.strategy.detection;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sentrysoftware.metricshub.engine.connector.model.Connector;

/**
 * The {@code ConnectorTestResult} class represents the result of testing criteria for a specific connector during the detection process.
 * It contains information about the tested connector, a list of {@link CriterionTestResult} objects, and a method to check if the connector's criteria are successful.
 *
 * <p>
 * The class is designed to be used as a result container during the connector detection process.
 * It includes information about the tested connector and the results of testing individual criteria associated with the connector.
 * The success of the connector's criteria is determined based on the results of individual criterion tests.
 * </p>
 */
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
