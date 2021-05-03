package com.sentrysoftware.matrix.model.parameter;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.model.threshold.Threshold;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public abstract class AbstractParam implements IParameterValue {

	private String name;
	private Long collectTime;
	private Threshold threshold;
	private ParameterState state = ParameterState.OK;
	private String unit;

	protected AbstractParam(String name, Long collectTime, Threshold threshold, ParameterState state, String unit) {
		this.name = name;
		this.collectTime = collectTime;
		this.threshold = threshold;
		this.state = state != null ? state :  ParameterState.OK;
		this.unit = unit;
	}

	@Override
	public void reset() {
		this.collectTime = null;
		this.state = null;
		this.state = ParameterState.OK;
	}

	/**
	 * Get the parameter value as {@link String}. Useful for status information
	 * @param <T>
	 * @param value the value we wish to append in the final result
	 * @return {@link String} value
	 */
	protected <T> String getValueAsString(T value) {
		final StringBuilder builder = new StringBuilder(getName());

		builder
		.append(HardwareConstants.COLON)
		.append(HardwareConstants.WHITE_SPACE)
		.append(value);

		if (getUnit() != null) {
			builder.append(HardwareConstants.WHITE_SPACE).append(getUnit());
		}

		return builder.toString();
	}

}
