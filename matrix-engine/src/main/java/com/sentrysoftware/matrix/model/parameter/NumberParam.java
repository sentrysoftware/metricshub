package com.sentrysoftware.matrix.model.parameter;

import com.sentrysoftware.matrix.model.threshold.Threshold;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class NumberParam extends AbstractParam {

	private Double value;
	private Double lastValue;
	private Long lastCollectTime;

	@Builder
	public NumberParam(String name, Long collectTime, Threshold threshold, ParameterState parameterState, Double value, String unit) {

		super(name, collectTime, threshold, parameterState, unit);
		this.value = value;
	}

	@Override
	public void reset() {

		this.lastValue = value;
		this.lastCollectTime = getCollectTime();
		this.value = null;

		super.reset();
	}

	@Override
	public String formatValueAsString() {
		return getValueAsString(value);
	}
}
