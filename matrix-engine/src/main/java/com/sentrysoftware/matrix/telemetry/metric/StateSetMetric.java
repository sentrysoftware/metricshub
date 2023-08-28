package com.sentrysoftware.matrix.telemetry.metric;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Map;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class StateSetMetric extends AbstractMetric {

	private String value;
	private String previousValue;
	private String[] stateSet;

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