package com.sentrysoftware.matrix.common.helpers;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Optional;

/**
 * Class helper to get network information
 */
public class NetworkHelper {

	private NetworkHelper() {
	}

	/**
	 * Check whether the given hostname is a localhost
	 * 
	 * @param hostname
	 * @return <code>true</code> if the passed hostname is a localhost
	 */
	public static boolean isLocalhost(final String hostname) {

		final Optional<InetAddress> inetAddressOpt = getInetAddress(hostname);

		if (inetAddressOpt.isPresent()) {
			final InetAddress inetAddress = inetAddressOpt.get();
		    // Check if the address is a valid local or loop back
		    if (inetAddress.isAnyLocalAddress() || inetAddress.isLoopbackAddress())
		        return true;

		    // Check if the address is defined on any interface
		    try {
		        return NetworkInterface.getByInetAddress(inetAddress) != null;
		    } catch (SocketException e) {
		        return false;
		    }
		}

		return false;
	}

	/**
	 * Get the InetAddress of the given hostname
	 * 
	 * @param hostname
	 * @return {@link InetAddress} of the given hostname
	 */
	private static Optional<InetAddress> getInetAddress(final String hostname) {

		if (hostname == null || hostname.trim().isEmpty()) {
			return Optional.empty();
		}

		try {
			return Optional.of(InetAddress.getByName(hostname));

		} catch (UnknownHostException ex) {
			return Optional.empty();
		}
	}
}
