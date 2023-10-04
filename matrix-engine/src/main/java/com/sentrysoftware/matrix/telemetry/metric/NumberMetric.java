package com.sentrysoftware.matrix.telemetry.metric;

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
public class NumberMetric extends AbstractMetric {

	public static final String NUMBER_METRIC_TYPE = "NumberMetric";

	private Double value;
	private Double previousValue;

	@Builder
	public NumberMetric(String name, long collectTime, Map<String, String> attributes, Double value) {
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
