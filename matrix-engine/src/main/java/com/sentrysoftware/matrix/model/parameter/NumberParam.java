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
	private Double rawValue;
	private Double previousRawValue;
	private Long previousCollectTime;

	@Builder
	public NumberParam(String name, Long collectTime, Threshold threshold, ParameterState parameterState, Double value, Double rawValue, String unit) {

		super(name, collectTime, threshold, parameterState, unit);
		this.value = value;
		this.rawValue = rawValue;
	}

	@Override
	public void reset() {

		if (rawValue != null) {
			this.previousCollectTime = getCollectTime();
			this.previousRawValue = rawValue;
		}

		this.rawValue = null;
		this.value = null;

		super.reset();
	}

	@Override
	public String formatValueAsString() {
		return getValueAsString(value);
	}

	@Override
	public Number numberValue() {
		return value;
	}
}
