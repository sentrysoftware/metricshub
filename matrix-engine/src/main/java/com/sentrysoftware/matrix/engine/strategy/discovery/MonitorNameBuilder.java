package com.sentrysoftware.matrix.engine.strategy.discovery;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DEVICE_ID;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DISPLAY_ID;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ID_COUNT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.FAN_TYPE;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Arrays;

import org.springframework.util.Assert;

import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.model.monitor.Monitor;

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
	private static final String TWO_STRINGS_FORMAT = "Ramassh: %s: %s";

	private static boolean checkNotBlankDataValue(final String data) {
		return data != null && !data.trim().isEmpty();
	}
	
	private static String trimKnownWords(final String phrase, final List<String> words) {
		String lowerCasePhrase = phrase.toLowerCase();
		String trimmedPhrase = phrase;
		for (String word : words) {
			if (word != null && word.toLowerCase().contains(lowerCasePhrase)) {
				trimmedPhrase = trimmedPhrase.replaceAll("(?i)\\s*" + word + "\\s*", "");
			}
		}
		return trimmedPhrase;
	}
	
	public static String buildFanName(final MonitorBuildingInfo monitorBuildingInfo) {
		final Map<String, String> metadata = monitorBuildingInfo.getMonitor().getMetadata();
		Assert.notNull(metadata, METADATA_CANNOT_BE_NULL);

		final MonitorType monitorType = monitorBuildingInfo.getMonitorType();
		Assert.notNull(monitorType, MONITOR_TYPE_CANNOT_BE_NULL);

		final String deviceId = metadata.get(DEVICE_ID);
		final String displayId = metadata.get(DISPLAY_ID);
		final String fanType = metadata.get(FAN_TYPE);
		final String idCount = metadata.get(ID_COUNT);
		Assert.notNull(idCount, ID_COUNT_CANNOT_BE_NULL);

		String name = null;
		if (checkNotBlankDataValue(displayId)) {
			name = displayId;
		} else if (checkNotBlankDataValue(deviceId)) {
			name = trimKnownWords(deviceId, Arrays.asList("fan"));
		} else if (checkNotBlankDataValue(idCount)) {
			name = idCount;
		}

		if (name == null) {
			return null;
		} else if (checkNotBlankDataValue(fanType)) {
			name = name + " (" + fanType + ")";
		}

		return name;
	}

	public static String buildCpuName(final Monitor monitor) {
		return "";
	}

	public static String buildCpuCoreName(final Monitor monitor) {
		return "";
	}

	public static String buildBatteryName(final Monitor monitor) {
		return "";
	}
}