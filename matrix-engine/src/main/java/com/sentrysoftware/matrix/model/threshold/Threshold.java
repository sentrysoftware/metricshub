package com.sentrysoftware.matrix.model.threshold;

import com.sentrysoftware.matrix.model.parameter.ParameterState;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Threshold { 

	@Default
	private boolean alarm1Active = true;
	@Default
	private int alarm1Min = Integer.MIN_VALUE;
	@Default
	private int alarm1Max = Integer.MAX_VALUE;
	@Default
	private int alarm1Trigger = 1;
	@Default
	private int alarm1AfterNTimes = 1;
	@Default
	private ParameterState alarm1State = ParameterState.WARN;

	@Default
	private boolean alarm2Active = true;
	@Default
	private int alarm2Min = Integer.MIN_VALUE;
	@Default
	private int alarm2Max = Integer.MAX_VALUE;
	@Default
	private int alarm2Trigger = 1;
	@Default
	private int alarm2AfterNTimes = 1;
	@Default
	private ParameterState alarm2State = ParameterState.ALARM;
}
