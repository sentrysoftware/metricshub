package com.sentrysoftware.metricshub.engine.alert;

import static com.sentrysoftware.metricshub.engine.common.helpers.StringHelper.addNonNull;

import com.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants;
import java.util.StringJoiner;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

		addNonNull(stringJoiner, "Problem           : ", problem);
		addNonNull(stringJoiner, "Consequence       : ", consequence);
		addNonNull(stringJoiner, "Recommended Action: ", recommendedAction);

		return stringJoiner.toString();
	}
}
