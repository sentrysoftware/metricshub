package com.sentrysoftware.matrix;

import com.sentrysoftware.matrix.common.helpers.KnownMonitorType;
import com.sentrysoftware.matrix.delegate.IPostExecutionService;
import com.sentrysoftware.matrix.sustainability.FanPowerAndEnergyEstimator;
import com.sentrysoftware.matrix.sustainability.RoboticsPowerAndEnergyEstimator;
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

	@Override
	public void run() {
		// Find fan monitors
		final Map<String, Monitor> fanMonitors = telemetryManager.findMonitorByType(KnownMonitorType.FAN.getKey());

		if (fanMonitors == null) {
			log.info("Host {} does not contain Fan monitors", telemetryManager.getHostname());
		} else {
			// For each fan monitor estimate and collect power and energy consumption metrics
			fanMonitors
				.values()
				.forEach(monitor ->
					PowerAndEnergyCollectHelper.collectPowerAndEnergy(
						monitor,
						"hw.power{hw.type=\"fan\"}",
						"hw.energy{hw.type=\"fan\"}",
						telemetryManager,
						new FanPowerAndEnergyEstimator(monitor, telemetryManager)
					)
				);
		}

		// Find robotics monitors
		final Map<String, Monitor> roboticsMonitors = telemetryManager.findMonitorByType(KnownMonitorType.ROBOTICS.getKey());

		if (roboticsMonitors == null) {
			log.info("Host {} does not contain Robotics monitors", telemetryManager.getHostname());
		} else {
			// For each robotics monitor estimate and collect power and energy consumption metrics
			roboticsMonitors
					.values()
					.forEach(monitor ->
							PowerAndEnergyCollectHelper.collectPowerAndEnergy(
									monitor,
									"hw.power{hw.type=\"robotics\"}",
									"hw.energy{hw.type=\"robotics\"}",
									telemetryManager,
									new RoboticsPowerAndEnergyEstimator(monitor, telemetryManager)
							)
					);
		}
	}
}
