package com.sentrysoftware.matrix.converter.state.mapping;

import static com.sentrysoftware.matrix.converter.state.ConversionHelper.YAML_BATTERY;
import static com.sentrysoftware.matrix.converter.state.ConversionHelper.YAML_BLADE;
import static com.sentrysoftware.matrix.converter.state.ConversionHelper.YAML_CPU;
import static com.sentrysoftware.matrix.converter.state.ConversionHelper.YAML_CPU_CORE;
import static com.sentrysoftware.matrix.converter.state.ConversionHelper.YAML_DISK_CONTROLLER;
import static com.sentrysoftware.matrix.converter.state.ConversionHelper.YAML_ENCLOSURE;
import static com.sentrysoftware.matrix.converter.state.ConversionHelper.YAML_FAN;
import static com.sentrysoftware.matrix.converter.state.ConversionHelper.YAML_GPU;
import static com.sentrysoftware.matrix.converter.state.ConversionHelper.YAML_LED;
import static com.sentrysoftware.matrix.converter.state.ConversionHelper.YAML_LOGICAL_DISK;
import static com.sentrysoftware.matrix.converter.state.ConversionHelper.YAML_LUN;
import static com.sentrysoftware.matrix.converter.state.ConversionHelper.YAML_MEMORY;
import static com.sentrysoftware.matrix.converter.state.ConversionHelper.YAML_NETWORK;
import static com.sentrysoftware.matrix.converter.state.ConversionHelper.YAML_OTHER_DEVICE;
import static com.sentrysoftware.matrix.converter.state.ConversionHelper.YAML_PHYSICAL_DISK;
import static com.sentrysoftware.matrix.converter.state.ConversionHelper.YAML_POWER_SUPPLY;
import static com.sentrysoftware.matrix.converter.state.ConversionHelper.YAML_ROBOTICS;
import static com.sentrysoftware.matrix.converter.state.ConversionHelper.YAML_TAPEDRIVE;
import static com.sentrysoftware.matrix.converter.state.ConversionHelper.YAML_TEMPERATURE;
import static com.sentrysoftware.matrix.converter.state.ConversionHelper.YAML_VM;
import static com.sentrysoftware.matrix.converter.state.ConversionHelper.YAML_VOLTAGE;

import java.util.Map;

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
	public static final Map<String, IMappingConverter> DEFAULT_CONVERTERS = Map.ofEntries(
			Map.entry(YAML_BATTERY, NOOP),
			Map.entry(YAML_BLADE, NOOP),
			Map.entry(YAML_CPU, NOOP),
			Map.entry(YAML_CPU_CORE, NOOP),
			Map.entry(YAML_DISK_CONTROLLER, NOOP),
			Map.entry(YAML_ENCLOSURE, NOOP),
			Map.entry(YAML_FAN, NOOP),
			Map.entry(YAML_GPU, NOOP),
			Map.entry(YAML_LED, NOOP),
			Map.entry(YAML_LOGICAL_DISK, NOOP),
			Map.entry(YAML_LUN, NOOP),
			Map.entry(YAML_MEMORY, NOOP),
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
	 * @param key                   HDF parameter activation key
	 * @param value                 The value to set in the new {@link TextNode}
	 * @param monitorType           The type of the monitor device E.g. temperature, battery, enclosure...etc.
	 * @param conditionalCollection The node on which we want to set the key-value pair
	 */
	public void convertParameterActivation(
		final String key,
		final String value,
		final String monitorType,
		final JsonNode conditionalCollection
	) {
		// TODO implement
	}

	/**
	 * Get the converter for the given monitor type
	 * 
	 * @param monitorType The type of the monitor
	 * @return {@link IMappingConverter} instance
	 */
	public IMappingConverter getConverterForMonitorType(final String monitorType) {
		// TODO implement
		return NOOP;
	}

	/**
	 * Convert HDF parameter key-value pair and set it under the <em>metrics</em> section
	 * 
	 * @param key         HDF parameter key
	 * @param value       The value to set in the new {@link TextNode}
	 * @param monitorType The type of the monitor device E.g. temperature, battery, enclosure...etc.
	 * @param  metrics    The node on which we want to set the key-value pair
	 */
	public void convertCollectProperty(
		final String key,
		final String value,
		final String monitorType,
		final JsonNode metrics
	) {
		// TODO implement
	}

	/**
	 * Post conversion of the HDF discovery properties to YAML discovery properties
	 * 
	 * @param connector The hardware connector object node
	 */
	public void postConvertDiscovery(final JsonNode connector) {
		// TODO implement
	}

}
