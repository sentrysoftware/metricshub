package com.sentrysoftware.matrix.converter.state;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConversionHelper {

	private static final String HDF_BATTERY = "battery";
	private static final String HDF_BLADE = "blade";
	private static final String HDF_CPU = "cpu";
	private static final String HDF_CPU_CORE = "cpucore";
	private static final String HDF_DISK_CONTROLLER = "diskcontroller";
	private static final String HDF_ENCLOSURE = "enclosure";
	private static final String HDF_FAN = "fan";
	private static final String HDF_GPU = "gpu";
	private static final String HDF_LED = "led";
	private static final String HDF_LOGICAL_DISK = "logicaldisk";
	private static final String HDF_LUN = "lun";
	private static final String HDF_MEMORY = "memory";
	private static final String HDF_NETWORK_CARD = "networkcard";
	private static final String HDF_OTHER_DEVICE = "otherdevice";
	private static final String HDF_PHYSICAL_DISK = "physicaldisk";
	private static final String HDF_POWER_SUPPLY = "powersupply";
	private static final String HDF_ROBOTIC = "robotic";
	private static final String HDF_TAPEDRIVE = "tapedrive";
	private static final String HDF_TEMPERATURE = "temperature";
	private static final String HDF_VM = "vm";
	private static final String HDF_VOLTAGE = "voltage";

	public static final String YAML_BATTERY = HDF_BATTERY;
	public static final String YAML_BLADE = HDF_BLADE;
	public static final String YAML_CPU = HDF_CPU;
	public static final String YAML_CPU_CORE = "cpu_core";
	public static final String YAML_DISK_CONTROLLER = "disk_controller";
	public static final String YAML_ENCLOSURE = HDF_ENCLOSURE;
	public static final String YAML_FAN = HDF_FAN;
	public static final String YAML_GPU = HDF_GPU;
	public static final String YAML_LED = HDF_LED;
	public static final String YAML_LOGICAL_DISK = "logical_disk";
	public static final String YAML_LUN = HDF_LUN;
	public static final String YAML_MEMORY = HDF_MEMORY;
	public static final String YAML_NETWORK = "network";
	public static final String YAML_OTHER_DEVICE = "other_device";
	public static final String YAML_PHYSICAL_DISK = "physical_disk";
	public static final String YAML_POWER_SUPPLY = "power_supply";
	public static final String YAML_ROBOTICS = "robotics";
	public static final String YAML_TAPEDRIVE = "tape_drive";
	public static final String YAML_TEMPERATURE = HDF_TEMPERATURE;
	public static final String YAML_VM = HDF_VM;
	public static final String YAML_VOLTAGE = HDF_VOLTAGE;

	/**
	 * HDF Device name to YAML connector Monitor name
	 */
	public static final Map<String, String> HDF_TO_YAML_MONITOR_NAME;
	static {
		Map<String, String> hdfToYamlMonitor = new HashMap<>();
		hdfToYamlMonitor.put(HDF_BATTERY, YAML_BATTERY);
		hdfToYamlMonitor.put(HDF_BLADE, YAML_BLADE);
		hdfToYamlMonitor.put(HDF_CPU, YAML_CPU);
		hdfToYamlMonitor.put(HDF_CPU_CORE, YAML_CPU_CORE);
		hdfToYamlMonitor.put(HDF_DISK_CONTROLLER, YAML_DISK_CONTROLLER);
		hdfToYamlMonitor.put(HDF_ENCLOSURE, YAML_ENCLOSURE);
		hdfToYamlMonitor.put(HDF_FAN, YAML_FAN);
		hdfToYamlMonitor.put(HDF_GPU, YAML_GPU);
		hdfToYamlMonitor.put(HDF_LED, YAML_LED);
		hdfToYamlMonitor.put(HDF_LOGICAL_DISK, YAML_LOGICAL_DISK);
		hdfToYamlMonitor.put(HDF_LUN, YAML_LUN);
		hdfToYamlMonitor.put(HDF_MEMORY, YAML_MEMORY);
		hdfToYamlMonitor.put(HDF_NETWORK_CARD, YAML_NETWORK);
		hdfToYamlMonitor.put(HDF_OTHER_DEVICE, YAML_OTHER_DEVICE);
		hdfToYamlMonitor.put(HDF_PHYSICAL_DISK, YAML_PHYSICAL_DISK);
		hdfToYamlMonitor.put(HDF_POWER_SUPPLY, YAML_POWER_SUPPLY);
		hdfToYamlMonitor.put(HDF_ROBOTIC, YAML_ROBOTICS);
		hdfToYamlMonitor.put(HDF_TAPEDRIVE, YAML_TAPEDRIVE);
		hdfToYamlMonitor.put(HDF_TEMPERATURE, YAML_TEMPERATURE);
		hdfToYamlMonitor.put(HDF_VM, YAML_VM);
		hdfToYamlMonitor.put(HDF_VOLTAGE, YAML_VOLTAGE);

		HDF_TO_YAML_MONITOR_NAME = Collections.unmodifiableMap(hdfToYamlMonitor);

	}

	/**
	 * A compiled representation of the HDF source reference regular expression.
	 * We attempt to match input like %Enclosure.Discovery.Source(2)%
	 */
	public static final Pattern SOURCE_REF_PATTERN = Pattern.compile(
		"%\\s*(\\w+)\\.(discovery|collect)\\.(source\\(\\d+\\))\\s*%",
		Pattern.CASE_INSENSITIVE | Pattern.MULTILINE 
	);

	/**
	 * A compiled representation of the HDF entry reference regular expression.
	 * We attempt to match input like %Entry.Column(2)%
	 */
	private static final Pattern SOURCE_ENTRY_PATTERN = Pattern.compile(
		"%\\s*(entry)\\.(column\\(\\d+\\))\\s*%",
		Pattern.CASE_INSENSITIVE | Pattern.MULTILINE 
	);

	/**
	 * A compile representation of the HDF instance table reference regular expression.
	 * We attempt to match input like "InstanceTable.Column(2)"
	 */
	private static final Pattern INSTANCE_REF_PATTERN = Pattern.compile(
		"(instancetable)\\.(column\\(\\d+\\))",
		Pattern.CASE_INSENSITIVE
	);


	/**
	 * List of pattern function converters
	 */
	private static final List<PatternFunctionConverter> PATTERN_FUNCTION_CONVERTERS = List.of(
		new PatternFunctionConverter(SOURCE_REF_PATTERN, ConversionHelper::convertSourceReference),
		new PatternFunctionConverter(SOURCE_ENTRY_PATTERN, ConversionHelper::convertEntryReference),
		new PatternFunctionConverter(INSTANCE_REF_PATTERN, ConversionHelper::convertInstanceReference)
	);

	/**
	 * Perform value conversions
	 * 
	 * @param input
	 * @return updated string value
	 */
	public static String performValueConversions(String input) {

		// Loop over the pattern functions
		for (final PatternFunctionConverter patternFunction : PATTERN_FUNCTION_CONVERTERS) {
			// Get the defined pattern and creates a matcher that will match the given input against this pattern.
			final Matcher matcher = patternFunction.getPattern().matcher(input);

			// Get the converter function
			final BiFunction<Matcher, String, String> converter = patternFunction.getConverter();

			while (matcher.find()) {
				// Convert the input value
				input = converter.apply(matcher, input);
			}

		}

		return input;
	}

	/**
	 * Convert source reference. E.g.
	 * <b><u>%Enclosure.Discovery.Source(2)%</u></b> becomes
	 * <b><u>$monitors.enclosure.discovery.sources.source(2)$</u></b>
	 * 
	 * @param matcher matcher used to find groups
	 * @param input   input value to be replaced
	 * @return updated string value
	 */
	private static String convertSourceReference(final Matcher matcher, final String input) {
		final String monitor = getYamlMonitorName(matcher.group(1));
		final String job = matcher.group(2).toLowerCase();
		final String source = matcher.group(3).toLowerCase();

		return input.replace(
			matcher.group(),
			String.format("$monitors.%s.%s.sources.%s$", monitor, job, source)
		);
	}

	/**
	 * Convert entry reference. E.g.
	 * <b><u>%Entry.Column(2)%</u></b> becomes
	 * <b><u>$entry.column(2)$</u></b>
	 * 
	 * @param matcher matcher used to find groups
	 * @param input   input value to be replaced
	 * @return updated string value
	 */
	private static String convertEntryReference(final Matcher matcher, final String input) {
		final String entry = matcher.group(1).toLowerCase();
		final String column = matcher.group(2).toLowerCase();
		return input.replace(
			matcher.group(),
			String.format("$%s.%s$", entry, column)
		);
	}

	private static String convertInstanceReference(final Matcher matcher, final String input) {
		final String column = matcher.group(2).toLowerCase();
		return String.format("$%s", column);
	}

	/**
	 * Build a source key regex
	 * 
	 * @param regex Keyword or regular expression used to build the final regex
	 * @return String value
	 */
	public static String buildSourceKeyRegex(final String regex) {
		return String.format("^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\.%s\\s*$", regex);
	}

	/**
	 * Build a criteria key regex
	 * 
	 * @param regex Keyword or regular expression used to build the final regex
	 * @return String value
	 */
	public static String buildCriteriaKeyRegex(final String regex) {
		return String.format("^\\s*detection\\.criteria\\(([1-9]\\d*)\\)\\.%s\\s*$", regex);
	}

	/**
	 * Build a compute key regex
	 * 
	 * @param regex Keyword or regular expression used to build the final regex
	 * @return String value
	 */
	public static String buildComputeKeyRegex(final String regex) {
		return String.format("^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\))\\.compute\\(([1-9]\\d*)\\)\\.%s\\s*$", regex);
	}

	/**
	* Get the corresponding YAML monitor name for the given HDF monitor name
	 * 
	 * @param monitorName
	 * @return String value
	 */
	public static String getYamlMonitorName(final String hdfMonitorName) {
		final String result = HDF_TO_YAML_MONITOR_NAME.get(hdfMonitorName.toLowerCase().trim());
		if (result == null) {
			throw new IllegalStateException(String.format("Could not find corresponding Monitor name for the HDF device name '%s'", hdfMonitorName));
		}
		return result;
	}

	@AllArgsConstructor
	static class PatternFunctionConverter {
		@Getter
		private Pattern pattern;
		@Getter
		private BiFunction<Matcher, String, String> converter;
	}
}
