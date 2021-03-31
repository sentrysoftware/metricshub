package com.sentrysoftware.matrix.model.parameter;

import com.sentrysoftware.matrix.model.threshold.Threshold;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TextParam extends AbstractParam {

	private String value;
	private String previousValue;

	@Builder
	public TextParam(String name, Long collectTime, Threshold threshold, ParameterState parameterState, String value, String previousValue) {

		super(name, collectTime, threshold, parameterState);
		this.value = value;
		this.previousValue = value;
	}

	
}
