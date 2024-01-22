package org.sentrysoftware.metricshub.engine.alert;

import java.util.StringJoiner;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants;
import org.sentrysoftware.metricshub.engine.common.helpers.StringHelper;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AlertDetails {

	private String problem;
	private String consequence;
	private String recommendedAction;

	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(MetricsHubConstants.NEW_LINE);

		StringHelper.addNonNull(stringJoiner, "Problem           : ", problem);
		StringHelper.addNonNull(stringJoiner, "Consequence       : ", consequence);
		StringHelper.addNonNull(stringJoiner, "Recommended Action: ", recommendedAction);

		return stringJoiner.toString();
	}
}
