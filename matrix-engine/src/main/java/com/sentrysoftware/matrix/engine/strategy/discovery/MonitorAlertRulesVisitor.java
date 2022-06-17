package com.sentrysoftware.matrix.engine.strategy.discovery;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.*;
import static com.sentrysoftware.matrix.engine.strategy.discovery.MonitorDiscoveryVisitor.METADATA_CANNOT_BE_NULL;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.springframework.util.Assert;

import com.sentrysoftware.matrix.common.helpers.NumberHelper;
import com.sentrysoftware.matrix.common.meta.monitor.Battery;
import com.sentrysoftware.matrix.common.meta.monitor.Blade;
import com.sentrysoftware.matrix.common.meta.monitor.Cpu;
import com.sentrysoftware.matrix.common.meta.monitor.CpuCore;
import com.sentrysoftware.matrix.common.meta.monitor.DiskController;
import com.sentrysoftware.matrix.common.meta.monitor.Enclosure;
import com.sentrysoftware.matrix.common.meta.monitor.Fan;
import com.sentrysoftware.matrix.common.meta.monitor.Gpu;
import com.sentrysoftware.matrix.common.meta.monitor.Host;
import com.sentrysoftware.matrix.common.meta.monitor.IMetaMonitor;
import com.sentrysoftware.matrix.common.meta.monitor.Led;
import com.sentrysoftware.matrix.common.meta.monitor.LogicalDisk;
import com.sentrysoftware.matrix.common.meta.monitor.Lun;
import com.sentrysoftware.matrix.common.meta.monitor.Memory;
import com.sentrysoftware.matrix.common.meta.monitor.MetaConnector;
import com.sentrysoftware.matrix.common.meta.monitor.NetworkCard;
import com.sentrysoftware.matrix.common.meta.monitor.OtherDevice;
import com.sentrysoftware.matrix.common.meta.monitor.PhysicalDisk;
import com.sentrysoftware.matrix.common.meta.monitor.PowerSupply;
import com.sentrysoftware.matrix.common.meta.monitor.Robotics;
import com.sentrysoftware.matrix.common.meta.monitor.TapeDrive;
import com.sentrysoftware.matrix.common.meta.monitor.Temperature;
import com.sentrysoftware.matrix.common.meta.monitor.Vm;
import com.sentrysoftware.matrix.common.meta.monitor.Voltage;
import com.sentrysoftware.matrix.engine.strategy.IMonitorVisitor;
import com.sentrysoftware.matrix.engine.strategy.collect.CollectHelper;
import com.sentrysoftware.matrix.model.alert.AlertCondition;
import com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder;
import com.sentrysoftware.matrix.model.alert.AlertDetails;
import com.sentrysoftware.matrix.model.alert.AlertRule;
import com.sentrysoftware.matrix.model.alert.AlertRule.AlertRuleType;
import com.sentrysoftware.matrix.model.alert.Severity;
import com.sentrysoftware.matrix.model.monitor.Monitor;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

public class MonitorAlertRulesVisitor implements IMonitorVisitor {

	private Monitor monitor;

	public MonitorAlertRulesVisitor(@NonNull Monitor monitor) {
		this.monitor = monitor;
		Assert.isTrue(monitor.getMetadata() != null, METADATA_CANNOT_BE_NULL);
	}

	@Override
	public void visit(MetaConnector metaConnector) {

		// Process the static alert rules
		processStaticAlertRules(monitor, metaConnector);
	}

	@Override
	public void visit(Host host) {

		// Process the static alert rules
		processStaticAlertRules(monitor, host);
	}

	@Override
	public void visit(Battery battery) {

		// Process the static alert rules
		processStaticAlertRules(monitor, battery);
	}

	@Override
	public void visit(Blade blade) {

		// Process the static alert rules
		processStaticAlertRules(monitor, blade);
	}

	@Override
	public void visit(Cpu cpu) {

		// Process the CPU Instance Alert Rules
		final Set<String> parametersToSkip = processCpuInstanceAlertRules(monitor);

		// Process the static alert rules
		processStaticAlertRules(monitor, cpu, parametersToSkip);
	}

	@Override
	public void visit(CpuCore cpuCore) {

		// Process the static alert rules
		processStaticAlertRules(monitor, cpuCore);
	}

	@Override
	public void visit(DiskController diskController) {

		// Process the static alert rules
		processStaticAlertRules(monitor, diskController);
	}

	@Override
	public void visit(Enclosure enclosure) {

		// Last step, for the enclosure we set the static alert rules.
		// We don't have instance (dynamic) threshold from the connector
		processStaticAlertRules(monitor, enclosure);
	}

	@Override
	public void visit(Fan fan) {

		// Process the Fan Instance Alert Rules
		final Set<String> parametersToSkip = processFanInstanceAlertRules(monitor);

		// Static alert rules processing
		processStaticAlertRules(monitor, fan, parametersToSkip);
	}

	@Override
	public void visit(Led led) {

		// Static alert rules processing
		processStaticAlertRules(monitor, led);
	}

	@Override
	public void visit(LogicalDisk logicalDisk) {

		// Process instance alert rules
		final Set<String> parametersToSkip = processErrorCountAlertRules(monitor, 
				LogicalDisk::checkErrorCountCondition,
				LogicalDisk::checkHighErrorCountCondition);

		// Process static alert rules
		processStaticAlertRules(monitor, logicalDisk, parametersToSkip);
	}

	@Override
	public void visit(Lun lun) {

		// Process instance alert rules
		final Set<String> parametersToSkip = processLunInstanceAlertRules(monitor);

		// Process static alert rules
		processStaticAlertRules(monitor, lun, parametersToSkip);
	}

	@Override
	public void visit(Memory memory) {

		// Process instance alert rules
		final Set<String> parametersToSkip = processErrorCountAlertRules(monitor,
				Memory::checkErrorCountCondition, 
				Memory::checkHighErrorCountCondition);

		// Process static alert rules
		processStaticAlertRules(monitor, memory, parametersToSkip);
	}

	@Override
	public void visit(NetworkCard networkCard) {

		// Process instance alert rules
		final Set<String> parametersToSkip = processNetworkCardInstanceAlertRules(monitor);

		// Process static alert rules
		processStaticAlertRules(monitor, networkCard, parametersToSkip);
	}

	@Override
	public void visit(OtherDevice otherDevice) {

		// Process instance alert rules
		final Set<String> parametersToSkip = processOtherDecviceInstanceAlertRules(monitor);

		// Process static alert rules
		processStaticAlertRules(monitor, otherDevice, parametersToSkip);
	}

	@Override
	public void visit(PhysicalDisk physicalDisk) {

		// Process instance alert rules
		final Set<String> parametersToSkip = processErrorCountAlertRules(monitor,
				(mo, conditions) -> PhysicalDisk.checkErrorCountCondition(mo, ERROR_COUNT_PARAMETER, conditions),
				(mo, conditions) -> PhysicalDisk.checkHighErrorCountCondition(mo, ERROR_COUNT_PARAMETER, conditions));

		// Process static alert rules
		processStaticAlertRules(monitor, physicalDisk, parametersToSkip);
	}

	@Override
	public void visit(PowerSupply powerSupply) {

		// Process static alert rules
		processStaticAlertRules(monitor, powerSupply);
	}

	@Override
	public void visit(TapeDrive tapeDrive) {

		// Process instance alert rules
		final Set<String> parametersToSkip = processErrorCountAlertRules(monitor,
				TapeDrive::checkErrorCountCondition,
				TapeDrive::checkHighErrorCountCondition);

		// Process static alert rules
		processStaticAlertRules(monitor, tapeDrive, parametersToSkip);
	}

	@Override
	public void visit(Temperature temperature) {

		// Process instance alert rules
		final Set<String> parametersToSkip = processTemperatureAlertRules(monitor);

		// Process static alert rules
		processStaticAlertRules(monitor, temperature, parametersToSkip);
	}

	@Override
	public void visit(Voltage voltage) {

		// Process instance alert rules
		final Set<String> parametersToSkip = updateVoltageInstanceAlertRules(monitor);

		// Process static alert rules
		processStaticAlertRules(monitor, voltage, parametersToSkip);
	}

	@Override
	public void visit(Robotics robotics) {

		// Process instance alert rules
		final Set<String> parametersToSkip = processErrorCountAlertRules(monitor,
				Robotics::checkErrorCountCondition,
				Robotics::checkHighErrorCountCondition);

		// Process static alert rules
		processStaticAlertRules(monitor, robotics, parametersToSkip);
	}

	@Override
	public void visit(Vm vm) {

		// Process the static alert rules
		processStaticAlertRules(monitor, vm);
	}

	@Override
	public void visit(Gpu gpu) {

		// Process the GPU Instance Alert Rules
		final Set<String> parametersToSkip = processGpuInstanceAlertRules(monitor);

		// Process the static alert rules
		processStaticAlertRules(monitor, gpu, parametersToSkip);
	}

	/**
	 * Process the alert rules for the given monitor, if some alert rules are already defined for a parameter, means they are top priority so we
	 * will skip them
	 * 
	 * @param monitor          The monitor on which we set the parameter alert rules
	 * @param metaMonitor      The meta monitor instance, e.g. {@link Fan} instance, from which we want to extract the static alert rules
	 * @param parametersToSkip The parameters to skip (priority parameters)
	 */
	void processStaticAlertRules(final Monitor monitor, final IMetaMonitor metaMonitor, final Set<String> parametersToSkip) {

		metaMonitor.getStaticAlertRules().entrySet()
			.stream()
			.filter(entry -> !parametersToSkip.contains(entry.getKey()))
			.forEach(entry -> monitor.addAlertRules(entry.getKey(), entry.getValue()
					.stream()
					.map(AlertRule::copy)
					.collect(Collectors.toCollection(ArrayList::new))));

	}

	/**
	 * Process the alert rules for the given monitor
	 * 
	 * @param monitor     The monitor on which we set the parameter alert rules
	 * @param metaMonitor The meta monitor instance, e.g. {@link Fan} instance, from which we want to extract the static alert rules
	 */
	void processStaticAlertRules(final Monitor monitor, final IMetaMonitor metaMonitor) {
		processStaticAlertRules(monitor, metaMonitor, Collections.emptySet());
	}


	/**
	 * Build the Fan instance alert rules
	 * 
	 * @param monitor              The monitor we wish to process the alert rules 
	 * @param parameterName        The name of the parameter we wish to build the alert rules 
	 * @param warningThresholdInfo The warning threshold info containing the value and the threshold metadata key
	 * @param alarmThresholdInfo   The alarm threshold info containing the value and the threshold metadata key
	 * @param isPercent            Whether we should handle percentage values
	 * @return Singleton set of the updated parameter or empty
	 */
	Set<String> updateFanInstanceSpeedAlertRules(final Monitor monitor, final String parameterName,
			ThresholdInfo warningThresholdInfo, ThresholdInfo alarmThresholdInfo,
			boolean isPercent) {

		final BiFunction<Monitor, Set<AlertCondition>, AlertDetails> outOfRangeSpeedChecker = 
				(mo, conditions) -> Fan.checkOutOfRangeSpeedCondition(mo, parameterName, conditions);
		final BiFunction<Monitor, Set<AlertCondition>, AlertDetails> lowSpeedChecker = 
				(mo, conditions) -> Fan.checkLowSpeedCondition(mo, parameterName, conditions);
		final BiFunction<Monitor, Set<AlertCondition>, AlertDetails> zeroSpeedConditionChecker = 
				(mo, conditions) -> Fan.checkZeroSpeedCondition(mo, parameterName, conditions);

		Double warningThreshold = warningThresholdInfo.getThreshold();
		Double alarmThreshold = alarmThresholdInfo.getThreshold();
		if (warningThreshold != null && alarmThreshold != null) {

			// Check that warning is above alarm
			if (warningThreshold < alarmThreshold) {
				var swap = warningThreshold;
				warningThreshold = alarmThreshold;
				alarmThreshold = swap;
			}

		} else if (alarmThreshold != null) {
			// Only alarm threshold is provided. Warning threshold will be 110% of alarm threshold
			warningThreshold = alarmThreshold * 1.1;
		} else if (warningThreshold != null) {
			// Only Warning thresholds is provided. Alarm threshold will be 90% of warning threshold
			alarmThreshold = warningThreshold * 0.9;
		} else {
			// Means the static rules will take over
			// But let's update the thresholds in the monitor metadata before exiting
			monitor.addMetadata(warningThresholdInfo.getMetadataKey(), isPercent ? "5" : "500");
			monitor.addMetadata(alarmThresholdInfo.getMetadataKey(), "0");

			return Collections.emptySet();
		}

		final Set<AlertCondition> alarmConditions = AlertConditionsBuilder.newInstance()
				.gte(0D)
				.lte(alarmThreshold)
				.build();

		final Set<AlertCondition> warningConditions = AlertConditionsBuilder.newInstance()
				.lte(warningThreshold)
				.build();


		// Get the good checker producing the consistent problem, consequence and recommended action
		final var alarmChecker = alarmThreshold > 0 ? lowSpeedChecker : zeroSpeedConditionChecker;

		// Create the alert rule
		final AlertRule alarmAlertRule = new AlertRule(alarmChecker, alarmConditions, Severity.ALARM, AlertRuleType.INSTANCE);
		final AlertRule warnAlertRule = new AlertRule(outOfRangeSpeedChecker, warningConditions, Severity.WARN, AlertRuleType.INSTANCE);

		// Add them to the monitor, they will be inserted only if they are updated.
		monitor.addAlertRules(parameterName, new ArrayList<>(Arrays.asList(warnAlertRule, alarmAlertRule)));

		// Update metadata thresholds
		monitor.addMetadata(warningThresholdInfo.getMetadataKey(), NumberHelper.formatNumber(warningThreshold));
		monitor.addMetadata(alarmThresholdInfo.getMetadataKey(), NumberHelper.formatNumber(alarmThreshold));

		// Return a list of one parameter as we are handling only one
		return Collections.singleton(parameterName);
	}

	/**
	 * Build the voltage instance alert rules
	 * 
	 * @param monitor            The monitor we wish to process the alert rules
	 * @return Singleton list of the updated parameter or empty
	 */
	Set<String> updateVoltageInstanceAlertRules(final Monitor monitor) {
		Double upperThreshold = NumberHelper.parseDouble(monitor.getMetadata(UPPER_THRESHOLD), null);
		Double lowerThreshold = NumberHelper.parseDouble(monitor.getMetadata(LOWER_THRESHOLD), null);

		final AlertRule alertRule1;
		final AlertRule alertRule2;
		if (lowerThreshold != null && upperThreshold != null) {

			// Check that warning is above alarm
			if (lowerThreshold > upperThreshold) {
				var swap = lowerThreshold;
				lowerThreshold = upperThreshold;
				upperThreshold = swap;
			}

			final Set<AlertCondition> alarm1Conditions = AlertConditionsBuilder.newInstance()
					.lte(lowerThreshold)
					.build();
			final Set<AlertCondition> alarm2Conditions = AlertConditionsBuilder.newInstance()
					.gte(upperThreshold)
					.build();

			// Create the alert rule
			alertRule1 = new AlertRule(Voltage::checkVoltageOutOfRangeCondition, alarm1Conditions, Severity.ALARM, AlertRuleType.INSTANCE);
			alertRule2 = new AlertRule(Voltage::checkVoltageOutOfRangeCondition, alarm2Conditions, Severity.ALARM, AlertRuleType.INSTANCE);

			// Update metadata thresholds
			monitor.addMetadata(UPPER_THRESHOLD, NumberHelper.formatNumber(upperThreshold));
			monitor.addMetadata(LOWER_THRESHOLD, NumberHelper.formatNumber(lowerThreshold));

		} else if (upperThreshold != null) {
			// The upper threshold becomes a warning
			double warningThreshold = upperThreshold;
			double alarmThreshold = upperThreshold * 1.1;
			final Set<AlertCondition> warningConditions;
			final Set<AlertCondition> alarmConditions;

			if (warningThreshold > 0) {
				warningConditions = AlertConditionsBuilder.newInstance()
						.gte(warningThreshold)
						.build();
				alarmConditions = AlertConditionsBuilder.newInstance()
						.gte(alarmThreshold)
						.build();

				// Update metadata thresholds
				monitor.addMetadata(UPPER_THRESHOLD, NumberHelper.formatNumber(alarmThreshold));
				monitor.addMetadata(LOWER_THRESHOLD, NumberHelper.formatNumber(warningThreshold));
			} else {
				warningConditions = AlertConditionsBuilder.newInstance()
						.lte(warningThreshold)
						.build();
				alarmConditions = AlertConditionsBuilder.newInstance()
						.lte(alarmThreshold)
						.build();

				// Update metadata thresholds
				monitor.addMetadata(UPPER_THRESHOLD, NumberHelper.formatNumber(warningThreshold));
				monitor.addMetadata(LOWER_THRESHOLD, NumberHelper.formatNumber(alarmThreshold));
			}

			// Create the alert rule
			alertRule1 = new AlertRule(Voltage::checkVoltageOutOfRangeCondition, warningConditions, Severity.WARN, AlertRuleType.INSTANCE);
			alertRule2 = new AlertRule(Voltage::checkVoltageOutOfRangeCondition, alarmConditions, Severity.ALARM, AlertRuleType.INSTANCE);

		} else if (lowerThreshold != null) {
			// The upper threshold becomes a warning
			double warningThreshold = lowerThreshold;
			double alarmThreshold = lowerThreshold * 0.9;

			final Set<AlertCondition> warningConditions;
			final Set<AlertCondition> alarmConditions;

			if (warningThreshold > 0) {
				warningConditions = AlertConditionsBuilder.newInstance()
						.lte(warningThreshold)
						.gte(0D)
						.build();
				alarmConditions = AlertConditionsBuilder.newInstance()
						.lte(alarmThreshold)
						.gte(0D)
						.build();

				// Update metadata thresholds
				monitor.addMetadata(UPPER_THRESHOLD, NumberHelper.formatNumber(warningThreshold));
				monitor.addMetadata(LOWER_THRESHOLD, NumberHelper.formatNumber(alarmThreshold));

			} else {
				warningConditions = AlertConditionsBuilder.newInstance()
						.gte(warningThreshold)
						.lte(0D)
						.build();
				alarmConditions = AlertConditionsBuilder.newInstance()
						.gte(alarmThreshold)
						.lte(0D)
						.build();

				// Update metadata thresholds
				monitor.addMetadata(UPPER_THRESHOLD, NumberHelper.formatNumber(alarmThreshold));
				monitor.addMetadata(LOWER_THRESHOLD, NumberHelper.formatNumber(warningThreshold));
			}

			// Create the alert rule
			alertRule1 = new AlertRule(Voltage::checkVoltageOutOfRangeCondition, warningConditions, Severity.WARN, AlertRuleType.INSTANCE);
			alertRule2 = new AlertRule(Voltage::checkVoltageOutOfRangeCondition, alarmConditions, Severity.ALARM, AlertRuleType.INSTANCE);

		} else {
			// Means the static rules will take over
			return Collections.emptySet();
		}

		// Add them to the monitor, they will be inserted only if they are updated.
		monitor.addAlertRules(VOLTAGE_PARAMETER, new ArrayList<>(Arrays.asList(alertRule1, alertRule2)));

		return Collections.singleton(VOLTAGE_PARAMETER);
	}

	/**
	 * Process the temperature instance alert rules
	 * 
	 * @param monitor The monitor we wish to process the alert rules
	 * @return list of parameters with alert rules otherwise empty list
	 */
	Set<String> processTemperatureAlertRules(Monitor monitor) {

		// warning threshold and alarm threshold on the error count
		final ThresholdInfo warningThreshold = ThresholdInfo.buildFromMetadata(monitor, WARNING_THRESHOLD);
		final ThresholdInfo alarmThreshold = ThresholdInfo.buildFromMetadata(monitor, ALARM_THRESHOLD);

		return updateWarningToAlarmEnhancedAlertRules(
			monitor,
			TEMPERATURE_PARAMETER,
			warningThreshold,
			alarmThreshold,
			Temperature::checkTemperatureAbnormallyHighCondition,
			Temperature::checkTemperatureCriticallyHighCondition,
			false
		);
	}

	/**
	 * Process the error count instance alert rules
	 * 
	 * @param monitor The monitor we wish to process the alert rules
	 * @return list of parameters with alert rules otherwise empty list
	 */
	Set<String> processErrorCountAlertRules(Monitor monitor,
			final BiFunction<Monitor, Set<AlertCondition>, AlertDetails> warnConditionsChecker,
			final BiFunction<Monitor, Set<AlertCondition>, AlertDetails> alarmConditionsChecker) {

		// warning threshold and alarm threshold on the error count
		final ThresholdInfo warningThreshold = ThresholdInfo.buildFromMetadata(monitor, ERROR_COUNT_WARNING_THRESHOLD);
		final ThresholdInfo alarmThreshold = ThresholdInfo.buildFromMetadata(monitor, ERROR_COUNT_ALARM_THRESHOLD);

		return updateWarningToAlarmAlertRules(
			monitor,
			ERROR_COUNT_PARAMETER,
			warningThreshold,
			alarmThreshold,
			warnConditionsChecker,
			alarmConditionsChecker
		);
	}

	/**
	 * Process the Cpu instance alert rules set by the connector
	 * 
	 * @param monitor The CPU monitor from which we extract the warning and alarm threshold
	 * @return list of parameters with alert rules otherwise empty list
	 */
	Set<String> processCpuInstanceAlertRules(Monitor monitor) {

		final ThresholdInfo correctedErrorWarningThreshold = ThresholdInfo.buildFromMetadata(monitor, CORRECTED_ERROR_WARNING_THRESHOLD);
		final ThresholdInfo correctedErrorAlarmThreshold = ThresholdInfo.buildFromMetadata(monitor, CORRECTED_ERROR_ALARM_THRESHOLD);

		return updateWarningToAlarmAlertRules(monitor,
				CORRECTED_ERROR_COUNT_PARAMETER,
				correctedErrorWarningThreshold,
				correctedErrorAlarmThreshold, 
				Cpu::checkCorrectedFiewErrorCountCondition, 
				Cpu::checkCorrectedLargeErrorCountCondition);
	}

	/**
	 * Process the LUN instance alert rules set by the connector
	 * 
	 * @param monitor The LUN monitor from which we extract the warning or the alarm threshold
	 * @return set of parameters with alert rules otherwise empty list
	 */
	Set<String> processLunInstanceAlertRules(Monitor monitor) {

		final Map<String, String> metadata = monitor.getMetadata();

		final Double availablePathWarning = NumberHelper.parseDouble(metadata.get(AVAILABLE_PATH_WARNING), null);

		if (availablePathWarning != null && availablePathWarning > 0) {
			// For now, it has been decided that expectedPathCount = availablePathWarning + 1
			monitor.addMetadata(EXPECTED_PATH_COUNT, NumberHelper.formatNumber(availablePathWarning + 1));

			final Set<AlertCondition> warningConditions = AlertConditionsBuilder
				.newInstance()
				.lte(availablePathWarning)
				.build();

			final AlertRule warnAlertRule = new AlertRule(Lun::checkLowerAvailablePathCountCondition, warningConditions, Severity.WARN, AlertRuleType.INSTANCE);

			// Add to the monitor, it will be inserted only if updated.
			monitor.addAlertRules(AVAILABLE_PATH_COUNT_PARAMETER, new ArrayList<>(Collections.singletonList(warnAlertRule)));

			return Collections.singleton(AVAILABLE_PATH_COUNT_PARAMETER);
		}

		return Collections.emptySet();
	}

	/**
	 * Process the network card instance alert rules set by the connector
	 * 
	 * @param monitor The network card monitor from which we extract the warning and the alarm threshold
	 * @return list of parameters with alert rules otherwise empty list
	 */
	Set<String> processNetworkCardInstanceAlertRules(Monitor monitor) {

		final ThresholdInfo errorPercentWarningThreshold = ThresholdInfo
			.buildFromMetadata(monitor, ERROR_PERCENT_WARNING_THRESHOLD);
		final ThresholdInfo errorPercentAlarmThreshold = ThresholdInfo
			.buildFromMetadata(monitor, ERROR_PERCENT_ALARM_THRESHOLD);

		if (!CollectHelper.isValidPercentage(errorPercentWarningThreshold.getThreshold())
				|| !CollectHelper.isValidPercentage(errorPercentAlarmThreshold.getThreshold())) {
			// Make sure to update the metadata
			monitor.addMetadata(ERROR_PERCENT_WARNING_THRESHOLD, "20");
			monitor.addMetadata(ERROR_PERCENT_ALARM_THRESHOLD, "30");
			// Means the static alert rule takes over
			return Collections.emptySet();
		}

		return updateWarningToAlarmAlertRules(
			monitor,
			ERROR_PERCENT_PARAMETER,
			errorPercentWarningThreshold,
			errorPercentAlarmThreshold, 
			NetworkCard::checkErrorPercentWarnCondition,
			NetworkCard::checkErrorPercentAlarmCondition
		);
	}

	/**
	 * Process the OtherDevice instance alert rules set by the connector
	 * 
	 * @param monitor The OtherDevice monitor from which we extract the warning and the alarm threshold
	 * @return set of parameters with alert rules otherwise empty list
	 */
	Set<String> processOtherDecviceInstanceAlertRules(Monitor monitor) {

		final ThresholdInfo valueWarningThreshold = ThresholdInfo.buildFromMetadata(monitor, VALUE_WARNING_THRESHOLD);
		final ThresholdInfo valueAlarmThreshold = ThresholdInfo.buildFromMetadata(monitor, VALUE_ALARM_THRESHOLD);

		final Set<String> parametersWithAlertRules = new HashSet<>();
		parametersWithAlertRules.addAll(
			updateWarningToAlarmEnhancedAlertRules(monitor,
				VALUE_PARAMETER,
				valueWarningThreshold,
				valueAlarmThreshold,
				OtherDevice::checkValueWarnCondition,
				OtherDevice::checkValueAlarmCondition,
				false
			)
		);

		final ThresholdInfo usageCountWarningThreshold = ThresholdInfo.buildFromMetadata(monitor, USAGE_COUNT_WARNING_THRESHOLD);
		final ThresholdInfo usageCountAlarmThreshold = ThresholdInfo.buildFromMetadata(monitor, USAGE_COUNT_ALARM_THRESHOLD);

		parametersWithAlertRules.addAll(
			updateWarningToAlarmEnhancedAlertRules(
				monitor,
				USAGE_COUNT_PARAMETER,
				usageCountWarningThreshold,
				usageCountAlarmThreshold,
				OtherDevice::checkUsageCountWarnCondition,
				OtherDevice::checkUsageCountAlarmCondition,
				false
			)
		);

		return parametersWithAlertRules;
	}

	/**
	 * Process the Gpu instance alert rules set by the connector
	 *
	 * @param monitor The GPU monitor from which we extract the warning and alarm threshold
	 * @return list of parameters with alert rules otherwise empty list
	 */
	Set<String> processGpuInstanceAlertRules(Monitor monitor) {

		final ThresholdInfo usedTimePercentWarningThreshold = ThresholdInfo.buildFromMetadata(monitor, USED_TIME_PERCENT_WARNING_THRESHOLD);
		final ThresholdInfo usedTimePercentAlarmThreshold = ThresholdInfo.buildFromMetadata(monitor, USED_TIME_PERCENT_ALARM_THRESHOLD);

		// Default thresholds: WARN = 80, ALARM = 90
		if (usedTimePercentWarningThreshold.getThreshold() == null && usedTimePercentAlarmThreshold.getThreshold() == null) {
			usedTimePercentWarningThreshold.setThreshold(80D);
			usedTimePercentAlarmThreshold.setThreshold(90D);
		}

		final Set<String> parametersWithAlertRules = new HashSet<>();
		parametersWithAlertRules.addAll(
			updateWarningToAlarmEnhancedAlertRules(
				monitor,
				USED_TIME_PERCENT_PARAMETER,
				usedTimePercentWarningThreshold,
				usedTimePercentAlarmThreshold,
				Gpu::checkUsedTimePercentWarnCondition,
				Gpu::checkUsedTimePercentAlarmCondition,
				true
			)
		);

		final ThresholdInfo memoryUtilizationWarningThreshold = ThresholdInfo.buildFromMetadata(monitor, MEMORY_UTILIZATION_WARNING_THRESHOLD);
		final ThresholdInfo memoryUtilizationAlarmThreshold = ThresholdInfo.buildFromMetadata(monitor, MEMORY_UTILIZATION_ALARM_THRESHOLD);

		// Default threshold of WARN = 90
		if (memoryUtilizationWarningThreshold.getThreshold() == null && memoryUtilizationAlarmThreshold.getThreshold() == null) {
			memoryUtilizationWarningThreshold.setThreshold(90D);
		}

		parametersWithAlertRules.addAll(
			updateWarningToAlarmEnhancedAlertRules(
				monitor,
				MEMORY_UTILIZATION_PARAMETER,
				memoryUtilizationWarningThreshold,
				memoryUtilizationAlarmThreshold,
				Gpu::checkMemoryUtilizationWarnCondition,
				Gpu::checkMemoryUtilizationAlarmCondition,
				true
			)
		);

		return parametersWithAlertRules;
	}

	/**
	 * Update the warning to alarm enhanced instance alert rules
	 * 
	 * @param monitor              The monitor we wish to process the alert rules 
	 * @param parameterName        The name of the parameter we wish to build the alert rules 
	 * @param warningThresholdInfo The warning threshold information containing the value and the threshold metadata key
	 * @param alarmThresholdInfo   The alarm threshold information containing the value and the threshold metadata key
	 * @param enhancePercentAlarm  Whether we should enhance the percent alarm threshold
	 * @return Singleton list of the updated parameter or empty
	 */
	Set<String> updateWarningToAlarmEnhancedAlertRules(final Monitor monitor, final String parameterName,
			final ThresholdInfo warningThresholdInfo, final ThresholdInfo alarmThresholdInfo, 
			final BiFunction<Monitor, Set<AlertCondition>, AlertDetails> warnConditionsChecker,
			final BiFunction<Monitor, Set<AlertCondition>, AlertDetails> alarmConditionsChecker,
			final boolean enhancePercentAlarm) {

		Double warningThreshold = warningThresholdInfo.getThreshold();
		Double alarmThreshold = alarmThresholdInfo.getThreshold();

		if (warningThreshold != null && alarmThreshold != null) {

			// Check that warning is above alarm
			if (warningThreshold > alarmThreshold) {
				var swap = warningThreshold;
				warningThreshold = alarmThreshold;
				alarmThreshold = swap;
			}

		} else if (alarmThreshold != null) {
			// Only alarm threshold is provided. Warning threshold will be 90% of alarm threshold
			warningThreshold = alarmThreshold * 0.9;
		} else if (warningThreshold != null) {
			// Only warning thresholds is provided.

			if (enhancePercentAlarm && warningThreshold < 100) {
				// Percent alarm threshold is computed from the complement of the warning threshold to 100% 
				alarmThreshold = 100 - ((100 - warningThreshold) * 0.5);
			} else {
				// Alarm threshold will be 110% of warning threshold
				alarmThreshold = warningThreshold * 1.1;
			}

		} else {
			// Means the static rules will take over
			return Collections.emptySet();
		}

		final Set<AlertCondition> warningConditions = AlertConditionsBuilder.newInstance()
				.gte(warningThreshold)
				.build();
		final Set<AlertCondition> alarmConditions = AlertConditionsBuilder.newInstance()
				.gte(alarmThreshold)
				.build();

		// Create the alert rule
		final AlertRule alarmAlertRule = new AlertRule(alarmConditionsChecker, alarmConditions, Severity.ALARM, AlertRuleType.INSTANCE);
		final AlertRule warnAlertRule = new AlertRule(warnConditionsChecker, warningConditions, Severity.WARN, AlertRuleType.INSTANCE);

		// Add them to the monitor, they will be inserted only if they are updated.
		monitor.addAlertRules(parameterName, new ArrayList<>(Arrays.asList(warnAlertRule, alarmAlertRule)));

		// Update metadata thresholds
		monitor.addMetadata(warningThresholdInfo.getMetadataKey(), NumberHelper.formatNumber(warningThreshold));
		monitor.addMetadata(alarmThresholdInfo.getMetadataKey(), NumberHelper.formatNumber(alarmThreshold));

		return Collections.singleton(parameterName);
	}

	/**
	 * Build alert rules <em>WARN = (value >= warningThreshold & ALARM < alarmThreshold)</em> and <em>ALARM = (value >= alarmThreshold)</em>
	 * 
	 * @param monitor                The monitor we wish to build the alert rules
	 * @param parameterName          The name of the parameter we wish to build the alert rules
	 * @param warningThresholdInfo   The warning threshold information containing the value and threshold metadata key
	 * @param alarmThresholdInfo     The alarm threshold information containing the value and threshold metadata key
	 * @param warnConditionsChecker  The warning conditions checker function
	 * @param alarmConditionsChecker The alarm conditions checker function
	 * @return Singleton set of the updated parameter
	 */
	Set<String> updateWarningToAlarmAlertRules(final Monitor monitor, final String parameterName,
			final ThresholdInfo warningThresholdInfo, final ThresholdInfo alarmThresholdInfo,
			final BiFunction<Monitor, Set<AlertCondition>, AlertDetails> warnConditionsChecker,
			final BiFunction<Monitor, Set<AlertCondition>, AlertDetails> alarmConditionsChecker) {

		Double warningThreshold = warningThresholdInfo.getThreshold();
		Double alarmThreshold = alarmThresholdInfo.getThreshold();

		if (warningThreshold != null && alarmThreshold != null) {

			// Check that warning is lower than alarm
			if (warningThreshold > alarmThreshold) {
				var swap = warningThreshold;
				warningThreshold = alarmThreshold;
				alarmThreshold = swap;
			}

			final Set<AlertCondition> warningConditions = AlertConditionsBuilder.newInstance()
					.gte(warningThreshold)
					.build();
			final Set<AlertCondition> alarmConditions = AlertConditionsBuilder.newInstance()
					.gte(alarmThreshold)
					.build();
	
			final AlertRule warnAlertRule = new AlertRule(warnConditionsChecker, warningConditions, Severity.WARN, AlertRuleType.INSTANCE);
			final AlertRule alarmAlertRule = new AlertRule(alarmConditionsChecker, alarmConditions, Severity.ALARM, AlertRuleType.INSTANCE);

			// Add them to the monitor, they will be inserted only if they are updated.
			monitor.addAlertRules(parameterName, new ArrayList<>(Arrays.asList(warnAlertRule, alarmAlertRule)));

			// Update metadata thresholds
			monitor.addMetadata(warningThresholdInfo.getMetadataKey(), NumberHelper.formatNumber(warningThreshold));
			monitor.addMetadata(alarmThresholdInfo.getMetadataKey(), NumberHelper.formatNumber(alarmThreshold));

			return Collections.singleton(parameterName);

		} else if (warningThreshold != null) {

			final Set<AlertCondition> warningConditions = AlertConditionsBuilder.newInstance()
					.gte(warningThreshold)
					.build();
			final AlertRule warnAlertRule = new AlertRule(warnConditionsChecker, warningConditions, Severity.WARN, AlertRuleType.INSTANCE);

			// Add to the monitor, it will be inserted only if updated.
			monitor.addAlertRules(parameterName, new ArrayList<>(Collections.singletonList(warnAlertRule)));

			// Update metadata thresholds
			monitor.addMetadata(warningThresholdInfo.getMetadataKey(), NumberHelper.formatNumber(warningThreshold));

			return Collections.singleton(parameterName);

		} else if (alarmThreshold != null) {

			final Set<AlertCondition> alarmConditions = AlertConditionsBuilder.newInstance()
					.gte(alarmThreshold)
					.build();
			final AlertRule alarmAlertRule = new AlertRule(alarmConditionsChecker, alarmConditions, Severity.ALARM, AlertRuleType.INSTANCE);

			// Add to the monitor, it will be inserted only if updated.
			monitor.addAlertRules(parameterName, new ArrayList<>(Collections.singletonList(alarmAlertRule)));

			// Update metadata thresholds
			monitor.addMetadata(alarmThresholdInfo.getMetadataKey(), NumberHelper.formatNumber(alarmThreshold));

			return Collections.singleton(parameterName);
		}

		return Collections.emptySet();
	}

	/**
	 * Process Fan alert rules using the monitor instance metadata
	 * 
	 * @param monitor The monitor we wish to process
	 * @return list of parameters with alert rules otherwise empty list
	 */
	Set<String> processFanInstanceAlertRules(final Monitor monitor) {

		// warning threshold and alarm threshold on speed
		final ThresholdInfo warningThreshold = ThresholdInfo.buildFromMetadata(monitor, WARNING_THRESHOLD);
		final ThresholdInfo alarmThreshold = ThresholdInfo.buildFromMetadata(monitor, ALARM_THRESHOLD);

		final Set<String> parametersWithAlertRules = new HashSet<>();
		parametersWithAlertRules.addAll(
			updateFanInstanceSpeedAlertRules(
				monitor,
				SPEED_PARAMETER,
				warningThreshold,
				alarmThreshold,
				false
			)
		);

		// percent warning threshold and percent alarm threshold on speed percentage
		final ThresholdInfo percentWarningThreshold = ThresholdInfo.buildFromMetadata(monitor, PERCENT_WARNING_THRESHOLD);
		final ThresholdInfo percentAlarmThreshold = ThresholdInfo.buildFromMetadata(monitor, PERCENT_ALARM_THRESHOLD);
		parametersWithAlertRules.addAll(
			updateFanInstanceSpeedAlertRules(
				monitor,
				SPEED_PERCENT_PARAMETER,
				percentWarningThreshold,
				percentAlarmThreshold,
				true
			)
		);

		return parametersWithAlertRules;
	}

	@AllArgsConstructor
	@Builder
	public static class ThresholdInfo {
		@Getter
		private String metadataKey;
		@Getter
		@Setter
		private Double threshold;

		static ThresholdInfo buildFromMetadata(final Monitor monitor, final String metadataKey) {
			return ThresholdInfo
				.builder()
				.metadataKey(metadataKey)
				.threshold(NumberHelper.parseDouble(monitor.getMetadata(metadataKey), null))
				.build();
		}
	}
}
