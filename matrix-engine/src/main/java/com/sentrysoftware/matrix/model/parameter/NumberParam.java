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
	private Double previousValue;

	@Builder
	public NumberParam(String name, Long collectTime, Threshold threshold, ParameterState parameterState, Double value,
			Double previousValue) {

		super(name, collectTime, threshold, parameterState);
		this.value = value;
		this.previousValue = value;
	}

}
