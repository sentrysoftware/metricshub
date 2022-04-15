package com.sentrysoftware.matrix.engine.strategy.source;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DEVICE_ID;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.EMPTY;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TABLE_SEP;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.EntryConcatMethod;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.http.HTTPSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.ipmi.IPMI;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.oscommand.OSCommandSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.reference.ReferenceSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.reference.StaticSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetTableSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.sshinteractive.SshInteractiveSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.tablejoin.TableJoinSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.tableunion.TableUnionSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.ucs.UCSSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.wbem.WBEMSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.winrm.WinRMSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.wmi.WMISource;
import com.sentrysoftware.matrix.engine.strategy.StrategyConfig;
import com.sentrysoftware.matrix.engine.strategy.utils.PslUtils;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
public class SourceUpdaterVisitor implements ISourceVisitor {

	private static final Pattern MONO_INSTANCE_REPLACEMENT_PATTERN = Pattern.compile("%\\w+\\.collect\\.deviceid%", Pattern.CASE_INSENSITIVE);

	private static final Pattern SOURCE_PATTERN = Pattern.compile(
			"^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\)(.*))\\s*$",
			Pattern.CASE_INSENSITIVE);

	private static final Pattern DYNAMIC_ENTRY_PATTERN = Pattern.compile(
			"%entry.column\\(([1-9]\\d*)\\)%",
			Pattern.CASE_INSENSITIVE);

	private ISourceVisitor sourceVisitor;
	private Connector connector;
	private Monitor monitor;

	@Autowired
	private StrategyConfig strategyConfig;

	@Override
	public SourceTable visit(final HTTPSource httpSource) {
		// Very important! otherwise we will overlap in multi-host mode
		final HTTPSource copy = httpSource.copy();

		// Replace HTTP Authentication token
		copy.setAuthenticationToken(extractHttpTokenFromSource(
				copy.getKey(),
				copy.getAuthenticationToken(),
				"authenticationToken"));

		return processSource(copy);
	}

	/**
	 * Run an execution for each entry in the {@link SourceTable} referenced by the given source copy.
	 * 
	 * @param copy Must be a copy of the source.
	 * @return {@link SourceTable} result
	 */
	private SourceTable runExecuteForEachEntryOf(final Source copy) {

		final String sourceTableKey = copy.getExecuteForEachEntryOf();

		SourceTable result = processExecuteForEachEntryOf(copy, sourceTableKey);

		if ((EntryConcatMethod.JSON_ARRAY_EXTENDED.equals(copy.getEntryConcatMethod())
				|| EntryConcatMethod.JSON_ARRAY.equals(copy.getEntryConcatMethod())) && result != null) {
			result.setRawData(String.format("[%s]", result.getRawData()));
		}

		return result;
	}

	/**
	 * Process the given {@link Source} for the source table entries in the source result of <em>sourceTableKey</em>
	 * 
	 * @param source         The {@link Source} we wish to process
	 * @param sourceTableKey The key of the source table defining the entries
	 * @return {@link SourceTable} containing all the result concatenated using the
	 *         EntryConcatMethod defined in the original {@link Source}
	 */
	private SourceTable processExecuteForEachEntryOf(final Source source, final String sourceTableKey) {
		final SourceTable sourceTable = getSourceTable(sourceTableKey);
		if (sourceTable == null) {
			log.error("The SourceTable referenced in the ExecuteForEachEntryOf field can't be found : {}", sourceTableKey);
			return SourceTable.empty();
		}

		final SourceTable result = SourceTable.builder().rawData(EMPTY).build();

		for (List<String> row : sourceTable.getTable()) {
			final Source copy = source.copy();

			try {
				copy.update(dataValue -> replaceDynamicEntry(dataValue, row));
			} catch (NumberFormatException e) {
				log.warn("The dynamic key from Source is badly formatted : {}", copy);
				continue;
			}

			copy.update(dataValue -> replaceDeviceId(dataValue, monitor));

			concatEntryResult(source, result, row, copy.accept(sourceVisitor));
		}

		return result;
	}

	
	/**
	 * Based on the EntryConcatMethod, concatenate the <em>sourceTableToConcat</em> in the <em>result</em> source table
	 * 
	 * @param source               The source used to get the entry concat start/end
	 * @param currentResult        The current result we wish to update
	 * @param row                  The row to concatenate in case we have the JSON_ARRAY_EXTENDED concatenation method
	 * @param sourceTableToConcat  The current source table result
	 */
	private void concatEntryResult(final Source source, final SourceTable currentResult, final List<String> row,
			final SourceTable sourceTableToConcat) {

		final EntryConcatMethod entryConcatMethod = source.getEntryConcatMethod() != null ? source.getEntryConcatMethod()
				: EntryConcatMethod.LIST;
		final String rawData = sourceTableToConcat.getRawData();

		switch (entryConcatMethod) {
		case JSON_ARRAY:
			appendJsonToArray(currentResult, rawData);
			break;
		case JSON_ARRAY_EXTENDED:
			appendExtendedJsonToArray(currentResult, row, sourceTableToConcat);
			break;
		case CUSTOM:
			appendCustomEntryResult(source, currentResult, rawData);
			break;
		default: // LIST or empty
			if (rawData != null && !rawData.isBlank()) {
				joinStringValue(currentResult, rawData, "\n");
			}

			final List<List<String>> table = sourceTableToConcat.getTable();

			if (table != null && !table.isEmpty()) {
				currentResult.getTable().addAll(table.stream().filter(line -> !line.isEmpty())
						.collect(Collectors.toCollection(ArrayList::new)));
			}
			break;
		}
	}

	/**
	 * Append the custom entry result <em>rawData</em> to the given <em>currentResult</em> {@link SourceTable}.
	 * 
	 * @param source         The source defining the <em>entryConcatStart</em> and <em>entryConcatEnd</em> properties
	 * @param currentResult  The {@link SourceTable} result to update
	 * @param rawData        The rawData to append
	 */
	private void appendCustomEntryResult(final Source source, final SourceTable currentResult, final String rawData) {
		if (rawData == null) {
			return;
		}

		final String entryConcatStart = source.getEntryConcatStart() != null ? source.getEntryConcatStart() : EMPTY;
		final String entryConcatEnd = source.getEntryConcatEnd() != null ? source.getEntryConcatEnd() : EMPTY;

		currentResult.setRawData(
				currentResult.getRawData()
				.concat(entryConcatStart)
				.concat(rawData)
				.concat(entryConcatEnd));
	}

	/**
	 * Build the Extended JSON then append it to the <em>currentResult</em> {@link SourceTable}.
	 * 
	 * @param currentResult       The {@link SourceTable} result to update
	 * @param row                 The row used to build the Extended JSON
	 * @param sourceTableToConcat The {@link SourceTable} defining the <em>rawData</em> to concatenate.
	 */
	private void appendExtendedJsonToArray(final SourceTable currentResult, final List<String> row,
			final SourceTable sourceTableToConcat) {

		final String formattedExtendedJSON = PslUtils.formatExtendedJSON(rowToCsv(row, ","), sourceTableToConcat);
		// This will mess the JSON Extended
		if (formattedExtendedJSON.isBlank()) {
			return;
		}

		joinStringValue(currentResult, formattedExtendedJSON, ",\n");
	}

	/**
	 * Append the string value to the <em>currentResult</em> {@link SourceTable} using the passed <em>separator</em>.
	 * The separator is not appended for the first value
	 * 
	 * @param currentResult The {@link SourceTable} result to update
	 * @param string        String value to append
	 * @param separator     Separator of the appended values 
	 */
	private void joinStringValue(final SourceTable currentResult, final String string, @NonNull String separator) {
		currentResult.setRawData(
				(currentResult.getRawData().isBlank() ? EMPTY : currentResult.getRawData().concat(separator)).concat(string));
	}

	/**
	 * Append the given <em>json</em> node to the <em>currentResult</em> {@link SourceTable} with
	 * the intent of building a JSON Array
	 * 
	 * @param currentResult The {@link SourceTable} result to update
	 * @param json      The JSON node as string to append
	 */
	private void appendJsonToArray(final SourceTable currentResult, final String json) {
		// Don't mess the JSON Array
		if (json == null || json.isBlank()) {
			return;
		}

		joinStringValue(currentResult, json, ",\n");
	}

	/**
	 * Get the value of the field from the foreign source identified by <em>foreignSourceKey</em>.
	 * 
	 * @param originalSourceKey The original source key used for debug purpose
	 * @param foreignSourceKey  The foreign source key used to extract the field value
	 * @param fieldLabel        The field label used for debug purpose
	 * @return {@link String} value
	 */
	String extractHttpTokenFromSource(final String originalSourceKey, String foreignSourceKey, String fieldLabel) {

		// No token to replace
		if (foreignSourceKey == null) {
			return null;
		}

		if (foreignSourceKey.isEmpty()) {
			return EMPTY;
		}

		// Get the source table defining where we are going to fetch the value
		final SourceTable sourceTable = getSourceTable(foreignSourceKey);
		if (sourceTable == null) {
			log.error("Couldn't extract the foreign source table identified by {} and defined in original source {} to set the {} field.",
					foreignSourceKey, originalSourceKey, fieldLabel);
			return null;
		}

		// Try the table
		final List<List<String>> table = sourceTable.getTable();
		String value = null;
		if (table != null && !table.isEmpty()) {
			log.debug("Get {} defined in source {} from list table.", fieldLabel, foreignSourceKey);
			final List<String> firstRow = table.get(0);
			if (firstRow != null && !firstRow.isEmpty()) {
				// First column
				value = firstRow.get(0);
			}
		}

		// Try raw data
		final String rawData = sourceTable.getRawData();
		if (value == null && rawData != null) {
			log.debug("Get {} defined in source {} from raw data.", fieldLabel, foreignSourceKey);
			// First column
			value = rawData.split(";")[0];
		}

		if (value == null) {
			log.error("Couldn't extract the {} defined in source {}.", fieldLabel, originalSourceKey);
		}

		return value;
	}

	@Override
	public SourceTable visit(final IPMI ipmi) {
		return processSource(ipmi.copy());
	}

	@Override
	public SourceTable visit(final OSCommandSource osCommandSource) {
		return processSource(osCommandSource.copy());
	}

	@Override
	public SourceTable visit(final ReferenceSource referenceSource) {
		return processSource(referenceSource.copy());
	}

	@Override
	public SourceTable visit(final StaticSource staticSource) {
		return processSource(staticSource.copy());
	}

	@Override
	public SourceTable visit(final SNMPGetSource snmpGetSource) {
		return processSource(snmpGetSource.copy());
	}

	@Override
	public SourceTable visit(final SNMPGetTableSource snmpGetTableSource) {
		return processSource(snmpGetTableSource.copy());
	}

	@Override
	public SourceTable visit(final TableJoinSource tableJoinSource) {
		return processSource(tableJoinSource.copy());
	}

	@Override
	public SourceTable visit(final TableUnionSource tableUnionSource) {
		return processSource(tableUnionSource.copy());
	}

	@Override
	public SourceTable visit(final SshInteractiveSource sshInteractiveSource) {
		return processSource(sshInteractiveSource.copy());
	}

	@Override
	public SourceTable visit(final UCSSource ucsSource) {
		return processSource(ucsSource.copy());
	}

	@Override
	public SourceTable visit(final WBEMSource wbemSource) {
		return processSource(wbemSource.copy());
	}

	@Override
	public SourceTable visit(final WMISource wmiSource) {
		return processSource(wmiSource.copy());
	}

	@Override
	public SourceTable visit(final WinRMSource winRMSource) {
		return processSource(winRMSource.copy());
	}

	/**
	 * Process the given source copy
	 * 
	 * @param copy copy of the original source
	 * @return {@link SourceTable}
	 */
	private SourceTable processSource(final Source copy) {
		if (copy.isExecuteForEachEntry()) {
			return runExecuteForEachEntryOf(copy);
		}

		copy.update(value -> replaceDeviceId(value, monitor));

		return copy.accept(sourceVisitor);
	}

	/**
	 * Replace the dynamic parts of the key by the right column from the row.
	 * 
	 * @param dataValue Source's member value
	 * @param row       The row used to extract the value to replace
	 * @return String value
	 */
	static String replaceDynamicEntry(final String dataValue, @NonNull final List<String> row) throws NumberFormatException {
		if (dataValue == null) {
			return null;
		}

		final Matcher matcher = DYNAMIC_ENTRY_PATTERN.matcher(dataValue);

		final StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			String value = row.get(Integer.parseInt(matcher.group(1)) - 1);
			matcher.appendReplacement(sb, Matcher.quoteReplacement(value));
		}

		matcher.appendTail(sb);

		return sb.toString();
	}

	/**
	 * Replace the deviceId in the key by the one in the metadata in MonoInstance collects.
	 * 
	 * @param key The key where to replace the deviceId.
	 * @param monitor Can be null, in that case key is directly returned.
	 * @return String value
	 */
	static String replaceDeviceId(final String key, final Monitor monitor) {
		if (monitor == null || key == null) {
			return key;
		}

		Assert.notNull(monitor.getMetadata(), "monitor metadata cannot be null.");
		Assert.notNull(monitor.getMetadata().get(DEVICE_ID), "monitor deviceId cannot be null.");

		final String deviceId = monitor.getMetadata().get(DEVICE_ID);

		final Matcher matcher = MONO_INSTANCE_REPLACEMENT_PATTERN.matcher(key);

		final StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			matcher.appendReplacement(sb, Matcher.quoteReplacement(deviceId));
		}
		matcher.appendTail(sb);

		return sb.toString();
	}

	/**
	 * Get source table based on the key
	 * 
	 * @param key
	 * @return A {@link SourceTable} already defined in the current {@link IHostMonitoring} or a hard-coded CSV sourceTable
	 */
	SourceTable getSourceTable(final String key) {

		if (SOURCE_PATTERN.matcher(key).matches()) {
			return strategyConfig
					.getHostMonitoring()
					.getConnectorNamespace(connector)
					.getSourceTable(key);
		}

		return SourceTable.builder()
				.table(SourceTable.csvToTable(key, TABLE_SEP))
				.build();
	}

	/**
	 * Transform the {@link List} row to a {@link String} representation
	 * [a1,b1,c2]
	 *  =>
	 * a1,b1,c1
	 *
	 * @param row             The row result we wish to parse
	 * @param separator       The cells separator on each line
	 * @return {@link String} value
	 */
	public static String rowToCsv(final List<String> row, final String separator) {
		if (row != null) {
			return row
					.stream()
					.filter(Objects::nonNull)
					.collect(Collectors.joining(separator));
		}

		return EMPTY;
	}
}
