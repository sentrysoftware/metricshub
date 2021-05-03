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
public class TextParam extends AbstractParam {

	private String value;

	@Builder
	public TextParam(String name, Long collectTime, Threshold threshold, ParameterState parameterState, String unit, String value) {

		super(name, collectTime, threshold, parameterState, unit);
		this.value = value;
	}

	public String getValueOrElse(final String other) {
		return value != null && !value.trim().isEmpty() ? value : other;
	}

	@Override
	public void reset() {
		super.reset();

		this.value = null;
	}

	@Override
	public String getValueAsString() {
		return getValueAsString(value);
	}

}
