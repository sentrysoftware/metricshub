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

	/**
	 * HDF Device name to YAML connector Monitor name
	 */
	private static final Map<String, String> HDF_TO_YAML_MONITOR_NAME;
	static {
		Map<String, String> hdfToYamlMonitor = new HashMap<>();
		hdfToYamlMonitor.put("battery", "battery");
		hdfToYamlMonitor.put("blade", "blade");
		hdfToYamlMonitor.put("cpu", "cpu");
		hdfToYamlMonitor.put("cpucore", "cpuCore");
		hdfToYamlMonitor.put("diskcontroller", "diskController");
		hdfToYamlMonitor.put("enclosure", "enclosure");
		hdfToYamlMonitor.put("fan", "fan");
		hdfToYamlMonitor.put("gpu", "gpu");
		hdfToYamlMonitor.put("led", "led");
		hdfToYamlMonitor.put("logicaldisk", "logicalDisk");
		hdfToYamlMonitor.put("lun", "lun");
		hdfToYamlMonitor.put("memory", "memory");
		hdfToYamlMonitor.put("networkcard", "networkCard");
		hdfToYamlMonitor.put("otherdevice", "otherDevice");
		hdfToYamlMonitor.put("physicaldisk", "physicalDisk");
		hdfToYamlMonitor.put("powersupply", "powerSupply");
		hdfToYamlMonitor.put("robotic", "robotics");
		hdfToYamlMonitor.put("tapedrive", "tapeDrive");
		hdfToYamlMonitor.put("temperature", "temperature");
		hdfToYamlMonitor.put("vm", "vm");
		hdfToYamlMonitor.put("voltage", "voltage");

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
	 * List of pattern function converters
	 */
	private static final List<PatternFunctionConverter> PATTERN_FUNCTION_CONVERTERS = List.of(
		new PatternFunctionConverter(SOURCE_REF_PATTERN, ConversionHelper::convertSourceReference),
		new PatternFunctionConverter(SOURCE_ENTRY_PATTERN, ConversionHelper::convertEntryReference)
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
