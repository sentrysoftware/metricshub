package com.sentrysoftware.matrix.model.parameter;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public abstract class AbstractParam implements IParameterValue {

	private String name;
	private Long collectTime;
	private String unit;

	protected AbstractParam(String name, Long collectTime, String unit) {
		this.name = name;
		this.collectTime = collectTime;
		this.unit = unit;
	}

}
