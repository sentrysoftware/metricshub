package org.sentrysoftware.metricshub.extension.oscommand;

/*-
 * ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲
 * MetricsHub OsCommand Extension
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

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.sentrysoftware.metricshub.engine.common.helpers.LoggingHelper;
import org.sentrysoftware.metricshub.engine.strategy.source.SourceTable;
import org.sentrysoftware.metricshub.engine.strategy.utils.IpmiHelper;
import org.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;

@Slf4j
@AllArgsConstructor
public class UnixIpmiSourceProcessor {

	/**
	 * Process IPMI Source for the Unix system
	 *
	 * @param sourceKey The key of the source
	 *
	 * @return {@link SourceTable} containing the IPMI result expected by the IPMI connector embedded AWK script
	 */
	SourceTable processUnixIpmiSource(
		final String sourceKey,
		final String connectorId,
		final TelemetryManager telemetryManager
	) {
		final String hostname = telemetryManager.getHostConfiguration().getHostname();

		// get the ipmiTool command to execute
		String ipmitoolCommand = telemetryManager.getHostProperties().getIpmitoolCommand();
		if (ipmitoolCommand == null || ipmitoolCommand.isEmpty()) {
			final String message = String.format(
				"Hostname %s - IPMI tool command cannot be found. Returning an empty result.",
				hostname
			);
			log.error(message);
			return SourceTable.empty();
		}

		final boolean isLocalHost = telemetryManager.getHostProperties().isLocalhost();

		final SshConfiguration sshConfiguration = (SshConfiguration) telemetryManager
			.getHostConfiguration()
			.getConfigurations()
			.get(SshConfiguration.class);

		final OsCommandConfiguration osCommandConfiguration = (OsCommandConfiguration) telemetryManager
			.getHostConfiguration()
			.getConfigurations()
			.get(OsCommandConfiguration.class);

		final long defaultTimeout = osCommandConfiguration != null
			? osCommandConfiguration.getTimeout()
			: OsCommandConfiguration.DEFAULT_TIMEOUT;

		// fru command
		String fruCommand = ipmitoolCommand + "fru";
		String fruResult;
		try {
			if (isLocalHost) {
				fruResult = OsCommandHelper.runLocalCommand(fruCommand, defaultTimeout, null);
			} else if (sshConfiguration != null) {
				fruResult = OsCommandHelper.runSshCommand(fruCommand, hostname, sshConfiguration, defaultTimeout, null, null);
			} else {
				log.warn("Hostname {} - Could not process UNIX IPMI Source. SSH protocol credentials are missing.", hostname);
				return SourceTable.empty();
			}

			log.debug("Hostname {} - IPMI OS command: {}:\n{}", hostname, fruCommand, fruResult);
		} catch (Exception e) {
			LoggingHelper.logSourceError(
				connectorId,
				sourceKey,
				String.format("IPMI OS command: %s.", fruCommand),
				hostname,
				e
			);

			Thread.currentThread().interrupt();

			return SourceTable.empty();
		}

		// "-v sdr elist all"
		String sdrCommand = ipmitoolCommand + "-v sdr elist all";
		String sensorResult;
		try {
			if (isLocalHost) {
				sensorResult = OsCommandHelper.runLocalCommand(sdrCommand, defaultTimeout, null);
			} else {
				sensorResult =
					OsCommandHelper.runSshCommand(sdrCommand, hostname, sshConfiguration, defaultTimeout, null, null);
			}
			log.debug("Hostname {} - IPMI OS command: {}:\n{}", hostname, sdrCommand, sensorResult);
		} catch (Exception e) {
			LoggingHelper.logSourceError(
				connectorId,
				sourceKey,
				String.format("IPMI OS command: %s.", sdrCommand),
				hostname,
				e
			);

			Thread.currentThread().interrupt();

			return SourceTable.empty();
		}

		return SourceTable.builder().table(IpmiHelper.ipmiTranslateFromIpmitool(fruResult, sensorResult)).build();
	}
}
