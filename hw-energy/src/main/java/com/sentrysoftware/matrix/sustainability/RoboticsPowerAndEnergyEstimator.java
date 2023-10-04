package com.sentrysoftware.matrix.sustainability;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class RoboticsPowerAndEnergyEstimator extends HardwarePowerAndEnergyEstimator {

	/**
	 * Estimates the power consumption of Robotics monitor
	 * @return Double
	 */
	@Override
	Double estimatePower() {
		// TODO
		return null;
	}

	/**
	 * Estimates the energy consumption of Robotics monitor
	 * @return Double
	 */
	@Override
	Double estimateEnergy() {
		// TODO
		return null;
	}
}
