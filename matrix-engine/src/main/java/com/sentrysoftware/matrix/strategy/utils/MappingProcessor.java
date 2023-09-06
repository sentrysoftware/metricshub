package com.sentrysoftware.matrix.strategy.utils;

import com.sentrysoftware.matrix.common.helpers.MatrixConstants;
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

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.UnaryOperator;
import java.util.regex.Matcher;

import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.EMPTY;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.SOURCE_REF_PATTERN;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.SOURCE_VALUE_WITH_DOLLAR_PATTERN;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Slf4j
public class MappingProcessor {
	private TelemetryManager telemetryManager;
	private Mapping mapping;
	private String connectorId;
	private String hostname;
	private String id;
	private long collectTime;

	private List<String> row;

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
					.getConnectorNamespace(connectorId)
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
	public Map<String, String> interpretNonContextMappingAttributes(final String monitorType) {
		return interpretNonContextMapping(mapping.getAttributes(), monitorType);
	}

	/**
	 *  This method interprets non context mapping metrics
	 * @return Map<String, String>
	 */
	public Map<String, String> interpretNonContextMappingMetrics(final String monitorType) {
		return interpretNonContextMapping(mapping.getMetrics(), monitorType);
	}

	/**
	 *  This method interprets non context mapping conditional collections
	 * @return Map<String, String>
	 */
	public Map<String, String> interpretNonContextMappingConditionalCollection(final String monitorType) {
		return interpretNonContextMapping(mapping.getConditionalCollection(), monitorType);
	}
	
	/**
	 * This method interprets non context mapping legacy text parameters
	 * @return Map<String, String>
	 */
	public Map<String, String> interpretNonContextMappingLegacyTextParameters(final String monitorType) {
		return interpretNonContextMapping(mapping.getLegacyTextParameters(), monitorType);
	}

	/**
	 * This method interprets non context mapping.
	 * The key value pairs are filled with values depending on the column type: extraction, awk, rate, etc...
	 * @return Map<String, String>
	 */
	public Map<String, String> interpretNonContextMapping(final Map<String, String> keyValuePairs, final String monitorType) { // NOSONAR on cognitive complexity of 16
		if (keyValuePairs == null) {
			return Collections.emptyMap();
		}

		final Map<String, String> result = new HashMap<>();

		keyValuePairs.forEach((key, value) -> {
			if (isColumnExtraction(value)) {
				result.put(key, extractColumnValue(value, key, monitorType));
			} else if (isAwkScript(value)) {
				result.put(key, executeAwkScript(value));
			} else if (isMegaBit2Bit(value)) { 
				result.put(key, megaBit2bit(value));
			} else if (isPercentToRatioFunction(value)) {
				result.put(key, percent2Ration(value));
			} else if (isMegaHertz2HertzFunction(value)) {
				result.put(key, megaHertz2Hertz(value));
			} else if (isMebiByte2ByteFunction(value)) {
				result.put(key, mebiByte2Byte(value));
			} else if (isBooleanFunction(value)) {
				result.put(key, booleanFunction(value));
			} else if (isLegacyLedStatusFunction(value)) {
				result.put(key, legacyLedStatus(value));
			} else if (isLegacyIntrusionStatusFunction(value)) {
				result.put(key, legacyIntrusionStatus(value));
			} else if (isLegacyPredictedFailureFunction(value)) {
				result.put(key, legacyPredictedFailure(value));
			} else if (islegacyNeedsCleaningFucntion(value)) {
				result.put(key, legacyNeedsCleaning(value));
			} else if (isComputePowerShareRatioFunction(value)) {
				result.put(String.format("%s.raw", key), computePowerShareRatio(value));
			} else if (isLegacyLinkStatusFunction(value)) {
				result.put(key, legacyLinkStatusFunction(value));
			} else if (isLegacyFullDuplex(value)) {
				result.put(key, legacyFullDuplex(value));
			} else if (isLookupFunction(value)) {
				lookupFunctions.put(key, this::lookup);
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

	private String lookup(final String value) {
		return null;
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
		// TODO Auto-generated method stub
		return false;
	}

	private boolean isRateFunction(String value) {
		// TODO Auto-generated method stub
		return false;
	}

	private String megaBit2bit(String value) {
		// TODO Auto-generated method stub
		return null;
	}

	private boolean isMegaBit2Bit(String value) {
		// TODO Auto-generated method stub
		return false;
	}

	private String legacyFullDuplex(String value) {
		// TODO Auto-generated method stub
		return null;
	}

	private boolean isLegacyFullDuplex(String value) {
		// TODO Auto-generated method stub
		return false;
	}

	private String legacyLinkStatusFunction(String value) {
		// TODO Auto-generated method stub
		return null;
	}

	private boolean isLegacyLinkStatusFunction(String value) {
		// TODO Auto-generated method stub
		return false;
	}

	private String computePowerShareRatio(String value) {
		// TODO Auto-generated method stub
		return null;
	}

	private boolean isComputePowerShareRatioFunction(String value) {
		// TODO Auto-generated method stub
		return false;
	}

	private String legacyNeedsCleaning(String value) {
		// TODO Auto-generated method stub
		return null;
	}

	private boolean islegacyNeedsCleaningFucntion(String value) {
		// TODO Auto-generated method stub
		return false;
	}

	private String legacyPredictedFailure(String value) {
		// TODO Auto-generated method stub
		return null;
	}

	private boolean isLegacyPredictedFailureFunction(String value) {
		// TODO Auto-generated method stub
		return false;
	}

	private String legacyIntrusionStatus(String value) {
		// TODO Auto-generated method stub
		return null;
	}

	private boolean isLegacyIntrusionStatusFunction(String value) {
		// TODO Auto-generated method stub
		return false;
	}

	private String legacyLedStatus(String value) {
		// TODO Auto-generated method stub
		return null;
	}

	private boolean isLegacyLedStatusFunction(String value) {
		// TODO Auto-generated method stub
		return false;
	}

	private String booleanFunction(String value) {
		// TODO Auto-generated method stub
		return null;
	}

	private boolean isBooleanFunction(String value) {
		// TODO Auto-generated method stub
		return false;
	}

	private String mebiByte2Byte(String value) {
		// TODO Auto-generated method stub
		return null;
	}

	private boolean isMebiByte2ByteFunction(String value) {
		// TODO Auto-generated method stub
		return false;
	}

	private String megaHertz2Hertz(String value) {
		// TODO Auto-generated method stub
		return null;
	}

	private boolean isMegaHertz2HertzFunction(String value) {
		// TODO Auto-generated method stub
		return false;
	}

	private String percent2Ration(String value) {
		// TODO Auto-generated method stub
		return null;
	}

	private boolean isPercentToRatioFunction(String value) {
		// TODO Auto-generated method stub
		return false;
	}

	private String executeAwkScript(String value) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * This method extracts column value using a Regex
	 * @param value
	 * @return string representing the column value
	 */
	private String extractColumnValue(final String value, final String key, final String monitorType) {
		final Matcher matcher = getStringRegexMatcher(value);
		matcher.find();
		final int columnIndex = Integer.parseInt(matcher.group(1)) - 1;
		if (columnIndex >= 0 && columnIndex < row.size()) {
			return row.get(columnIndex);
		} else {
			final Optional<SourceTable> sourceKey = lookupSourceTable();
			log.warn(
				"Hostname {} - Column number {} is invalid for the source {}. Column number should not exceed the size of the row. key {} - " +
				"Row {} - monitor type {}.",
				hostname,
				columnIndex,
				sourceKey,
				key,
				row,
				monitorType
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
	public Resource interpretMappingResource(final String monitorType) {

		final MappingResource mappingResource = mapping.getResource();

		if (mappingResource != null && mappingResource.hasType()) {
			return Resource.builder()
				.type(
					interpretNonContextMapping(Map.of("type", mappingResource.getType()), monitorType)
					.get("type")
				)
				.attributes(interpretNonContextMapping(mappingResource.getAttributes(), monitorType))
				.build();
		}

		return null;
	}

	/**
	 * This method interprets context mapping attributes
	 * @param monitor
	 * @return Map<String, String>
	 */
	public Map<String, String> interpretContextMappingAttributes(final Monitor monitor) {
		return interpretContextKeyValuePairs(monitor, mapping.getAttributes());
	}

	/**
	 * This method interprets context mapping metrics
	 * @param monitor
	 * @return Map<String, String>
	 */
	public Map<String, String> interpretContextMappingMetrics(final Monitor monitor) {
		return interpretContextKeyValuePairs(monitor, mapping.getMetrics());
	}

	/**
	 * This method interprets context mapping conditional collections
	 * @param monitor
	 * @return Map<String, String>
	 */
	public Map<String, String> interpretContextMappingConditionalCollection(final Monitor monitor) {
		return interpretContextKeyValuePairs(monitor, mapping.getConditionalCollection());
	}


	/**
	 * This method interprets context mapping legacy text parameters
	 * @param monitor
	 * @return Map<String, String>
	 */
	public Map<String, String> interpretContextMappingLegacyTextParameters(final Monitor monitor) {
		return interpretContextKeyValuePairs(monitor, mapping.getLegacyTextParameters());
	}


	/**
	 * This method interprets context key value pairs
	 * @param monitor
	 * @param keyValuePairs
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
