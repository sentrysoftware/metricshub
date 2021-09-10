package com.sentrysoftware.matrix.common.helpers;

import lombok.extern.slf4j.Slf4j;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Set;

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

	private NetworkHelper() {
	}

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
			log.error("Unknown host %s. Assuming non-local.", hostname);
			return false;
		}

		if (inetAddress == null) {
			log.error("Could not resolve %s into an IP addrress. Assuming non-local.", hostname);
			return false;
		}

		// Check if the address is a valid local or loop back
		if (inetAddress.isAnyLocalAddress() || inetAddress.isLoopbackAddress())
			return true;

		// Check if the address is defined on any interface
		try {
			return NetworkInterface.getByInetAddress(inetAddress) != null;
		} catch (SocketException e) {
			log.error("Error while checking network interfaces. Assuming non-local.", e);
			return false;
		}
	}


	/**
	 * @param hostname					The hostname whose FQDN is being searched for.
	 *
	 * @return							The FQDN of the given hostname
	 * @throws UnknownHostException		If the given hostname cannot be resolved.
	 */
	public static String getFqdn(String hostname) throws UnknownHostException  {

		String fqdn = hostname;
		InetAddress inetAddress = null;

		if (hostname != null && !hostname.isBlank()) {
			try {
				inetAddress = InetAddress.getByName(hostname);
			} catch (UnknownHostException e) {
				log.error("Unknown host: {}", hostname);
				throw e;
			}
		}

		if (inetAddress != null) {
			fqdn = inetAddress.getCanonicalHostName();
		}

		return fqdn;
	}
}
