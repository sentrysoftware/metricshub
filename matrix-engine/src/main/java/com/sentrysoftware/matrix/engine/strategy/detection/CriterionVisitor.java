package com.sentrysoftware.matrix.engine.strategy.detection;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sentrysoftware.matrix.connector.model.detection.criteria.http.HTTP;
import com.sentrysoftware.matrix.connector.model.detection.criteria.ipmi.IPMI;
import com.sentrysoftware.matrix.connector.model.detection.criteria.kmversion.KMVersion;
import com.sentrysoftware.matrix.connector.model.detection.criteria.os.OS;
import com.sentrysoftware.matrix.connector.model.detection.criteria.oscommand.OSCommand;
import com.sentrysoftware.matrix.connector.model.detection.criteria.process.Process;
import com.sentrysoftware.matrix.connector.model.detection.criteria.service.Service;
import com.sentrysoftware.matrix.connector.model.detection.criteria.snmp.SNMPGet;
import com.sentrysoftware.matrix.connector.model.detection.criteria.snmp.SNMPGetNext;
import com.sentrysoftware.matrix.connector.model.detection.criteria.telnet.TelnetInteractive;
import com.sentrysoftware.matrix.connector.model.detection.criteria.ucs.UCS;
import com.sentrysoftware.matrix.connector.model.detection.criteria.wbem.WBEM;
import com.sentrysoftware.matrix.connector.model.detection.criteria.wmi.WMI;
import com.sentrysoftware.matrix.engine.protocol.IProtocolConfiguration;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol;
import com.sentrysoftware.matrix.engine.strategy.MatsyaClientsExecutor;
import com.sentrysoftware.matrix.engine.strategy.StrategyConfig;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class CriterionVisitor implements ICriterionVisitor {

	private static final Pattern SNMP_GETNEXT_RESULT_REGEX = Pattern.compile("\\w+\\s+\\w+\\s+(.*)");

	@Autowired
	private StrategyConfig strategyConfig;

	@Autowired
	private MatsyaClientsExecutor matsyaClientsExecutor;

	@Override
	public CriterionTestResult visit(final HTTP criterion) {
		// Not implemented yet
		return CriterionTestResult.empty();
	}

	@Override
	public CriterionTestResult visit(final IPMI ipmi) {
		// Not implemented yet
		return CriterionTestResult.empty();
	}

	@Override
	public CriterionTestResult visit(final KMVersion kmVersion) {
		// Not implemented yet
		return CriterionTestResult.empty();
	}

	@Override
	public CriterionTestResult visit(final OS os) {
		// Not implemented yet
		return CriterionTestResult.empty();
	}

	@Override
	public CriterionTestResult visit(final OSCommand osCommand) {
		// Not implemented yet
		return CriterionTestResult.empty();
	}

	@Override
	public CriterionTestResult visit(final Process process) {
		// Not implemented yet
		return CriterionTestResult.empty();
	}

	@Override
	public CriterionTestResult visit(final Service service) {
		// Not implemented yet
		return CriterionTestResult.empty();
	}

	@Override
	public CriterionTestResult visit(final SNMPGet snmpGet) {
		// Not implemented yet
		return CriterionTestResult.empty();
	}

	@Override
	public CriterionTestResult visit(final TelnetInteractive telnetInteractive) {
		// Not implemented yet
		return CriterionTestResult.empty();
	}

	@Override
	public CriterionTestResult visit(final UCS ucs) {
		// Not implemented yet
		return CriterionTestResult.empty();
	}

	@Override
	public CriterionTestResult visit(final WBEM wbem) {
		// Not implemented yet
		return CriterionTestResult.empty();
	}

	@Override
	public CriterionTestResult visit(final WMI wmi) {
		// Not implemented yet
		return CriterionTestResult.empty();
	}

	@Override
	public CriterionTestResult visit(final SNMPGetNext snmpGetNext) {

		if (null == snmpGetNext || snmpGetNext.getOid() == null) {
			return CriterionTestResult.empty();
		}

		final Optional<IProtocolConfiguration> snmpProtocolOpt = strategyConfig.getEngineConfiguration()
				.getProtocolConfigurations().stream().filter(SNMPProtocol.class::isInstance).findFirst();

		if (!snmpProtocolOpt.isPresent()) {
			return CriterionTestResult.empty();
		}

		final SNMPProtocol protocol = (SNMPProtocol) snmpProtocolOpt.get();
		final String hostname = strategyConfig.getEngineConfiguration().getTarget().getHostname();

		try {

			final String result = matsyaClientsExecutor.executeSNMPGetNext(snmpGetNext.getOid(), protocol, hostname,
					false);
			final TestResult testResult = checkSNMPGetNextResult(hostname, snmpGetNext.getOid(),
					snmpGetNext.getExpectedResult(), result);
			return CriterionTestResult.builder().result(result).success(testResult.isSuccess())
					.message(testResult.getMessage()).build();
		} catch (Exception e) {
			final String message = String.format(
					"SNMP Test Failed - SNMP GetNext of %s on %s was unsuccessful due to an exception. Message: %s.",
					snmpGetNext.getOid(), hostname, e.getMessage());
			log.debug(message, e);
			return CriterionTestResult.builder().message(message).build();
		}
	}

	@Data
	@Builder
	public static class TestResult {
		private String message;
		private boolean success;
	}

	/**
	 * Verify the value returned by SNMP GetNext query. Check the value consistency
	 * when the expected output is not defined. Otherwise check if the value matches
	 * the expected regex.
	 * 
	 * @param hostname
	 * @param oid
	 * @param expected
	 * @param result
	 * @return {@link TestResult} wrapping the success status and the message
	 */
	private TestResult checkSNMPGetNextResult(final String hostname, final String oid, final String expected,
			final String result) {
		if (expected == null) {
			return checkSNMPGetNextValue(hostname, oid, result);
		}

		return checkSNMPGetNextExpectedValue(hostname, oid, expected, result);
	}

	/**
	 * Check if the result matches the expected value
	 * 
	 * @param hostname
	 * @param oid
	 * @param expected
	 * @param result
	 * @return {@link TestResult} wrapping the message and the success status
	 */
	private TestResult checkSNMPGetNextExpectedValue(final String hostname, final String oid, final String expected,
			final String result) {
		String message;
		boolean success = true;
		final Matcher matcher = SNMP_GETNEXT_RESULT_REGEX.matcher(result);
		if (matcher.find()) {
			final String value = matcher.group(1);
			if (!value.matches(expected)) {
				message = String.format(
						"SNMP Test Failed - SNMP GetNext of %s on %s was successful but the value of the returned OID did not match with the expected result. ",
						oid, hostname);
				message += String.format("Expected value: %s - returned value %s.", expected, value);
				success = false;
			} else {
				message = String.format("Successful SNMP GetNext of %s on %s. Returned Result: %s.", oid, hostname, result);
				success = true;
			}
		} else {
			message = String.format(
					"SNMP Test Failed - SNMP GetNext of %s on %s was successful but the value cannot be extracted. ",
					oid, hostname);
			message += String.format("Returned Result: %s.", result);
			success = false;
		}

		log.debug(message);

		return TestResult.builder().message(message).success(success).build();
	}

	/**
	 * Simply check the value consistency and verify whether the returned OID is
	 * under the same tree of the requested OID.
	 * 
	 * @param hostname
	 * @param oid
	 * @param result
	 * @return {@link TestResult} wrapping the message and the success status
	 */
	private TestResult checkSNMPGetNextValue(final String hostname, final String oid, final String result) {
		String message;
		boolean success = false;
		if (result == null) {
			message = String.format(
					"SNMP Test Failed - SNMP GetNext of %s on %s was unsuccessful due to a null result.", oid,
					hostname);
		} else if (result.trim().isEmpty()) {
			message = String.format(
					"SNMP Test Failed - SNMP GetNext of %s on %s was unsuccessful due to an empty result.", oid,
					hostname);
		} else if (!result.startsWith(oid)) {
			message = String.format(
					"SNMP Test Failed - SNMP GetNext of %s on %s was successful but the returned OID is not under the same tree. Returned OID: %s.",
					oid, hostname, result.split("\\s")[0]);
		} else {
			message = String.format("Successful SNMP GetNext of %s on %s. Returned Result: %s.", oid, hostname, result);
			success = true;
		}

		log.debug(message);

		return TestResult.builder().message(message).success(success).build();
	}

}
