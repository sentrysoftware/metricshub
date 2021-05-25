package com.sentrysoftware.matrix.model.parameter;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
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
public class PresentParam extends AbstractParam {

	private Integer present;
	private ParameterState previousState;

	@Builder
	public PresentParam(Long collectTime, Threshold threshold, ParameterState state) {

		super(HardwareConstants.PRESENT_PARAMETER, collectTime, threshold, state, HardwareConstants.PRESENT_PARAMETER_UNIT);

		if (state == null) {
			return;
		}

		this.present = ParameterState.OK.equals(state) ? 1 : 0;
	}

	@Override
	public void reset() {
		// Do nothing
	}

	@Override
	public String formatValueAsString() {

		return present == 1
			? "present: 1 (Present)"
			: "present: 0 (Missing)";
	}

	public void discoveryReset() {

		present = null;
		previousState = getState();
	}
}
