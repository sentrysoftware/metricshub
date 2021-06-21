package com.sentrysoftware.matrix.engine.strategy.source;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.util.Assert;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.common.EmbeddedFile;
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
import com.sentrysoftware.matrix.model.monitor.Monitor;

import lombok.AllArgsConstructor;

@AllArgsConstructor
public class SourceUpdaterVisitor implements ISourceVisitor {

	private static final Pattern MONO_INSTANCE_REPLACEMENT_PATTERN = Pattern.compile("%\\w+\\.collect\\.deviceid%", Pattern.CASE_INSENSITIVE);

	private ISourceVisitor sourceVisitor;
	private Connector connector;
	private Monitor monitor;

	@Override
	public SourceTable visit(final HTTPSource httpSource) {
		
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
	 * @param monitor    The monitor passed by the mono instance collect
	 */
	void replaceDeviceIdInSNMPOid(final SNMPSource snmpSource, final Monitor monitor) {
		Assert.notNull(snmpSource, "snmpSource cannot be null.");
		Assert.notNull(snmpSource.getOid(), "snmpSource Oid cannot be null.");

		// The monitor is null means the replacement is not required this occurs
		// when the visitor is called from the discovery or the MultiInstance collect
		if (monitor == null) {
			return;
		}

		Assert.notNull(monitor.getMetadata(), "monitor metadata cannot be null.");
		Assert.notNull(monitor.getMetadata().get(HardwareConstants.DEVICE_ID), "monitor deviceId cannot be null.");

		final String oid = snmpSource.getOid();
		final String deviceId = monitor.getMetadata().get(HardwareConstants.DEVICE_ID);

		final Matcher matcher = MONO_INSTANCE_REPLACEMENT_PATTERN.matcher(oid);

		final StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			matcher.appendReplacement(sb, deviceId);
		}
		matcher.appendTail(sb);

		snmpSource.setOid(sb.toString());
	}

}
