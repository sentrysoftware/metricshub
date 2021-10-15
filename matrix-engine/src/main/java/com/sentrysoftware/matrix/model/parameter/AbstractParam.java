package com.sentrysoftware.matrix.model.parameter;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public abstract class AbstractParam implements IParameter {

	private String name;
	private Long collectTime;
	private Long previousCollectTime;
	private String unit;

	protected AbstractParam(String name, Long collectTime, String unit) {
		this.name = name;
		this.collectTime = collectTime;
		this.unit = unit;
	}

	@Override
	public void save() {
		previousCollectTime = collectTime;
	}
}
