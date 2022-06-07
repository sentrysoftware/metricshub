package com.sentrysoftware.hardware.agent.dto.metric;

import java.util.function.Predicate;

import com.sentrysoftware.matrix.common.meta.parameter.state.IState;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NonNull;

@Builder
@Data
@AllArgsConstructor
public class MetricInfo {

	@NonNull
	private String name;

	@Default
	private double factor = 1D;

	@Default
	@NonNull
	private String unit = "";

	@Default
	@NonNull
	private MetricType type = MetricType.GAUGE;

	@Default
	@NonNull
	private String description = "";

	private IIdentifyingAttribute identifyingAttribute;

	private Predicate<IState> predicate;

	private String additionalId;

	public enum MetricType {
		GAUGE, COUNTER, UP_DOWN_COUNTER
	}

	/**
	 * Whether the metric must report a boolean state 0 (false) or 1 (true)
	 * 
	 * @return <code>true</code> if the predicate is defined
	 */
	public boolean isBooleanMetric() {
		return predicate != null;
	}
}