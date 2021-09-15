package com.sentrysoftware.matrix.engine.strategy.discovery;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ADDITIONAL_LABEL;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.BLADE_ENCLOSURE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.BLADE_NAME;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.COLOR;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.COMPUTER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DEVICE_ID;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DEVICE_TYPE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DISK_CONTROLLER_NUMBER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DISPLAY_ID;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENCLOSURE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.FAN_TYPE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ID_COUNT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ID_MAXLENGTH;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.LOCALHOST;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.LOCAL_DEVICE_NAME;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.LOCATION;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.MAXIMUM_SPEED;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.MODEL;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.NAME;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.POWER_SUPPLY_POWER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.POWER_SUPPLY_TYPE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.RAID_LEVEL;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.REMOTE_DEVICE_NAME;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ROBOTIC_TYPE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SIZE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.STORAGE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SWITCH;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TEMPERATURE_TYPE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TYPE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.VENDOR;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.VOLTAGE_TYPE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.WHITE_SPACE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.WHITE_SPACE_REPEAT_REGEX;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.util.Assert;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.engine.target.TargetType;
import com.sentrysoftware.matrix.model.monitor.Monitor;

public class MonitorNameBuilder {

	private MonitorNameBuilder() {}

	// Enclosure details
	public static final String HP_OPEN_VMS_COMPUTER = "HP Open-VMS Computer";
	public static final String HP_TRU64_UNIX_COMPUTER = "HP Tru64 Computer";
	public static final String HP_UX_COMPUTER = "HP-UX Computer";
	public static final String IBM_AIX_COMPUTER = "IBM AIX Computer";
	public static final String LINUX_COMPUTER = "Linux Computer";
	public static final String MGMT_CARD_ENCLOSURE = "Management Card";
	public static final String WINDOWS_COMPUTER = "Windows Computer";
	public static final String NETWORK_SWITCH_ENCLOSURE = "Network Switch";
	public static final String STORAGE_ENCLOSURE = "Storage System";
	public static final String SUN_SOLARIS_COMPUTER = "Oracle Solaris Computer";
	public static final String LOCALHOST_ENCLOSURE = "Localhost Enclosure";

	// Error messages
	private static final String ID_COUNT_CANNOT_BE_NULL = "idCount cannot be null.";
	private static final String TARGET_MONITOR_CANNOT_BE_NULL = "targetMonitor cannot be null.";
	private static final String TARGET_TYPE_CANNOT_BE_NULL = "targetType cannot be null.";
	private static final String METADATA_CANNOT_BE_NULL = "metadata cannot be null.";

	// Patterns to trim unwanted contents from deviceId
	private static final Pattern BATTERY_TRIM_PATTERN = Pattern.compile("battery", Pattern.CASE_INSENSITIVE);
	private static final Pattern BLADE_TRIM_PATTERN = Pattern.compile("blade", Pattern.CASE_INSENSITIVE);
	private static final Pattern CPU_TRIM_PATTERN = Pattern.compile("cpu|proc(essor)*", Pattern.CASE_INSENSITIVE);
	private static final Pattern CPU_CORE_TRIM_PATTERN = Pattern.compile("cpu|proc(essor)*|core", Pattern.CASE_INSENSITIVE);
	private static final Pattern DISK_CONTROLLER_TRIM_PATTERN = null;
	private static final Pattern ENCLOSURE_TRIM_PATTERN = null;
	private static final Pattern FAN_TRIM_PATTERN = Pattern.compile("fan", Pattern.CASE_INSENSITIVE);
	private static final Pattern LED_TRIM_PATTERN = Pattern.compile("led", Pattern.CASE_INSENSITIVE);
	private static final Pattern LOGICAL_DISK_TRIM_PATTERN = Pattern.compile("disk|drive|logical", Pattern.CASE_INSENSITIVE);
	private static final Pattern LUN_TRIM_PATTERN = Pattern.compile("lun", Pattern.CASE_INSENSITIVE);
	private static final Pattern MEMORY_TRIM_PATTERN = Pattern.compile("memory|module", Pattern.CASE_INSENSITIVE);
	private static final Pattern NETWORK_CARD_TRIM_PATTERN = Pattern.compile("network", Pattern.CASE_INSENSITIVE);
	private static final Pattern OTHER_DEVICE_TRIM_PATTERN = null;
	private static final Pattern PHYSICAL_DISK_TRIM_PATTERN = Pattern.compile("disk|drive", Pattern.CASE_INSENSITIVE);
	private static final Pattern POWER_SUPPLY_TRIM_PATTERN = Pattern.compile("power|supply", Pattern.CASE_INSENSITIVE);
	private static final Pattern ROBOTICS_TRIM_PATTERN = Pattern.compile("robotics", Pattern.CASE_INSENSITIVE);
	private static final Pattern TAPE_DRIVE_TRIM_PATTERN = Pattern.compile("tape|drive", Pattern.CASE_INSENSITIVE);
	private static final Pattern TEMPERATURE_TRIM_PATTERN = Pattern.compile("temp(erature)*|sensor", Pattern.CASE_INSENSITIVE);
	private static final Pattern VOLTAGE_TRIM_PATTERN = Pattern.compile("voltage", Pattern.CASE_INSENSITIVE);

	// Network card vendor/model words to be trimmed
	private static final Pattern NETWORK_VENDOR_MODEL_TRIM_PATTERN = Pattern.compile("network|ndis|client|server|adapter|ethernet|interface|controller|miniport|scheduler|packet|connection|multifunction|(1([0]+[/]*))*(base[\\-tx]*)*", Pattern.CASE_INSENSITIVE);

	private static final Map<TargetType, String> COMPUTE_DISPLAY_NAMES;
	static {
		final Map<TargetType, String> map = new EnumMap<>(TargetType.class);
		for (TargetType targetType : TargetType.values()) {

			final String value;

			switch (targetType) {
				case HP_OPEN_VMS:
					value = HP_OPEN_VMS_COMPUTER;
					break;

				case HP_TRU64_UNIX:
					value = HP_TRU64_UNIX_COMPUTER;
					break;

				case HP_UX:
					value = HP_UX_COMPUTER;
					break;

				case IBM_AIX:
					value = IBM_AIX_COMPUTER;
					break;

				case LINUX:
					value = LINUX_COMPUTER;
					break;

				case MGMT_CARD_BLADE_ESXI:
					value = MGMT_CARD_ENCLOSURE;
					break;

				case MS_WINDOWS:
					value = WINDOWS_COMPUTER;
					break;

				case NETWORK_SWITCH:
					value = NETWORK_SWITCH_ENCLOSURE;
					break;

				case STORAGE:
					value = STORAGE_ENCLOSURE;
					break;

				case SUN_SOLARIS:
					value = SUN_SOLARIS_COMPUTER;
					break;

				default:
					value = null;
			}

			map.put(targetType, value);
		}

		COMPUTE_DISPLAY_NAMES = Collections.unmodifiableMap(map);
	}

	/**
	 * Check whether the string has meaningful content: not just white spaces, empty or null
	 *
	 * @param data        {@link String} to be checked
	 *
	 * @return {@link boolean} true/false whether it has meaningful content or not
	 */
	private static boolean hasMeaningfulContent(final String data) {
		return data != null && !data.trim().isEmpty();
	}

	/**
	 * Trims unwanted characters from the name, especially comma, empty parenthesis,
	 * redundant white spaces leading & trailing white spaces
	 *
	 * @param name  {@link String} name to be trimmed
	 *
	 * @return {@link String} trimmedPhrase Trimmed phrase
	 */
	private static String trimUnwantedCharacters(final String name) {
		return name
			.replace(",", "")
			.replace("()", "")
			.replaceAll(WHITE_SPACE_REPEAT_REGEX, WHITE_SPACE)
			.trim();
	}

	/**
	 * Joins the given non-empty words using the separator
	 *
	 * @param words     {@link String[]} of words to be joined
	 * @param separator {@link String} one or more characters to be used as separator
	 *
	 * @return {@link String} joinedWords Joined words
	 */
	private static String joinWords(final String[] words, final String separator) {

		// Check the words and add them to a string buffer if set
		return Arrays.stream(words)
				.filter(MonitorNameBuilder::hasMeaningfulContent)
				.collect(Collectors.joining(separator));
	}

	/**
	 * Joins the given non-empty words using dash ( - ) as the separator
	 *
	 * @param words     {@link String[]} of words to be joined
	 *
	 * @return {@link String} joinedWords Joined words
	 */
	public static String joinWords(final String[] words) {
		return joinWords(words, " - ");
	}

	/**
	 * Joins vendor and model; if model includes vendor, returns the model
	 *
	 * @param metadata {@link Map} of metadata containing vendor and model info
	 *
	 * @return {@link String} joinedVendorAndModel Joined vendor and model
	 */
	private static String joinVendorAndModel(final Map<String, String> metadata) {

		String vendor = metadata.get(VENDOR);
		String model = metadata.get(MODEL);

		if (vendor != null && model != null && model.toLowerCase().contains(vendor.toLowerCase())) {
			// Model includes the vendor, so no need to join them
			return model;
		}

		return joinWords(new String[] { vendor, model }, WHITE_SPACE);
	}

	/**
	 * Try to get the {@value HardwareConstants#LOCATION} metadata and return <code>true</code> for localhost value
	 * Note: {@value HardwareConstants#LOCATION} is computed on {@link MonitorType#TARGET} in the detection operation
	 *
	 * @param metadata         Metadata containing location and localhost constants
	 *
	 * @return {@link boolean} true/false whether it is a localhost or not
	 */
	public static boolean isLocalhost(final Map<String, String> metadata) {
		if (metadata != null) {
			return LOCALHOST.equalsIgnoreCase(metadata.get(LOCATION));
		}

		return false;
	}

	/**
	 * Handle the computer display name based on the target location. I.e. local or remote
	 *
	 * @param targetMonitor Monitor with type {@link MonitorType#TARGET}
	 * @param targetType    The type of the target monitor
	 *
	 * @return {@link String} value to append with the full monitor name
	 */
	public static String handleComputerDisplayName(final Monitor targetMonitor, final TargetType targetType) {
		Assert.notNull(targetMonitor, TARGET_MONITOR_CANNOT_BE_NULL);
		Assert.notNull(targetType, TARGET_TYPE_CANNOT_BE_NULL);

		if (isLocalhost(targetMonitor.getMetadata())) {
			// TODO Handle localhost machine type, processor architecture detection
			return LOCALHOST_ENCLOSURE;
		} else {
			return COMPUTE_DISPLAY_NAMES.get(targetType);
		}
	}

	/**
	 * Converts a number in the string to readable bytes format using binary divisor
	 *
	 * @param string        {@link String} to be formatted
	 *
	 * @return {@link String} formatted bytes with units
	 */
	private static String humanReadableByteCountBin(final String string) {

		if (string == null) {
			return string;
		}

		Double bytesD;
		try {
			bytesD = Double.parseDouble(string);
		} catch (NumberFormatException e) {
			return string;
		}

		long bytes = bytesD.longValue();
		long absB = bytes == Long.MIN_VALUE ? Long.MAX_VALUE : Math.abs(bytes);
		if (absB < 1024) {
			return bytes + " B";
		}

		long value = absB;

		CharacterIterator ci = new StringCharacterIterator("KMGTPE");
		for (int i = 40; i >= 0 && absB > 0xfffccccccccccccL >> i; i -= 10) {
			value >>= 10;
			ci.next();
		}

		value *= Long.signum(bytes);

		return String.format("%.1f %cB", value / 1024.0, ci.current());
	}

	/**
	 * Converts a number in the string to readable bytes format using decimal divisor
	 *
	 * @param string        {@link String} to be formatted
	 *
	 * @return {@link String} formatted bytes with units
	 */
	private static String humanReadableByteCountSI(final String string) {

		if (string == null) {
			return string;
		}

		Double bytes;
		try {
			bytes = Double.parseDouble(string);
		} catch (NumberFormatException e) {
			return string;
		}

		if (-1000 < bytes && bytes < 1000) {
			return bytes + " B";
		}

		CharacterIterator ci = new StringCharacterIterator("KMGTPE");
		while (bytes <= -999_950 || bytes >= 999_950) {
			bytes /= 1000;
			ci.next();
		}

		return String.format("%.1f %cB", bytes / 1000.0, ci.current());
	}

	/**
	 * Builds the name for hardware device following the standard naming:
	 * <code>[type :][name][(additional-label)]</code>
	 *
	 * @param type                  {@link String} containing the type to be prefixed with colon
	 * @param displayId             {@link String} containing the display ID, part of the name
	 * @param deviceId              {@link String} containing the device ID, part of the name
	 * @param idCount               {@link String} containing the ID count, part of the name
	 * @param trimPattern			{@link String} of regex words (case-insensitive) to be trimmed off the device ID
	 * @param additionalLabelFields {@link String} containing any additional labels to be included within parenthesis
	 *
	 * @return {@link String} name Full name following the standard naming based on the inputs
	 */
	public static String buildName(
			final String type,
			final String displayId,
			final String deviceId,
			final String idCount,
			final Pattern trimPattern,
			final String ...additionalLabelFields) {

		// Make sure the ID count is set
		Assert.notNull(idCount, ID_COUNT_CANNOT_BE_NULL);

		final StringBuilder fullName = new StringBuilder();

		// Add the type
		if (hasMeaningfulContent(type)) {

			fullName
				.append(type)
				.append(": ");
		}

		// Add the name
		String name = null;
		if (hasMeaningfulContent(displayId)) {
			name = displayId;
		} else if (hasMeaningfulContent(deviceId)) {
			name = (trimPattern == null) ? deviceId : trimPattern.matcher(deviceId).replaceAll("");
			if (name.length() > ID_MAXLENGTH) {
				name = idCount;
			}
		}

		// Use the ID count as name, if we couldn't build one from display ID or device ID
		if (!hasMeaningfulContent(name)) {
			name = idCount;
		}

		fullName.append(name);

		// Add the additional label in parenthesis
		final String additionalLabel = joinWords(additionalLabelFields);
		if (hasMeaningfulContent(additionalLabel)) {

			fullName
				.append(" (")
				.append(additionalLabel)
				.append(")");
		}

		return trimUnwantedCharacters(fullName.toString());
	}

	/**
	 * Builds the battery name based on the current implementation in Hardware Sentry
	 * KM
	 *
	 * @param monitorBuildingInfo {@link MonitorBuildingInfo} of the monitor instance
	 *
	 * @return {@link String} name Label of the battery to be displayed
	 */
	public static String buildBatteryName(final MonitorBuildingInfo monitorBuildingInfo) {

		// Check the metadata
		final Map<String, String> metadata = monitorBuildingInfo.getMonitor().getMetadata();
		Assert.notNull(metadata, METADATA_CANNOT_BE_NULL);

		// Build the name
		return buildName(

				// Type
				null,

				// Name
				metadata.get(DISPLAY_ID),
				metadata.get(DEVICE_ID),
				metadata.get(ID_COUNT),
				BATTERY_TRIM_PATTERN,

				// Additional label
				joinVendorAndModel(metadata),
				metadata.get(TYPE)
		);
	}

	/**
	 * Builds the blade name based on the current implementation in Hardware Sentry KM
	 *
	 * @param monitorBuildingInfo {@link MonitorBuildingInfo} of the monitor instance
	 *
	 * @return {@link String} name Label of the blade to be displayed
	 */
	public static String buildBladeName(final MonitorBuildingInfo monitorBuildingInfo) {

		// Check the metadata
		final Map<String, String> metadata = monitorBuildingInfo.getMonitor().getMetadata();
		Assert.notNull(metadata, METADATA_CANNOT_BE_NULL);

		// Build the name
		return buildName(

				// Type
				null,

				// Name
				metadata.get(DISPLAY_ID),
				metadata.get(DEVICE_ID),
				metadata.get(ID_COUNT),
				BLADE_TRIM_PATTERN,

				// Additional label
				metadata.get(BLADE_NAME),
				metadata.get(MODEL)
		);
	}

	/**
	 * Builds the CPU name based on the current implementation in Hardware Sentry KM
	 *
	 * @param monitorBuildingInfo {@link MonitorBuildingInfo} of the monitor instance
	 *
	 * @return {@link String} name Label of the CPU to be displayed
	 */
	public static String buildCpuName(final MonitorBuildingInfo monitorBuildingInfo) {

		// Check the metadata
		final Map<String, String> metadata = monitorBuildingInfo.getMonitor().getMetadata();
		Assert.notNull(metadata, METADATA_CANNOT_BE_NULL);

		// Format the maximum speed
		String cpuMaxSpeed = metadata.get(MAXIMUM_SPEED);
		if (cpuMaxSpeed != null) {
			try {
				double cpuMaxSpeedD = Double.parseDouble(cpuMaxSpeed);
				if (cpuMaxSpeedD < 1000D) {
					cpuMaxSpeed = String.format("%.0f MHz", cpuMaxSpeedD);
				} else {
					cpuMaxSpeed = String.format("%.2f GHz", (cpuMaxSpeedD / 1000D));
				}
			} catch (NumberFormatException nfe) {
				cpuMaxSpeed = null;
			}
		}

		// Build the name
		return buildName(

				// Type
				null,

				// Name
				metadata.get(DISPLAY_ID),
				metadata.get(DEVICE_ID),
				metadata.get(ID_COUNT),
				CPU_TRIM_PATTERN,

				// Additional label
				metadata.get(VENDOR),
				metadata.get(MODEL),
				cpuMaxSpeed
		);
	}

	/**
	 * Builds the CPU core name based on the current implementation in Hardware Sentry KM
	 *
	 * @param monitorBuildingInfo {@link MonitorBuildingInfo} of the monitor instance
	 *
	 * @return {@link String} name Label of the CPU core to be displayed
	 */
	public static String buildCpuCoreName(final MonitorBuildingInfo monitorBuildingInfo) {

		// Check the metadata
		final Map<String, String> metadata = monitorBuildingInfo.getMonitor().getMetadata();
		Assert.notNull(metadata, METADATA_CANNOT_BE_NULL);

		// Build the name
		return buildName(

				// Type
				null,

				// Name
				metadata.get(DISPLAY_ID),
				metadata.get(DEVICE_ID),
				metadata.get(ID_COUNT),
				CPU_CORE_TRIM_PATTERN
		);
	}

	/**
	 * Builds the disk controller name based on the current implementation in Hardware Sentry KM
	 *
	 * @param monitorBuildingInfo {@link MonitorBuildingInfo} of the monitor instance
	 *
	 * @return {@link String} name Label of the disk controller to be displayed
	 */
	public static String buildDiskControllerName(final MonitorBuildingInfo monitorBuildingInfo) {

		// Check the metadata
		final Map<String, String> metadata = monitorBuildingInfo.getMonitor().getMetadata();
		Assert.notNull(metadata, METADATA_CANNOT_BE_NULL);

		// Build the name
		return buildName(

				// Type
				"Disk Controller",

				// Name
				metadata.get(DISPLAY_ID),
				metadata.get(DISK_CONTROLLER_NUMBER),
				metadata.get(ID_COUNT),
				DISK_CONTROLLER_TRIM_PATTERN,

				// Additional label
				joinVendorAndModel(metadata)
		);
	}

	/**
	 * Builds the enclosure name based on the current implementation in Hardware Sentry KM
	 *
	 * @param monitorBuildingInfo {@link MonitorBuildingInfo} of the monitor instance
	 *
	 * @return {@link String} name Label of the enclosure to be displayed
	 */
	public static String buildEnclosureName(final MonitorBuildingInfo monitorBuildingInfo) {

		final TargetType targetType = monitorBuildingInfo.getTargetType();
		Assert.notNull(targetType, TARGET_TYPE_CANNOT_BE_NULL);

		final Monitor targetMonitor = monitorBuildingInfo.getTargetMonitor();
		Assert.notNull(targetMonitor, TARGET_MONITOR_CANNOT_BE_NULL);

		final Map<String, String> metadata = monitorBuildingInfo.getMonitor().getMetadata();
		Assert.notNull(metadata, METADATA_CANNOT_BE_NULL);

		// Find the enclosure type
		String enclosureType = metadata.get(TYPE);
		if (enclosureType != null) {
			switch (enclosureType.toLowerCase()) {
			case "":
			case "computer":
				enclosureType = COMPUTER;
				break;
			case "storage":
				enclosureType = STORAGE;
				break;
			case "blade":
				enclosureType = BLADE_ENCLOSURE;
				break;
			case "switch":
				enclosureType = SWITCH;
				break;
			default:
				enclosureType = ENCLOSURE;
			}
		} else {
			enclosureType = ENCLOSURE;
		}

		// If enclosureDisplayID is specified, use it and put the rest in parenthesis
		String enclosureDisplayId = metadata.get(DISPLAY_ID);
		String additionalInfo = null;

		// Find the vendor/model details
		String vendorModel = joinVendorAndModel(metadata);
		if (hasMeaningfulContent(vendorModel)) {

			// We will use vendor/model as enclosureDisplayId, if it is not set
			if (hasMeaningfulContent(enclosureDisplayId)) {
				// Add vendor/model as additionalInfo in parenthesis
				additionalInfo = vendorModel;
			} else {
				// Use it as enclosureDisplayId
				enclosureDisplayId = vendorModel;
			}

		} else if (COMPUTER.equals(enclosureType)) {

			// Find the computer display name
			String computerDisplayName = handleComputerDisplayName(targetMonitor, targetType);
			if (hasMeaningfulContent(computerDisplayName)) {
				// We will use computer display name as enclosureDisplayId, if it is still not set
				if (hasMeaningfulContent(enclosureDisplayId)) {
					// Add computerDisplayName as additionalInfo in parenthesis
					additionalInfo = computerDisplayName;
				} else {
					// Use it as enclosureDisplayId
					enclosureDisplayId = computerDisplayName;
				}
			}
		}

		// Build the name
		return buildName(

				// Type
				enclosureType,

				// Name
				enclosureDisplayId,
				metadata.get(DEVICE_ID),
				metadata.get(ID_COUNT),
				ENCLOSURE_TRIM_PATTERN,

				// Additional label
				additionalInfo
		);
	}

	/**
	 * Builds the fan name based on the current implementation in Hardware Sentry KM
	 *
	 * @param monitorBuildingInfo {@link MonitorBuildingInfo} of the monitor instance
	 *
	 * @return {@link String} name Label of the fan to be displayed
	 */
	public static String buildFanName(final MonitorBuildingInfo monitorBuildingInfo) {

		// Check the metadata
		final Map<String, String> metadata = monitorBuildingInfo.getMonitor().getMetadata();
		Assert.notNull(metadata, METADATA_CANNOT_BE_NULL);

		// Build the name
		return buildName(

				// Type
				null,

				// Name
				metadata.get(DISPLAY_ID),
				metadata.get(DEVICE_ID),
				metadata.get(ID_COUNT),
				FAN_TRIM_PATTERN,

				// Additional label
				metadata.get(FAN_TYPE)
		);
	}

	/**
	 * Builds the LED name based on the current implementation in Hardware Sentry KM
	 *
	 * @param monitorBuildingInfo {@link MonitorBuildingInfo} of the monitor instance
	 *
	 * @return {@link String} name Label of the LED to be displayed
	 */
	public static String buildLedName(final MonitorBuildingInfo monitorBuildingInfo) {

		// Check the metadata
		final Map<String, String> metadata = monitorBuildingInfo.getMonitor().getMetadata();
		Assert.notNull(metadata, METADATA_CANNOT_BE_NULL);

		// Format the LED color
		String ledColor = metadata.get(COLOR);
		if (hasMeaningfulContent(ledColor)) {
			ledColor = ledColor.substring(0, 1).toUpperCase() + ledColor.substring(1).toLowerCase();
		}

		// Build the name
		return buildName(

				// Type
				null,

				// Name
				metadata.get(DISPLAY_ID),
				metadata.get(DEVICE_ID),
				metadata.get(ID_COUNT),
				LED_TRIM_PATTERN,

				// Additional label
				ledColor,
				metadata.get(NAME)
		);
	}

	/**
	 * Builds the logical disk name based on the current implementation in Hardware Sentry KM
	 *
	 * @param monitorBuildingInfo {@link MonitorBuildingInfo} of the monitor instance
	 *
	 * @return {@link String} name Label of the logical disk to be displayed
	 */
	public static String buildLogicalDiskName(final MonitorBuildingInfo monitorBuildingInfo) {

		// Check the metadata
		final Map<String, String> metadata = monitorBuildingInfo.getMonitor().getMetadata();
		Assert.notNull(metadata, METADATA_CANNOT_BE_NULL);

		// Format the RAID level
		String logicalDiskRaidLevel = metadata.get(RAID_LEVEL);
		if (logicalDiskRaidLevel != null) {
			try {
				int logicalDiskRaidLevelD = Integer.parseInt(logicalDiskRaidLevel);
				logicalDiskRaidLevel = String.format("RAID %d", logicalDiskRaidLevelD);
			} catch (NumberFormatException nfe) {}
		}

		// Build the name
		return buildName(

				// Type
				metadata.get(DEVICE_TYPE),

				// Name
				metadata.get(DISPLAY_ID),
				metadata.get(DEVICE_ID),
				metadata.get(ID_COUNT),
				LOGICAL_DISK_TRIM_PATTERN,

				// Additional label
				logicalDiskRaidLevel,
				humanReadableByteCountBin(metadata.get(SIZE))
		);
	}

	/**
	 * Builds the LUN name based on the current implementation in Hardware Sentry KM
	 *
	 * @param monitorBuildingInfo {@link MonitorBuildingInfo} of the monitor instance
	 *
	 * @return {@link String} name Label of the LUN to be displayed
	 */
	public static String buildLunName(final MonitorBuildingInfo monitorBuildingInfo) {

		// Check the metadata
		final Map<String, String> metadata = monitorBuildingInfo.getMonitor().getMetadata();
		Assert.notNull(metadata, METADATA_CANNOT_BE_NULL);

		// Build the name
		return buildName(

				// Type
				null,

				// Name
				metadata.get(DISPLAY_ID),
				metadata.get(DEVICE_ID),
				metadata.get(ID_COUNT),
				LUN_TRIM_PATTERN,

				// Additional label
				metadata.get(LOCAL_DEVICE_NAME),
				metadata.get(REMOTE_DEVICE_NAME)
		);
	}

	/**
	 * Builds the memory name based on the current implementation in Hardware Sentry KM
	 *
	 * @param monitorBuildingInfo {@link MonitorBuildingInfo} of the monitor instance
	 *
	 * @return {@link String} name Label of the memory to be displayed
	 */
	public static String buildMemoryName(final MonitorBuildingInfo monitorBuildingInfo) {

		// Check the metadata
		final Map<String, String> metadata = monitorBuildingInfo.getMonitor().getMetadata();
		Assert.notNull(metadata, METADATA_CANNOT_BE_NULL);

		// Format the memory size
		String memorySize = metadata.get(SIZE);
		if (memorySize != null) {
			try {
				double memorySizeD = Double.parseDouble(memorySize);
				if (memorySizeD > 50D) {
					memorySize = String.format("%.0f MB", memorySizeD);
				} else {
					memorySize = null;
				}
			} catch (NumberFormatException nfe) {
				memorySize = null;
			}
		}

		// Build the name
		return buildName(

				// Type
				null,

				// Name
				metadata.get(DISPLAY_ID),
				metadata.get(DEVICE_ID),
				metadata.get(ID_COUNT),
				MEMORY_TRIM_PATTERN,

				// Additional label
				metadata.get(VENDOR),
				metadata.get(TYPE),
				memorySize
		);
	}

	/**
	 * Builds the network card name based on the current implementation in Hardware Sentry KM
	 *
	 * @param monitorBuildingInfo {@link MonitorBuildingInfo} of the monitor instance
	 *
	 * @return {@link String} name Label of the network card to be displayed
	 */
	public static String buildNetworkCardName(final MonitorBuildingInfo monitorBuildingInfo) {

		// Check the metadata
		final Map<String, String> metadata = monitorBuildingInfo.getMonitor().getMetadata();
		Assert.notNull(metadata, METADATA_CANNOT_BE_NULL);

		// Network card vendor without unwanted words
		String networkCardVendor = metadata.get(VENDOR);
		if (hasMeaningfulContent(networkCardVendor)) {
			networkCardVendor = NETWORK_VENDOR_MODEL_TRIM_PATTERN.matcher(networkCardVendor).replaceAll("");
		}

		// Network card model without unwanted words and up to 30 characters
		String networkCardModel = metadata.get(MODEL);
		if (hasMeaningfulContent(networkCardModel)) {
			networkCardModel = NETWORK_VENDOR_MODEL_TRIM_PATTERN.matcher(networkCardModel).replaceAll("");
			if (networkCardModel.length() > 30) {
				networkCardModel = networkCardModel.substring(0, 30);
			}
		}

		// Build the name
		return buildName(

				// Type
				null,

				// Name
				metadata.get(DISPLAY_ID),
				metadata.get(DEVICE_ID),
				metadata.get(ID_COUNT),
				NETWORK_CARD_TRIM_PATTERN,

				// Additional label
				metadata.get(DEVICE_TYPE),
				networkCardVendor,
				networkCardModel
		);
	}


	/**
	 * Builds the other device name based on the current implementation in Hardware Sentry KM
	 *
	 * @param monitorBuildingInfo {@link MonitorBuildingInfo} of the monitor instance
	 *
	 * @return {@link String} name Label of the other device to be displayed
	 */
	public static String buildOtherDeviceName(final MonitorBuildingInfo monitorBuildingInfo) {

		// Check the metadata
		final Map<String, String> metadata = monitorBuildingInfo.getMonitor().getMetadata();
		Assert.notNull(metadata, METADATA_CANNOT_BE_NULL);

		// Build the name
		return buildName(

				// Type
				metadata.get(DEVICE_TYPE),

				// Name
				metadata.get(DISPLAY_ID),
				metadata.get(DEVICE_ID),
				metadata.get(ID_COUNT),
				OTHER_DEVICE_TRIM_PATTERN,

				// Additional label
				metadata.get(ADDITIONAL_LABEL)
		);
	}

	/**
	 * Builds the physical disk name based on the current implementation in Hardware Sentry KM
	 *
	 * @param monitorBuildingInfo {@link MonitorBuildingInfo} of the monitor instance
	 *
	 * @return {@link String} name Label of the physical disk to be displayed
	 */
	public static String buildPhysicalDiskName(final MonitorBuildingInfo monitorBuildingInfo) {

		// Check the metadata
		final Map<String, String> metadata = monitorBuildingInfo.getMonitor().getMetadata();
		Assert.notNull(metadata, METADATA_CANNOT_BE_NULL);

		// Build the name
		return buildName(

				// Type
				metadata.get(DEVICE_TYPE),

				// Name
				metadata.get(DISPLAY_ID),
				metadata.get(DEVICE_ID),
				metadata.get(ID_COUNT),
				PHYSICAL_DISK_TRIM_PATTERN,

				// Additional label
				metadata.get(VENDOR),
				humanReadableByteCountSI(metadata.get(SIZE))
		);
	}

	/**
	 * Builds the power supply name based on the current implementation in Hardware Sentry KM
	 *
	 * @param monitorBuildingInfo {@link MonitorBuildingInfo} of the monitor instance
	 *
	 * @return {@link String} name Label of the physical disk to be displayed
	 */
	public static String buildPowerSupplyName(final MonitorBuildingInfo monitorBuildingInfo) {

		// Check the metadata
		final Map<String, String> metadata = monitorBuildingInfo.getMonitor().getMetadata();
		Assert.notNull(metadata, METADATA_CANNOT_BE_NULL);

		// Format the power
		String powerSupplyPower = metadata.get(POWER_SUPPLY_POWER);
		if (hasMeaningfulContent(powerSupplyPower)) {
			powerSupplyPower = powerSupplyPower + " W";
		}

		// Build the name
		return buildName(

				// Type
				null,

				// Name
				metadata.get(DISPLAY_ID),
				metadata.get(DEVICE_ID),
				metadata.get(ID_COUNT),
				POWER_SUPPLY_TRIM_PATTERN,

				// Additional label
				metadata.get(POWER_SUPPLY_TYPE),
				powerSupplyPower
		);
	}

	/**
	 * Builds the robotics name based on the current implementation in Hardware Sentry KM
	 *
	 * @param monitorBuildingInfo {@link MonitorBuildingInfo} of the monitor instance
	 *
	 * @return {@link String} name Label of the robotics to be displayed
	 */
	public static String buildRoboticsName(final MonitorBuildingInfo monitorBuildingInfo) {

		// Check the metadata
		final Map<String, String> metadata = monitorBuildingInfo.getMonitor().getMetadata();
		Assert.notNull(metadata, METADATA_CANNOT_BE_NULL);

		// Build the name
		return buildName(

				// Type
				null,

				// Name
				metadata.get(DISPLAY_ID),
				metadata.get(DEVICE_ID),
				metadata.get(ID_COUNT),
				ROBOTICS_TRIM_PATTERN,

				// Additional label
				joinVendorAndModel(metadata),
				metadata.get(ROBOTIC_TYPE)
		);
	}

	/**
	 * Builds the tape drive name based on the current implementation in Hardware Sentry KM
	 *
	 * @param monitorBuildingInfo {@link MonitorBuildingInfo} of the monitor instance
	 *
	 * @return {@link String} name Label of the tape drive to be displayed
	 */
	public static String buildTapeDriveName(final MonitorBuildingInfo monitorBuildingInfo) {

		// Check the metadata
		final Map<String, String> metadata = monitorBuildingInfo.getMonitor().getMetadata();
		Assert.notNull(metadata, METADATA_CANNOT_BE_NULL);

		// Build the name
		return buildName(

				// Type
				null,

				// Name
				metadata.get(DISPLAY_ID),
				metadata.get(DEVICE_ID),
				metadata.get(ID_COUNT),
				TAPE_DRIVE_TRIM_PATTERN,

				// Additional label
				joinVendorAndModel(metadata)
		);
	}

	/**
	 * Builds the temperature name based on the current implementation in Hardware Sentry KM
	 *
	 * @param monitorBuildingInfo {@link MonitorBuildingInfo} of the monitor instance
	 *
	 * @return {@link String} name Label of the temperature to be displayed
	 */
	public static String buildTemperatureName(final MonitorBuildingInfo monitorBuildingInfo) {

		// Check the metadata
		final Map<String, String> metadata = monitorBuildingInfo.getMonitor().getMetadata();
		Assert.notNull(metadata, METADATA_CANNOT_BE_NULL);

		// Build the name
		return buildName(

				// Type
				null,

				// Name
				metadata.get(DISPLAY_ID),
				metadata.get(DEVICE_ID),
				metadata.get(ID_COUNT),
				TEMPERATURE_TRIM_PATTERN,

				// Additional label
				metadata.get(TEMPERATURE_TYPE)
		);
	}

	/**
	 * Builds the voltage name based on the current implementation in Hardware Sentry KM
	 *
	 * @param monitorBuildingInfo {@link MonitorBuildingInfo} of the monitor instance
	 *
	 * @return {@link String} name Label of the voltage to be displayed
	 */
	public static String buildVoltageName(final MonitorBuildingInfo monitorBuildingInfo) {

		// Check the metadata
		final Map<String, String> metadata = monitorBuildingInfo.getMonitor().getMetadata();
		Assert.notNull(metadata, METADATA_CANNOT_BE_NULL);

		// Build the name
		return buildName(

				// Type
				null,

				// Name
				metadata.get(DISPLAY_ID),
				metadata.get(DEVICE_ID),
				metadata.get(ID_COUNT),
				VOLTAGE_TRIM_PATTERN,

				// Additional label
				metadata.get(VOLTAGE_TYPE)
		);
	}

}
