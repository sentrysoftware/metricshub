package org.sentrysoftware.metricshub.extension.jawk;

/*-
 * ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲
 * MetricsHub Jawk Extension
 * ჻჻჻჻჻჻
 * Copyright 2023 - 2024 Sentry Software
 * ჻჻჻჻჻჻
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱
 */

import static org.sentrysoftware.metricshub.extension.jawk.KeyWords.EXECUTE_HTTP_REQUEST;
import static org.sentrysoftware.metricshub.extension.jawk.KeyWords.EXECUTE_IPMI_REQUEST;
import static org.sentrysoftware.metricshub.extension.jawk.KeyWords.EXECUTE_SNMP_GET;
import static org.sentrysoftware.metricshub.extension.jawk.KeyWords.EXECUTE_SNMP_TABLE;
import static org.sentrysoftware.metricshub.extension.jawk.KeyWords.EXECUTE_WBEM_REQUEST;
import static org.sentrysoftware.metricshub.extension.jawk.KeyWords.EXECUTE_WMI_REQUEST;
import static org.sentrysoftware.metricshub.extension.jawk.KeyWords.GET_SOURCE;
import static org.sentrysoftware.metricshub.extension.jawk.KeyWords.JSON_2CSV;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.sentrysoftware.jawk.NotImplementedError;
import org.sentrysoftware.jawk.ext.AbstractExtension;
import org.sentrysoftware.jawk.ext.JawkExtension;
import org.sentrysoftware.metricshub.engine.connector.model.common.HttpMethod;
import org.sentrysoftware.metricshub.engine.connector.model.common.ResultContent;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.HttpSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.IpmiSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.SnmpGetSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.SnmpTableSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.Source;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.WbemSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.WmiSource;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.Compute;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.Json2Csv;
import org.sentrysoftware.metricshub.engine.strategy.source.SourceProcessor;
import org.sentrysoftware.metricshub.engine.strategy.source.SourceTable;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;

/**
 * This class implements the {@link JawkExtension} contract, reports the supported features, processes sources and computes.
 */
@Slf4j
@Builder
@EqualsAndHashCode(callSuper = false)
@AllArgsConstructor
public class MetricsHubExtensionForJawk extends AbstractExtension implements JawkExtension {

	/**
	 * The list of keywords supported by this extension.
	 */
	protected static final String[] KEYWORDS = {
		GET_SOURCE,
		EXECUTE_HTTP_REQUEST,
		EXECUTE_IPMI_REQUEST,
		EXECUTE_SNMP_GET,
		EXECUTE_SNMP_TABLE,
		EXECUTE_WBEM_REQUEST,
		EXECUTE_WMI_REQUEST,
		JSON_2CSV
	};

	private TelemetryManager telemetryManager;

	private String connectorId;

	private SourceProcessor sourceProcessor;

	@Override
	public String getExtensionName() {
		return "MetricsHubExtensionForJawk";
	}

	@Override
	public String[] extensionKeywords() {
		return KEYWORDS;
	}

	@Override
	public int[] getAssocArrayParameterPositions(final String extensionKeyword, final int numArgs) {
		log.error("getAssocArrayParameterPositions");

		if (extensionKeyword.equals(GET_SOURCE) || extensionKeyword.equals(EXECUTE_IPMI_REQUEST)) {
			return new int[] {};
		} else if (extensionKeyword.equals(EXECUTE_SNMP_GET)) {
			return new int[] { 2 };
		} else if (
			extensionKeyword.equals(EXECUTE_SNMP_TABLE) ||
			extensionKeyword.equals(EXECUTE_WBEM_REQUEST) ||
			extensionKeyword.equals(EXECUTE_WMI_REQUEST) ||
			extensionKeyword.equals(JSON_2CSV)
		) {
			return new int[] { 3 };
		} else if (extensionKeyword.equals(EXECUTE_HTTP_REQUEST)) {
			return new int[] { 7 };
		} else {
			return super.getAssocArrayParameterPositions(extensionKeyword, numArgs);
		}
	}

	@Override
	public Object invoke(String keyword, Object[] args) {
		log.error("invoke");

		if (keyword.equals(GET_SOURCE)) {
			checkNumArgs(args, 1);
			return getSource(toAwkString(args[0]));
		} else if (keyword.equals(EXECUTE_HTTP_REQUEST)) {
			checkNumArgs(args, 7);
			return executeHttpRequest(args);
		} else if (keyword.equals(EXECUTE_IPMI_REQUEST)) {
			checkNumArgs(args, 1);
			return executeIpmiRequest(args);
		} else if (keyword.equals(EXECUTE_SNMP_GET)) {
			checkNumArgs(args, 2);
			return executeSnmpGetRequest(args);
		} else if (keyword.equals(EXECUTE_SNMP_TABLE)) {
			checkNumArgs(args, 3);
			return executeSnmpTableRequest(args);
		} else if (keyword.equals(EXECUTE_WBEM_REQUEST)) {
			checkNumArgs(args, 3);
			return executeWbemRequest(args);
		} else if (keyword.equals(EXECUTE_WMI_REQUEST)) {
			checkNumArgs(args, 3);
			return executeWmiRequest(args);
		} else if (keyword.equals(JSON_2CSV)) {
			checkNumArgs(args, 3);
			return executeJson2csv(args);
		} else {
			throw new NotImplementedError(keyword);
		}
	}

	/**
	 * Use the {@link Source} name to retrieve its table from the context.
	 * @param sourceName The Source name.
	 * @return The Source table.
	 */
	private List<List<String>> getSource(final String sourceName) {
		final Optional<SourceTable> maybeSourceTable = SourceTable.lookupSourceTable(
			sourceName,
			connectorId,
			telemetryManager
		);
		return maybeSourceTable.isEmpty() ? null : maybeSourceTable.get().getTable();
	}

	/**
	 * Execute a HTTP request through the context.
	 * @param args The array of arguments to use to create the {@link HttpSource}.
	 * @return The table result from the execution of the {@link HttpSource}.
	 */
	private List<List<String>> executeHttpRequest(final Object[] args) {
		return executeSource(
			HttpSource
				.builder()
				.method(HttpMethod.valueOf(toAwkString(args[0])))
				.path(toAwkString(args[1]))
				.header(toAwkString(args[2]))
				.body(toAwkString(args[3]))
				.authenticationToken(toAwkString(args[4]))
				.resultContent(ResultContent.detect(toAwkString(args[5])))
				.forceSerialization(toAwkString(args[6]).equals("true"))
				.build()
		);
	}

	/**
	 * Execute a Ipmi request through the context.
	 * @param args The array of arguments to use to create the {@link IpmiSource}.
	 * @return The table result from the execution of the {@link IpmiSource}.
	 */
	private List<List<String>> executeIpmiRequest(Object[] args) {
		return executeSource(IpmiSource.builder().forceSerialization(toAwkString(args[0]).equals("true")).build());
	}

	/**
	 * Execute a Snmp Get request through the context.
	 * @param args The array of arguments to use to create the {@link SnmpGetSource}.
	 * @return The table result from the execution of the {@link SnmpGetSource}.
	 */
	private List<List<String>> executeSnmpGetRequest(final Object[] args) {
		return executeSource(
			SnmpGetSource.builder().oid(toAwkString(args[0])).forceSerialization(toAwkString(args[1]).equals("true")).build()
		);
	}

	/**
	 * Execute a Snmp Table request through the context.
	 * @param args The array of arguments to use to create the {@link SnmpTableSource}.
	 * @return The table result from the execution of the {@link SnmpTableSource}.
	 */
	private List<List<String>> executeSnmpTableRequest(final Object[] args) {
		return executeSource(
			SnmpTableSource
				.builder()
				.oid(toAwkString(args[0]))
				.selectColumns(toAwkString(args[1]))
				.forceSerialization(toAwkString(args[2]).equals("true"))
				.build()
		);
	}

	/**
	 * Execute a Wbem request through the context.
	 * @param args The array of arguments to use to create the {@link WbemSource}.
	 * @return The table result from the execution of the {@link WbemSource}.
	 */
	private List<List<String>> executeWbemRequest(final Object[] args) {
		return executeSource(
			WbemSource
				.builder()
				.query(toAwkString(args[0]))
				.namespace(toAwkString(args[1]))
				.forceSerialization(toAwkString(args[2]).equals("true"))
				.build()
		);
	}

	/**
	 * Execute a Wmi request through the context.
	 * @param args The array of arguments to use to create the {@link WmiSource}.
	 * @return The table result from the execution of the {@link WmiSource}.
	 */
	private List<List<String>> executeWmiRequest(final Object[] args) {
		return executeSource(
			WmiSource
				.builder()
				.query(toAwkString(args[0]))
				.namespace(toAwkString(args[1]))
				.forceSerialization(toAwkString(args[2]).equals("true"))
				.build()
		);
	}

	/**
	 * Execute a {@link Source} through the context.
	 * @param source The {@link Source} to execute.
	 * @return The table result from the execution of the {@link Source}.
	 */
	private List<List<String>> executeSource(final Source source) {
		final SourceTable sourceTableResult = source.accept(sourceProcessor);

		return sourceTableResult != null && !sourceTableResult.isEmpty() ? sourceTableResult.getTable() : new ArrayList<>();
	}

	/**
	 * Execute a {@link Json2Csv} compute on the current source through the context.
	 * @param args The array of arguments to use to create the {@link Json2Csv} compute.
	 * @return The table result from the execution of the compute.
	 */
	private List<List<String>> executeJson2csv(final Object[] args) {
		final SourceTable sourceTable = SourceTable.builder().rawData(toAwkString(args[0])).build();
		// computeProcessor.setSourceTable(sourceTable); TODO
		executeCompute(
			Json2Csv
				.builder()
				.entryKey(toAwkString(args[1]))
				.properties(toAwkString(args[2]))
				.separator(toAwkString(args[3]))
				.build()
		);
		return sourceTable.getTable();
	}

	/**
	 * Execute a {@link Compute} through the context.
	 * @param compute The {@link Compute} to execute.
	 * @return The table result from the execution of the {@link Compute}.
	 */
	private void executeCompute(final Compute compute) {
		//		compute.accept(computeProcessor);
	}
}
