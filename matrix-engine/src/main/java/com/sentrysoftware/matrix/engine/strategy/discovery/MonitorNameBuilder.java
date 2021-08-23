package com.sentrysoftware.matrix.engine.strategy.discovery;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

import org.springframework.util.Assert;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.engine.target.TargetType;
import com.sentrysoftware.matrix.model.monitor.Monitor;

public class MonitorNameBuilder {

	private MonitorNameBuilder() {}

	public static final String UNKNOWN_COMPUTER = "Unknown computer";
	public static final String HP_TRU64_COMPUTER = "HP Tru64 computer";
	public static final String LINUX_COMPUTER = "Linux computer";
	public static final String HP_OPEN_VMS_COMPUTER = "HP Open-VMS computer";
	public static final String WINDOWS_COMPUTER = "Windows computer";
	public static final String LOCALHOST_ENCLOSURE = "Localhost Enclosure";
	
	private static final String ID_COUNT_CANNOT_BE_NULL = "idCount cannot be null.";
	private static final String TARGET_MONITOR_CANNOT_BE_NULL = "targetMonitor cannot be null.";
	private static final String TARGET_TYPE_CANNOT_BE_NULL = "targetType cannot be null.";
	private static final String METADATA_CANNOT_BE_NULL = "metadata cannot be null.";

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
		
		return phrase.replaceAll("(?i)\\s*" + regexp + "\\s*", "");
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
	 * Joins the given non-empty words using dash ( - ) as the separator
	 * 
	 * @param words     {@link String[]} of words to be joined
	 * 
	 * @return {@link String} joinedWords Joined words
	 */
	private static String joinWords(final String[] words) {
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
	 * Converts a number in the string to readable bytes format using binary divisor
	 * 
	 * @param string        {@link String} to be formatted
	 * 
	 * @return {@link String} formatted bytes with units
	 */
	private static String humanReadableByteCountBin(final String string) {
		
		long bytes;
		try { 
			bytes = Long.parseLong(string); 
		} catch (Exception e) { 
			return string; 
		}
		
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
		
		return String.format("%.0f %cB", value / 1024.0, ci.current());
	}
	
	/**
	 * Converts a number in the string to readable bytes format using decimal divisor
	 * 
	 * @param string        {@link String} to be formatted
	 * 
	 * @return {@link String} formatted bytes with units
	 */
	private static String humanReadableByteCountSI(final String string) {
		
		long bytes;
		try { 
			bytes = Long.parseLong(string); 
		} catch (Exception e) { 
			return string; 
		}
		
		if (-1000 < bytes && bytes < 1000) {
			return bytes + " B";
		}
		
		CharacterIterator ci = new StringCharacterIterator("kMGTPE");
		while (bytes <= -999_950 || bytes >= 999_950) {
			bytes /= 1000;
			ci.next();
		}
		
		return String.format("%.0f %cB", bytes / 1000.0, ci.current());
	}
	
	/**
	 * Builds the name for hardware device following the standard naming:
	 * <code>[type :][name][(additional-label)]</code>
	 * 
	 * @param type                  {@link String} containing the type to be prefixed with colon
	 * @param displayId             {@link String} containing the display ID, part of the name
	 * @param deviceId              {@link String} containing the device ID, part of the name
	 * @param idCount               {@link String} containing the ID count, part of the name
	 * @param trimmableRegex        {@link String} of regex words (case-insensitive) to be trimmed off the device ID
	 * @param additionalLabelFields {@link String} containing any additional labels to be included within parenthesis
	 * 
	 * @return {@link String} name Full name following the standard naming based on the inputs
	 */
	public static String buildName(final String type, final String displayId, final String deviceId, final String idCount,
			final String trimmableRegex, final String ...additionalLabelFields) {

		// Make sure the ID count is set
		Assert.notNull(idCount, ID_COUNT_CANNOT_BE_NULL);

		final StringBuilder fullName = new StringBuilder();
		
		// Add the type
		if (!isBlankDataValue(type)) {
			fullName.append(type + ": ");
		}

		// Add the name
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
			name = idCount;
		}
		
		fullName.append(name);
		
		// Add the additional label in parenthesis
		final String additionalLabel = joinWords(additionalLabelFields);
		if (!isBlankDataValue(additionalLabel)) {
			fullName.append(" (" + additionalLabel + ")");
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
				"",

				// Name
				metadata.get(HardwareConstants.DISPLAY_ID),
				metadata.get(HardwareConstants.DEVICE_ID), 
				metadata.get(HardwareConstants.ID_COUNT), 
				"battery",

				// Additional label
				joinVendorAndModel(metadata), 
				metadata.get(HardwareConstants.TYPE)
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
				"",

				// Name
				metadata.get(HardwareConstants.DISPLAY_ID),
				metadata.get(HardwareConstants.DEVICE_ID), 
				metadata.get(HardwareConstants.ID_COUNT), 
				"blade",

				// Additional label
				metadata.get(HardwareConstants.BLADE_NAME), 
				metadata.get(HardwareConstants.MODEL) 
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
		String cpuMaxSpeed = metadata.get(HardwareConstants.MAXIMUM_SPEED);
		if (isInteger(cpuMaxSpeed)) {
			Integer cpuMaxSpeedAsInt = Integer.parseInt(cpuMaxSpeed);
			if (cpuMaxSpeedAsInt < 1000) {
				cpuMaxSpeed = String.format("%d MHz", cpuMaxSpeedAsInt);
			} else {
				cpuMaxSpeed = String.format("%.2f GHz", (cpuMaxSpeedAsInt / 1000D));
			}
		}

		// Build the name
		return buildName(

				// Type
				"",

				// Name
				metadata.get(HardwareConstants.DISPLAY_ID),
				metadata.get(HardwareConstants.DEVICE_ID), 
				metadata.get(HardwareConstants.ID_COUNT), 
				"cpu|processor|proc",

				// Additional label
				metadata.get(HardwareConstants.VENDOR), 
				metadata.get(HardwareConstants.MODEL), 
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
				"",

				// Name
				metadata.get(HardwareConstants.DISPLAY_ID),
				metadata.get(HardwareConstants.DEVICE_ID), 
				metadata.get(HardwareConstants.ID_COUNT), 
				"cpu|processor|core|proc"
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
				metadata.get(HardwareConstants.DISPLAY_ID),
				metadata.get(HardwareConstants.DISK_CONTROLLER_NUMBER), 
				metadata.get(HardwareConstants.ID_COUNT), 
				"",

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
		
		// Build the name
		return buildName(

				// Type
				enclosureType,

				// Name
				enclosureDisplayId,
				metadata.get(HardwareConstants.DEVICE_ID), 
				metadata.get(HardwareConstants.ID_COUNT), 
				"",

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
				"",

				// Name
				metadata.get(HardwareConstants.DISPLAY_ID), 
				metadata.get(HardwareConstants.DEVICE_ID),
				metadata.get(HardwareConstants.ID_COUNT), 
				"fan",

				// Additional label
				metadata.get(HardwareConstants.FAN_TYPE) 
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
		String ledColor = metadata.get(HardwareConstants.COLOR);
		if (!isBlankDataValue(ledColor)) {
			ledColor = ledColor.substring(0, 1).toUpperCase() + ledColor.substring(1).toLowerCase();
		}
		
		// Build the name
		return buildName(

				// Type
				"",

				// Name
				metadata.get(HardwareConstants.DISPLAY_ID), 
				metadata.get(HardwareConstants.DEVICE_ID),
				metadata.get(HardwareConstants.ID_COUNT), 
				"led",

				// Additional label
				ledColor, 
				metadata.get(HardwareConstants.NAME) 
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
		String logicalDiskRaidLevel = metadata.get(HardwareConstants.RAID_LEVEL);
		if (!isBlankDataValue(logicalDiskRaidLevel) && isInteger(logicalDiskRaidLevel)) {
			logicalDiskRaidLevel = "RAID " + logicalDiskRaidLevel;
		}
		
		// Build the name
		return buildName(

				// Type
				metadata.get(HardwareConstants.TYPE),

				// Name
				metadata.get(HardwareConstants.DISPLAY_ID), 
				metadata.get(HardwareConstants.DEVICE_ID),
				metadata.get(HardwareConstants.ID_COUNT), 
				"disk|drive|logical",

				// Additional label
				logicalDiskRaidLevel, 
				humanReadableByteCountBin(metadata.get(HardwareConstants.SIZE))
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
				"",

				// Name
				metadata.get(HardwareConstants.DISPLAY_ID), 
				metadata.get(HardwareConstants.DEVICE_ID),
				metadata.get(HardwareConstants.ID_COUNT), 
				"lun",

				// Additional label
				metadata.get(HardwareConstants.LOCAL_DEVICE_NAME), 
				metadata.get(HardwareConstants.REMOTE_DEVICE_NAME) 
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

		// Build the name
		return buildName(

				// Type
				"",

				// Name
				metadata.get(HardwareConstants.DISPLAY_ID), 
				metadata.get(HardwareConstants.DEVICE_ID),
				metadata.get(HardwareConstants.ID_COUNT), 
				"memory|module",

				// Additional label
				metadata.get(HardwareConstants.VENDOR), 
				metadata.get(HardwareConstants.TYPE),
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
		final String unwantedWords = "network|ndis|client|server|adapter|ethernet|interface|controller|miniport|scheduler|packet|connection|multifunction|(1([0]+[/]*))*(base[\\-tx]*)*";
		String networkCardVendor = metadata.get(HardwareConstants.VENDOR);
		if (!isBlankDataValue(networkCardVendor)) {
			networkCardVendor = trimKnownWords(networkCardVendor, unwantedWords);
		}
		
		// Network card model without unwanted words and up to 30 characters
		String networkCardModel = metadata.get(HardwareConstants.MODEL);
		if (!isBlankDataValue(networkCardModel)) {
			networkCardModel = trimKnownWords(networkCardModel, unwantedWords);
			if (networkCardModel.length() > 30) {
				networkCardModel = networkCardModel.substring(0, 30);
			}
		}
		
		// Build the name
		return buildName(

				// Type
				"",

				// Name
				metadata.get(HardwareConstants.DISPLAY_ID), 
				metadata.get(HardwareConstants.DEVICE_ID),
				metadata.get(HardwareConstants.ID_COUNT), 
				"network",

				// Additional label
				metadata.get(HardwareConstants.TYPE),
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
				metadata.get(HardwareConstants.TYPE), 

				// Name
				metadata.get(HardwareConstants.DISPLAY_ID), 
				metadata.get(HardwareConstants.DEVICE_ID),
				metadata.get(HardwareConstants.ID_COUNT), 
				"",

				// Additional label
				metadata.get(HardwareConstants.ADDITIONAL_LABEL)
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
				metadata.get(HardwareConstants.TYPE),

				// Name
				metadata.get(HardwareConstants.DISPLAY_ID), 
				metadata.get(HardwareConstants.DEVICE_ID),
				metadata.get(HardwareConstants.ID_COUNT), 
				"disk|drive",

				// Additional label
				metadata.get(HardwareConstants.VENDOR),
				humanReadableByteCountSI(metadata.get(HardwareConstants.SIZE)) 
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
		String powerSupplyPower = metadata.get(HardwareConstants.POWER_SUPPLY_POWER);
		if (!isBlankDataValue(powerSupplyPower)) {
			powerSupplyPower = powerSupplyPower + " W";
		}
		
		// Build the name
		return buildName(

				// Type
				"",

				// Name
				metadata.get(HardwareConstants.DISPLAY_ID), 
				metadata.get(HardwareConstants.DEVICE_ID),
				metadata.get(HardwareConstants.ID_COUNT), 
				"power|supply",

				// Additional label
				metadata.get(HardwareConstants.POWER_SUPPLY_TYPE),
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
				"",

				// Name
				metadata.get(HardwareConstants.DISPLAY_ID), 
				metadata.get(HardwareConstants.DEVICE_ID),
				metadata.get(HardwareConstants.ID_COUNT), 
				"robotics",

				// Additional label
				joinVendorAndModel(metadata), 
				metadata.get(HardwareConstants.ROBOTIC_TYPE)
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
				"",

				// Name
				metadata.get(HardwareConstants.DISPLAY_ID), 
				metadata.get(HardwareConstants.DEVICE_ID),
				metadata.get(HardwareConstants.ID_COUNT), 
				"tape|drive",

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
				"",

				// Name
				metadata.get(HardwareConstants.DISPLAY_ID), 
				metadata.get(HardwareConstants.DEVICE_ID),
				metadata.get(HardwareConstants.ID_COUNT), 
				"temperature|temp|sensor",

				// Additional label
				metadata.get(HardwareConstants.TEMPERATURE_TYPE)
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
				"",

				// Name
				metadata.get(HardwareConstants.DISPLAY_ID), 
				metadata.get(HardwareConstants.DEVICE_ID),
				metadata.get(HardwareConstants.ID_COUNT), 
				"voltage",

				// Additional label
				metadata.get(HardwareConstants.VOLTAGE_TYPE)
		);
	}
	
}
