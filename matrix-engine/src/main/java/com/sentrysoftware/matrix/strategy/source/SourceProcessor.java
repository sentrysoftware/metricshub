package com.sentrysoftware.matrix.strategy.source;

import com.sentrysoftware.matrix.common.helpers.StringHelper;
import com.sentrysoftware.matrix.configuration.HttpConfiguration;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.CopySource;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.HttpSource;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.IpmiSource;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.OsCommandSource;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.SnmpSource;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.StaticSource;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.TableJoinSource;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.TableUnionSource;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.WbemSource;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.WmiSource;
import com.sentrysoftware.matrix.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.matrix.matsya.http.HttpRequest;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Slf4j
public class SourceProcessor implements ISourceProcessor {

	private TelemetryManager telemetryManager;
	private String connectorName;
	private MatsyaClientsExecutor matsyaClientsExecutor;

	@Override
	public SourceTable process(final CopySource copySource) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SourceTable process(final HttpSource httpSource) {

		final String hostname = telemetryManager.getHostConfiguration().getHostname();
		if (httpSource == null) {
			log.error("Hostname {} - HttpSource cannot be null, the HttpSource operation will return an empty result.", hostname);
			return SourceTable.empty();
		}

		final HttpConfiguration httpConfiguration = (HttpConfiguration) telemetryManager.getHostConfiguration()
			.getConfigurations().get(HttpConfiguration.class);

		if (httpConfiguration == null) {

			log.debug("Hostname {} - The HTTP credentials are not configured. Returning an empty table for HttpSource {}.",
				hostname, httpSource);

			return SourceTable.empty();
		}

		try {

			final String result = matsyaClientsExecutor.executeHttp(
				HttpRequest.builder()
					.hostname(hostname)
					.method(httpSource.getMethod().toString())
					.url(httpSource.getUrl())
					.header(httpSource.getHeader(), connectorName, hostname)
					.body(httpSource.getBody(), connectorName, hostname)
					.resultContent(httpSource.getResultContent())
					.authenticationToken(httpSource.getAuthenticationToken())
					.httpConfiguration(httpConfiguration)
					.build(),
				true);

			if (result != null && !result.isEmpty()) {

				return SourceTable
					.builder()
					.rawData(result)
					.build();
			}

		} catch (Exception e) {
			logSourceError(connectorName, httpSource.getKey(), String.format("HTTP %s %s", httpSource.getMethod(),
				httpSource.getUrl()) , hostname, e);
		}

		return SourceTable.empty();
	}

	@Override
	public SourceTable process(final IpmiSource ipmiSource) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SourceTable process(final OsCommandSource osCommandSource) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SourceTable process(final SnmpSource snmpSource) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SourceTable process(final StaticSource staticSource) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SourceTable process(final TableJoinSource tableJoinSource) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SourceTable process(final TableUnionSource tableUnionSource) {
		// TODO Auto-generated method stub
		return null;
	}

	public SourceTable process(final WbemSource wbemSource) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public SourceTable process(final WmiSource wmiSource) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * Log the given throwable
	 * 
	 * @param connectorName  The name of the connector
	 * @param sourceKey      The key of the source
	 * @param hostname       The host's hostname
	 * @param context        Additional information about the operation
	 * @param throwable      The catched throwable to log
	 */
	private static void logSourceError(
		final String connectorName,
		final String sourceKey,
		final String context,
		final String hostname,
		final Throwable throwable) {

		if (log.isErrorEnabled()) {
			log.error(
				"Hostname {} - Source [{}] was unsuccessful due to an exception."
				+ " Context [{}]. Connector: [{}]. Returning an empty table. Errors:\n{}\n",
				hostname, sourceKey, context, connectorName, StringHelper.getStackMessages(throwable));
		}

		if (log.isDebugEnabled()) {
			log.debug(String.format(
				"Hostname %s - Source [%s] was unsuccessful due to an exception. Context [%s]. Connector: [%s]. Returning an empty table. Stack trace:",
				hostname, sourceKey, context, connectorName), throwable);
		}
	}
}
