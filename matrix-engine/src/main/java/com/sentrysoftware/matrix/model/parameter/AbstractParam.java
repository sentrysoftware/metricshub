package com.sentrysoftware.matrix.model.parameter;

import com.sentrysoftware.matrix.model.threshold.Threshold;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class AbstractParam implements IParameterValue{

	private String name;
	private Long collectTime;
	private Threshold threshold;
	private ParameterState state = ParameterState.OK;

}
