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
	public TextParam(String name, Long collectTime, Threshold threshold, ParameterState parameterState, String value) {

		super(name, collectTime, threshold, parameterState);
		this.value = value;
	}

	public String getValueOrElse(final String other) {
		return value != null && !value.trim().isEmpty() ? value : other;
	}

}
