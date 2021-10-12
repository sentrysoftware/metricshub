package com.sentrysoftware.matrix.model.parameter;

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
	private String previousValue;

	@Builder
	public TextParam(String name, Long collectTime, String unit, String value) {

		super(name, collectTime, unit);
		this.value = value;
	}

	public String getValueOrElse(final String other) {
		return value != null && !value.trim().isEmpty() ? value : other;
	}

	@Override
	public void save() {
		previousValue = value;
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
