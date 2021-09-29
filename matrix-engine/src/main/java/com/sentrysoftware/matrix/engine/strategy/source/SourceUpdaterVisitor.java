package com.sentrysoftware.matrix.engine.strategy.source;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.http.body.Body;
import com.sentrysoftware.matrix.connector.model.common.http.body.StringBody;
import com.sentrysoftware.matrix.connector.model.common.http.header.Header;
import com.sentrysoftware.matrix.connector.model.common.http.header.StringHeader;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.http.EntryConcatMethod;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.http.HTTPSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.ipmi.IPMI;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.oscommand.OSCommandSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.reference.ReferenceSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.reference.StaticSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetTableSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.tablejoin.TableJoinSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.tableunion.TableUnionSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.telnet.TelnetInteractiveSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.ucs.UCSSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.wbem.WBEMSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.wmi.WMISource;
import com.sentrysoftware.matrix.engine.strategy.StrategyConfig;
import com.sentrysoftware.matrix.engine.strategy.utils.PslUtils;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.DEVICE_ID;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.LOG_BEGIN_OPERATION_TEMPLATE;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TABLE_SEP;

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

		if (httpSource == null) {
			log.error("HTTPSource cannot be null, the HTTPSource operation will return an empty result.");
			return SourceTable.empty();
		}

		log.info(LOG_BEGIN_OPERATION_TEMPLATE, "HTTP source", httpSource.getKey(), httpSource.toString());

		// Very important! otherwise we will overlap in multi-host mode
		final HTTPSource copy = httpSource.copy();

		// Replace HTTP Authentication token
		copy.setAuthenticationToken(extractHttpTokenFromSource(
				copy.getKey(),
				copy.getAuthenticationToken(),
				"authenticationToken"));

		final String sourceTableKey = copy.getExecuteForEachEntryOf();

		if (sourceTableKey != null && !sourceTableKey.isEmpty()) {
			SourceTable result =  processExecuteForEachEntryOf(copy, sourceTableKey);
			if ((EntryConcatMethod.JSON_ARRAY_EXTENDED.equals(copy.getEntryConcatMethod())
					|| EntryConcatMethod.JSON_ARRAY.equals(copy.getEntryConcatMethod()))
					&& result != null) {
				result.setRawData(String.format("[%s]", result.getRawData()));
			}
			return result;
		}

		if (monitor != null) {
			replaceDeviceIdsInHttpSource(copy, monitor);
			return copy.accept(sourceVisitor);
		}

		return copy.accept(sourceVisitor);
	}

	/**
	 * Process the given {@link HTTPSource} for the source table entries identified by the <em>sourceTableKey</em>
	 * 
	 * @param httpSource     The {@link HTTPSource} we wish to process
	 * @param sourceTableKey The key of the source table defining the entries
	 * @return {@link SourceTable} containing all the result concatenated using the EntryConcatMethod defined in the original {@link HTTPSource}
	 */
	private SourceTable processExecuteForEachEntryOf(final HTTPSource httpSource, final String sourceTableKey) {
		final SourceTable sourceTable = getSourceTable(sourceTableKey);
		if (sourceTable == null) {
			log.error("The SourceTable referenced in the ExecuteForEachEntryOf field can't be found : {}", sourceTableKey);
			return SourceTable.empty();
		}

		final SourceTable result = SourceTable.builder().rawData("").build();

		for  (List<String> row : sourceTable.getTable()) {
			final HTTPSource copy = httpSource.copy();

			try {
				replaceDynamicEntriesInHttpSource(copy, row);
			} catch (NumberFormatException e) {
				log.warn("The dynamic key from HTTPSource is badly formatted : {}", copy);
				continue;
			}

			if (monitor != null) {
				replaceDeviceIdsInHttpSource(copy, monitor);
			}

			final SourceTable thisSourceTable = copy.accept(sourceVisitor);

			if (thisSourceTable != null && thisSourceTable.getRawData() != null) {
				if (httpSource.getEntryConcatMethod() == null) {
					result.setRawData(
							result.getRawData()
							.concat(thisSourceTable.getRawData()));
				} else {
					concatHttpResult(httpSource, result, row, thisSourceTable);
				}
			}
		}

		return result;
	}

	/**
	 * Based on the EntryConcatMethod, concatenate the <em>sourceTableToConcat</em> in the <em>result</em> source table
	 * 
	 * @param httpSource           The http source used to get the entry concat start/end
	 * @param result               The result we wish to update
	 * @param row                  The row to concatenate in case we have the JSON_ARRAY_EXTENDED concatenation method
	 * @param sourceTableToConcat  The current source table result
	 */
	private void concatHttpResult(final HTTPSource httpSource, final SourceTable result, final List<String> row,
			final SourceTable sourceTableToConcat) {
		switch (httpSource.getEntryConcatMethod()) {
		case JSON_ARRAY:
			result.setRawData(
					(result.getRawData().isEmpty() ? result.getRawData() : result.getRawData().concat(",\n"))
					.concat(sourceTableToConcat.getRawData()));
			break;
		case JSON_ARRAY_EXTENDED:
			result.setRawData(
					(result.getRawData().isEmpty() ? "" : result.getRawData().concat(",\n"))
					.concat(PslUtils.formatExtendedJSON(rowToCsv(row, ","), sourceTableToConcat)));
			break;
		case CUSTOM:
			result.setRawData(
					result.getRawData()
					.concat(httpSource.getEntryConcatStart())
					.concat(sourceTableToConcat.getRawData())
					.concat(httpSource.getEntryConcatEnd()));
			break;
		default: // LIST or empty
			result.setRawData(
					result.getRawData()
					.concat(sourceTableToConcat.getRawData()));
			break;
		}
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
			return "";
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
		
		return ipmi.accept(sourceVisitor);
	}

	@Override
	public SourceTable visit(final OSCommandSource osCommandSource) {

		log.info(LOG_BEGIN_OPERATION_TEMPLATE, "OSCommand source", osCommandSource.getKey(), osCommandSource.toString());

		// We must copy the source so that we don't modify the original source 
		// which needs to be passed for each monitor when running the mono instance collect.
		final OSCommandSource copy = osCommandSource.copy();
		if (monitor != null) {
			copy.setCommandLine(
					replaceDeviceId(osCommandSource.getCommandLine(), monitor));
		}

		return copy.accept(sourceVisitor);
	}

	@Override
	public SourceTable visit(final ReferenceSource referenceSource) {

		return referenceSource.accept(sourceVisitor);
	}

	@Override
	public SourceTable visit(final StaticSource staticSource) {

		return staticSource.accept(sourceVisitor);
	}

	@Override
	public SourceTable visit(final SNMPGetSource snmpGetSource) {

		log.info(LOG_BEGIN_OPERATION_TEMPLATE, "SNMP Get source", snmpGetSource.getKey(), snmpGetSource.toString());

		if (monitor != null) {
			// We must copy the source so that we don't modify the original source 
			// which needs to be passed for each monitor when running the mono instance collect.
			final SNMPGetSource copy = snmpGetSource.copy();
			replaceDeviceIdInSNMPOid(copy, monitor);
			return copy.accept(sourceVisitor);
		}

		return snmpGetSource.accept(sourceVisitor);
	}

	@Override
	public SourceTable visit(final SNMPGetTableSource snmpGetTableSource) {

		log.info(LOG_BEGIN_OPERATION_TEMPLATE, "SNMP Table source", snmpGetTableSource.getKey(), snmpGetTableSource.toString());

		if (monitor != null) {
			// We must copy the source so that we don't modify the original source 
			// which needs to be passed for each monitor when running the mono instance collect.
			final SNMPGetTableSource copy = snmpGetTableSource.copy();
			replaceDeviceIdInSNMPOid(copy, monitor);
			return copy.accept(sourceVisitor);
		}

		return snmpGetTableSource.accept(sourceVisitor);
	}

	@Override
	public SourceTable visit(final TableJoinSource tableJoinSource) {
		
		return tableJoinSource.accept(sourceVisitor);
	}

	@Override
	public SourceTable visit(final TableUnionSource tableUnionSource) {
		
		return tableUnionSource.accept(sourceVisitor);
	}

	@Override
	public SourceTable visit(final TelnetInteractiveSource telnetInteractiveSource) {
		
		return telnetInteractiveSource.accept(sourceVisitor);
	}

	@Override
	public SourceTable visit(final UCSSource ucsSource) {
		
		return ucsSource.accept(sourceVisitor);
	}

	@Override
	public SourceTable visit(final WBEMSource wbemSource) {
		
		return wbemSource.accept(sourceVisitor);
	}

	@Override
	public SourceTable visit(final WMISource wmiSource) {
		
		return wmiSource.accept(sourceVisitor);
	}

	/**
	 * Detect the mono instance replacements in the given {@link SNMPSource} oid then replace all the occurrences by the DeviceID
	 * 
	 * @param snmpSource The Source we wish to update
	 * @param monitor The monitor passed by the mono instance collect
	 */
	void replaceDeviceIdInSNMPOid(@NonNull final SNMPSource snmpSource, final Monitor monitor) {
		Assert.notNull(snmpSource.getOid(), "snmpSource Oid cannot be null.");

		snmpSource.setOid(replaceDeviceId(snmpSource.getOid(), monitor));
	}

	/**
	 * Replace all dynamic entries from the {@link HTTPSource} by the values in the row.
	 * 
	 * @param httpSource
	 * @param row
	 */
	void replaceDynamicEntriesInHttpSource(@NonNull final HTTPSource httpSource, @NonNull final List<String> row) throws NumberFormatException {
		final String url = httpSource.getUrl();
		final Header header = httpSource.getHeader();
		final Body body = httpSource.getBody();

		if (url != null) {
			httpSource.setUrl(replaceDynamicEntry(url, row));
		}

		if (header != null && header.getClass().isInstance(StringHeader.class)) {
			httpSource.setHeader(new StringHeader(replaceDynamicEntry(((StringHeader) httpSource.getHeader()).getHeader(), row)));
		}

		if (body != null && body.getClass().isInstance(StringBody.class)) {
			httpSource.setBody(new StringBody(replaceDynamicEntry(((StringBody) httpSource.getBody()).getBody(), row)));
		}
	}

	/**
	 * Replace the dynamic parts of the key by the right column from the row.
	 * 
	 * @param key
	 * @param row
	 * @return
	 */
	static String replaceDynamicEntry(@NonNull final String key, @NonNull final List<String> row) throws NumberFormatException {

		final Matcher matcher = DYNAMIC_ENTRY_PATTERN.matcher(key);

		final StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			String value = row.get(Integer.parseInt(matcher.group(1)) - 1);
			matcher.appendReplacement(sb, value);
		}

		matcher.appendTail(sb);

		return sb.toString();
	}

	/**
	 * Replace the deviceId in the {@link HTTPSource} by the one in the metadata in MonoInstance collects.
	 * 
	 * @param httpSource
	 * @param monitor
	 */
	void replaceDeviceIdsInHttpSource(@NonNull final HTTPSource httpSource, @NonNull final Monitor monitor) {
		final String url = httpSource.getUrl();
		final Header header = httpSource.getHeader();
		final Body body = httpSource.getBody();

		if (url != null) {
			httpSource.setUrl(replaceDeviceId(url, monitor));
		}

		if (header != null && header.getClass().isInstance(StringHeader.class)) {
			httpSource.setHeader(new StringHeader(replaceDeviceId(((StringHeader) httpSource.getHeader()).getHeader(), monitor)));
		}

		if (body != null && body.getClass().isInstance(StringBody.class)) {
			httpSource.setBody(new StringBody(replaceDeviceId(((StringBody) httpSource.getBody()).getBody(), monitor)));
		}
	}

	/**
	 * Replace the deviceId in the key by the one in the metadata in MonoInstance collects.
	 * 
	 * @param key The key where to replace the deviceId.
	 * @param monitor Can be null, in that case key is directly returned.
	 * @return
	 */
	String replaceDeviceId(final String key, final Monitor monitor) {
		if (monitor == null) {
			return key;
		}

		Assert.notNull(monitor.getMetadata(), "monitor metadata cannot be null.");
		Assert.notNull(monitor.getMetadata().get(DEVICE_ID), "monitor deviceId cannot be null.");

		final String deviceId = monitor.getMetadata().get(DEVICE_ID);

		final Matcher matcher = MONO_INSTANCE_REPLACEMENT_PATTERN.matcher(key);

		final StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			matcher.appendReplacement(sb, deviceId);
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

		return "";
	}
}
