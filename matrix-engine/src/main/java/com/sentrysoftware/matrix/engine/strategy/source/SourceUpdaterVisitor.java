package com.sentrysoftware.matrix.engine.strategy.source;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
import com.sentrysoftware.matrix.engine.strategy.StrategyConfig;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoring;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
public class SourceUpdaterVisitor implements ISourceVisitor {

	private static final Pattern MONO_INSTANCE_REPLACEMENT_PATTERN = Pattern.compile("%\\w+\\.collect\\.deviceid%", Pattern.CASE_INSENSITIVE);
	private static final Pattern EMBEDDEDFILE_REPLACEMENT_PATTERN = Pattern.compile("%EmbeddedFile\\((\\d+)\\)%", Pattern.CASE_INSENSITIVE);
	private static final Pattern SOURCE_PATTERN = Pattern.compile(
			"^\\s*((.*)\\.(discovery|collect)\\.source\\(([1-9]\\d*)\\)(.*))\\s*$",
			Pattern.CASE_INSENSITIVE);
	private static final Pattern DYNAMIC_ENTRY_PATTERN = Pattern.compile(
			"^\\s*(.*)(\\%entry.column\\(([1-9]\\d*)\\)\\%)(.*)\\s*$",
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
				SourceTable result = SourceTable.builder().table(new ArrayList<>()).build();

				for  (List<String> row : sourceTable.getTable()) {
					final HTTPSource copy = httpSource.copy();
					replaceDynamicEntriesInHttpSource(copy, row);

					if (monitor != null) {
						replaceDeviceIdsInHttpSource(copy, monitor);
					}

					SourceTable thisSourceTable = copy.accept(sourceVisitor);

					result.getTable().addAll(thisSourceTable.getTable());
				}
				return result;
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

		final String commandLine = osCommandSource.getCommandLine();

		final Matcher matcher = EMBEDDEDFILE_REPLACEMENT_PATTERN.matcher(commandLine);

		final StringBuilder sb = new StringBuilder();
		while (matcher.find()) {
			// EmbeddedFile(embeddedFileIndex)
			final Integer embeddedFileIndex = Integer.parseInt(matcher.group(1));

			// The embedded file is available in the connector
			final EmbeddedFile embeddedFile = connector.getEmbeddedFiles().get(embeddedFileIndex);

			// This means there is a design problem or the HDF developer indicated a wrong embedded file
			Assert.notNull(embeddedFile, () -> "Cannot get the EmbeddedFile from the Connector. EmbeddedFile Index: " + embeddedFileIndex);
			final String embeddedFileContent = embeddedFile.getContent();

			// This means there is a design problem, the content can never be null
			Assert.notNull(embeddedFileContent, () -> "EmbeddedFile content is null. EmbeddedFile Index: " + embeddedFileIndex);
			matcher.appendReplacement(sb, embeddedFileContent);
		}
		matcher.appendTail(sb);

		osCommandSource.setCommandLine(sb.toString());
		
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
	 * Replace all dynamic entries from the {@link httpSource} by the values in the {@link row}.
	 * 
	 * @param httpSource
	 * @param row
	 */
	void replaceDynamicEntriesInHttpSource(@NonNull final HTTPSource httpSource, @NonNull final List<String> row) {
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
	 * Replace the dynamic parts of the {@link key} by the right column from the {@link row}.
	 * 
	 * @param key
	 * @param row
	 * @return
	 */
	String replaceDynamicEntry(@NonNull final String key, @NonNull final List<String> row) {
		// We need to keep the original key in case there is a problem.
		String res = key;
		Matcher matcher = DYNAMIC_ENTRY_PATTERN.matcher(res);

		while (matcher.matches()) {
			try {
				res = res.replace(matcher.group(2), row.get(Integer.parseInt(matcher.group(3)) - 1));
			} catch (NumberFormatException e) {
				log.warn("The dynamic key from HTTPSource is badly formatted : {}", key);
				return key;
			}
			matcher = DYNAMIC_ENTRY_PATTERN.matcher(res);
		}

		return res;
	}

	/**
	 * Replace the deviceId in the {@link httpSource} by the one in the metadata in MonoInstance collects.
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
	 * Replace the deviceId in the {@link key} by the one in the metadata in MonoInstance collects.
	 * 
	 * @param key The key where to replace the deviceId.
	 * @param monitor Can be null, in that case {@link key} is directly returned.
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
	 * Get source table based on the {@link key}
	 * 
	 * @param key
	 * @return A {@link SourceTable} already defined in the current {@link IHostMonitoring} or a hard-coded CSV sourceTable
	 */
	SourceTable getSourceTable(final String key) {

		if (SOURCE_PATTERN.matcher(key).matches()) {
			HostMonitoring hostMonitoring = (HostMonitoring) strategyConfig.getHostMonitoring();
			final SourceTable sourceTable = hostMonitoring.getSourceTableByKey(key);
			return sourceTable;
		}

		return SourceTable.builder()
				.table(SourceTable.csvToTable(key, HardwareConstants.SEMICOLON))
				.build();
	}
}
