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
public class StatusParam extends AbstractParam {

	public static final String STATUS_TYPE = "StatusParam";

	private Integer status;
	private String statusInformation;
	private ParameterState previousState;

	@Builder
	public StatusParam(String name, Long collectTime, ParameterState state, String unit, String statusInformation) {

		super(name, collectTime, state, unit);
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
	public Number numberValue() {
		return status;
	}

	@Override
	public String getType() {
		return STATUS_TYPE;
	}

}
