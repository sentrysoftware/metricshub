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

	@Builder
	public StatusParam(String name, Long collectTime, Threshold threshold, ParameterState state,
			String unit, String statusInformation) {

		super(name, collectTime, threshold, state, unit);
		this.status = state.ordinal();
		this.statusInformation = statusInformation;
	}

	@Override
	public void reset() {
		super.reset();

		this.status = null;
		this.statusInformation = null;
	}

	@Override
	public String getValueAsString() {
		return statusInformation;
	}
}
