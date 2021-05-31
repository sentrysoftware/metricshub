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
public class StatusParam extends AbstractParam {

	private Integer status;
	private String statusInformation;
	private ParameterState previousState;

	@Builder
	public StatusParam(String name, Long collectTime, Threshold threshold, ParameterState state,
			String unit, String statusInformation) {

		super(name, collectTime, threshold, state, unit);
		this.status = state != null ? state.ordinal() : null;
		this.statusInformation = statusInformation;
	}

	@Override
	public void reset() {

		if (status != null) {
			this.previousState = getState();
		}

		this.status = null;
		this.statusInformation = null;

		super.reset();
	}

	@Override
	public String formatValueAsString() {
		return statusInformation;
	}

	@Override
	public Number numberValue() {
		return status;
	}

}
