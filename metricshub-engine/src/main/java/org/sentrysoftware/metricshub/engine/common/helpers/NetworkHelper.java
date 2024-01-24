package org.sentrysoftware.metricshub.engine.common.helpers;

/*-
 * ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲
 * MetricsHub Engine
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

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;

/**
 * Class helper to get network information
 */
@Slf4j
public class NetworkHelper {

	/**
	 * Typical localhost names, which we should figure out immediately
	 */
	private static final Set<String> TYPICAL_LOCALHOST_HOSTNAMES = Set.of(
		"localhost",
		"127.0.0.1",
		"::1",
		"0:0:0:0:0:0:0:1",
		"0000:0000:0000:0000:0000:0000:0000:0001"
	);

	private NetworkHelper() {}

	/**
	 * Check whether the given hostname is a localhost
	 *
	 * @param hostname
	 * @return <code>true</code> if the passed hostname is a localhost
	 */
	public static boolean isLocalhost(final String hostname) {
		// Empty or null hostname is assumed local
		if (hostname == null || hostname.isBlank()) {
			return true;
		}

		// Obvious case
		if (TYPICAL_LOCALHOST_HOSTNAMES.contains(hostname.toLowerCase())) {
			return true;
		}

		// Try to resolve the provided hostname
		InetAddress inetAddress = null;
		try {
			inetAddress = InetAddress.getByName(hostname);
		} catch (UnknownHostException e) {
			log.warn(
				"Hostname {} - Could not resolve the hostname to a valid IP address. The host is considered remote.",
				hostname
			);
			return false;
		}

		if (inetAddress == null) {
			log.warn(
				"Hostname {} - Could not resolve the hostname to a valid IP address. The host is considered remote.",
				hostname
			);
			return false;
		}

		// Check if the address is a valid local or loop back
		if (inetAddress.isAnyLocalAddress() || inetAddress.isLoopbackAddress()) {
			return true;
		}

		// Check if the address is defined on any interface
		try {
			return NetworkInterface.getByInetAddress(inetAddress) != null;
		} catch (SocketException e) {
			log.warn(
				"Hostname {} - Could not find a network interface associated to this IP address. The network interface is considered remote.",
				hostname
			);
			log.debug("Hostname {} - Exception while checking network interfaces: ", hostname, e);
			return false;
		}
	}

	/**
	 * @param hostname					The hostname whose IP is being searched for.
	 *
	 * @return							The IP Address of the given hostname
	 */

	public static String resolveDns(final String hostname) {
		String ipAddress = null;
		InetAddress inetAddress = null;

		if (hostname != null && !hostname.isBlank()) {
			try {
				inetAddress = InetAddress.getByName(hostname);

				if (inetAddress != null) {
					ipAddress = inetAddress.getHostAddress();
				}
			} catch (UnknownHostException e) {
				log.warn("Could not resolve {} into an IP address.", hostname);
				log.debug("UnknownHostException: ", e);
			}
		}

		return ipAddress;
	}

	/**
	 * @param hostname					The hostname whose FQDN is being searched for.
	 *
	 * @return							The FQDN of the given hostname
	 */
	public static String getFqdn(final String hostname) {
		if (hostname != null && !hostname.isBlank()) {
			try {
				final InetAddress inetAddress = InetAddress.getByName(hostname);
				if (inetAddress != null) {
					return inetAddress.getCanonicalHostName();
				}
			} catch (UnknownHostException e) {
				log.error(
					"Hostname {} - Could not resolve the hostname to a valid IP address. Cannot retrieve FQDN. Using hostname as FQDN.",
					hostname
				);
				return hostname;
			}
		}

		return hostname;
	}
}
