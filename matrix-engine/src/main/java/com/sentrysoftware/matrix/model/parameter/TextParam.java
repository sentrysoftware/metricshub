package com.sentrysoftware.matrix.model.parameter;

import java.util.List;

import com.sentrysoftware.matrix.model.alert.AlertRule;

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

	public static final String TEXT_TYPE = "TextParam";

	private String value;

	@Builder
	public TextParam(String name, Long collectTime, List<AlertRule> alertRules, ParameterState parameterState, String unit, String value) {

		super(name, collectTime, parameterState, unit);
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
	public String formatValueAsString() {
		return getValueAsString(value);
	}

	@Override
	public Number numberValue() {
		return null;
	}

	@Override
	public String getType() {
		return TEXT_TYPE;
	}

}
