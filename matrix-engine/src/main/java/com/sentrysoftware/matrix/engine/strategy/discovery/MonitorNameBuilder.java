package com.sentrysoftware.matrix.engine.strategy.discovery;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.util.Assert;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.engine.target.TargetType;
import com.sentrysoftware.matrix.model.monitor.Monitor;

public class MonitorNameBuilder {

	private MonitorNameBuilder() {
	}

	public static final String UNKNOWN_COMPUTER = "Unknown computer";
	public static final String HP_TRU64_COMPUTER = "HP Tru64 computer";
	public static final String LINUX_COMPUTER = "Linux computer";
	public static final String HP_OPEN_VMS_COMPUTER = "HP Open-VMS computer";
	public static final String WINDOWS_COMPUTER = "Windows computer";
	public static final String LOCALHOST_ENCLOSURE = "Localhost Enclosure";

	private static final String CANNOT_CREATE_MONITOR_NULL_NAME_MSG = "Cannot create monitor {} with null name. Connector {}. System {}.";
	private static final String NAME_SEPARATOR = ": ";
	private static final String ID_COUNT_CANNOT_BE_NULL = "idCount cannot be null.";
	private static final String HOSTNAME_CANNOT_BE_NULL = "hostname cannot be null.";
	private static final String HOST_MONITORING_CANNOT_BE_NULL = "hostMonitoring cannot be null.";
	private static final String CONNECTOR_NAME_CANNOT_BE_NULL = "connectorName cannot be null.";
	private static final String MONITOR_CANNOT_BE_NULL = "monitor cannot be null.";
	private static final String TARGET_ID_CANNOT_BE_NULL = "target id cannot be null.";
	private static final String TARGET_MONITOR_CANNOT_BE_NULL = "targetMonitor cannot be null.";
	private static final String TARGET_TYPE_CANNOT_BE_NULL = "targetType cannot be null.";
	private static final String MONITOR_TYPE_CANNOT_BE_NULL = "monitorType cannot be null.";
	private static final String METADATA_CANNOT_BE_NULL = "metadata cannot be null.";
	private static final String MONITOR_BUILDING_INFO_CANNOT_BE_NULL = "monitorBuildingInfo cannot be null.";
	private static final String CANNOT_CREATE_MONITOR_ERROR_MSG = "Cannot create {} with deviceId {}. Connector {}. System {}";

	private static final Map<TargetType, String> COMPUTE_DISPLAY_NAMES;
	static {
		final Map<TargetType, String> map = new EnumMap<>(TargetType.class);
		for (TargetType targetType : TargetType.values()) {
			final String value;
			switch (targetType) {
			case MS_WINDOWS:
				value = WINDOWS_COMPUTER;
				break;
			case HP_OPEN_VMS:
				value = HP_OPEN_VMS_COMPUTER;
				break;
			case LINUX:
				value = LINUX_COMPUTER;
				break;
			case HP_TRU64_UNIX:
				value = HP_TRU64_COMPUTER;
				break;
			default:
				value = UNKNOWN_COMPUTER;
			}
			map.put(targetType, value);
		}

		COMPUTE_DISPLAY_NAMES = Collections.unmodifiableMap(map);
	}

	private static boolean checkNotBlankDataValue(final String data) {
		return data != null && !data.trim().isEmpty();
	}

	/**
	 * Trims known/given words off the phrase to return only the unknown part of the content
	 * 
	 * @param phrase {@link String} phrase to be trimmed
	 * @param words  {@link List} of words (case-insensitive) to be trimmed off the phrase
	 * 
	 * @return {@link String} trimmedPhrase Trimmed phrase
	 */
	private static String trimKnownWords(final String phrase, final List<String> words) {
		String lowerCasePhrase = phrase.toLowerCase();
		String trimmedPhrase = phrase;
		for (String word : words) {
			if (word != null && lowerCasePhrase.contains(word.toLowerCase())) {
				trimmedPhrase = trimmedPhrase.replaceAll("(?i)\\s*" + word + "\\s*", "");
			}
		}
		return trimmedPhrase;
	}

	/**
	 * Trims known/given words off the phrase to return only the unknown part of the content
	 * 
	 * @param phrase {@link String} phrase to be trimmed
	 * @param words  {@link List} of words (case-insensitive) to be trimmed off the phrase
	 * 
	 * @return {@link String} trimmedPhrase Trimmed phrase
	 */
	private static String trimUnwantedCharacters(final String name) {

		// Trim any comma
		String trimmedName = name.replaceAll(",", "");

		// Trim empty parenthesis
		trimmedName = trimmedName.replace("()", "");

		// Trim redundant blank spaces
		trimmedName = trimmedName.replaceAll(" +", " ");

		// Trim leading and trailing white spaces
		trimmedName = trimmedName.trim();

		return trimmedName;
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
		final StringBuilder buffer = new StringBuilder();
		String nextSeparator = "";
		for (String word : words) {
			if (checkNotBlankDataValue(word)) {
				buffer.append(nextSeparator);
				nextSeparator = separator;
				buffer.append(word);
			}
		}

		return buffer.toString();
	}

	/**
	 * Joins vendor and model; if model includes vendor, returns the model
	 * 
	 * @param metadata {@link Map} of metadata containing vendor and model info
	 * 
	 * @return {@link String} joinedVendorAndModel Joined vendor and model
	 */
	private static String joinVendorAndModel(final Map<String, String> metadata) {

		String vendor = metadata.get(HardwareConstants.VENDOR);
		String model = metadata.get(HardwareConstants.MODEL);

		if (vendor != null && model != null) {
			if (model.toLowerCase().contains(vendor.toLowerCase())) {
				// Model includes the vendor, so no need to join them
				return model;
			}
		}

		return joinWords(new String[] { vendor, model }, " ");
	}

	/**
	 * Try to get the {@value HardwareConstants#LOCATION} metadata and return <code>true</code> for localhost value 
	 * Note: {@value HardwareConstants#LOCATION} is computed on {@link MonitorType#TARGET} in the detection operation
	 * 
	 * @param metadata
	 * 
	 * @return {@link boolean} value
	 */
	static boolean isLocalhost(final Map<String, String> metadata) {
		if (metadata != null) {
			final String location = metadata.get(HardwareConstants.LOCATION);
			if (location != null) {
				return location.equalsIgnoreCase(HardwareConstants.LOCALHOST);
			}
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
	static String handleComputerDisplayName(final Monitor targetMonitor, final TargetType targetType) {
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
	 * Build a generic name common to all hardware devices
	 * 
	 * @param displayId      {@link String} containing the display ID
	 * @param deviceId       {@link String} containing the device ID
	 * @param idCount        {@link String} containing the ID count
	 * @param trimmableWords {@link List} of words (case-insensitive) to be trimmed off the device ID
	 * 
	 * @return {@link String} name Generic label based on the inputs
	 */
	public static String buildGenericName(final String displayId, final String deviceId, final String idCount,
			final List<String> trimmableWords) {

		// Make sure the ID count is set
		Assert.notNull(idCount, ID_COUNT_CANNOT_BE_NULL);

		// Build the name
		String name = null;
		if (checkNotBlankDataValue(displayId)) {
			name = displayId;
		} else if (checkNotBlankDataValue(deviceId)) {
			name = trimKnownWords(deviceId, trimmableWords);
			if (name.length() > HardwareConstants.ID_MAXLENGTH) {
				name = idCount;
			}
		}

		// Use the ID count as name, if we couldn't build one from display ID or device ID
		if (!checkNotBlankDataValue(name)) {
			return idCount;
		}

		return name;
	}
	
	/**
	 * Build a generic name common to all hardware devices without any word trimming
	 * 
	 * @param displayId      {@link String} containing the display ID
	 * @param deviceId       {@link String} containing the device ID
	 * @param idCount        {@link String} containing the ID count
	 * 
	 * @return {@link String} name Generic label based on the inputs
	 */
	public static String buildGenericName(final String displayId, final String deviceId, final String idCount) {

		// Make sure the ID count is set
		Assert.notNull(idCount, ID_COUNT_CANNOT_BE_NULL);

		// Build the name
		String name = null;
		if (checkNotBlankDataValue(displayId)) {
			name = displayId;
		} else if (checkNotBlankDataValue(deviceId)) {
			name = deviceId;
			if (name.length() > HardwareConstants.ID_MAXLENGTH) {
				name = idCount;
			}
		}

		// Use the ID count as name, if we couldn't build one from display ID or device ID
		if (!checkNotBlankDataValue(name)) {
			return idCount;
		}

		return name;
	}

	/**
	 * Build the battery name based on the current implementation in Hardware Sentry
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

		// Build the generic name
		final StringBuilder name = new StringBuilder();
		name.append(buildGenericName(
				metadata.get(HardwareConstants.DISPLAY_ID),
				metadata.get(HardwareConstants.DEVICE_ID), 
				metadata.get(HardwareConstants.ID_COUNT), 
				List.of("battery")));

		// Add the additional info to the label
		name.append(" ("
				+ joinWords(new String[] { joinVendorAndModel(metadata), metadata.get(HardwareConstants.TYPE) }, " - ")
				+ ")");

		return trimUnwantedCharacters(name.toString());
	}

	/**
	 * Build the blade name based on the current implementation in Hardware Sentry KM
	 * 
	 * @param monitorBuildingInfo {@link MonitorBuildingInfo} of the monitor instance
	 * 
	 * @return {@link String} name Label of the blade to be displayed
	 */
	public static String buildBladeName(final MonitorBuildingInfo monitorBuildingInfo) {

		// Check the metadata
		final Map<String, String> metadata = monitorBuildingInfo.getMonitor().getMetadata();
		Assert.notNull(metadata, METADATA_CANNOT_BE_NULL);

		// Build the generic name
		final StringBuilder name = new StringBuilder();
		name.append(buildGenericName(
				metadata.get(HardwareConstants.DISPLAY_ID),
				metadata.get(HardwareConstants.DEVICE_ID),
				metadata.get(HardwareConstants.ID_COUNT), 
				List.of("blade")));

		// Add the additional info to the label
		name.append(" (" + joinWords(
				new String[] { metadata.get(HardwareConstants.BLADE_NAME), metadata.get(HardwareConstants.MODEL) },
				" - ") + ")");

		return trimUnwantedCharacters(name.toString());
	}

	/**
	 * Build the CPU name based on the current implementation in Hardware Sentry KM
	 * 
	 * @param monitorBuildingInfo {@link MonitorBuildingInfo} of the monitor instance
	 * 
	 * @return {@link String} name Label of the CPU to be displayed
	 */
	public static String buildCpuName(final MonitorBuildingInfo monitorBuildingInfo) {

		// Check the metadata
		final Map<String, String> metadata = monitorBuildingInfo.getMonitor().getMetadata();
		Assert.notNull(metadata, METADATA_CANNOT_BE_NULL);

		// Build the generic name
		final StringBuilder name = new StringBuilder();
		name.append(buildGenericName(
				metadata.get(HardwareConstants.DISPLAY_ID),
				metadata.get(HardwareConstants.DEVICE_ID), 
				metadata.get(HardwareConstants.ID_COUNT), 
				List.of("cpu", "processor", "proc")));

		// Format the maximum speed
		String cpuMaxSpeed = metadata.get(HardwareConstants.MAXIMUM_SPEED);
		if (checkNotBlankDataValue(cpuMaxSpeed)) {
			Integer cpuMaxSpeedAsInt = Integer.parseInt(cpuMaxSpeed);
			if (cpuMaxSpeedAsInt < 1000) {
				cpuMaxSpeed = String.format("%d MHz", cpuMaxSpeedAsInt);
			} else {
				cpuMaxSpeed = String.format("%.2f GHz", (cpuMaxSpeedAsInt / 1000D));
			}
		}

		// Add the additional info to the label
		name.append(" (" + joinWords(new String[] { metadata.get(HardwareConstants.VENDOR),
				metadata.get(HardwareConstants.MODEL), cpuMaxSpeed }, " - ") + ")");

		return trimUnwantedCharacters(name.toString());
	}

	/**
	 * Build the CPU core name based on the current implementation in Hardware Sentry KM
	 * 
	 * @param monitorBuildingInfo {@link MonitorBuildingInfo} of the monitor instance
	 * 
	 * @return {@link String} name Label of the CPU core to be displayed
	 */
	public static String buildCpuCoreName(final MonitorBuildingInfo monitorBuildingInfo) {

		// Check the metadata
		final Map<String, String> metadata = monitorBuildingInfo.getMonitor().getMetadata();
		Assert.notNull(metadata, METADATA_CANNOT_BE_NULL);

		// Build the generic name
		String name = buildGenericName(
				metadata.get(HardwareConstants.DISPLAY_ID), 
				metadata.get(HardwareConstants.DEVICE_ID),
				metadata.get(HardwareConstants.ID_COUNT),
				List.of("cpu", "processor", "core", "proc"));

		return trimUnwantedCharacters(name);
	}

	/**
	 * Build the disk controller name based on the current implementation in Hardware Sentry KM
	 * 
	 * @param monitorBuildingInfo {@link MonitorBuildingInfo} of the monitor instance
	 * 
	 * @return {@link String} name Label of the disk controller to be displayed
	 */
	public static String buildDiskControllerName(final MonitorBuildingInfo monitorBuildingInfo) {

		// Check the metadata
		final Map<String, String> metadata = monitorBuildingInfo.getMonitor().getMetadata();
		Assert.notNull(metadata, METADATA_CANNOT_BE_NULL);

		// Build the generic name with prefix (controllerNumber is used for deviceId)
		final StringBuilder name = new StringBuilder("Disk Controller: ");
		name.append(buildGenericName(
				metadata.get(HardwareConstants.DISPLAY_ID), 
				metadata.get(HardwareConstants.DISK_CONTROLLER_NUMBER),
				metadata.get(HardwareConstants.ID_COUNT)));

		// Add the additional info to the label
		name.append(" (" + joinVendorAndModel(metadata) + ")");

		return trimUnwantedCharacters(name.toString());
	}

	/**
	 * Build the enclosure name based on the current implementation in Hardware Sentry KM
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
		String enclosureType = metadata.get(HardwareConstants.TYPE);
		if (enclosureType != null) {
			switch (enclosureType.toLowerCase()) {
			case "computer":
				enclosureType = HardwareConstants.COMPUTER;
				break;
			case "storage":
				enclosureType = HardwareConstants.STORAGE;
				break;
			case "blade":
				enclosureType = HardwareConstants.BLADE_ENCLOSURE;
				break;
			case "":
				enclosureType = HardwareConstants.COMPUTER;
				break;
			default:
				enclosureType = HardwareConstants.ENCLOSURE;
			}
		} else {
			enclosureType = HardwareConstants.ENCLOSURE;
		}

		// Build the generic name with prefix
		final StringBuilder name = new StringBuilder(enclosureType + ": ");

		// If enclosureDisplayID is specified, use it and put the rest in parenthesis
		String enclosureDisplayId = metadata.get(HardwareConstants.DISPLAY_ID);
		boolean parenthesisOpened = false;
		if (checkNotBlankDataValue(enclosureDisplayId)) {
			name.append(enclosureDisplayId + " (");
			parenthesisOpened = true;
		}

		// Find the vendor/model details
		final String vendorModel = joinVendorAndModel(metadata);

		if (checkNotBlankDataValue(vendorModel)) {
			name.append(vendorModel);
		} else if (HardwareConstants.COMPUTER.equals(enclosureType)) {
			name.append(handleComputerDisplayName(targetMonitor, targetType));
		} else if (!parenthesisOpened) {
			name.append(buildGenericName(
					null, 
					metadata.get(HardwareConstants.DEVICE_ID),
					metadata.get(HardwareConstants.ID_COUNT)));
		}

		// At the end, close the parenthesis, if opened
		if (parenthesisOpened) {
			name.append(")");
		}

		return trimUnwantedCharacters(name.toString());
	}

	/**
	 * Build the fan name based on the current implementation in Hardware Sentry KM
	 * 
	 * @param monitorBuildingInfo {@link MonitorBuildingInfo} of the monitor instance
	 * 
	 * @return {@link String} name Label of the fan to be displayed
	 */
	public static String buildFanName(final MonitorBuildingInfo monitorBuildingInfo) {

		// Check the metadata
		final Map<String, String> metadata = monitorBuildingInfo.getMonitor().getMetadata();
		Assert.notNull(metadata, METADATA_CANNOT_BE_NULL);

		// Build the generic name
		final StringBuilder name = new StringBuilder();
		name.append(buildGenericName(
				metadata.get(HardwareConstants.DISPLAY_ID), 
				metadata.get(HardwareConstants.DEVICE_ID),
				metadata.get(HardwareConstants.ID_COUNT), 
				List.of("fan")));

		// Add the additional info to the label
		name.append(" (" + joinWords(new String[] { metadata.get(HardwareConstants.FAN_TYPE) }, " - ") + ")");

		return trimUnwantedCharacters(name.toString());
	}
	
	/**
	 * Build the LED name based on the current implementation in Hardware Sentry KM
	 * 
	 * @param monitorBuildingInfo {@link MonitorBuildingInfo} of the monitor instance
	 * 
	 * @return {@link String} name Label of the LED to be displayed
	 */
	public static String buildLedName(final MonitorBuildingInfo monitorBuildingInfo) {

		// Check the metadata
		final Map<String, String> metadata = monitorBuildingInfo.getMonitor().getMetadata();
		Assert.notNull(metadata, METADATA_CANNOT_BE_NULL);

		// Build the generic name
		final StringBuilder name = new StringBuilder();
		name.append(buildGenericName(
				metadata.get(HardwareConstants.DISPLAY_ID),
				metadata.get(HardwareConstants.DEVICE_ID),
				metadata.get(HardwareConstants.ID_COUNT), 
				List.of("led")));

		// Add the additional info to the label
		String ledColor = metadata.get(HardwareConstants.COLOR);
		if (checkNotBlankDataValue(ledColor)) {
			ledColor = ledColor.substring(0, 1).toUpperCase() + ledColor.substring(1).toLowerCase();
		}
		name.append(" (" + joinWords(new String[] { ledColor, metadata.get(HardwareConstants.NAME) }, " - ") + ")");

		return trimUnwantedCharacters(name.toString());
	}
}