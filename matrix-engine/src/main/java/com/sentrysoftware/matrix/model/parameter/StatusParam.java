package com.sentrysoftware.matrix.model.parameter;

import com.sentrysoftware.matrix.model.threshold.Threshold;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class StatusParam extends AbstractParam {

	private ParameterState status;
	private String statusInformation;

	@Builder
	public StatusParam(String name, Long collectTime, Threshold threshold, ParameterState status,
			String statusInformation) {

		super(name, collectTime, threshold, status);
		this.status = status;
		this.statusInformation = statusInformation;
	}

}
