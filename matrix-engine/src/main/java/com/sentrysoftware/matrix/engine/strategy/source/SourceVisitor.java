package com.sentrysoftware.matrix.engine.strategy.source;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.http.HTTPSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.ipmi.IPMI;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.oscommand.OSCommandSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.reference.ReferenceSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp.SNMPGetTableSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.tablejoin.TableJoinSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.tableunion.TableUnionSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.telnet.TelnetInteractiveSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.ucs.UCSSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.wbem.WBEMSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.wmi.WMISource;
import com.sentrysoftware.matrix.engine.protocol.IProtocolConfiguration;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol;
import com.sentrysoftware.matrix.engine.strategy.StrategyConfig;
import com.sentrysoftware.matrix.engine.strategy.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class SourceVisitor implements ISourceVisitor {

	private static final String WBEM = "wbem";

	@Autowired
	private StrategyConfig strategyConfig;

	@Autowired
	private MatsyaClientsExecutor matsyaClientsExecutor;

	@Override
	public SourceTable visit(final HTTPSource httpSource) {
		return SourceTable.empty();
	}

	@Override
	public SourceTable visit(final IPMI ipmi) {
		return SourceTable.empty();
	}

	@Override
	public SourceTable visit(final OSCommandSource osCommandSource) {
		return SourceTable.empty();
	}

	@Override
	public SourceTable visit(final ReferenceSource referenceSource) {
		return SourceTable.empty();
	}

	@Override
	public SourceTable visit(final SNMPGetSource snmpGetSource) {
		if (snmpGetSource == null || snmpGetSource.getOid() == null) {
			return SourceTable.empty();
		}

		final Optional<IProtocolConfiguration> snmpProtocolOpt = Optional.ofNullable(strategyConfig.getEngineConfiguration()
				.getProtocolConfigurations().get(SNMPProtocol.class));

		if (!snmpProtocolOpt.isPresent()) {
			return SourceTable.empty();
		}

		final SNMPProtocol protocol = (SNMPProtocol) snmpProtocolOpt.get();
		final String hostname = strategyConfig.getEngineConfiguration().getTarget().getHostname();

		try {

			final String result = matsyaClientsExecutor.executeSNMPGet(
					snmpGetSource.getOid(),
					protocol,
					hostname,
					true);

			if (result != null) {
					return SourceTable
							.builder()
							.table(Stream.of(Stream.of(result).collect(Collectors.toList())).collect(Collectors.toList()))
							.build();
			}

		} catch (Exception e) {
			final String message = String.format(
					"SNMP Get Source of %s on %s was unsuccessful due to an exception. Message: %s.",
					snmpGetSource.getOid(), hostname, e.getMessage());
			log.debug(message, e);
		}

		return SourceTable.empty();
	}

	@Override
	public SourceTable visit(final SNMPGetTableSource snmpGetTableSource) {
		if (snmpGetTableSource == null || snmpGetTableSource.getOid() == null) {
			return SourceTable.empty();
		}
		// run Matsya in order to execute the snmpTable
		// receives a List structure
		SourceTable sourceTable = new SourceTable();
		List<String> selectedColumns = snmpGetTableSource.getSnmpTableSelectColumns();

		if (selectedColumns == null) {
			return SourceTable.empty();
		}
		String[] selectColumnArray = new String[selectedColumns.size()];
		selectColumnArray = selectedColumns.toArray(selectColumnArray);
		
		final Optional<IProtocolConfiguration> snmpProtocolOpt = Optional.ofNullable(strategyConfig.getEngineConfiguration()
				.getProtocolConfigurations().get(SNMPProtocol.class));

		if (!snmpProtocolOpt.isPresent()) {
			return SourceTable.empty();
		}

		final SNMPProtocol protocol = (SNMPProtocol) snmpProtocolOpt.get();
		final String hostname = strategyConfig.getEngineConfiguration().getTarget().getHostname();

		try {

			final List<List<String>> result = matsyaClientsExecutor.executeSNMPTable(
					snmpGetTableSource.getOid(),
					selectColumnArray,
					protocol,
					hostname,
					true);

			sourceTable.setHeaders(selectedColumns);
			sourceTable.setTable(result);

			return sourceTable;

		} catch (Exception e) {
			final String message = String.format(
					"SNMP Test Failed - SNMP Table of %s on %s was unsuccessful due to an exception. Message: %s.",
					snmpGetTableSource.getOid(), hostname, e.getMessage());
			log.debug(message, e);
			return SourceTable.empty();
		}
	}

	@Override
	public SourceTable visit(final TableJoinSource tableJoinSource) {
		if (tableJoinSource == null || strategyConfig.getHostMonitoring() == null) {
			return SourceTable.empty();
		}

		final Map<String, SourceTable> sources = strategyConfig.getHostMonitoring().getSourceTables();
		if (sources == null ) {
			log.debug("SourceTable Map cannot be null, the Join {} will return an empty result.", tableJoinSource);
			return SourceTable.empty();
		}

		if (tableJoinSource.getLeftTable() == null || sources.get(tableJoinSource.getLeftTable()) == null ||  sources.get(tableJoinSource.getLeftTable()).getTable() == null) {
			log.debug("LeftTable cannot be null, the Join {} will return an empty result.", tableJoinSource);
			return SourceTable.empty();
		}

		if (tableJoinSource.getRightTable() == null || sources.get(tableJoinSource.getRightTable()) == null || sources.get(tableJoinSource.getRightTable()).getTable() == null) {
			log.debug("RightTable cannot be null, the Join {} will return an empty result.", tableJoinSource);
			return SourceTable.empty();
		}
		if (tableJoinSource.getLeftKeyColumn() < 1 || tableJoinSource.getRightKeyColumn() < 1) {
			log.debug("Invalid key column number (leftKeyColumnNumber=" + tableJoinSource.getLeftKeyColumn()
			+ ", rightKeyColumnNumber=" + tableJoinSource.getDefaultRightLine() + ")");
			return SourceTable.empty();
		}

		final List<List<String>> executeTableJoin = matsyaClientsExecutor.executeTableJoin(
				sources.get(tableJoinSource.getLeftTable()).getTable(), 
				sources.get(tableJoinSource.getRightTable()).getTable(), 
				tableJoinSource.getLeftKeyColumn(), 
				tableJoinSource.getRightKeyColumn(), 
				tableJoinSource.getDefaultRightLine(), 
				WBEM.equalsIgnoreCase(tableJoinSource.getKeyType()), 
				false);

		SourceTable sourceTable = new SourceTable();
		if (executeTableJoin != null) {
			sourceTable.setTable(executeTableJoin);
		}

		return sourceTable;
	}

	@Override
	public SourceTable visit(final TableUnionSource tableUnionSource) {

		if (tableUnionSource == null) {
			log.warn("Table Union cannot be null, the Union operation {} will return an empty result.",
					tableUnionSource);
			return SourceTable.empty();
		}

		final List<String> unionTables = tableUnionSource.getTables();
		if (unionTables == null) {
			log.warn("Table list in the Union cannot be null, the Union operation {} will return an empty result.",
					tableUnionSource);
			return SourceTable.empty();
		}

		final List<SourceTable> sourceTablesToConcat = unionTables
				.stream()
				.map(this::getSourceTable)
				.filter(Objects::nonNull)
				.collect(Collectors.toList());

		final SourceTable sourceTable = new SourceTable();
		final List<List<String>> executeTableUnion = sourceTablesToConcat
				.stream()
				.map(SourceTable::getTable)
				.flatMap(Collection::stream)
				.collect(Collectors.toList());

		sourceTable.setTable(executeTableUnion);

		return sourceTable;
	}

	/**
	 * Get source table based on the key
	 * 
	 * @param key
	 * @return A {@link SourceTable} already defined in the current {@link IHostMonitoring} or a hard-coded CSV sourceTable
	 */
	SourceTable getSourceTable(final String key) {

		// In case the key contains a ';' convert the CSV to table
		if (key.indexOf(';') >= 0) {
			return SourceTable.builder()
					.table(SourceTable.csvToTable(key, HardwareConstants.SEMICOLON))
					.build();
		}

		final SourceTable sourceTable = strategyConfig.getHostMonitoring().getSourceTableByKey(key);
		if (sourceTable == null) {
			log.warn("The following source table {} cannot be found.", key);
		}
		return sourceTable;
	}

	@Override
	public SourceTable visit(final TelnetInteractiveSource telnetInteractiveSource) {
		return SourceTable.empty();
	}

	@Override
	public SourceTable visit(final UCSSource ucsSource) {
		return SourceTable.empty();
	}

	@Override
	public SourceTable visit(final WBEMSource wbemSource) {
		return SourceTable.empty();
	}

	@Override
	public SourceTable visit(final WMISource wmiSource) {
		return SourceTable.empty();
	}

	
}
