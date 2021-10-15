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

	@Builder
	public NumberParam(String name, Long collectTime, Double value, Double rawValue, String unit) {

		super(name, collectTime, unit);
		this.value = value;
		this.rawValue = rawValue;
	}

	@Override
	public void save() {
		super.save();
		this.previousRawValue = rawValue;
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
