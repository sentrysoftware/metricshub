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

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import lombok.NonNull;

/**
 * 
 * Interface defining the contract for executing SNMP (Simple Network Management Protocol) requests
 * on a specified host.
 */
public interface ISnmpRequestExecutor {

	/**
	 * Execute SNMP GetNext request
	 *
	 * @param oid            The Object Identifier (OID) for the SNMP GETNEXT request.
	 * @param configuration  The SNMP configuration specifying parameters like version, community, etc.
	 * @param hostname       The hostname or IP address of the SNMP-enabled device.
	 * @param logMode        A boolean indicating whether to log errors and warnings during execution.
	 * @return The SNMP response as a String value.
	 * @throws InterruptedException If the execution is interrupted.
	 * @throws ExecutionException  If an exception occurs during execution.
	 * @throws TimeoutException    If the execution times out.
	 */
	String executeSNMPGetNext(@NonNull String oid, @NonNull ISnmpConfiguration configuration, @NonNull String hostname,
			boolean logMode) throws InterruptedException, ExecutionException, TimeoutException;

	/**
	 * Execute SNMP Get request
	 *
	 * @param oid            The Object Identifier (OID) for the SNMP GET request.
	 * @param configuration  The SNMP configuration specifying parameters like version, community, etc.
	 * @param hostname       The hostname or IP address of the SNMP-enabled device.
	 * @param logMode        A boolean indicating whether to log errors and warnings during execution.
	 * @return The SNMP response as a String value.
	 * @throws InterruptedException If the execution is interrupted.
	 * @throws ExecutionException  If an exception occurs during execution.
	 * @throws TimeoutException    If the execution times out.
	 */
	String executeSNMPGet(@NonNull String oid, @NonNull ISnmpConfiguration configuration, @NonNull String hostname,
			boolean logMode) throws InterruptedException, ExecutionException, TimeoutException;

	/**
	 * Execute SNMP Table
	 *
	 * @param oid               The SNMP Object Identifier (OID) representing the table.
	 * @param selectColumnArray An array of column names to select from the SNMP table.
	 * @param configuration     The SNMP configuration containing connection details.
	 * @param hostname          The hostname or IP address of the SNMP-enabled device.
	 * @param logMode           Flag indicating whether to log warnings in case of errors.
	 * @return A list of rows, where each row is a list of string cells representing the SNMP table.
	 * @throws InterruptedException If the thread executing this method is interrupted.
	 * @throws ExecutionException  If an exception occurs during the execution of the SNMP request.
	 * @throws TimeoutException    If the SNMP request times out.
	 */
	List<List<String>> executeSNMPTable(@NonNull String oid, @NonNull String[] selectColumnArray,
			@NonNull ISnmpConfiguration configuration, @NonNull String hostname, boolean logMode)
			throws InterruptedException, ExecutionException, TimeoutException;
	
}