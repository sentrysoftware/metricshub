package org.sentrysoftware.metricshub.engine.telemetry.metric;

import java.util.Map;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class StateSetMetric extends AbstractMetric {

	public static final String STATE_SET_METRIC_TYPE = "StateSetMetric";

	private String value;
	private String previousValue;
	private String[] stateSet;

	@Builder
	public StateSetMetric(
		final String name,
		final Long collectTime,
		final Map<String, String> attributes,
		final String value,
		final String[] stateSet
	) {
		super(name, collectTime, attributes);
		this.value = value;
		this.stateSet = stateSet;
	}

	@Override
	public void save() {
		super.save();
		previousValue = value;
	}

	@Override
	public String getType() {
		return STATE_SET_METRIC_TYPE;
	}
}
