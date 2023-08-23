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
			log.warn("Hostname {} - Could not resolve the hostname to a valid IP address. The host is considered remote.", hostname);
			return false;
		}

		if (inetAddress == null) {
			log.warn("Hostname {} - Could not resolve the hostname to a valid IP address. The host is considered remote.", hostname);
			return false;
		}

		// Check if the address is a valid local or loop back
		if (inetAddress.isAnyLocalAddress() || inetAddress.isLoopbackAddress())
			return true;

		// Check if the address is defined on any interface
		try {
			return NetworkInterface.getByInetAddress(inetAddress) != null;
		} catch (SocketException e) {
			log.warn("Hostname {} - Could not find a network interface associated to this IP address. The network interface is considered remote.", hostname);
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
				log.error("Hostname {} - Could not resolve the hostname to a valid IP address. Cannot retrieve FQDN. Using hostname as FQDN.", hostname);
				return hostname;
			}
		}

		return hostname;
	}
}
