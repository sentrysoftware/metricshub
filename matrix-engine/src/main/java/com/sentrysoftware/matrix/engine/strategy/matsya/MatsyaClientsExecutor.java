package com.sentrysoftware.matrix.engine.strategy.matsya;

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

	private static final String SELECTED_COLUMN_CANNOT_BE_NULL = "selectedColumn cannot be null";
	private static final String HOSTNAME_CANNOT_BE_NULL = "hostname cannot be null";
	private static final String PROTOCOL_CANNOT_BE_NULL = "protocol cannot be null";
	private static final String OID_CANNOT_BE_NULL = "oid cannot be null";

	/**
	 * Run the given {@link Callable} using the passed timeout in seconds.
	 * @param <T>
	 * @param <T>
	 * 
	 * @param callable
	 * @param timeout
	 * @return {@link IMatsyaQueryResult} result returned by the callable.
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	private <T> T execute(final Callable<T> callable, final long timeout)
			throws InterruptedException, ExecutionException, TimeoutException {
		final ExecutorService executorService = Executors.newSingleThreadExecutor();
		try {
			final Future<T> handler = executorService.submit(callable);

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
	 * @return {@link IMatsyaQueryResult} value
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	public IMatsyaQueryResult executeSNMPGetNext(final String oid, final SNMPProtocol protocol, final String hostname,
			final boolean logMode) throws InterruptedException, ExecutionException, TimeoutException {
		Assert.notNull(oid, OID_CANNOT_BE_NULL);
		Assert.notNull(protocol, PROTOCOL_CANNOT_BE_NULL);
		Assert.notNull(hostname, HOSTNAME_CANNOT_BE_NULL);

		return executeSNMPGetRequest(SNMPGetRequest.GETNEXT, oid, protocol, hostname, null, logMode);

	}

	/**
	 * Execute SNMP Get request through Matsya
	 * 
	 * @param oid
	 * @param protocol
	 * @param hostname
	 * @param logMode
	 * @return {@link IMatsyaQueryResult} value
	 * @throws InterruptedException
	 * @throws ExecutionException
	 * @throws TimeoutException
	 */
	public IMatsyaQueryResult executeSNMPGet(final String oid, final SNMPProtocol protocol, final String hostname,
			final boolean logMode) throws InterruptedException, ExecutionException, TimeoutException {
		Assert.notNull(oid, OID_CANNOT_BE_NULL);
		Assert.notNull(protocol, PROTOCOL_CANNOT_BE_NULL);
		Assert.notNull(hostname, HOSTNAME_CANNOT_BE_NULL);

		return executeSNMPGetRequest(SNMPGetRequest.GET, oid, protocol, hostname, null, logMode);

	}
	
	public IMatsyaQueryResult executeSNMPTable(final String oid, String[] selectColumnArray, final SNMPProtocol protocol, final String hostname,
			final boolean logMode) throws InterruptedException, ExecutionException, TimeoutException {
		Assert.notNull(oid, OID_CANNOT_BE_NULL);
		Assert.notNull(protocol, PROTOCOL_CANNOT_BE_NULL);
		Assert.notNull(hostname, HOSTNAME_CANNOT_BE_NULL);
		Assert.notNull(selectColumnArray, SELECTED_COLUMN_CANNOT_BE_NULL);

		return executeSNMPGetRequest(SNMPGetRequest.TABLE, oid, protocol, hostname, selectColumnArray, logMode);

	}

	private IMatsyaQueryResult executeSNMPGetRequest(final SNMPGetRequest request, final String oid, final SNMPProtocol protocol,
			final String hostname, final String[] selectColumnArray, final boolean logMode)
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
					return MatsyaStringResult.builder().data(snmpClient.get(oid)).build();
				case GETNEXT:
					return MatsyaStringResult.builder().data(snmpClient.getNext(oid)).build();
				case TABLE : 
					return MatsyaListResult.builder().data(snmpClient.table(oid, selectColumnArray)).build();
				default : 
					throw new IllegalArgumentException("Not implemented.");
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
		GET, GETNEXT, TABLE
	}
}
