package com.sentrysoftware.matrix.engine.strategy;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol.Privacy;
import com.sentrysoftware.matsya.snmp.SNMPClient;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class MatsyaClientsExecutor {

	/**
	 * Run the given {@link Callable} using the passed timeout in seconds.
	 * 
	 * @param callable
	 * @param timeout
	 * @return {@link String} result returned by the callable.
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	private String execute(final Callable<String> callable, final long timeout)
			throws InterruptedException, ExecutionException, TimeoutException {
		final ExecutorService executorService = Executors.newSingleThreadExecutor();
		try {
			final Future<String> handler = executorService.submit(callable);

			return handler.get(timeout, TimeUnit.SECONDS);
		} finally {
			executorService.shutdownNow();
		}
	}

	/**
	 * Execute SNMP GetNext request through Matsya
	 * 
	 * @param oid
	 * @param protocol
	 * @param hostname
	 * @param logMode
	 * @return {@link String} value
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	public String executeSNMPGetNext(final String oid, final SNMPProtocol protocol, final String hostname,
			final boolean logMode) throws InterruptedException, ExecutionException, TimeoutException {
		Assert.notNull(oid, "oid cannot be null");
		Assert.notNull(protocol, "protocol cannot be null");
		Assert.notNull(hostname, "strategyConfig cannot be null");

		return executeSNMPGetRequest(SNMPGetRequest.GETNEXT, oid, protocol, hostname, logMode);

	}

	/**
	 * Execute SNMP GetNext request through Matsya
	 * 
	 * @param oid
	 * @param protocol
	 * @param hostname
	 * @param logMode
	 * @return {@link String} value
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	public String executeSNMPGet(final String oid, final SNMPProtocol protocol, final String hostname,
			final boolean logMode) throws InterruptedException, ExecutionException, TimeoutException {
		Assert.notNull(oid, "oid cannot be null");
		Assert.notNull(protocol, "protocol cannot be null");
		Assert.notNull(hostname, "hostname cannot be null");

		return executeSNMPGetRequest(SNMPGetRequest.GET, oid, protocol, hostname, logMode);

	}

	private String executeSNMPGetRequest(final SNMPGetRequest request, final String oid, final SNMPProtocol protocol,
			final String hostname, final boolean logMode)
			throws InterruptedException, ExecutionException, TimeoutException {
		final int port = protocol.getPort();
		final int version = protocol.getVersion().getIntVersion();
		final int[] retryIntervals = null;
		final String community = protocol.getCommunity();
		final String authType = protocol.getVersion().getAuthType();
		final String authUsername = protocol.getUsername();
		final String authPassword = protocol.getPassword();
		final String privacyType = protocol.getPrivacy() != Privacy.NO_ENCRYPTION && protocol.getPrivacy() != null
				? protocol.getPrivacy().name()
				: null;

		final String privacyPassword = protocol.getPrivacyPassword();
		final String contextName = Thread.currentThread().getName();
		final byte[] contextID = String.valueOf(Thread.currentThread().getId()).getBytes();

		// Create the Matsya SNMPClient and run the GetNext request
		return execute(() -> {

			final SNMPClient snmpClient = new SNMPClient(hostname, port, version, retryIntervals, community, authType,
					authUsername, authPassword, privacyType, privacyPassword, contextName, contextID);

			try {
				switch (request) {
				case GET:
					return snmpClient.get(oid);
				default:
				case GETNEXT:
					return snmpClient.getNext(oid);
				}
			} catch (Exception e) {
				if (logMode) {
					log.error("Error detected when running SNMP {} query OID:{} on HOST:{}", request, oid, hostname);
				}
				return null;
			} finally {
				snmpClient.freeResources();
			}
		}, protocol.getTimeout());
	}

	public enum SNMPGetRequest {
		GET, GETNEXT
	}
}
