package com.sentrysoftware.matrix.engine.strategy.source;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.EMPTY;

import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.EmbeddedFile;
import com.sentrysoftware.matrix.connector.model.common.http.body.Body;
import com.sentrysoftware.matrix.connector.model.common.http.body.StringBody;
import com.sentrysoftware.matrix.connector.model.common.http.header.Header;
import com.sentrysoftware.matrix.connector.model.common.http.header.StringHeader;
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
import com.sentrysoftware.matrix.engine.strategy.utils.OsCommandHelper;
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

		String entries = httpSource.getExecuteForEachEntryOf();

		if (entries != null && !entries.isEmpty()) {
			SourceTable sourceTable = getSourceTable(entries);
			if (sourceTable != null) {
				SourceTable result = SourceTable.builder().rawData("").build();

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

					SourceTable thisSourceTable = copy.accept(sourceVisitor);

					if (httpSource.getEntryConcatMethod() == null) {
						result.setRawData(
								result.getRawData()
								.concat(thisSourceTable.getRawData()));
					} else {
						switch (httpSource.getEntryConcatMethod()) {
						case JSON_ARRAY:
							result.setRawData(
									(result.getRawData().isEmpty() ? result.getRawData() : result.getRawData().concat(",\n"))
									.concat(thisSourceTable.getRawData()));
							break;
						case JSON_ARRAY_EXTENDED:
							result.setRawData(
									(result.getRawData().isEmpty() ? "" : result.getRawData().concat(",\n"))
									.concat(PslUtils.formatExtendedJSON(rowToCsv(row, ","), thisSourceTable)));
							break;
						case CUSTOM:
							result.setRawData(
									result.getRawData()
									.concat(httpSource.getEntryConcatStart())
									.concat(thisSourceTable.getRawData())
									.concat(httpSource.getEntryConcatEnd()));
							break;
						default: // LIST or empty
							result.setRawData(
									result.getRawData()
									.concat(thisSourceTable.getRawData()));
							break;
						}
					}
				}
				return result;
			} else {
				log.error("The SourceTable referenced in the ExecuteForEachEntryOf field can't be found : {}", entries);
				return SourceTable.empty();
			}
		}

		if (monitor != null) {
			final HTTPSource copy = httpSource.copy();
			replaceDeviceIdsInHttpSource(copy, monitor);
			return copy.accept(sourceVisitor);
		}

		return httpSource.accept(sourceVisitor);
	}

	@Override
	public SourceTable visit(final IPMI ipmi) {
		
		return ipmi.accept(sourceVisitor);
	}

	@Override
	public SourceTable visit(final OSCommandSource osCommandSource) {
		replaceEmbeddedFilesInOsCommandLine(osCommandSource, connector);
		return osCommandSource.accept(sourceVisitor);
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
	 * Replace the content of the EmbeddedFiles in the command line of the given {@link OSCommandSource}
	 * 
	 * @param osCommandSource The {@link OSCommandSource} we wish to update
	 * @param connector       The connector defining all the {@link EmbeddedFile} instances
	 */
	void replaceEmbeddedFilesInOsCommandLine(final OSCommandSource osCommandSource, final Connector connector) {
		Assert.notNull(osCommandSource, "osCommandSource cannot be null.");
		Assert.notNull(osCommandSource.getCommandLine(), "osCommandSource CommandLine cannot be null.");
		Assert.notNull(connector, "connector cannot be null.");

		// Nothing to replace
		if (connector.getEmbeddedFiles().isEmpty()) {
			return;
		}

		osCommandSource.setCommandLine(OsCommandHelper
				.updateOsCommandEmbeddedFile(osCommandSource.getCommandLine(), connector));

	}

	/**
	 * Detect the mono instance replacements in the given {@link SNMPSource} oid then replace all the occurrences by the DeviceID
	 * 
	 * @param snmpSource The Source we wish to update
	 * @param monitor The monitor passed by the mono instance collect
	 */
	void replaceDeviceIdInSNMPOid(final SNMPSource snmpSource, final Monitor monitor) {
		Assert.notNull(snmpSource, "snmpSource cannot be null.");
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
		Assert.notNull(monitor.getMetadata().get(HardwareConstants.DEVICE_ID), "monitor deviceId cannot be null.");

		final String deviceId = monitor.getMetadata().get(HardwareConstants.DEVICE_ID);

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
			return strategyConfig.getHostMonitoring().getSourceTableByKey(key);
		}

		return SourceTable.builder()
				.table(SourceTable.csvToTable(key, HardwareConstants.SEMICOLON))
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
