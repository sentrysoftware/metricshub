package org.sentrysoftware.metricshub.engine.telemetry.metric;

import java.util.Map;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * The `StateSetMetric` class represents a metric that holds a state value from a predefined set of states.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class StateSetMetric extends AbstractMetric {

	/**
	 * The type identifier for the StateSetMetric.
	 */
	public static final String STATE_SET_METRIC_TYPE = "StateSetMetric";

	private String value;
	private String previousValue;
	private String[] stateSet;

	/**
	 * Constructs a new StateSetMetric.
	 *
	 * @param name         The name of the metric.
	 * @param collectTime  The time when the metric was collected.
	 * @param attributes   Additional attributes associated with the metric.
	 * @param value        The current value of the state.
	 * @param stateSet     The set of possible states for the metric.
	 */
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
