package com.sentrysoftware.matrix.telemetry.metric;

import java.util.Map;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class StateSetMetric extends AbstractMetric {

	private String value;
	private String previousValue;
	private String[] stateSet;

	// Default constructor
	public StateSetMetric() {
	}

	@Builder
	public StateSetMetric(
			String name,
			long collectTime,
			Map<String, String> attributes,
			String value,
			String[] stateSet
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
}