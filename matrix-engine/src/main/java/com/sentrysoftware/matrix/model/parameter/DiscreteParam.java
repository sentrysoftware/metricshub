package com.sentrysoftware.matrix.model.parameter;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PRESENT_PARAMETER;

import com.sentrysoftware.matrix.common.meta.parameter.state.IState;
import com.sentrysoftware.matrix.common.meta.parameter.state.Present;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class DiscreteParam extends AbstractParam {

	public static final String DISCRETE_TYPE = "DiscreteParam";

	private IState state;
	private IState previousState;

	@Builder
	public DiscreteParam(String name, Long collectTime, @NonNull IState state) {

		super(name, collectTime, state.getUnit());
		this.state = state;
	}

	@Override
	public Number numberValue() {
		return state != null ? state.getNumericValue() : null;
	}

	@Override
	public String getType() {
		return DISCRETE_TYPE;
	}

	/**
	 * Get the display name of the current state
	 */
	public String getDisplayName() {
		return state != null ? state.getDisplayName() : null;
	}

	/**
	 * Get the numeric value of the current state
	 */
	public Number getNumericValue() {
		return numberValue();
	}

	@Override
	public void save() {
		super.save();
		previousState = state;
	}

	/**
	 * @return A new {@link DiscreteParam} with state = present
	 */
	public static DiscreteParam present() {
		return DiscreteParam.builder().name(PRESENT_PARAMETER).state(Present.PRESENT).build();
	}

	/**
	 * @return A new {@link DiscreteParam} with state = missing
	 */
	public static DiscreteParam missing() {
		return DiscreteParam.builder().name(PRESENT_PARAMETER).state(Present.MISSING).build();
	}
}
