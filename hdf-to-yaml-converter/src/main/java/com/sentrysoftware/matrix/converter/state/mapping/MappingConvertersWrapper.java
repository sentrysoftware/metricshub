package com.sentrysoftware.matrix.converter.state.mapping;

import static com.sentrysoftware.matrix.converter.ConverterConstants.ATTRIBUTES;
import static com.sentrysoftware.matrix.converter.ConverterConstants.DISCOVERY;
import static com.sentrysoftware.matrix.converter.ConverterConstants.MAPPING;
import static com.sentrysoftware.matrix.converter.ConverterConstants.MONITORS;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_BATTERY;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_BLADE;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_CPU;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_CPU_CORE;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_DISK_CONTROLLER;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_ENCLOSURE;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_FAN;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_GPU;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_LED;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_LOGICAL_DISK;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_LUN;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_MEMORY;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_NETWORK;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_OTHER_DEVICE;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_PHYSICAL_DISK;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_POWER_SUPPLY;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_ROBOTICS;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_TAPEDRIVE;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_TEMPERATURE;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_VM;
import static com.sentrysoftware.matrix.converter.ConverterConstants.YAML_VOLTAGE;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class MappingConvertersWrapper {

	/**
	 * Remove me
	 */
	private static final IMappingConverter NOOP = new NoopConverter();

	/**
	 * Map of mapping converters
	 */
	private static final Map<String, IMappingConverter> DEFAULT_CONVERTERS = Map.ofEntries(
		Map.entry(YAML_BATTERY, new BatteryConverter()),
		Map.entry(YAML_BLADE, new BladeConverter()),
		Map.entry(YAML_CPU, new CpuConverter()),
		Map.entry(YAML_CPU_CORE, NOOP),
		Map.entry(YAML_DISK_CONTROLLER, NOOP),
		Map.entry(YAML_ENCLOSURE, new EnclosureConverter()),
		Map.entry(YAML_FAN, NOOP),
		Map.entry(YAML_GPU, NOOP),
		Map.entry(YAML_LED, new LedConverter()),
		Map.entry(YAML_LOGICAL_DISK, new LogicalDiskConverter()),
		Map.entry(YAML_LUN, NOOP),
		Map.entry(YAML_MEMORY, new MemoryConverter()),
		Map.entry(YAML_NETWORK, NOOP),
		Map.entry(YAML_OTHER_DEVICE, NOOP),
		Map.entry(YAML_PHYSICAL_DISK, NOOP),
		Map.entry(YAML_POWER_SUPPLY, NOOP),
		Map.entry(YAML_ROBOTICS, NOOP),
		Map.entry(YAML_TAPEDRIVE, NOOP),
		Map.entry(YAML_TEMPERATURE, NOOP),
		Map.entry(YAML_VM, NOOP),
		Map.entry(YAML_VOLTAGE, NOOP)
	);

	private Map<String, IMappingConverter> converters;

	public MappingConvertersWrapper() {
		converters = DEFAULT_CONVERTERS;
	}

	/**
	 * Convert the HDF parameter activation key-value pair and set it under the <em>conditionalCollection</em> section
	 * 
	 * @param key                   HDF parameter activation key to be converted.
	 * @param value                 The value to be set in a new {@link TextNode}.<br>
	 *                              Depending on the conversion specifications this value may change.
	 * @param monitorType           The type of the monitor device E.g. temperature, battery, enclosure...etc.
	 * @param conditionalCollection The node on which we want to set the key-value pair
	 */
	public void convertParameterActivation(
		final String key,
		final String value,
		final String monitorType,
		final JsonNode conditionalCollection
	) {
		getConverterForMonitorType(monitorType)
			.convertCollectProperty(key.replace("parameteractivation.", ""), value, conditionalCollection);
	}

	/**
	 * Get the converter for the given monitor type
	 * 
	 * @param monitorType The type of the monitor used to get the corresponding converter
	 * @return {@link IMappingConverter} instance
	 */
	public IMappingConverter getConverterForMonitorType(final String monitorType) {
		return converters.get(monitorType);
	}

	/**
	 * Convert HDF parameter key-value pair and set it under the <em>metrics</em> section
	 * 
	 * @param key         HDF parameter key to be converted.
	 * @param value       The value to be set in a new {@link TextNode}.<br>
	 *                    Depending on the conversion specifications this value may change.
	 * @param monitorType The type of the monitor device E.g. temperature, battery, enclosure...etc.
	 * @param  metrics    The node on which we want to set the key-value pair
	 */
	public void convertCollectProperty(
		final String key,
		final String value,
		final String monitorType,
		final JsonNode metrics
	) {
		getConverterForMonitorType(monitorType).convertCollectProperty(key, value, metrics);
	}

	/**
	 * Post conversion of the HDF discovery properties to YAML discovery properties
	 * 
	 * @param connector The hardware connector object node
	 */
	public void postConvertDiscovery(final JsonNode connector) {
		final JsonNode monitors = connector.get(MONITORS);
		if (monitors != null) {
			final Iterator<Entry<String, JsonNode>> monitorsIter = monitors.fields();
			while (monitorsIter.hasNext()) {
				final Entry<String, JsonNode> monitorEntry = monitorsIter.next();
				final JsonNode job = monitorEntry.getValue();
				if (job != null) {
					final JsonNode discovery = job.get(DISCOVERY);
					if (discovery != null) {
						postConvertDiscovery(monitorEntry.getKey(), discovery);
					}
				}
			}
		}
	}

	/**
	 * Discovery attributes post conversion
	 * 
	 * @param monitorType The type of the monitor we wish to get its converter
	 * @param discovery   The discovery job we wish to get its mapping section
	 */
	private void postConvertDiscovery(final String monitorType, final JsonNode discovery) {
		final JsonNode mapping = discovery.get(MAPPING);
		if (mapping != null && mapping.get(ATTRIBUTES) != null) {
			getConverterForMonitorType(monitorType).postConvertDiscoveryProperties(mapping);
		}
	}

	/**
	 * Get the mapping converter for the given monitor name
	 * 
	 * @param monitorName The YAML monitor name
	 * @return {@link IMappingConverter} instance
	 */
	public IMappingConverter getConverter(final String monitorName) {
		return converters.get(monitorName);
	}
}
