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
			String statusInformation) {

		super(name, collectTime, threshold, state);
		this.status = state.ordinal();
		this.statusInformation = statusInformation;
	}

}
