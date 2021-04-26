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
public class BooleanParam extends AbstractParam {

	private boolean value;

	@Builder
	public BooleanParam(String name, Long collectTime, Threshold threshold, ParameterState parameterState, boolean value) {

		super(name, collectTime, threshold, parameterState);
		this.value = value;
	}
}
