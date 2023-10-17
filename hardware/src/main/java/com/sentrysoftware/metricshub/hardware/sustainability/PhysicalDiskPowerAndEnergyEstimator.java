package com.sentrysoftware.metricshub.hardware.sustainability;

import static com.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.MONITOR_ATTRIBUTE_NAME;
import static com.sentrysoftware.metricshub.hardware.util.HwConstants.HW_ENERGY_PHYSICAL_DISK_METRIC;
import static com.sentrysoftware.metricshub.hardware.util.HwConstants.HW_POWER_PHYSICAL_DISK_METRIC;

import com.sentrysoftware.metricshub.engine.common.helpers.ArrayHelper;
import com.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants;
import com.sentrysoftware.metricshub.engine.telemetry.Monitor;
import com.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;
import com.sentrysoftware.metricshub.hardware.util.HwCollectHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class PhysicalDiskPowerAndEnergyEstimator extends HardwarePowerAndEnergyEstimator {

	public PhysicalDiskPowerAndEnergyEstimator(final Monitor monitor, final TelemetryManager telemetryManager) {
		super(monitor, telemetryManager);
	}

	/**
	 * Estimates the power consumption of the Physical disk
	 * @return Double
	 */
	@Override
	protected Double doPowerEstimation() {
		final double powerConsumption;

		final List<String> monitorDataList = new ArrayList<>();
		monitorDataList.add(monitor.getAttribute(MONITOR_ATTRIBUTE_NAME));
		monitorDataList.add(monitor.getAttribute("model"));
		monitorDataList.add(monitor.getAttribute("info"));

		final String hwParentId = monitor.getAttribute("hw.parent.id");
		final String hwParentType = monitor.getAttribute("hw.parent.type");

		if (hwParentType != null && hwParentId != null) {
			Optional
				.ofNullable(telemetryManager.findMonitorByType(hwParentType))
				.ifPresent(sameTypeMonitors ->
					sameTypeMonitors
						.entrySet()
						.stream()
						.filter(entry -> hwParentId.equals(entry.getValue().getAttribute(MetricsHubConstants.MONITOR_ATTRIBUTE_ID)))
						.map(Map.Entry::getValue)
						.findFirst()
						.ifPresent(parent -> monitorDataList.add(parent.getAttribute(MONITOR_ATTRIBUTE_NAME)))
				);
		}

		final String[] monitorData = monitorDataList.toArray(new String[0]);

		// SSD
		if (ArrayHelper.anyMatchLowerCase(str -> str.contains("ssd") || str.contains("solid"), monitorData)) {
			powerConsumption = estimateSsdPowerConsumption(monitorData);
		} else if (ArrayHelper.anyMatchLowerCase(str -> str.contains("sas"), monitorData)) {
			// HDD (non-SSD), depending on the interface
			// SAS
			powerConsumption = estimateSasPowerConsumption(monitorData);
		} else if (ArrayHelper.anyMatchLowerCase(str -> str.contains("scsi") || str.contains("ide"), monitorData)) {
			// SCSI and IDE
			powerConsumption = estimateScsiAndIde(monitorData);
		} else {
			// SATA (and unknown, we'll assume it's the most common case)
			powerConsumption = estimateSataOrDefault(monitorData);
		}
		return powerConsumption;
	}

	/**
	 * Estimates SATA physical disk power consumption. Default is 11W.
	 * @param data the physical disk information
	 * @return double value
	 */
	double estimateSataOrDefault(final String[] data) {
		// Factor in the rotational speed
		if (ArrayHelper.anyMatchLowerCase(str -> str.contains("10k"), data)) {
			return 27.0;
		} else if (ArrayHelper.anyMatchLowerCase(str -> str.contains("15k"), data)) {
			return 32.0;
		} else if (ArrayHelper.anyMatchLowerCase(str -> str.contains("5400") || str.contains("5.4"), data)) {
			return 7.0;
		}

		// Default for 7200-RPM disks
		return 11.0;
	}

	/**
	 * Estimates SCSI and IDE physical disk power consumption
	 * @param data the physical disk information
	 * @return double value
	 */
	double estimateScsiAndIde(final String[] data) {
		// SCSI and IDE
		// Factor in the rotational speed
		if (ArrayHelper.anyMatchLowerCase(str -> str.contains("10k"), data)) {
			// Only SCSI supports 10k
			return 32.0;
		} else if (ArrayHelper.anyMatchLowerCase(str -> str.contains("15k"), data)) {
			// Only SCSI supports 15k
			return 35.0;
		} else if (ArrayHelper.anyMatchLowerCase(str -> str.contains("5400") || str.contains("5.4"), data)) {
			// Likely to be cheap IDE
			return 19;
		}

		// Default for 7200-rpm disks, SCSI or IDE, who knows?
		// SCSI is 31 watts, IDE is 21...
		return 30.0;
	}

	/**
	 * Estimates SAS physical disk power consumption
	 * @param data the physical disk information
	 * @return double value
	 */
	double estimateSasPowerConsumption(final String[] data) {
		// Factor in the rotational speed
		if (ArrayHelper.anyMatchLowerCase(str -> str.contains("15k"), data)) {
			return 17.0;
		}
		// Default for 10k-rpm disks (rarely lower than that anyway)
		return 12.0;
	}

	/**
	 * Estimates SSD physical disk power consumption
	 * @param data the physical disk information
	 * @return double value
	 */
	double estimateSsdPowerConsumption(final String[] data) {
		if (ArrayHelper.anyMatchLowerCase(str -> str.contains("pcie"), data)) {
			return 18.0;
		} else if (ArrayHelper.anyMatchLowerCase(str -> str.contains("nvm"), data)) {
			return 6.0;
		}
		return 3.0;
	}

	/**
	 * Estimates the energy consumption of the Physical disk
	 * @return Double
	 */
	@Override
	public Double estimateEnergy() {
		return HwCollectHelper.estimateEnergyUsingPower(
			monitor,
			telemetryManager,
			estimatedPower,
			HW_POWER_PHYSICAL_DISK_METRIC,
			HW_ENERGY_PHYSICAL_DISK_METRIC,
			telemetryManager.getStrategyTime()
		);
	}
}
