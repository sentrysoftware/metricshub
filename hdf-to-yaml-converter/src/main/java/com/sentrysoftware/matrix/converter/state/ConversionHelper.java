package com.sentrysoftware.matrix.converter.state;

import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_BATTERY;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_BLADE;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_CPU;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_CPU_CORE;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_DISK_CONTROLLER;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_ENCLOSURE;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_FAN;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_GPU;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_LED;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_LOGICAL_DISK;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_LUN;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_MEMORY;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_NETWORK_CARD;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_OTHER_DEVICE;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_PHYSICAL_DISK;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_POWER_SUPPLY;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_ROBOTIC;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_TAPEDRIVE;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_TEMPERATURE;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_VM;
import static com.sentrysoftware.matrix.converter.ConverterConstants.HDF_VOLTAGE;
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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ConversionHelper {

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
		"%\\s*entry\\.column\\((\\d+)\\)\\s*%",
		Pattern.CASE_INSENSITIVE | Pattern.MULTILINE 
	);

	/**
	 * A compiled representation of the HDF column reference regular expression.
	 * We attempt to match input like Column(2)
	 */
	private static final Pattern COLUMN_REF_PATTERN = Pattern.compile(
		"^column\\((\\d+)\\)$",
		Pattern.CASE_INSENSITIVE | Pattern.MULTILINE 
	);

	/**
	 * A compiled representation of the HDF instance table reference regular expression.
	 * We attempt to match input like "InstanceTable.Column(2)" or "ValueTable.Column(2)"
	 */
	private static final Pattern INSTANCE_REF_PATTERN = Pattern.compile(
		"(instancetable|valuetable)\\.column\\((\\d+)\\)",
		Pattern.CASE_INSENSITIVE
	);

	/**
	 * A compiled representation of an EmbeddedFile reference regular expression.
	 * We attempt to match input like "%EmbeddedFile(1)%"
	 */
	private static final Pattern EMBEDDED_FILE_PATTERN = Pattern.compile(
		"%EmbeddedFile\\((\\d+)\\)%",
		Pattern.CASE_INSENSITIVE
	);

	/**
	 * A compiled representation of an EmbeddedFile reference regular expression.
	 * We attempt to match input like "EmbeddedFile(1)"
	 */
	private static final Pattern EMBEDDED_FILE_NO_PERCENT_PATTERN = Pattern.compile(
		"^\\s*EmbeddedFile\\((\\d+)\\)\\s*$",
		Pattern.CASE_INSENSITIVE
	);

	/**
	 * A compiled representation of a mono instance id reference regular expression.
	 * We attempt to match input like "%NetworkCard.Collect.DeviceID%"
	 */
	private static final Pattern MONO_INSTANCE_ID_PATTERN = Pattern.compile(
		"%(\\w+)\\.collect\\.deviceid%",
		Pattern.CASE_INSENSITIVE
	);

	/**
	 * List of pattern function converters
	 */
	private static final List<PatternFunctionConverter> PATTERN_FUNCTION_CONVERTERS = List.of(
		new PatternFunctionConverter(SOURCE_REF_PATTERN, ConversionHelper::convertSourceReference),
		new PatternFunctionConverter(SOURCE_ENTRY_PATTERN, (matcher, input) -> convertColumnReference(matcher, input, 1)),
		new PatternFunctionConverter(INSTANCE_REF_PATTERN, (matcher, input) -> convertColumnReference(matcher, input, 2)),
		new PatternFunctionConverter(COLUMN_REF_PATTERN, (matcher, input) -> convertColumnReference(matcher, input, 1)),
		new PatternFunctionConverter(EMBEDDED_FILE_PATTERN, ConversionHelper::convertEmbeddedFileReference),
		new PatternFunctionConverter(EMBEDDED_FILE_NO_PERCENT_PATTERN, ConversionHelper::convertEmbeddedFileReference),
		new PatternFunctionConverter(MONO_INSTANCE_ID_PATTERN, ConversionHelper::convertMonoInstanceReference)
	);

	/**
	 * Perform value conversions
	 * 
	 * @param input
	 * @return updated string value
	 */
	public static String performValueConversions(final String input) {

		return getYamlMonitorNameOptional(input)
			.orElseGet(() -> performPatternConversions(input));

	}

	/**
	 * Perform value conversions using pattern function converters
	 * 
	 * @param input
	 * @return updated string value
	 */
	private static String performPatternConversions(String input) {
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
	 * <b><u>${source::monitors.enclosure.discovery.sources.source(2)}</u></b>
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
			String.format("${source::monitors.%s.%s.sources.%s}", monitor, job, source)
		);
	}

	/**
	 * Convert column reference. E.g.<br>
	 * <b><u>%Entry.Column(2)%</u></b> becomes
	 * <b><u>$2</u></b><br>
	 * <b><u>%InstanceTable.Column(2)%</u></b> becomes
	 * <b><u>$2</u></b><br>
	 * <b><u>%Column(2)%</u></b> becomes
	 * <b><u>$2</u></b><br>
	 * @param matcher matcher used to find groups
	 * @param input   input value to be replaced
	 * @param group   index of a capturing group in the matcher's pattern
	 * @return updated string value
	 */
	private static String convertColumnReference(final Matcher matcher, final String input, final int group) {
		final String column = matcher.group(group).toLowerCase();
		return input.replace(
			matcher.group(),
			String.format("$%s", column)
		);
	}

	/**
	 * Convert embedded file reference. E.g.
	 * <b><u>%EmbeddedFile(1)%</u></b> becomes
	 * <b><u>$embedded.EmbeddedFile(1)$</u></b>
	 * 
	 * @param matcher matcher used to find groups
	 * @param input   input value to be replaced
	 * @return updated string value
	 */
	private static String convertEmbeddedFileReference(final Matcher matcher, final String input) {
		final String index = matcher.group(1);
		return input.replace(
			matcher.group(),
			String.format("$embedded.EmbeddedFile(%s)$", index)
		);
	}

	/**
	 * Convert mono instance reference. E.g.
	 * <b><u>%NetworkCard.Collect.DeviceID%</u></b> becomes
	 * <b><u>${network::id}</u></b>
	 * 
	 * @param matcher matcher used to find groups
	 * @param input   input value to be replaced
	 * @return updated string value
	 */
	private static String convertMonoInstanceReference(final Matcher matcher, final String input) {
		final String monitorName = getYamlMonitorName(matcher.group(1));
		return input.replace(
			matcher.group(),
			String.format("${%s::id}", monitorName)
		);
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
		return getYamlMonitorNameOptional(hdfMonitorName)
			.orElseThrow(
				() -> 
					new IllegalStateException(
						String.format(
							"Could not find corresponding Monitor name for the HDF device name '%s'",
							hdfMonitorName
					)
			)
		);
	}

	/**
	 * Try to get the YAML monitor name for the given HDF value
	 * 
	 * @param value
	 * @return {@link Optional} of {@link String} value
	 */
	private static Optional<String> getYamlMonitorNameOptional(final String value) {
		return Optional.ofNullable(HDF_TO_YAML_MONITOR_NAME.get(value.toLowerCase()));
	}

	/**
	 * Wrap this value in AWK reference ${awk::value} if the awk function is
	 * detected
	 * 
	 * @param value to wrap
	 * @return String value
	 */
	public static String wrapInAwkRefIfFunctionDetected(final String value) {
		if (value.startsWith("sprintf(")) {
			return String.format("${awk::%s}", value);
		}
		return value;
	}

	@AllArgsConstructor
	static class PatternFunctionConverter {
		@Getter
		private Pattern pattern;
		@Getter
		private BiFunction<Matcher, String, String> converter;
	}
}
