package org.sentrysoftware.metricshub.engine.telemetry.metric;

import java.util.Map;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * The NumberMetric class represents a numeric metric in the telemetry system.
 * It extends the AbstractMetric class and includes additional properties such as value and previousValue.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class NumberMetric extends AbstractMetric {

	/**
	 * The constant representing the type of the metric (NumberMetric).
	 */
	public static final String NUMBER_METRIC_TYPE = "NumberMetric";

	private Double value;
	private Double previousValue;

	/**
	 * Creates a new instance of NumberMetric using the provided parameters.
	 *
	 * @param name         The name of the metric.
	 * @param collectTime  The timestamp when the metric was collected.
	 * @param attributes   Additional attributes associated with the metric.
	 * @param value        The numeric value of the metric.
	 */
	@Builder
	public NumberMetric(
		final String name,
		final Long collectTime,
		final Map<String, String> attributes,
		final Double value
	) {
		super(name, collectTime, attributes);
		this.value = value;
	}

	@Override
	public void save() {
		super.save();
		previousValue = value;
	}

	@Override
	public String getType() {
		return NUMBER_METRIC_TYPE;
	}
}
