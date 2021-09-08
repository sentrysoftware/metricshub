package com.sentrysoftware.matrix.common.meta.monitor;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import com.sentrysoftware.matrix.common.meta.parameter.MetaParameter;
import com.sentrysoftware.matrix.common.meta.parameter.ParameterType;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.engine.strategy.IMonitorVisitor;
import com.sentrysoftware.matrix.model.alert.AlertCondition;
import com.sentrysoftware.matrix.model.alert.AlertDetails;
import com.sentrysoftware.matrix.model.alert.AlertRule;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitor.Monitor.AssertedParameter;
import com.sentrysoftware.matrix.model.parameter.NumberParam;
import com.sentrysoftware.matrix.model.parameter.ParameterState;
import com.sentrysoftware.matrix.model.parameter.PresentParam;
import com.sentrysoftware.matrix.model.parameter.StatusParam;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ADDITIONAL_INFORMATION1;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ADDITIONAL_INFORMATION2;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ADDITIONAL_INFORMATION3;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.BANDWIDTH;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.BANDWIDTH_UTILIZATION_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.BYTES_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.BYTES_RATE_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DEVICE_ID;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DUPLEX_MODE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DUPLEX_MODE_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ERROR_COUNT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ERROR_PERCENT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.FULL_DUPLEX_MODE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.HALF_DUPLEX_MODE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.LINK_SPEED_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.LINK_STATUS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.LINK_STATUS_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.LOGICAL_ADDRESS;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.MODEL;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PACKETS_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PACKETS_RATE_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PERCENT_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PHYSICAL_ADDRESS;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.PRESENT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.RECEIVED_BYTES_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.RECEIVED_BYTES_RATE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.RECEIVED_PACKETS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.RECEIVED_PACKETS_RATE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.REMOTE_PHYSICAL_ADDRESS;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SERIAL_NUMBER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SPEED_MBITS_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.STATUS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TRANSMITTED_BYTES_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TRANSMITTED_BYTES_RATE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TRANSMITTED_PACKETS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TRANSMITTED_PACKETS_RATE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.VENDOR;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ZERO_BUFFER_CREDIT_COUNT_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ZERO_BUFFER_CREDIT_COUNT_PARAMETER_UNIT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ZERO_BUFFER_CREDIT_PERCENT_PARAMETER;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.BANDWIDTH_UTILIZATION_WARN_CONDITION;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.ERROR_PERCENT_ALARM_CONDITION;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.ERROR_PERCENT_WARN_CONDITION;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.PRESENT_ALARM_CONDITION;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.STATUS_ALARM_CONDITION;
import static com.sentrysoftware.matrix.model.alert.AlertConditionsBuilder.STATUS_WARN_CONDITION;

public class NetworkCard implements IMetaMonitor {

	private static final String CONSEQUENCE_FOR_BAD_NETWORK_CARD = "The network traffic (if any) that was processed by this adapter is no longer being handled, or is overloading another network adapter.";

	public static final MetaParameter BANDWIDTH_UTILIZATION = MetaParameter.builder()
			.basicCollect(true)
			.name(BANDWIDTH_UTILIZATION_PARAMETER)
			.unit(PERCENT_PARAMETER_UNIT)
			.type(ParameterType.NUMBER)
			.build();

	public static final MetaParameter DUPLEX_MODE = MetaParameter.builder()
			.basicCollect(false)
			.name(DUPLEX_MODE_PARAMETER)
			.unit(DUPLEX_MODE_PARAMETER_UNIT)
			.type(ParameterType.NUMBER)
			.build();

	public static final MetaParameter ERROR_PERCENT = MetaParameter.builder()
			.basicCollect(true)
			.name(ERROR_PERCENT_PARAMETER)
			.unit(PERCENT_PARAMETER_UNIT)
			.type(ParameterType.NUMBER)
			.build();

	public static final MetaParameter LINK_SPEED = MetaParameter.builder()
			.basicCollect(true)
			.name(LINK_SPEED_PARAMETER)
			.unit(SPEED_MBITS_PARAMETER_UNIT)
			.type(ParameterType.NUMBER)
			.build();

	public static final MetaParameter LINK_STATUS = MetaParameter.builder()
			.basicCollect(true)
			.name(LINK_STATUS_PARAMETER)
			.unit(LINK_STATUS_PARAMETER_UNIT)
			.type(ParameterType.STATUS)
			.build();

	public static final MetaParameter RECEIVED_BYTES_RATE = MetaParameter.builder()
			.basicCollect(false)
			.name(RECEIVED_BYTES_RATE_PARAMETER)
			.unit(BYTES_RATE_PARAMETER_UNIT)
			.type(ParameterType.NUMBER)
			.build();

	public static final MetaParameter RECEIVED_PACKETS_RATE = MetaParameter.builder()
			.basicCollect(false)
			.name(RECEIVED_PACKETS_RATE_PARAMETER)
			.unit(PACKETS_RATE_PARAMETER_UNIT)
			.type(ParameterType.NUMBER)
			.build();

	public static final MetaParameter TRANSMITTED_BYTES_RATE = MetaParameter.builder()
			.basicCollect(false)
			.name(TRANSMITTED_BYTES_RATE_PARAMETER)
			.unit(BYTES_RATE_PARAMETER_UNIT)
			.type(ParameterType.NUMBER)
			.build();

	public static final MetaParameter TRANSMITTED_PACKETS_RATE = MetaParameter.builder()
			.basicCollect(false)
			.name(TRANSMITTED_PACKETS_RATE_PARAMETER)
			.unit(PACKETS_RATE_PARAMETER_UNIT)
			.type(ParameterType.NUMBER)
			.build();

	public static final MetaParameter ZERO_BUFFER_CREDIT_PERCENT = MetaParameter.builder()
			.basicCollect(false)
			.name(ZERO_BUFFER_CREDIT_PERCENT_PARAMETER)
			.unit(PERCENT_PARAMETER_UNIT)
			.type(ParameterType.NUMBER)
			.build();

	public static final MetaParameter ZERO_BUFFER_CREDIT_COUNT = MetaParameter.builder()
			.basicCollect(true)
			.name(ZERO_BUFFER_CREDIT_COUNT_PARAMETER)
			.unit(ZERO_BUFFER_CREDIT_COUNT_PARAMETER_UNIT)
			.type(ParameterType.NUMBER)
			.build();

	public static final MetaParameter TRANSMITTED_BYTES = MetaParameter.builder()
			.basicCollect(true)
			.name(TRANSMITTED_BYTES_PARAMETER)
			.unit(BYTES_PARAMETER_UNIT)
			.type(ParameterType.NUMBER)
			.build();

	public static final MetaParameter RECEIVED_BYTES = MetaParameter.builder()
			.basicCollect(true)
			.name(RECEIVED_BYTES_PARAMETER)
			.unit(BYTES_PARAMETER_UNIT)
			.type(ParameterType.NUMBER)
			.build();

	public static final MetaParameter TRANSMITTED_PACKETS = MetaParameter.builder()
			.basicCollect(true)
			.name(TRANSMITTED_PACKETS_PARAMETER)
			.unit(PACKETS_PARAMETER_UNIT)
			.type(ParameterType.NUMBER)
			.build();

	public static final MetaParameter RECEIVED_PACKETS = MetaParameter.builder()
			.basicCollect(true)
			.name(RECEIVED_PACKETS_PARAMETER)
			.unit(PACKETS_PARAMETER_UNIT)
			.type(ParameterType.NUMBER)
			.build();

	private static final List<String> METADATA = List.of(DEVICE_ID, SERIAL_NUMBER, VENDOR, MODEL, BANDWIDTH, PHYSICAL_ADDRESS,
			LOGICAL_ADDRESS, REMOTE_PHYSICAL_ADDRESS, ADDITIONAL_INFORMATION1, ADDITIONAL_INFORMATION2, ADDITIONAL_INFORMATION3);

	public static final AlertRule PRESENT_ALERT_RULE = new AlertRule(NetworkCard::checkMissingCondition,
			PRESENT_ALARM_CONDITION,
			ParameterState.ALARM);
	public static final AlertRule STATUS_WARN_ALERT_RULE = new AlertRule(NetworkCard::checkStatusWarnCondition,
			STATUS_WARN_CONDITION,
			ParameterState.WARN);
	public static final AlertRule STATUS_ALARM_ALERT_RULE = new AlertRule(NetworkCard::checkStatusAlarmCondition,
			STATUS_ALARM_CONDITION,
			ParameterState.ALARM);
	public static final AlertRule LINK_STATUS_ALERT_RULE = new AlertRule(NetworkCard::checkLinkStatusCondition,
			STATUS_WARN_CONDITION,
			ParameterState.WARN);
	public static final AlertRule ERROR_PERCENT_WARN_ALART_RULE = new AlertRule(NetworkCard::checkErrorPercentWarnCondition,
			ERROR_PERCENT_WARN_CONDITION,
			ParameterState.WARN);
	public static final AlertRule ERROR_PERCENT_ALARM_ALERT_RULE = new AlertRule(NetworkCard::checkErrorPercentAlarmCondition,
			ERROR_PERCENT_ALARM_CONDITION,
			ParameterState.ALARM);
	public static final AlertRule BANDWIDTH_UTILIZATION_HIGH_ALERT_RULE = new AlertRule(NetworkCard::checkHighBandwidthUtilizationCondition,
			BANDWIDTH_UTILIZATION_WARN_CONDITION,
			ParameterState.WARN);

	private static final Map<String, MetaParameter> META_PARAMETERS;
	private static final Map<String, List<AlertRule>> ALERT_RULES;

	static {
		final Map<String, MetaParameter> map = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		map.put(STATUS_PARAMETER, STATUS);
		map.put(PRESENT_PARAMETER, PRESENT);
		map.put(BANDWIDTH_UTILIZATION_PARAMETER, BANDWIDTH_UTILIZATION);
		map.put(DUPLEX_MODE_PARAMETER, DUPLEX_MODE);
		map.put(ERROR_PERCENT_PARAMETER, ERROR_PERCENT);
		map.put(LINK_SPEED_PARAMETER, LINK_SPEED);
		map.put(LINK_STATUS_PARAMETER, LINK_STATUS);
		map.put(RECEIVED_BYTES_RATE_PARAMETER, RECEIVED_BYTES_RATE);
		map.put(RECEIVED_PACKETS_RATE_PARAMETER, RECEIVED_PACKETS_RATE);
		map.put(TRANSMITTED_BYTES_RATE_PARAMETER, TRANSMITTED_BYTES_RATE);
		map.put(TRANSMITTED_PACKETS_RATE_PARAMETER, TRANSMITTED_PACKETS_RATE);
		map.put(ZERO_BUFFER_CREDIT_PERCENT_PARAMETER, ZERO_BUFFER_CREDIT_PERCENT);
		map.put(ZERO_BUFFER_CREDIT_COUNT_PARAMETER, ZERO_BUFFER_CREDIT_COUNT);
		map.put(TRANSMITTED_BYTES_PARAMETER, TRANSMITTED_BYTES);
		map.put(RECEIVED_BYTES_PARAMETER, RECEIVED_BYTES);
		map.put(TRANSMITTED_PACKETS_PARAMETER, TRANSMITTED_PACKETS);
		map.put(RECEIVED_PACKETS_PARAMETER, RECEIVED_PACKETS);
		map.put(ERROR_COUNT_PARAMETER, ERROR_COUNT);

		META_PARAMETERS = Collections.unmodifiableMap(map);

		final Map<String, List<AlertRule>> alertRulesMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

		alertRulesMap.put(PRESENT_PARAMETER, Collections.singletonList(PRESENT_ALERT_RULE));
		alertRulesMap.put(STATUS_PARAMETER, List.of(STATUS_WARN_ALERT_RULE, STATUS_ALARM_ALERT_RULE));
		alertRulesMap.put(LINK_STATUS_PARAMETER, Collections.singletonList(LINK_STATUS_ALERT_RULE));
		alertRulesMap.put(ERROR_PERCENT_PARAMETER, List.of(ERROR_PERCENT_WARN_ALART_RULE, ERROR_PERCENT_ALARM_ALERT_RULE));
		alertRulesMap.put(BANDWIDTH_UTILIZATION_PARAMETER, Collections.singletonList(BANDWIDTH_UTILIZATION_HIGH_ALERT_RULE));

		ALERT_RULES = Collections.unmodifiableMap(alertRulesMap); 
	}

	/**
	 * Check missing NetworkCard condition.
	 * 
	 * @param monitor    The monitor we wish to check
	 * @param conditions The conditions used to determine the abnormality
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkMissingCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<PresentParam> assertedPresent = monitor.assertPresentParameter(conditions);
		if (assertedPresent.isAbnormal()) {

			return AlertDetails.builder()
					.problem("This network adapter is not detected anymore.")
					.consequence(CONSEQUENCE_FOR_BAD_NETWORK_CARD)
					.recommendedAction("Check whether this network adapter has been intentionally uninstalled by an administrator, or whether the adapter is out of order.")
					.build();
		}

		return null;
	}

	/**
	 * Check condition when the monitor bandwidthUtilization is high.
	 * 
	 * @param monitor    The monitor we wish to check
	 * @param conditions The conditions used to check the bandwidth utilization
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkHighBandwidthUtilizationCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<NumberParam> assertedBandwidthUtilization = monitor.assertNumberParameter(BANDWIDTH_UTILIZATION_PARAMETER, conditions);

		if (assertedBandwidthUtilization.isAbnormal()) {

			final NumberParam duplexModeParameter = monitor.getParameter(DUPLEX_MODE_PARAMETER, NumberParam.class);
			final Double duplexModeValue = duplexModeParameter != null ? duplexModeParameter.getValue() : null;
			String duplexMode = null;
			if (duplexModeValue != null) {
				duplexMode = duplexModeValue == 1 ? FULL_DUPLEX_MODE : HALF_DUPLEX_MODE;
			}

			final NumberParam speedParameter = monitor.getParameter(LINK_SPEED_PARAMETER, NumberParam.class);
			final Double speed = speedParameter != null ? speedParameter.getValue() : null;

			return AlertDetails.builder()
					.problem(String.format("The network adapter is using (%f %s) of its bandwidth.",
							assertedBandwidthUtilization.getParameter().getValue(), PERCENT_PARAMETER_UNIT))
					.consequence("High network usage can increase response times at an unacceptable level and sometimes lead to file transfer failures.")
					.recommendedAction("Check on the operating system what is generating such a high network traffic. If network traffic level is normal (in bytes/sec), check the link speed and duplex mode of the card"
							+ (speed != null && duplexMode != null ? String.format(" currently reported as running at (%f) in (%s) duplex mode", speed, duplexMode) : ""))
					.build();
		}

		return null;
	}

	/**
	 * Check condition when the monitor bandwidthUtilization is low.
	 * 
	 * @param monitor    The monitor we wish to check
	 * @param conditions The conditions used to check the bandwidth utilization
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkLowBandwidthUtilizationCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<NumberParam> assertedBandwidthUtilization = monitor.assertNumberParameter(BANDWIDTH_UTILIZATION_PARAMETER, conditions);
		if (assertedBandwidthUtilization.isAbnormal()) {

			return AlertDetails.builder()
					.problem("Network usage on this link is very low, which is suspect.")
					.consequence("The application usually using this link may be down or switched to another network adapter.")
					.recommendedAction("Check whether the application usually generating traffic on this link is functioning properly. If so, check whether the network configuration fo the operating system didn't lead to a network card switch.")
					.build();
		}

		return null;
	}

	/**
	 * Check condition when the monitor bandwidthUtilization is low.
	 * 
	 * @param monitor    The monitor we wish to check
	 * @param conditions The condition used to check the zero buffer credit parameter value
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkZeroBufferCreditCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<NumberParam> assertedZeroBufferCreditPercent = monitor.assertNumberParameter(ZERO_BUFFER_CREDIT_PERCENT_PARAMETER, conditions);

		if (assertedZeroBufferCreditPercent.isAbnormal()) {

			return AlertDetails.builder()
					.problem("The percentage of Zero Buffer Credit errors is too high.")
					.consequence("The performance of the adapter for data transmission is not optimal.")
					.recommendedAction("Modify the buffer credit settings of this port on either this system or the remote system.")
					.build();
		}

		return null;
	}

	/**
	 * Check condition when the monitor error percentage is in WARN state.
	 * 
	 * @param monitor    The monitor we wish to check
	 * @param conditions The conditions used to check the error percent
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkErrorPercentWarnCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<NumberParam> assertedErrorPercent = monitor.assertNumberParameter(ERROR_PERCENT_PARAMETER, conditions);
		if (assertedErrorPercent.isAbnormal()) {

			return AlertDetails.builder()
					.problem(String.format("This network card is encountering or generating a high number of errors (%f %s).",
							assertedErrorPercent.getParameter().getValue(), PERCENT_PARAMETER_UNIT))
					.consequence("This strongly impacts the network performance.")
					.recommendedAction("Check the network cable, the driver settings, the speed and duplex mode of the link. If everything seems normal, you may have to replace this network adapter.")
					.build();
		}

		return null;
	}

	/**
	 * Check condition when the monitor error percentage is in ALARM state.
	 * 
	 * @param monitor    The monitor we wish to check
	 * @param conditions The conditions used to check the error percent
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkErrorPercentAlarmCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<NumberParam> assertedErrorPercent = monitor.assertNumberParameter(ERROR_PERCENT_PARAMETER, conditions);
		if (assertedErrorPercent.isAbnormal()) {

			return AlertDetails.builder()
					.problem(String.format("This network card is encountering or generating a critically high number of errors (%f %s).",
							assertedErrorPercent.getParameter().getValue(), PERCENT_PARAMETER_UNIT))
					.consequence("This strongly impacts the network performance.")
					.recommendedAction("Check the network cable, the driver settings, the speed and duplex mode of the link. If everything seems normal, you may have to replace this network adapter.")
					.build();
		}

		return null;
	}

	/**
	 * Check condition when the monitor status is in WARN state.
	 * 
	 * @param monitor    The monitor we wish to check its status
	 * @param conditions The conditions used to detect the abnormality
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkStatusWarnCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<StatusParam> assertedStatus = monitor.assertStatusParameter(STATUS_PARAMETER, conditions);
		if (assertedStatus.isAbnormal()) {

			return AlertDetails.builder()
					.problem("This network adapter is degraded." + IMetaMonitor.getStatusInformationMessage(assertedStatus.getParameter()))
					.consequence("The network traffic handled by this adapter may be slowed down and this may affect other systems communicating with this computer.")
					.recommendedAction("If possible, try to fix the problem through the network driver settings. Otherwise, replace the network adapter.")
					.build();
		}

		return null;
	}

	/**
	 * Check condition when the monitor status is in ALARM state.
	 * 
	 * @param monitor    The monitor we wish to check its status
	 * @param conditions The conditions used to detect the abnormality
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkStatusAlarmCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<StatusParam> assertedStatus = monitor.assertStatusParameter(STATUS_PARAMETER, conditions);
		if (assertedStatus.isAbnormal()) {

			return AlertDetails.builder()
					.problem("This network adapter has failed." +  IMetaMonitor.getStatusInformationMessage(assertedStatus.getParameter()))
					.consequence(CONSEQUENCE_FOR_BAD_NETWORK_CARD)
					.recommendedAction("Replace this network adapter as soon as possible.")
					.build();
		}

		return null;
	}

	/**
	 * Check condition when the monitor LinkStatus is in WARN state.
	 * 
	 * @param monitor    The monitor we wish to check its link status
	 * @param conditions The conditions used to detect the abnormality
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkLinkStatusCondition(Monitor monitor, Set<AlertCondition> conditions) {
		AssertedParameter<StatusParam> assertedlinkStatus = monitor.assertStatusParameter(LINK_STATUS_PARAMETER, conditions);
		if (assertedlinkStatus.isAbnormal()) {

			return AlertDetails.builder()
					.problem("The network link is down.")
					.consequence(CONSEQUENCE_FOR_BAD_NETWORK_CARD)
					.recommendedAction("Check that the network cable (if any) is not unplugged or broken/cut, and that it is properly plugged into the network card. Ensure that the network hub/switch/router is working properly.")
					.build();
		}

		return null;
	}

	/**
	 * Check condition for the link speed parameter.
	 * 
	 * @param monitor    The monitor we wish to check its link speed
	 * @param conditions The conditions used to check the link speed
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkLinkSpeedCondition(Monitor monitor, Set<AlertCondition> conditions) {
		AssertedParameter<NumberParam> assertedLinkSpeed = monitor.assertNumberParameter(LINK_SPEED_PARAMETER, conditions);
		if (assertedLinkSpeed.isAbnormal()) {

			return AlertDetails.builder()
					.problem(String.format("The network link is operating at an improper speed (%f %s).",
							assertedLinkSpeed.getParameter().getValue(), SPEED_MBITS_PARAMETER_UNIT))
					.consequence("If the network link has negotiated too low, it may not be able to carry all of the traffic, leading to delayed response times for the applications relying on it. On the opposite, if negotiated too high, it can (rarely though) generate a unacceptable amount of errors, leading to response time delays and even data corruption.")
					.recommendedAction("A badly negotiated link speed may be caused by a problem with the cable (defective, poor quality). It can also be caused by bugs in the firmware of the network cards themselves. If configured to automatically negotiate the link speed (as it is often the case), try configuring the adapter with a fixed speed.")
					.build();
		}

		return null;
	}

	/**
	 * Check condition for the duplex mode parameter.
	 * 
	 * @param monitor    The monitor we wish to check its duplex mode (Half = 0, Full = 1)
	 * @param conditions The conditions used to check the duplex mode
	 * @return {@link AlertDetails} if the abnormality is detected otherwise null
	 */
	public static AlertDetails checkDuplexModeCondition(Monitor monitor, Set<AlertCondition> conditions) {
		final AssertedParameter<NumberParam> assertedDuplexMode = monitor.assertNumberParameter(DUPLEX_MODE_PARAMETER, conditions);
		if (assertedDuplexMode.isAbnormal()) {

			return AlertDetails.builder()
					.problem("The network link is, unexpectedly, in half-duplex mode.")
					.consequence("In half-duplex mode, a network adapter can carry less than 50% of the traffic it could at the advertised speed, which can lead to a network congestion on this link. If you get this alarm repeatedly, it probably means that the network cable is at fault and that the network adapters encounter difficulties to negotiate the link speed and duplex mode.")
					.recommendedAction("If the half-duplex mode was not desired, check the quality of the cable. You can also try forcing the full-duplex mode in the driver of the adapter. If that works without generating a high volume of errors, it could be that the firmware (or driver) of the adapter is at fault because it failed to automatically negotiate a duplex mode that it can actually handle.")
					.build();
		}

		return null;
	}

	@Override
	public void accept(IMonitorVisitor monitorVisitor) {
		monitorVisitor.visit(this);
	}

	@Override
	public Map<String, MetaParameter> getMetaParameters() {
		return META_PARAMETERS;
	}

	@Override
	public MonitorType getMonitorType() {
		return MonitorType.NETWORK_CARD;
	}

	@Override
	public List<String> getMetadata() {
		return METADATA;
	}

	@Override
	public Map<String, List<AlertRule>> getStaticAlertRules() {
		return ALERT_RULES;
	}
}