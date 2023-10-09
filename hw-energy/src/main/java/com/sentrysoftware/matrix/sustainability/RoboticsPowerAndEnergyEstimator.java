package com.sentrysoftware.matrix.sustainability;

import com.sentrysoftware.matrix.telemetry.Monitor;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;
import com.sentrysoftware.matrix.telemetry.metric.NumberMetric;
import com.sentrysoftware.matrix.util.HwCollectHelper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@Slf4j
public class RoboticsPowerAndEnergyEstimator extends HardwarePowerAndEnergyEstimator {

	public RoboticsPowerAndEnergyEstimator(final Monitor monitor, final TelemetryManager telemetryManager) {
		super(monitor, telemetryManager);
	}

	/**
	 * Estimates the power consumption of Robotics monitor
	 * @return Double
	 */
	@Override
	public Double estimatePower() {
		Double powerConsumption = null;
		final Monitor monitor = super.getMonitor();
		final NumberMetric moveCountMetric = monitor.getMetric("hw.robotics.moves", NumberMetric.class);
		if (moveCountMetric == null) {
			log.warn(
				"Could not estimate power of Robotics monitor {} since hw.robotics.moves metric is null",
				monitor.getId()
			);
		} else {
			if (moveCountMetric != null && moveCountMetric.getValue() > 0.0) {
				powerConsumption = 154.0;
			} else {
				powerConsumption = 48.0;
			}
		}
		return powerConsumption;
	}

	/**
	 * Estimates the energy consumption of Robotics monitor
	 * @return Double
	 */
	@Override
	public Double estimateEnergy() {
		final Double estimatedPower = estimatePower();
		return HwCollectHelper.estimateEnergyUsingPower(
			getMonitor(),
			getTelemetryManager(),
			estimatedPower,
			"hw.power{hw.type=\"robotics\"}",
			getTelemetryManager().getStrategyTime()
		);
	}
}
