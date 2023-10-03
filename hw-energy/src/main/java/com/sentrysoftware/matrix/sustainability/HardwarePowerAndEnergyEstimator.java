package com.sentrysoftware.matrix.sustainability;

import com.sentrysoftware.matrix.telemetry.TelemetryManager;
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
public class HardwarePowerAndEnergyEstimator {

	private TelemetryManager telemetryManager;

	public static Double estimateFanPower() {
		//TODO
		return null;
	}

	public static Double estimateNetworkPower() {
		//TODO
		return null;
	}

	public static Double estimateRoboticsPower() {
		//TODO
		return null;
	}

	public static Double estimateTapeDrivePower() {
		//TODO
		return null;
	}

	public static Double estimateDiskControllerPower() {
		//TODO
		return null;
	}

	public static Double estimateFanEnergy() {
		//TODO
		return null;
	}

	public static Double estimateNetworkEnergy() {
		//TODO
		return null;
	}

	public static Double estimateRoboticsEnergy() {
		//TODO
		return null;
	}

	public static Double estimateTapeDriveEnergy() {
		//TODO
		return null;
	}

	public static Double estimateDiskControllerEnergy() {
		//TODO
		return null;
	}
}
