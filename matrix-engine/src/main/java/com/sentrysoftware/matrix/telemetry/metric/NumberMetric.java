package com.sentrysoftware.matrix.telemetry.metric;

import java.util.Map;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class NumberMetric extends AbstractMetric {

	private Double value;
	private Double previousValue;

	//Default constructor
	public NumberMetric() {
	}

	@Builder
	public NumberMetric(
			String name,
			long collectTime,
			Map<String, String> attributes,
			Double value
	) {
		super(name, collectTime, attributes);
		this.value = value;
	}

	@Override
	public void save() {
		super.save();
		previousValue = value;
	}
}