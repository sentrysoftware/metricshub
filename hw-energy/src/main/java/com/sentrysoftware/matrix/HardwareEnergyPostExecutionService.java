package com.sentrysoftware.matrix;

import com.sentrysoftware.matrix.common.helpers.KnownMonitorType;
import com.sentrysoftware.matrix.delegate.IPostExecutionService;
import com.sentrysoftware.matrix.sustainability.CpuPowerEstimator;
import com.sentrysoftware.matrix.sustainability.DiskControllerPowerAndEnergyEstimator;
import com.sentrysoftware.matrix.sustainability.FanPowerAndEnergyEstimator;
import com.sentrysoftware.matrix.sustainability.HardwarePowerAndEnergyEstimator;
import com.sentrysoftware.matrix.sustainability.MemoryPowerAndEnergyEstimator;
import com.sentrysoftware.matrix.sustainability.NetworkPowerAndEnergyEstimator;
import com.sentrysoftware.matrix.sustainability.PhysicalDiskPowerAndEnergyEstimator;
import com.sentrysoftware.matrix.sustainability.RoboticsPowerAndEnergyEstimator;
import com.sentrysoftware.matrix.sustainability.TapeDrivePowerAndEnergyEstimator;
import com.sentrysoftware.matrix.sustainability.VmPowerEstimator;
import com.sentrysoftware.matrix.telemetry.Monitor;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;
import com.sentrysoftware.matrix.util.PowerAndEnergyCollectHelper;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Slf4j
public class HardwareEnergyPostExecutionService implements IPostExecutionService {

	private TelemetryManager telemetryManager;

	/**
	 * Estimates and collects power and energy consumption for a given monitor type e.g: FAN, ROBOTICS, NETWORK, etc ..
	 * @param monitorType a given monitor type {@link KnownMonitorType}
	 * @param monitorPowerMetricName the name of the power metric of the given monitor type
	 * @param monitorEnergyMetricName the name of the energy metric of the given monitor type
	 */
	private void estimateAndCollectPowerAndEnergyForMonitorType(
		final KnownMonitorType monitorType,
		final String monitorPowerMetricName,
		final String monitorEnergyMetricName
	) {
		// Find monitors having the selected monitor type
		final String monitorTypeKey = monitorType.getKey();
		final Map<String, Monitor> sameTypeMonitors = telemetryManager.findMonitorByType(monitorTypeKey);

		// If no monitors are found, log a message
		if (sameTypeMonitors == null) {
			log.info("Host {} does not contain {} monitors", telemetryManager.getHostname(), monitorTypeKey);
		} else {
			// CHECKSTYLE:OFF
			// Retrieve the corresponding child class of HardwarePowerAndEnergyEstimator
			final HardwarePowerAndEnergyEstimator hardwarePowerAndEnergyEstimator;
			switch (monitorType) {
				case FAN -> hardwarePowerAndEnergyEstimator = new FanPowerAndEnergyEstimator();
				case ROBOTICS -> hardwarePowerAndEnergyEstimator = new RoboticsPowerAndEnergyEstimator();
				case VM -> hardwarePowerAndEnergyEstimator = new VmPowerEstimator();
				case CPU -> hardwarePowerAndEnergyEstimator = new CpuPowerEstimator();
				case DISK_CONTROLLER -> hardwarePowerAndEnergyEstimator = new DiskControllerPowerAndEnergyEstimator();
				case PHYSICAL_DISK -> hardwarePowerAndEnergyEstimator = new PhysicalDiskPowerAndEnergyEstimator();
				case MEMORY -> hardwarePowerAndEnergyEstimator = new MemoryPowerAndEnergyEstimator();
				case NETWORK -> hardwarePowerAndEnergyEstimator = new NetworkPowerAndEnergyEstimator();
				case TAPE_DRIVE -> hardwarePowerAndEnergyEstimator = new TapeDrivePowerAndEnergyEstimator();
				default -> throw new IllegalStateException("Unexpected value: " + monitorType);
			}

			// For each monitor, estimate and collect power and energy consumption metrics
			sameTypeMonitors
				.values()
				.forEach(monitor -> {
					hardwarePowerAndEnergyEstimator.setMonitor(monitor);
					hardwarePowerAndEnergyEstimator.setTelemetryManager(telemetryManager);
					PowerAndEnergyCollectHelper.collectPowerAndEnergy(
						monitor,
						monitorPowerMetricName,
						monitorEnergyMetricName,
						telemetryManager,
						hardwarePowerAndEnergyEstimator
					);
				});
			// CHECKSTYLE:ON
		}
	}

	/**
	 * Runs the estimation of several metrics like power consumption, energy consumption, thermal consumption information, etc ...
	 */
	@Override
	public void run() {
		estimateAndCollectPowerAndEnergyForMonitorType(
			KnownMonitorType.FAN,
			"hw.power{hw.type=\"fan\"}",
			"hw.energy{hw.type=\"fan\"}"
		);
		estimateAndCollectPowerAndEnergyForMonitorType(
			KnownMonitorType.ROBOTICS,
			"hw.power{hw.type=\"robotics\"}",
			"hw.energy{hw.type=\"robotics\"}"
		);
	}
}
