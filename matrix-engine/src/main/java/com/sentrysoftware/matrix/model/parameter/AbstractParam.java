package com.sentrysoftware.matrix.model.parameter;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public abstract class AbstractParam implements IParameterValue {

	private String name;
	private Long collectTime;
	private ParameterState state = ParameterState.OK;
	private String unit;

	protected AbstractParam(String name, Long collectTime, ParameterState state, String unit) {
		this.name = name;
		this.collectTime = collectTime;
		this.state = state != null ? state :  ParameterState.OK;
		this.unit = unit;
	}

	@Override
	public void reset() {
		this.collectTime = null;
		this.state = ParameterState.OK;
	}

}
