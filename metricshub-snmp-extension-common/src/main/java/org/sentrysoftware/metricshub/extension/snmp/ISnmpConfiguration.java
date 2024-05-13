package org.sentrysoftware.metricshub.extension.snmp;

/*-
 * ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲
 * MetricsHub Snmp Extension Common
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

import org.sentrysoftware.metricshub.engine.configuration.IConfiguration;

/**
 * The ISnmpConfiguration interface represents the configuration for Snmp
 * protocols in the MetricsHub extension system.
 */
public interface ISnmpConfiguration extends IConfiguration {

	/**
	 * Gets the timeout for the Snmp protocol
	 * 
	 * @return The timeout as a Long value.
	 */
	public Long getTimeout();

	/**
	 * Gets the community for the snmp protocol.
	 *
	 * @return The community as a character array.
	 */
	public char[] getCommunity();

	/**
	 * Gets the version for the Snmp protocol
	 * 
	 * @return The version as a int value.
	 */

	/**
	 * @return
	 */
	public int getIntVersion();

	/**
	 * Gets the port for the Snmp protocol
	 * 
	 * @return The port as a Integer value.
	 */
	public Integer getPort();

}
