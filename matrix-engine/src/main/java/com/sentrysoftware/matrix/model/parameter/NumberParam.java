package com.sentrysoftware.matrix.model.parameter;

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

	public static final String NUMBER_TYPE = "NumberParam";

	private Double value;
	private Double rawValue;
	private Double previousRawValue;
	private Long previousCollectTime;

	@Builder
	public NumberParam(String name, Long collectTime, ParameterState parameterState, Double value, Double rawValue, String unit) {

		super(name, collectTime, parameterState, unit);
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
	public Number numberValue() {
		return value;
	}

	@Override
	public String getType() {
		return NUMBER_TYPE;
	}

}
