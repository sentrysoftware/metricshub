package com.sentrysoftware.hardware.agent.service.opentelemetry.mapping;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.springframework.util.Assert;

import com.sentrysoftware.matrix.common.meta.parameter.state.IState;
import com.sentrysoftware.matrix.common.meta.parameter.state.IntrusionStatus;
import com.sentrysoftware.matrix.common.meta.parameter.state.LedIndicator;
import com.sentrysoftware.matrix.common.meta.parameter.state.NeedsCleaning;
import com.sentrysoftware.matrix.common.meta.parameter.state.PowerState;
import com.sentrysoftware.matrix.common.meta.parameter.state.PredictedFailure;
import com.sentrysoftware.matrix.common.meta.parameter.state.Present;
import com.sentrysoftware.matrix.common.meta.parameter.state.Status;
import com.sentrysoftware.matrix.common.meta.parameter.state.Up;
import com.sentrysoftware.hardware.agent.service.ServiceHelper;
import com.sentrysoftware.matrix.common.meta.parameter.state.DuplexMode;
import com.sentrysoftware.matrix.common.meta.parameter.state.LinkStatus;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MappingConstants {

	// Predicates
	public static final Predicate<IState> UP_PREDICATE = state -> Up.UP == state;
	public static final Predicate<IState> OK_STATUS_PREDICATE = state -> Status.OK == state;
	public static final Predicate<IState> DEGRADED_STATUS_PREDICATE = state -> Status.DEGRADED == state;
	public static final Predicate<IState> FAILED_STATUS_PREDICATE = state -> Status.FAILED == state;
	public static final Predicate<IState> PRESENT_PREDICATE = state -> Present.PRESENT == state;
	public static final Predicate<IState> INTRUSION_STATUS_PREDICATE = state -> IntrusionStatus.OPEN == state;
	public static final Predicate<IState> PREDICTED_FAILURE_PREDICATE = state -> PredictedFailure.FAILURE_PREDICTED == state;
	public static final Predicate<IState> CLEANING_NEEDED_PREDICATE = state -> NeedsCleaning.NEEDED == state || NeedsCleaning.NEEDED_IMMEDIATELY == state;
	public static final Predicate<IState> ON_POWER_STATE_PREDICATE = powerState -> PowerState.ON == powerState;
	public static final Predicate<IState> OFF_POWER_STATE_PREDICATE = powerState -> PowerState.OFF == powerState;
	public static final Predicate<IState> SUSPENDED_POWER_STATE_PREDICATE = powerState -> PowerState.SUSPENDED == powerState;
	public static final Predicate<IState> ON_LED_INDICATOR_PREDICATE = ledState -> LedIndicator.ON == ledState;
	public static final Predicate<IState> OFF_LED_INDICATOR_PREDICATE = ledState -> LedIndicator.OFF == ledState;
	public static final Predicate<IState> BLINKING_LED_INDICATOR_PREDICATE = ledState -> LedIndicator.BLINKING == ledState;
	public static final Predicate<IState> FULL_DUPLEX_MODE_PREDICATE = duplexMode -> DuplexMode.FULL == duplexMode;
	public static final Predicate<IState> PLUGGED_LINK_STATUS_PREDICATE = linkStatus -> LinkStatus.PLUGGED == linkStatus;

	// Attribute keys
	public static final String STATE_ATTRIBUTE_KEY = "state";
	public static final String PROTOCOL_ATTRIBUTE_KEY = "protocol";
	public static final String TYPE_ATTRIBUTE_KEY = "type";
	public static final String TASK_ATTRIBUTE_KEY = "task";
	public static final String DIRECTION_ATTRIBUTE_KEY = "direction";
	public static final String BATTERY_STATE_ATTRIBUTE_KEY = "battery_state";
	public static final String LIMIT_TYPE_ATTRIBUTE_KEY = "limit_type";
	public static final String INFO_ATTRIBUTE_KEY = "info";
	public static final String SENSOR_LOCATION_ATTRIBUTE_KEY = "sensor_location";

	// Attribute values
	public static final String OK_ATTRIBUTE_VALUE = "ok";
	public static final String CORRECTED_ATTRIBUTE_VALUE = "corrected";
	public static final String DECODER_ATTRIBUTE_VALUE = "decorder";
	public static final String ENCODER_ATTRIBUTE_VALUE = "encorder";
	public static final String GENERAL_ATTRIBUTE_VALUE = "general";
	public static final String RECEIVE_ATTRIBUTE_VALUE = "receive";
	public static final String TRANSMIT_ATTRIBUTE_VALUE = "transmit";
	public static final String ALL_ATTRIBUTE_VALUE = "all";
	public static final String DEGRADED_ATTRIBUTE_VALUE = "degraded";
	public static final String FAILED_ATTRIBUTE_VALUE = "failed";
	public static final String SNMP_ATTRIBUTE_VALUE = "snmp";
	public static final String WMI_ATTRIBUTE_VALUE = "wmi";
	public static final String WBEM_ATTRIBUTE_VALUE = "wbem";
	public static final String SSH_ATTRIBUTE_VALUE = "ssh";
	public static final String HTTP_ATTRIBUTE_VALUE = "http";
	public static final String IPMI_ATTRIBUTE_VALUE = "ipmi";
	public static final String PRESENT_ATTRIBUTE_VALUE = "present";
	public static final String OPEN_ATTRIBUTE_VALUE = "open";
	public static final String PREDICTED_FAILURE_ATTRIBUTE_VALUE = "predicted_failure";
	public static final String DISCHARGING_ATTRIBUTE_VALUE = "discharging";
	public static final String ON_ATTRIBUTE_VALUE = "on";
	public static final String OFF_ATTRIBUTE_VALUE = "off";
	public static final String SUSPENDED_ATTRIBUTE_VALUE = "suspended";
	public static final String AVAILABLE_ATTRIBUTE_VALUE = "available";
	public static final String EXPECTED_ATTRIBUTE_VALUE = "expected";
	public static final String BLINKING_ATTRIBUTE_VALUE = "blinking";
	public static final String ZERO_BUFFER_CREDIT_ATTRIBUTE_VALUE = "zero_buffer_credit";
	public static final String FREE_ATTRIBUTE_VALUE = "free";
	public static final String USED_ATTRIBUTE_VALUE = "used";
	public static final String MAX_ATTRIBUTE_VALUE = "max";
	public static final String CRITICAL_ATTRIBUTE_VALUE = "critical";
	public static final String LOW_DEGRADED_ATTRIBUTE_VALUE = "low.degraded";
	public static final String LOW_CRITICAL_ATTRIBUTE_VALUE = "low.critical";
	public static final String HIGH_DEGRADED_ATTRIBUTE_VALUE = "high.degraded";
	public static final String HIGH_CRITICAL_ATTRIBUTE_VALUE = "high.critical";

	// Default attribute keys
	public static final String NAME = "name";
	public static final String PARENT = "parent";
	public static final String ID = "id";

	// Default attribute set to be reported by each metric
	public static final Set<String> DEFAULT_ATTRIBUTE_NAMES = Set.of(ID, NAME, PARENT);

	// Units
	public static final String CELSIUS_UNIT = "Cel";
	public static final String JOULES_UNIT = "J";
	public static final String WATTS_UNIT = "W";
	public static final String SECONDS_UNIT = "s";
	public static final String ERRORS_UNIT = "{errors}";
	public static final String BYTES_UNIT = "By";
	public static final String MOVES_UNIT = "{moves}";
	public static final String OPERATIONS_UNIT = "{operations}";
	public static final String RPM_UNIT = "{rpm}";
	public static final String PATHS_UNIT = "{paths}";
	public static final String HERTZ_UNIT = "Hz";
	public static final String RATIO_UNIT = "1";
	public static final String USAGE_UNIT = "{uses}";
	public static final String VOLTS_UNIT = "V";
	public static final String PACKETS_UNIT = "{packets}";

	// Descriptions
	public static final String WARNING_OR_ALARM_THRESHOLD_OF_ERRORS = createCustomDescriptionWithAttributes(
		"Warning or alarm threshold of the encountered errors",
		LIMIT_TYPE_ATTRIBUTE_KEY,
		CRITICAL_ATTRIBUTE_VALUE, DEGRADED_ATTRIBUTE_VALUE
	);

	// Factors
	public static final double MHZ_TO_HZ_FACTOR = 1000000.0;
	public static final double RATIO_FACTOR = 0.01;
	public static final double MEGABYTES_TO_BYTES_FACTOR = 1000000.0;
	public static final double MILLIVOLTS_TO_VOLTS_FACTOR = 0.001;
	public static final double MEGABITS_TO_BYTES_FACTOR = 125000.0;
	public static final double GIGABYTES_TO_BYTES_FACTOR = 1073741824.0;

	/**
	 * Creates the status description.
	 *
	 * @param monitorType     The type of the monitor, as string.
	 * @param attributeName   The attribute name, as string.
	 * @param attributeValues Possible attribute values, as string array.
	 *
	 * @return {@link String} value
	 */
	public static String createStatusDescription(@NonNull String monitorType, @NonNull String attributeName, @NonNull String... attributeValues) {

		return createStatusDescription(monitorType, buildAttributeSection(attributeName, attributeValues));
	}

	/**
	 * Creates the status description.
	 *
	 * @param monitorType           The type of the monitor, as string.
	 * @param formattedAttributes   The formatted attributes value, as string.
	 *
	 * @return {@link String} value
	 */
	public static String createStatusDescription(@NonNull String monitorType, @NonNull String formattedAttributes) {
		monitorType = ServiceHelper.capitalize(monitorType);
		return String.format(
			"%s operational status: 1 (true) or 0 (false) for each of the possible states. Attribute(s): %s.",
			monitorType,
			formattedAttributes
		);
	}

	/**
	 * Make the value enclosed in the quotation marks
	 * 
	 * @param value the value format
	 * @return String value
	 */
	static String makeInQuotationMarks(String value) {
		return String.format("\"%s\"", value);
	}

	/**
	 * Build the attribute section formatted as the following example: <br>
	 * <em>state = "degraded", "failed", "ok" and "present"</em>
	 * @param attributeName
	 * @param attributeValues
	 * @return String value
	 */
	public static String buildAttributeSection(@NonNull String attributeName, @NonNull String... attributeValues) {

		Assert.isTrue(attributeValues.length > 0, "Attribute values cannot be empty.");
		final List<String> sortedAttributeValues = Stream.of(attributeValues).sorted().collect(Collectors.toList());
		final int size = sortedAttributeValues.size();

		return String.format(
			"%s = %s" ,
			attributeName,
			size == 1 ? 
				makeInQuotationMarks(sortedAttributeValues.get(0)) : 
				Stream.of(
					IntStream
						.range(0, size - 1)
						.mapToObj(i -> makeInQuotationMarks(sortedAttributeValues.get(i)))
						.collect(Collectors.joining(", ")
					),
					makeInQuotationMarks(sortedAttributeValues.get(size - 1))
				)
				.collect(Collectors.joining(" and "))
		);
	}

	/**
	 * Creates the description of the energy metrics.
	 *
	 * @param monitorType The type of the monitor, as string.
	 *
	 * @return {@link String} value
	 */
	public static String createEnergyDescription(@NonNull String monitorType) {
		return String.format("Energy consumed by the %s since the start of the Hardware Sentry Agent.", monitorType);
	}

	/**
	 * Creates the description of the power consumption metrics.
	 *
	 * @param monitorType The type of the monitor, as string.
	 *
	 * @return {@link String} value
	 */
	public static String createPowerConsumptionDescription(@NonNull String monitorType) {
		return String.format("Energy consumed by the %s.", monitorType);
	}

	/**
	 * Creates the power state description.
	 *
	 * @param monitorType     The type of the monitor, as string.
	 * @param attributeName   The attribute name, as string.
	 * @param attributeValues Possible attribute values, as string array.
	 *
	 * @return {@link String} value
	 */
	public static String createPowerStateDescription(@NonNull String monitorType, @NonNull String attributeName, @NonNull String... attributeValues) {
		monitorType = ServiceHelper.capitalize(monitorType);
		return String.format(
			"%s power state: 1 (true) or 0 (false) for each of the possible states. Attribute(s): %s.",
			monitorType,
			buildAttributeSection(attributeName, attributeValues)
		);
	}

	/**
	 * Create a custom description.
	 * 
	 * @param text            Description text, as string.
	 * @param attributeName   The attribute name, as string.
	 * @param attributeValues Possible attribute values, as string array.
	 * @return {@link String} value
	 */
	public static String createCustomDescriptionWithAttributes(@NonNull String text,  @NonNull String attributeName, @NonNull String... attributeValues) {
		return String.format(
			"%s. Attribute(s): %s.",
			text,
			buildAttributeSection(attributeName, attributeValues)
		);
	}
}
