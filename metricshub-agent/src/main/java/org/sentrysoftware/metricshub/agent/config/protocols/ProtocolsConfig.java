package org.sentrysoftware.metricshub.agent.config.protocols;

/*-
 * ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲
 * MetricsHub Agent
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

import static com.fasterxml.jackson.annotation.Nulls.SKIP;

import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Configuration class for various protocols.
 * Aggregates specific protocol configuration instances.
 */
@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class ProtocolsConfig {

	@JsonSetter(nulls = SKIP)
	private SnmpProtocolConfig snmp;

	@JsonSetter(nulls = SKIP)
	private IpmiProtocolConfig ipmi;

	@JsonSetter(nulls = SKIP)
	private SshProtocolConfig ssh;

	@JsonSetter(nulls = SKIP)
	private WbemProtocolConfig wbem;

	@JsonSetter(nulls = SKIP)
	private WmiProtocolConfig wmi;

	@JsonSetter(nulls = SKIP)
	private HttpProtocolConfig http;

	@JsonSetter(nulls = SKIP)
	private OsCommandProtocolConfig commandLine;

	@JsonSetter(nulls = SKIP)
	private WinRmProtocolConfig winrm;
}
