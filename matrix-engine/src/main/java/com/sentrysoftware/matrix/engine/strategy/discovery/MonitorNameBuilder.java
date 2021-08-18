package com.sentrysoftware.matrix.engine.strategy.discovery;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;

import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.LinkedList;

import org.springframework.util.Assert;

public class MonitorNameBuilder {

	private MonitorNameBuilder() {	}
	
	private static final String UNKNOWN_COMPUTER = "Unknown computer";
	private static final String HP_TRU64_COMPUTER = "HP Tru64 computer";
	private static final String LINUX_COMPUTER = "Linux computer";
	private static final String HP_OPEN_VMS_COMPUTER = "HP Open-VMS computer";
	private static final String WINDOWS_COMPUTER = "Windows computer";
	private static final String LOCALHOST_ENCLOSURE = "Localhost Enclosure";
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
	private static final String TWO_STRINGS_FORMAT = "%s: %s";

	private static boolean checkNotBlankDataValue(final String data) {
		return data != null && !data.trim().isEmpty();
	}

	/**
	 * Trims known/given words off the phrase to return only the unknown part of the
	 * content
	 * 
	 * @param phrase {@link String} phrase to be trimmed
	 * @param words  {@link List} of words (case-insensitive) to be trimmed off the
	 *               phrase
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
	 * Trims known/given words off the phrase to return only the unknown part of the
	 * content
	 * 
	 * @param phrase {@link String} phrase to be trimmed
	 * @param words  {@link List} of words (case-insensitive) to be trimmed off the
	 *               phrase
	 * 
	 * @return {@link String} trimmedPhrase Trimmed phrase
	 */
	private static String trimUnwantedCharacters(final String name) {

		// Trim any comma
		String trimmedName = name.replaceAll(",", "");

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
		StringBuilder buffer = new StringBuilder();
		String nextSeparator = "";
		for (String word: words)
		{
			if (checkNotBlankDataValue(word)) {
				buffer.append(nextSeparator);
				nextSeparator = separator;
				buffer.append(word);
			}
		}
		
		return buffer.toString();
	}

	/**
	 * Build a generic name common to all hardware devices
	 * 
	 * @param deviceId       {@link String} containing the device ID
	 * @param displayId      {@link String} containing the display ID
	 * @param idCount        {@link String} containing the ID count
	 * @param trimmableWords {@link List} of words (case-insensitive) to be trimmed
	 *                       off the device ID
	 * 
	 * @return {@link String} name Generic label based on the inputs
	 */
	public static String buildGenericName(final String deviceId, final String displayId, final String idCount,
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

		// Use the ID count as name, if we couldn't build one from display ID or device
		// ID
		if (!checkNotBlankDataValue(name)) {
			return idCount;
		}

		return name;
	}

	/**
	 * Build the fan name based on the current implementation in Hardware Sentry KM
	 * @param monitorBuildingInfo   {@link MonitorBuildingInfo} of the monitor instance 
	 * 
	 * @return {@link String} name  Label of the fan to be displayed
	 */
	public static String buildFanName(final MonitorBuildingInfo monitorBuildingInfo) {
		
		// Check the metadata
		final Map<String, String> metadata = monitorBuildingInfo.getMonitor().getMetadata();
		Assert.notNull(metadata, METADATA_CANNOT_BE_NULL);

		// Build the generic name
		String name = buildGenericName(metadata.get(HardwareConstants.DEVICE_ID),
				metadata.get(HardwareConstants.DISPLAY_ID), metadata.get(HardwareConstants.ID_COUNT),
				List.of("fan"));

		// Get any additional info to be included in the label
		final String fanType = metadata.get(HardwareConstants.FAN_TYPE);
		if (checkNotBlankDataValue(fanType)) {
			name = name + " (" + fanType + ")";
		} 

		return trimUnwantedCharacters(name);
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
		String name = buildGenericName(metadata.get(HardwareConstants.DEVICE_ID),
				metadata.get(HardwareConstants.DISPLAY_ID), metadata.get(HardwareConstants.ID_COUNT),
				List.of("cpu", "processor", "proc"));
		
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

		// Prepare the additional info to be included in the label
		String additionalInfo = joinWords(new String[] {
				metadata.get(HardwareConstants.VENDOR), 
				metadata.get(HardwareConstants.MODEL), cpuMaxSpeed}, " - ");
		
		if (checkNotBlankDataValue(additionalInfo)) {
			name = name + " (" + additionalInfo + ")";
		}

		return trimUnwantedCharacters(name);
	}

	/**
	 * Build the CPU core name based on the current implementation in Hardware Sentry KM
	 * @param monitorBuildingInfo   {@link MonitorBuildingInfo} of the monitor instance 
	 * 
	 * @return {@link String} name  Label of the CPU core to be displayed
	 */
	public static String buildCpuCoreName(final MonitorBuildingInfo monitorBuildingInfo) {
		
		// Check the metadata
		final Map<String, String> metadata = monitorBuildingInfo.getMonitor().getMetadata();
		Assert.notNull(metadata, METADATA_CANNOT_BE_NULL);

		// Build the generic name
		String name = buildGenericName(metadata.get(HardwareConstants.DEVICE_ID),
				metadata.get(HardwareConstants.DISPLAY_ID), metadata.get(HardwareConstants.ID_COUNT),
				List.of("cpu", "processor", "core", "proc"));
		
		return trimUnwantedCharacters(name);
	}
	

	/**
	 * Build the battery name based on the current implementation in Hardware Sentry KM
	 * @param monitorBuildingInfo   {@link MonitorBuildingInfo} of the monitor instance 
	 * 
	 * @return {@link String} name  Label of the battery to be displayed
	 */
	public static String buildBatteryName(final MonitorBuildingInfo monitorBuildingInfo) {
		
		// Check the metadata
		final Map<String, String> metadata = monitorBuildingInfo.getMonitor().getMetadata();
		Assert.notNull(metadata, METADATA_CANNOT_BE_NULL);

		// Build the generic name
		String name = buildGenericName(metadata.get(HardwareConstants.DEVICE_ID),
				metadata.get(HardwareConstants.DISPLAY_ID), metadata.get(HardwareConstants.ID_COUNT),
				List.of("battery"));
		
		// Prepare the additional info to be included in the label
		String additionalInfo = joinWords(new String[] {
				metadata.get(HardwareConstants.VENDOR), 
				metadata.get(HardwareConstants.MODEL)}, " ");
		additionalInfo = joinWords(new String[] {additionalInfo, metadata.get(HardwareConstants.TYPE)}, " - ");
		
		if (checkNotBlankDataValue(additionalInfo)) {
			name = name + " (" + additionalInfo + ")";
		}
		
		return trimUnwantedCharacters(name);
	}
	

}