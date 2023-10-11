package com.sentrysoftware.matrix.sustainability;

import com.sentrysoftware.matrix.strategy.utils.CollectHelper;
import com.sentrysoftware.matrix.telemetry.Monitor;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;
import com.sentrysoftware.matrix.util.HwCollectHelper;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class NetworkPowerAndEnergyEstimator extends HardwarePowerAndEnergyEstimator {

	public NetworkPowerAndEnergyEstimator(final Monitor monitor, final TelemetryManager telemetryManager) {
		super(monitor, telemetryManager);
	}

	/**
	 * Estimates the power consumption of the Network monitor
	 * @return Double
	 */
	@Override
	public Double estimatePower() {
		final String name = monitor.getAttribute("name");

		// If the network card's display name contains "wan" or "virt" it's a virtual card, its power consumption is 0.
		if (name != null && (name.contains("wan") || name.contains("virt"))) {
			return 0.0;
		}

		// If the Link Status value is '1' (unplugged), the power consumption is 1 watt.
		final Double linkStatus = CollectHelper.getNumberMetricValue(monitor, "hw.network.up", false);
		if (linkStatus != null && Double.valueOf(1.0).compareTo(linkStatus) != 0) {
			return 1.0;
		}

		final Double bandwidthUtilization = CollectHelper.getNumberMetricValue(
			monitor,
			"hw.network.bandwidth.limit",
			false
		);
		final Double linkSpeed = monitor.getAttribute("bandwidth") != null
			? Double.valueOf(monitor.getAttribute("bandwidth"))
			: null;

		/*
		 * If Bandwidth Utilization is provided:
		 * Link Speed > 10: (0.5 + 0.5 * Bandwidth Utilization / 100) * 5 * log10(Link Speed)
		 * Default: (0.5 + 0.5 * bandwidth Utilization / 100) * 5
		 */
		if (bandwidthUtilization != null) {
			if (linkSpeed != null && linkSpeed.compareTo(10.0) > 0) {
				return (0.5 + 0.5 * bandwidthUtilization / 100) * 5 * Math.log10(linkSpeed);
			} else {
				return (0.5 + 0.5 * bandwidthUtilization / 100) * 5;
			}
		}

		/*
		 * If Link Speed is provided:
		 * Link Speed > 10: 0.75 * 5 * log10(linkSpeed)
		 * Default: 2
		 */
		if (linkSpeed != null) {
			if (linkSpeed.compareTo(10.0) > 0) {
				return 0.75 * 5 * Math.log10(linkSpeed);
			} else {
				return 2.0;
			}
		}

		// In any other case, assume 10 watts.
		return 10.0;
	}

	/**
	 * Estimates the energy consumption of the Network monitor
	 * @return Double
	 */
	@Override
	public Double estimateEnergy() {
		final Double estimatedPower = estimatePower();
		return HwCollectHelper.estimateEnergyUsingPower(
			monitor,
			telemetryManager,
			estimatedPower,
			"hw.power{hw.type=\"network\"}",
			telemetryManager.getStrategyTime()
		);
	}
}
