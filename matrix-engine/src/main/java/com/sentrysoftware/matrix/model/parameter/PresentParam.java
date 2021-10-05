package com.sentrysoftware.matrix.model.parameter;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PRESENT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PRESENT_PARAMETER_UNIT;

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

	public static final String PRESENT_TYPE = "PresentParam";

	private ParameterState state;
	private Integer present;
	private ParameterState previousState;

	@Builder
	public PresentParam(Long collectTime, ParameterState state) {

		super(PRESENT_PARAMETER, collectTime, PRESENT_PARAMETER_UNIT);

		this.state = state;
		this.present = ParameterState.OK.equals(state) ? 1 : 0;
	}

	@Override
	public void save() {
		previousState = state;
	}

	/**
	 * Reset present and previous state
	 */
	public void discoveryReset() {
		present = null;
		save();
	}

	/**
	 * @return A new {@link PresentParam} with state = OK - present
	 */
	public static PresentParam present() {
		return PresentParam.builder().state(ParameterState.OK).build();
	}

	/**
	 * @return A new {@link PresentParam} with state = ALARM - missing
	 */
	public static PresentParam missing() {
		return PresentParam.builder().state(ParameterState.ALARM).build();
	}

	@Override
	public Number numberValue() {
		return present;
	}

	@Override
	public String getType() {
		return PRESENT_TYPE;
	}

}
