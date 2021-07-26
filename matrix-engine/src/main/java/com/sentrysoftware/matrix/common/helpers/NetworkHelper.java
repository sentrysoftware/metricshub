package com.sentrysoftware.matrix.common.helpers;

import com.sentrysoftware.matrix.common.exception.LocalhostCheckException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Class helper to get network information
 */
@Slf4j
public class NetworkHelper {

	private NetworkHelper() {
	}

	/**
	 * Check whether the given hostname is a localhost
	 * 
	 * @param hostname
	 * @return <code>true</code> if the passed hostname is a localhost
	 * @throws LocalhostCheckException 
	 */
	public static boolean isLocalhost(final String hostname) throws LocalhostCheckException {
		Assert.notNull(hostname, "hostname cannot be null.");
		Assert.isTrue(!hostname.trim().isEmpty(), "hostname cannot be empty.");

		InetAddress inetAddress = null;
		try {
			inetAddress = InetAddress.getByName(hostname);
		} catch (UnknownHostException e) {
			String message = String.format("Error detected on InetAddress.getByName(%s)", hostname);
			log.error(message, e);
			throw new LocalhostCheckException(message, e);
		}

		if (inetAddress != null) {
			// Check if the address is a valid local or loop back
			if (inetAddress.isAnyLocalAddress() || inetAddress.isLoopbackAddress())
				return true;

			// Check if the address is defined on any interface
			try {
				return NetworkInterface.getByInetAddress(inetAddress) != null;
			} catch (SocketException e) {
				final String message = String.format(
						"Error detected on NetworkInterface.getByInetAddress(%s) for hostname: %s",
						inetAddress.toString(), hostname);
				log.error(message, e);
				throw new LocalhostCheckException(message, e);
			}
		}

		final String message = String.format("Cannot check if %s is localhost or not", hostname);
		log.error(message);
		throw new LocalhostCheckException(message);
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
				log.error(String.format("Error detected on InetAddress.getByName(%s)", hostname), e);
				throw e;
			}
		}

		if (inetAddress != null) {
			fqdn = inetAddress.getCanonicalHostName();
		}

		return fqdn;
	}
}
