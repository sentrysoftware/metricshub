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

	/**
	 * Check whether the string is blank, empty or null
	 * 
	 * @param data        {@link String} to be checked
	 * 
	 * @return {@link boolean} true/false whether it is blank or not
	 */
	private static boolean isBlankDataValue(final String data) {
		return data == null || data.trim().isEmpty();
	}
	
	/**
	 * Trims matching regular expression off the phrase to return only the unknown part of the content
	 * 
	 * @param phrase      {@link String} phrase to be trimmed
	 * @param regexp      {@link String} of regex (case-insensitive) to be trimmed off the phrase
	 * 
	 * @return {@link String} trimmedPhrase Trimmed phrase
	 */
	private static String trimKnownWords(final String phrase, final String regexp) {
		
		if (isBlankDataValue(regexp)) {
			return phrase;
		}
		
		String pattern = "(?i)\\s*" + regexp + "\\s*";
		return phrase.replaceAll(pattern, "");
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
			if (!isBlankDataValue(word)) {
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
	 * @param metadata         Metadata containing location and localhost constants
	 * 
	 * @return {@link boolean} true/false whether it is a localhost or not
	 */
	public static boolean isLocalhost(final Map<String, String> metadata) {
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
	 * Check whether the string contains only integer or not
	 * 
	 * @param string        {@link String} to be checked for integer
	 * 
	 * @return {@link boolean} true/false whether it is an integer or not
	 */
	private static boolean isInteger(final String string) {
		try { 
			Integer.parseInt(string); 
		} catch (Exception e) { 
			return false; 
		}
		
		// Must be a good integer
		return true;
	}
	
	/**
	 * Converts a number in the string to readable bytes format
	 * 
	 * @param string        {@link String} to be formatted
	 * 
	 * @return {@link String} formatted bytes
	 */
	private static String formatByteNumber(final String string) {
		
		long bytes;
		try { 
			bytes = Long.parseLong(string); 
		} catch (Exception e) { 
			return string; 
		}
		
		if (bytes < 0) {
			bytes = bytes * -1;
		}
		
		long divider = 1125899906842624L; //

		for (String unit : new String[] {"PB", "TB", "GB", "MB", "KB"}) {
			if (bytes >= divider) {
				return String.format("%d %s", bytes/divider, unit);
			}
			
			divider = divider/1024;
		}
		
		return String.format("%d Bytes", bytes);

	}
	
	/**
	 * Build a generic name common to all hardware devices
	 * 
	 * @param displayId      {@link String} containing the display ID
	 * @param deviceId       {@link String} containing the device ID
	 * @param idCount        {@link String} containing the ID count
	 * @param trimmableRegex {@link String} of regex words (case-insensitive) to be trimmed off the device ID
	 * 
	 * @return {@link String} name Generic label based on the inputs
	 */
	public static String buildGenericName(final String displayId, final String deviceId, final String idCount,
			final String trimmableRegex) {

		// Make sure the ID count is set
		Assert.notNull(idCount, ID_COUNT_CANNOT_BE_NULL);

		// Build the name
		String name = null;
		if (!isBlankDataValue(displayId)) {
			name = displayId;
		} else if (!isBlankDataValue(deviceId)) {
			name = trimKnownWords(deviceId, trimmableRegex);
			if (name.length() > HardwareConstants.ID_MAXLENGTH) {
				name = idCount;
			}
		}

		// Use the ID count as name, if we couldn't build one from display ID or device ID
		if (isBlankDataValue(name)) {
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
				"battery"));

		// Add the additional info to the label
		name.append(" (" + joinWords(new String[] { 
				joinVendorAndModel(metadata), 
				metadata.get(HardwareConstants.TYPE) 
			}, " - ") + ")");

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
				"blade"));

		// Add the additional info to the label
		name.append(" (" + joinWords(new String[] { 
				metadata.get(HardwareConstants.BLADE_NAME), 
				metadata.get(HardwareConstants.MODEL) 
			}, " - ") + ")");

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
				"cpu|processor|proc"));

		// Format the maximum speed
		String cpuMaxSpeed = metadata.get(HardwareConstants.MAXIMUM_SPEED);
		if (isInteger(cpuMaxSpeed)) {
			Integer cpuMaxSpeedAsInt = Integer.parseInt(cpuMaxSpeed);
			if (cpuMaxSpeedAsInt < 1000) {
				cpuMaxSpeed = String.format("%d MHz", cpuMaxSpeedAsInt);
			} else {
				cpuMaxSpeed = String.format("%.2f GHz", (cpuMaxSpeedAsInt / 1000D));
			}
		}

		// Add the additional info to the label
		name.append(" (" + joinWords(new String[] { 
				metadata.get(HardwareConstants.VENDOR),
				metadata.get(HardwareConstants.MODEL), 
				cpuMaxSpeed 
			}, " - ") + ")");

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
				"cpu|processor|core|proc");

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
				metadata.get(HardwareConstants.ID_COUNT),
				""));

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

		// If enclosureDisplayID is specified, use it and put the rest in parenthesis
		String enclosureDisplayId = metadata.get(HardwareConstants.DISPLAY_ID);
		String additionalInfo = "";

		// Find the vendor/model details
		String vendorModel = joinVendorAndModel(metadata);
		if (!isBlankDataValue(vendorModel)) {
			
			// We will use vendor/model as enclosureDisplayId, if it is not set
			if (!isBlankDataValue(enclosureDisplayId)) {
				// Add vendor/model as additionalInfo in parenthesis
				additionalInfo = vendorModel;
			} else {
				// Use it as enclosureDisplayId
				enclosureDisplayId = vendorModel;
			}
			
		} else if (HardwareConstants.COMPUTER.equals(enclosureType)) {
			
			// Find the computer display name
			String computerDisplayName = handleComputerDisplayName(targetMonitor, targetType);
			if (!isBlankDataValue(computerDisplayName)) {
				// We will use computer display name as enclosureDisplayId, if it is still not set
				if (!isBlankDataValue(enclosureDisplayId)) {
					// Add computerDisplayName as additionalInfo in parenthesis
					additionalInfo = computerDisplayName;
				} else {
					// Use it as enclosureDisplayId
					enclosureDisplayId = computerDisplayName;
				}
			}
		}
		
		// Build the generic name with prefix
		final StringBuilder name = new StringBuilder(enclosureType + ": ");
		name.append(buildGenericName(
				enclosureDisplayId, 
				metadata.get(HardwareConstants.DEVICE_ID),
				metadata.get(HardwareConstants.ID_COUNT),
				""));

		// Add the additional info to the label
		name.append(" (" + additionalInfo + ")");
		
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
				"fan"));

		// Add the additional info to the label
		name.append(" (" + joinWords(new String[] { 
				metadata.get(HardwareConstants.FAN_TYPE) 
			}, " - ") + ")");

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
				"led"));

		// Add the additional info to the label
		String ledColor = metadata.get(HardwareConstants.COLOR);
		if (!isBlankDataValue(ledColor)) {
			ledColor = ledColor.substring(0, 1).toUpperCase() + ledColor.substring(1).toLowerCase();
		}
		name.append(" (" + joinWords(new String[] { 
				ledColor, 
				metadata.get(HardwareConstants.NAME) 
			}, " - ") + ")");

		return trimUnwantedCharacters(name.toString());
	}
	
	/**
	 * Build the logical disk name based on the current implementation in Hardware Sentry KM
	 * 
	 * @param monitorBuildingInfo {@link MonitorBuildingInfo} of the monitor instance
	 * 
	 * @return {@link String} name Label of the logical disk to be displayed
	 */
	public static String buildLogicalDiskName(final MonitorBuildingInfo monitorBuildingInfo) {

		// Check the metadata
		final Map<String, String> metadata = monitorBuildingInfo.getMonitor().getMetadata();
		Assert.notNull(metadata, METADATA_CANNOT_BE_NULL);

		// Build the generic name with prefix
		final StringBuilder name = new StringBuilder();
		final String logicalDiskType = metadata.get(HardwareConstants.TYPE);
		if (!isBlankDataValue(logicalDiskType)) {
			name.append(logicalDiskType + ": ");
		}

		name.append(buildGenericName(
				metadata.get(HardwareConstants.DISPLAY_ID),
				metadata.get(HardwareConstants.DEVICE_ID),
				metadata.get(HardwareConstants.ID_COUNT), 
				"disk|drive|logical"));

		// Add the additional info to the label
		String logicalDiskRaidLevel = metadata.get(HardwareConstants.RAID_LEVEL);
		if (!isBlankDataValue(logicalDiskRaidLevel) && isInteger(logicalDiskRaidLevel)) {
			logicalDiskRaidLevel = "RAID " + logicalDiskRaidLevel;
		}
		
		name.append(" (" + joinWords(new String[] { 
				logicalDiskRaidLevel, 
				formatByteNumber(metadata.get(HardwareConstants.SIZE)) 
			}, " - ") + ")");

		return trimUnwantedCharacters(name.toString());
	}
	
	/**
	 * Build the LUN name based on the current implementation in Hardware Sentry KM
	 * 
	 * @param monitorBuildingInfo {@link MonitorBuildingInfo} of the monitor instance
	 * 
	 * @return {@link String} name Label of the LUN to be displayed
	 */
	public static String buildLunName(final MonitorBuildingInfo monitorBuildingInfo) {

		// Check the metadata
		final Map<String, String> metadata = monitorBuildingInfo.getMonitor().getMetadata();
		Assert.notNull(metadata, METADATA_CANNOT_BE_NULL);

		// Build the generic name
		final StringBuilder name = new StringBuilder();
		name.append(buildGenericName(
				metadata.get(HardwareConstants.DISPLAY_ID),
				metadata.get(HardwareConstants.DEVICE_ID),
				metadata.get(HardwareConstants.ID_COUNT), 
				"lun"));

		// Add the additional info to the label
		name.append(" (" + joinWords(new String[] { 
				metadata.get(HardwareConstants.LOCAL_DEVICE_NAME), 
				metadata.get(HardwareConstants.REMOTE_DEVICE_NAME) 
			}, " - ") + ")");

		return trimUnwantedCharacters(name.toString());
	}
	
	/**
	 * Build the memory name based on the current implementation in Hardware Sentry KM
	 * 
	 * @param monitorBuildingInfo {@link MonitorBuildingInfo} of the monitor instance
	 * 
	 * @return {@link String} name Label of the memory to be displayed
	 */
	public static String buildMemoryName(final MonitorBuildingInfo monitorBuildingInfo) {

		// Check the metadata
		final Map<String, String> metadata = monitorBuildingInfo.getMonitor().getMetadata();
		Assert.notNull(metadata, METADATA_CANNOT_BE_NULL);

		// Build the generic name
		final StringBuilder name = new StringBuilder();
		name.append(buildGenericName(
				metadata.get(HardwareConstants.DISPLAY_ID),
				metadata.get(HardwareConstants.DEVICE_ID),
				metadata.get(HardwareConstants.ID_COUNT), 
				"memory|module"));
		
		// Format the memory size
		String memorySize = metadata.get(HardwareConstants.SIZE);
		if (isInteger(memorySize)) {
			Integer memorySizeAsInt = Integer.parseInt(memorySize);
			if (memorySizeAsInt > 50) {
				memorySize = String.format("%d MB", memorySizeAsInt);
			} else {
				memorySize = "";
			}
		} else {
			memorySize = "";
		}

		// Add the additional info to the label
		name.append(" (" + joinWords(new String[] { 
				metadata.get(HardwareConstants.VENDOR), 
				metadata.get(HardwareConstants.TYPE),
				memorySize
			}, " - ") + ")");

		return trimUnwantedCharacters(name.toString());
	}
	
	/**
	 * Build the network card name based on the current implementation in Hardware Sentry KM
	 * 
	 * @param monitorBuildingInfo {@link MonitorBuildingInfo} of the monitor instance
	 * 
	 * @return {@link String} name Label of the network card to be displayed
	 */
	public static String buildNetworkCardName(final MonitorBuildingInfo monitorBuildingInfo) {

		// Check the metadata
		final Map<String, String> metadata = monitorBuildingInfo.getMonitor().getMetadata();
		Assert.notNull(metadata, METADATA_CANNOT_BE_NULL);

		// Build the generic name
		final StringBuilder name = new StringBuilder();
		name.append(buildGenericName(
				metadata.get(HardwareConstants.DISPLAY_ID),
				metadata.get(HardwareConstants.DEVICE_ID),
				metadata.get(HardwareConstants.ID_COUNT), 
				"network"));
		
		// Find the network card vendor without unwanted words
		final String unwantedWords = "network|ndis|client|server|adapter|ethernet|interface|controller|miniport|scheduler|packet|connection|multifunction|(1([0]+[/]*))*(base[\\-tx]*)*";
		String networkCardVendor = metadata.get(HardwareConstants.VENDOR);
		if (!isBlankDataValue(networkCardVendor)) {
			networkCardVendor = trimKnownWords(networkCardVendor, unwantedWords);
		}
		
		// Find the network card model without unwanted words and up to 30 characters
		String networkCardModel = metadata.get(HardwareConstants.MODEL);
		if (!isBlankDataValue(networkCardModel)) {
			networkCardModel = trimKnownWords(networkCardModel, unwantedWords);
			if (networkCardModel.length() > 30) {
				networkCardModel = networkCardModel.substring(0, 30);
			}
		}
		
		// Add the additional info to the label
		name.append(" (" + joinWords(new String[] { 
				metadata.get(HardwareConstants.TYPE),
				networkCardVendor,
				networkCardModel
			}, " - ") + ")");

		return trimUnwantedCharacters(name.toString());
	}
	
}
