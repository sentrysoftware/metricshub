package com.sentrysoftware.matrix.strategy.utils;

import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.EMPTY;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.SOURCE_REF_PATTERN;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.SOURCE_VALUE_WITH_DOLLAR_PATTERN;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import com.sentrysoftware.matrix.common.JobInfo;
import com.sentrysoftware.matrix.common.helpers.MatrixConstants;
import com.sentrysoftware.matrix.common.helpers.state.DuplexMode;
import com.sentrysoftware.matrix.common.helpers.state.IntrusionStatus;
import com.sentrysoftware.matrix.common.helpers.state.LinkStatus;
import com.sentrysoftware.matrix.common.helpers.state.NeedsCleaning;
import com.sentrysoftware.matrix.common.helpers.state.PredictedFailure;
import com.sentrysoftware.matrix.connector.model.monitor.mapping.MappingResource;
import com.sentrysoftware.matrix.connector.model.monitor.task.Mapping;
import com.sentrysoftware.matrix.strategy.source.SourceTable;
import com.sentrysoftware.matrix.telemetry.Monitor;
import com.sentrysoftware.matrix.telemetry.Resource;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Slf4j
public class MappingProcessor {

	private static final double MEBIBYTE_2_BYTE_FACTOR = 1_048_576.0;
	private static final double MEGABIT_2_BIT_FACTOR = 1_000_000.0;
	private static final double MEGAHERTZ_2_HERTZ_FACTOR = 1_000_000.0;
	private static final double PERCENT_2_RATIO_FACTOR = 0.01;

	private static final String ZERO = "0";
	private static final String ONE = "1";
	private static final String TRUE = "true";
	private static final String INVALID_VALUE = "Hostname {} - Value {} is invalid for {}.";

	private static final Pattern MEBIBYTE_2_BYTE_PATTERN = Pattern.compile("mebibyte2byte\\((.+)\\)", Pattern.CASE_INSENSITIVE);
	private static final Pattern MEGABIT_2_BIT_PATTERN = Pattern.compile("megabit2bit\\((.+)\\)", Pattern.CASE_INSENSITIVE);
	private static final Pattern MEGAHERTZ_2_HERTZ_PATTERN = Pattern.compile("megahertz2hertz\\((.+)\\)", Pattern.CASE_INSENSITIVE);
	private static final Pattern PERCENT_2_RATIO_PATTERN = Pattern.compile("percent2ratio\\((.+)\\)", Pattern.CASE_INSENSITIVE);
	private static final Pattern LEGACY_FULL_DUPLEX_PATTERN = Pattern.compile("legacyfullduplex\\((.+)\\)", Pattern.CASE_INSENSITIVE);
	private static final Pattern LEGACY_LINK_STATUS_PATTERN = Pattern.compile("legacylinkstatus\\((.+)\\)", Pattern.CASE_INSENSITIVE);
	private static final Pattern LEGACY_PREDICTED_FAILURE_PATTERN = Pattern.compile("legacypredictedfailure\\((.+)\\)", Pattern.CASE_INSENSITIVE);
	private static final Pattern LEGACY_NEEDS_CLEANING_PATTERN = Pattern.compile("legacyneedscleaning\\((.+)\\)", Pattern.CASE_INSENSITIVE);
	private static final Pattern LEGACY_INTRUSION_STATUS_PATTERN = Pattern.compile("legacyintrusionstatus\\((.+)\\)", Pattern.CASE_INSENSITIVE);
	private static final Pattern LOOKUP_PATTERN = Pattern.compile("lookup\\((.+)\\)", Pattern.CASE_INSENSITIVE);
	private static final Pattern BOOLEAN_PATTERN = Pattern.compile("boolean\\((.+)\\)", Pattern.CASE_INSENSITIVE);

	private TelemetryManager telemetryManager;
	private Mapping mapping;
	private String id;
	private long collectTime;
	private List<String> row;
	private JobInfo jobInfo;

	@Default
	private Map<String, UnaryOperator<String>> lookupFunctions = new HashMap<>();

	@Default
	private Map<String, BiFunction<String, Monitor, String>> legacyPowerSupplyFunctions = new HashMap<>();

	@Default
	private Map<String, BiFunction<KeyValuePair, Monitor, String>> computationFunctions = new HashMap<>();

	/**
	 * Find the source table instance from the connector namespace.<br>
	 * If we have a hard-coded source then we will create a source wrapping the
	 * csv input.
	 * 
	 * @return {@link Optional} instance of {@link SourceTable}
	 */
	public Optional<SourceTable> lookupSourceTable() {
		final String source = mapping.getSource();

		final Matcher matcher = SOURCE_REF_PATTERN.matcher(source);

		if (matcher.find()) {
			final String sourceKey = matcher.group();
			return Optional.ofNullable(
				telemetryManager
					.getHostProperties()
					.getConnectorNamespace(jobInfo.getConnectorName())
					.getSourceTable(sourceKey)
			);
		}

		// Hard-coded source
		return Optional.of(
			SourceTable
				.builder()
				.table(SourceTable.csvToTable(source, MatrixConstants.SEMICOLON))
				.build()
		);
	}

	/**
	 * This method interprets non context mapping attributes
	 * @return Map<String, String>
	 */
	public Map<String, String> interpretNonContextMappingAttributes() {
		return interpretNonContextMapping(mapping.getAttributes());
	}

	/**
	 * This method interprets non context mapping metrics
	 * @return Map<String, String>
	 */
	public Map<String, String> interpretNonContextMappingMetrics() {
		return interpretNonContextMapping(mapping.getMetrics());
	}

	/**
	 *  This method interprets non context mapping conditional collections
	 * @return Map<String, String>
	 */
	public Map<String, String> interpretNonContextMappingConditionalCollection() {
		return interpretNonContextMapping(mapping.getConditionalCollection());
	}
	
	/**
	 * This method interprets non context mapping legacy text parameters
	 * @return Map<String, String>
	 */
	public Map<String, String> interpretNonContextMappingLegacyTextParameters() {
		return interpretNonContextMapping(mapping.getLegacyTextParameters());
	}

	/**
	 * This method interprets non context mapping.
	 * The key value pairs are filled with values depending on the column type: extraction, awk, rate, etc...
	 * @param keyValuePairs pairs of key values (for example: attribute key and attribute value)
	 * @return Map<String, String>
	 */
	public Map<String, String> interpretNonContextMapping(final Map<String, String> keyValuePairs) { // NOSONAR on cognitive complexity of 16
		if (keyValuePairs == null) {
			return Collections.emptyMap();
		}

		final Map<String, String> result = new HashMap<>();

		keyValuePairs.forEach((key, value) -> {
			if (isColumnExtraction(value)) {
				result.put(key, extractColumnValue(value, key));
			} else if (isAwkScript(value)) {
				result.put(key, executeAwkScript(value));
			} else if (isMegaBit2Bit(value)) { 
				result.put(key, megaBit2bit(value, key));
			} else if (isPercentToRatioFunction(value)) {
				result.put(key, percent2Ratio(value, key));
			} else if (isMegaHertz2HertzFunction(value)) {
				result.put(key, megaHertz2Hertz(value, key));
			} else if (isMebiByte2ByteFunction(value)) {
				result.put(key, mebiByte2Byte(value, key));
			} else if (isBooleanFunction(value)) {
				result.put(key, booleanFunction(value, key));
			} else if (isLegacyLedStatusFunction(value)) {
				result.put(key, legacyLedStatus(value));
			} else if (isLegacyIntrusionStatusFunction(value)) {
				result.put(key, legacyIntrusionStatus(value, key));
			} else if (isLegacyPredictedFailureFunction(value)) {
				result.put(key, legacyPredictedFailure(value, key));
			} else if (islegacyNeedsCleaningFunction(value)) {
				result.put(key, legacyNeedsCleaning(value, key));
			} else if (isComputePowerShareRatioFunction(value)) {
				result.put(String.format("%s.raw", key), computePowerShareRatio(value));
			} else if (isLegacyLinkStatusFunction(value)) {
				result.put(key, legacyLinkStatusFunction(value, key));
			} else if (isLegacyFullDuplex(value)) {
				result.put(key, legacyFullDuplex(value, key));
			} else if (isLookupFunction(value)) {
				lookupFunctions.put(key, lookupValue -> lookup(value, key));
			} else if (isLegacyPowerSupplyUtilization(value)) {
				legacyPowerSupplyFunctions.put(key, this::legacyPowerSupplyUtilization);
			} else if (isFakeCounterFunction(value)) {
				computationFunctions.put(key, this::fakeCounter);
			} else if (isRateFunction(value)) {
				computationFunctions.put(key, this::rate);
			} else {
				result.put(key, value);
			}
		});

		return result;
	}

	private String lookup(final String value, String key) {
		final Matcher matcher = LOOKUP_PATTERN.matcher(value);
		matcher.find();
		String extracted = matcher.group(1);

		String extractedValue = extracted;
		if (isColumnExtraction(extracted)) {
			extractedValue = extractColumnValue(extracted, key);
		}

		final String[] lookupValues = extractedValue.split(",");

		if (lookupValues.length != 4) {
			log.error("Hostname {} - Lookup should contain exactly 4 arguments (detected {}) in lookup function {}.",
				jobInfo.getHostname(),
				lookupValues.length,
				value);

			return null;
		}

		final String monitorType = lookupValues[0];
		final String lookupValue = lookupValues[1];
		final String attributeKey = lookupValues[2];
		final String attributeValue = lookupValues[3];
		
		final Map<String, Monitor> typedMonitors = telemetryManager.getMonitors().get(monitorType);

		if (typedMonitors == null) {
			log.error("Hostname {} - No monitors found of type {}.",jobInfo.getHostname(), monitorType);
			return null;
		}

		final Stream<Monitor> monitors = typedMonitors.values().stream().filter(x -> x.getAttributes().get(attributeKey).equals(attributeValue));
		Monitor monitor = monitors.findFirst().orElse(null);

		if (monitor == null) {
			log.error("Hostname {} - No monitors found matching attribute {} with value {}.", jobInfo.getHostname(), attributeKey, attributeValue);
			return null;
		}

		return monitor.getAttributes().get(lookupValue);
	}

	private String legacyPowerSupplyUtilization(final String value, final Monitor monitor) {
		return null;
	}

	private String fakeCounter(final KeyValuePair keyValuePair, final Monitor monitor) {
		return null;
	}

	private String rate(final KeyValuePair keyValuePair, final Monitor monitor) {
		return null;
	}

	private boolean isFakeCounterFunction(String value) {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean isLegacyPowerSupplyUtilization(String value) {
		// TODO Auto-generated method stub
		return false;
	}

	private boolean isLookupFunction(String value) {
		return LOOKUP_PATTERN.matcher(value).find();
	}

	private boolean isRateFunction(String value) {
		// TODO Auto-generated method stub
		return false;
	}

	private String megaBit2bit(String value, String key) {
		final Matcher matcher = MEGABIT_2_BIT_PATTERN.matcher(value);
		matcher.find();

		final String extracted = matcher.group(1);

		if (isColumnExtraction(extracted)) {
			return multiplyValueByFactor(extractColumnValue(extracted, key), key, MEGABIT_2_BIT_FACTOR);
		} else {
			return multiplyValueByFactor(extracted, key, MEGABIT_2_BIT_FACTOR);
		}
	}

	private boolean isMegaBit2Bit(String value) {
		return MEGABIT_2_BIT_PATTERN.matcher(value).find();
	}

	private String legacyFullDuplex(String value, String key) {
		final Matcher matcher = LEGACY_FULL_DUPLEX_PATTERN.matcher(value);
		matcher.find();

		final String extracted = matcher.group(1);
		String extractedValue = extracted;
		if (isColumnExtraction(extracted)) {
			extractedValue = extractColumnValue(extracted, key);
		}

		final Optional<DuplexMode> maybeDuplexMode = DuplexMode.interpret(extractedValue);

		if (maybeDuplexMode.isPresent()) {
			return String.valueOf(maybeDuplexMode.get().getNumericValue());
		}

		log.debug(INVALID_VALUE, jobInfo.getHostname(), extractedValue, key);
		return null;
	}

	private boolean isLegacyFullDuplex(String value) {
		return LEGACY_FULL_DUPLEX_PATTERN.matcher(value).find();
	}

	private String legacyLinkStatusFunction(String value, String key) {
		final Matcher matcher = LEGACY_LINK_STATUS_PATTERN.matcher(value);
		matcher.find();

		final String extracted = matcher.group(1);
		String extractedValue = extracted;
		if (isColumnExtraction(extracted)) {
			extractedValue = extractColumnValue(extracted, key);
		}

		final Optional<LinkStatus> maybeLinkStatus = LinkStatus.interpret(extractedValue);

		if (maybeLinkStatus.isPresent()) {
			return String.valueOf(maybeLinkStatus.get().getNumericValue());
		}

		log.debug(INVALID_VALUE, jobInfo.getHostname(), extractedValue, key);
		return null;
	}

	private boolean isLegacyLinkStatusFunction(String value) {
		return LEGACY_LINK_STATUS_PATTERN.matcher(value).find();
	}

	private String computePowerShareRatio(String value) {
		// TODO Auto-generated method stub
		return null;
	}

	private boolean isComputePowerShareRatioFunction(String value) {
		// TODO Auto-generated method stub
		return false;
	}

	private String legacyNeedsCleaning(String value, String key) {
		final Matcher matcher = LEGACY_NEEDS_CLEANING_PATTERN.matcher(value);
		matcher.find();

		final String extracted = matcher.group(1);
		String extractedValue = extracted;
		if (isColumnExtraction(extracted)) {
			extractedValue = extractColumnValue(extracted, key);
		}

		final Optional<NeedsCleaning> maybeNeedsCleaning = NeedsCleaning.interpret(extractedValue);

		if (maybeNeedsCleaning.isPresent()) {
			return String.valueOf(maybeNeedsCleaning.get().getNumericValue());
		}

		log.debug(INVALID_VALUE, jobInfo.getHostname(), extractedValue, key);
		return null;
	}

	private boolean islegacyNeedsCleaningFunction(String value) {
		return LEGACY_NEEDS_CLEANING_PATTERN.matcher(value).find();
	}

	private String legacyPredictedFailure(String value, String key) {
		final Matcher matcher = LEGACY_PREDICTED_FAILURE_PATTERN.matcher(value);
		matcher.find();

		final String extracted = matcher.group(1);
		String extractedValue = extracted;
		if (isColumnExtraction(extracted)) {
			extractedValue = extractColumnValue(extracted, key);
		}

		final Optional<PredictedFailure> maybePredictedFailure = PredictedFailure.interpret(extractedValue);

		if (maybePredictedFailure.isPresent()) {
			return String.valueOf(maybePredictedFailure.get().getNumericValue());
		}

		log.debug(INVALID_VALUE, jobInfo.getHostname(), extractedValue, key);
		return null;
	}

	private boolean isLegacyPredictedFailureFunction(String value) {
		return LEGACY_PREDICTED_FAILURE_PATTERN.matcher(value).find();
	}

	private String legacyIntrusionStatus(String value, String key) {
		final Matcher matcher = LEGACY_INTRUSION_STATUS_PATTERN.matcher(value);
		matcher.find();

		final String extracted = matcher.group(1);
		String extractedValue = extracted;
		if (isColumnExtraction(extracted)) {
			extractedValue = extractColumnValue(extracted, key);
		}

		final Optional<IntrusionStatus> maybeIntrusionStatus = IntrusionStatus.interpret(extractedValue);

		if (maybeIntrusionStatus.isPresent()) {
			return String.valueOf(maybeIntrusionStatus.get().getNumericValue());
		}

		log.debug(INVALID_VALUE, jobInfo.getHostname(), extractedValue, key);
		return null;
	}

	private boolean isLegacyIntrusionStatusFunction(String value) {
		return LEGACY_INTRUSION_STATUS_PATTERN.matcher(value).find();
	}

	private String legacyLedStatus(String value) {
		// TODO Auto-generated method stub
		return null;
	}

	private boolean isLegacyLedStatusFunction(String value) {
		// TODO Auto-generated method stub
		return false;
	}

	private String booleanFunction(String value, String key) {
		final Matcher matcher = BOOLEAN_PATTERN.matcher(value);
		matcher.find();

		final String extracted = matcher.group(1);
		String extractedValue = extracted;
		if (isColumnExtraction(extracted)) {
			extractedValue = extractColumnValue(extracted, key);
		}

		if (ONE.equals(extractedValue) || TRUE.equals(extractedValue)) {
			return ONE;
		}

		return ZERO;
	}

	private boolean isBooleanFunction(String value) {
		return BOOLEAN_PATTERN.matcher(value).find();
	}

	private String mebiByte2Byte(String value, String key) {
		final Matcher matcher = MEBIBYTE_2_BYTE_PATTERN.matcher(value);
		matcher.find();

		final String extracted = matcher.group(1);

		if (isColumnExtraction(extracted)) {
			return multiplyValueByFactor(extractColumnValue(extracted, key), key, MEBIBYTE_2_BYTE_FACTOR);
		} else {
			return multiplyValueByFactor(extracted, key, MEBIBYTE_2_BYTE_FACTOR);
		}
	}

	private boolean isMebiByte2ByteFunction(String value) {
		return MEBIBYTE_2_BYTE_PATTERN.matcher(value).find();
	}

	private String megaHertz2Hertz(String value, String key) {
		final Matcher matcher = MEGAHERTZ_2_HERTZ_PATTERN.matcher(value);
		matcher.find();

		final String extracted = matcher.group(1);

		if (isColumnExtraction(extracted)) {
			return multiplyValueByFactor(extractColumnValue(extracted, key), key, MEGAHERTZ_2_HERTZ_FACTOR);
		} else {
			return multiplyValueByFactor(extracted, key, MEGAHERTZ_2_HERTZ_FACTOR);
		}
	}

	private boolean isMegaHertz2HertzFunction(String value) {
		return MEGAHERTZ_2_HERTZ_PATTERN.matcher(value).find();
	}

	private String percent2Ratio(String value, String key) {
		final Matcher matcher = PERCENT_2_RATIO_PATTERN.matcher(value);
		matcher.find();

		final String extracted = matcher.group(1);

		if (isColumnExtraction(extracted)) {
			return multiplyValueByFactor(extractColumnValue(extracted, key), key, PERCENT_2_RATIO_FACTOR);
		} else {
			return multiplyValueByFactor(extracted, key, PERCENT_2_RATIO_FACTOR);
		}
	}

	private boolean isPercentToRatioFunction(String value) {
		return PERCENT_2_RATIO_PATTERN.matcher(value).find();
	}

	private String executeAwkScript(String value) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * We multiply the value by a predetermined factor, usually for unit conversion
	 * 
	 * @param value		A string with an already extracted value
	 * @param key 
	 * @param factor	Double value to be multiplied to the value
	 * @return			A String containing only the new value
	 */
	private String multiplyValueByFactor(final String value, String key, final double factor) {

		try {
			double doubleValue = Double.parseDouble(value);
			return Double.toString(doubleValue * factor);
		} catch (Exception e) {
			log.error("Hostname {} - Value expected, but got {} for parameter {}.", jobInfo.getHostname(), value, key);
			log.debug("Hostname {} - Exception: {}", jobInfo.getHostname(), e);
			return null;
		}
	}

	/**
	 * This method extracts column value using a Regex
	 * @param value
	 * @param key
	 * @return string representing the column value
	 */
	private String extractColumnValue(final String value, final String key) {
		final Matcher matcher = getStringRegexMatcher(value);
		matcher.find();
		final int columnIndex = Integer.parseInt(matcher.group(1)) - 1;
		if (columnIndex >= 0 && columnIndex < row.size()) {
			return row.get(columnIndex);
		} else {
			log.warn(
				"Hostname {} - Column number {} is invalid for the source {}. Column number should not exceed the size of the row. key {} - " +
					"Row {} - monitor type {}.",
				jobInfo.getHostname(),
				columnIndex,
				mapping.getSource(),
				key,
				row,
				jobInfo.getMonitorType()
			);
			return EMPTY;
		}
	}

	private boolean isAwkScript(String value) {
		// TODO Auto-generated method stub
		return false;
	}

	/**
	 * This method checks whether a column contains a value to be extracted (e.g: $1, $2, etc ...)
	 * @param value
	 * @return true or false
	 */
	private boolean isColumnExtraction(String value) {
		return getStringRegexMatcher(value).find();
	}

	/**
	 * This method returns the matcher of a regex on a given string value
	 * @param value
	 * @return Matcher
	 */
	private Matcher getStringRegexMatcher(String value) {
		return SOURCE_VALUE_WITH_DOLLAR_PATTERN.matcher(value);
	}

	/**
	 * This method interprets mapping instance mapping resource field
	 * @return Resource
	 */
	public Resource interpretMappingResource() {

		final MappingResource mappingResource = mapping.getResource();

		if (mappingResource != null && mappingResource.hasType()) {
			return Resource.builder()
				.type(
					interpretNonContextMapping(Map.of("type", mappingResource.getType()))
					.get("type")
				)
				.attributes(interpretNonContextMapping(mappingResource.getAttributes()))
				.build();
		}

		return null;
	}

	/**
	 * This method interprets context mapping attributes
	 * @param monitor a given monitor
	 * @return Map<String, String>
	 */
	public Map<String, String> interpretContextMappingAttributes(final Monitor monitor) {
		return interpretContextKeyValuePairs(monitor, mapping.getAttributes());
	}

	/**
	 * This method interprets context mapping metrics
	 * @param monitor a given monitor
	 * @return Map<String, String>
	 */
	public Map<String, String> interpretContextMappingMetrics(final Monitor monitor) {
		return interpretContextKeyValuePairs(monitor, mapping.getMetrics());
	}

	/**
	 * This method interprets context mapping conditional collections
	 * @param monitor a given monitor
	 * @return Map<String, String>
	 */
	public Map<String, String> interpretContextMappingConditionalCollection(final Monitor monitor) {
		return interpretContextKeyValuePairs(monitor, mapping.getConditionalCollection());
	}


	/**
	 * This method interprets context mapping legacy text parameters
	 * @param monitor a given monitor
	 * @return Map<String, String>
	 */
	public Map<String, String> interpretContextMappingLegacyTextParameters(final Monitor monitor) {
		return interpretContextKeyValuePairs(monitor, mapping.getLegacyTextParameters());
	}


	/**
	 * This method interprets context key value pairs
	 * @param monitor a given monitor
	 * @param keyValuePairs key value pairs (for example: attribute key and attribute value)
	 * @return Map<String, String>
	 */
	private Map<String, String> interpretContextKeyValuePairs(final Monitor monitor, final Map<String, String> keyValuePairs) {
		if (keyValuePairs== null) {
			return Collections.emptyMap();
		}

		final Map<String, String> result = new HashMap<>();
		lookupFunctions
			.entrySet()
			.forEach(entry -> {
				final String attributeKey = entry.getKey();
				result.put(
					attributeKey,
					entry.getValue().apply(keyValuePairs.get(attributeKey))
				);
			});

		legacyPowerSupplyFunctions
			.entrySet()
			.forEach(entry -> {
				final String attributeKey = entry.getKey();
				result.put(
					attributeKey,
					entry.getValue().apply(keyValuePairs.get(attributeKey), monitor)
				);
			});

		computationFunctions
			.entrySet()
			.forEach(entry -> {
				final String attributeKey = entry.getKey();
				result.put(
					attributeKey,
					entry.getValue().apply(new KeyValuePair(attributeKey, keyValuePairs.get(attributeKey)), monitor)
				);
			});

		lookupFunctions.clear();
		legacyPowerSupplyFunctions.clear();
		computationFunctions.clear();

		return result;
	}

	@Data
	@Builder
	static class KeyValuePair {
		String key;
		String value;
	}
}
