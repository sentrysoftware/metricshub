package org.sentrysoftware.metricshub.cli.snmpv3;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;
import picocli.CommandLine.ParameterException;

class SnmpV3CliTest {

	SnmpV3Cli snmpV3Cli;
	CommandLine commandLine;

	static String SNMP_OID = "1.3.6.1.4.1.64.58.5.5.1.20.10.4";
	static String SNMP_VERSION = "v3";
	static String SNMP_COMMUNITY = "public";

	void initCli() {
		snmpV3Cli = new SnmpV3Cli();
		commandLine = new CommandLine(snmpV3Cli);
	}

	void execute(String snmpMethod) {
		commandLine.execute(
			"hostname",
			snmpMethod,
			SNMP_OID,
			"--snmpv3",
			"--snmpv3-retryIntervals",
			"1000",
			"--snmpv3-retryIntervals",
			"6000"
		);
	}

	@Test
	void testExecute() {
		initCli();
		execute("--snmpv3-get");
		assertEquals(SNMP_OID, snmpV3Cli.get);
		execute("--snmpv3-getnext");
		assertEquals(SNMP_OID, snmpV3Cli.getNext);
		execute("--snmpv3-walk");
		assertEquals(SNMP_OID, snmpV3Cli.walk);
	}

	@Test
	void testGetQuery() {
		initCli();
		execute("--snmpv3-get");
		JsonNode snmpQuery = snmpV3Cli.getQuery();
		assertEquals("get", snmpQuery.get("action").asText());
		assertEquals(SNMP_OID, snmpQuery.get("oid").asText());

		execute("--snmpv3-getnext");
		snmpQuery = snmpV3Cli.getQuery();
		assertEquals("getNext", snmpQuery.get("action").asText());
		assertEquals(SNMP_OID, snmpQuery.get("oid").asText());

		execute("--snmpv3-walk");
		snmpQuery = snmpV3Cli.getQuery();
		assertEquals("walk", snmpQuery.get("action").asText());
		assertEquals(SNMP_OID, snmpQuery.get("oid").asText());
	}

	@Test
	void testValidate() {
		initCli();
		ParameterException noQueriesException = assertThrows(ParameterException.class, () -> snmpV3Cli.validate());
		assertEquals(
			"At least one SNMP V3 query must be specified: --snmpv3-get, --snmpv3-getnext, --snmpv3-walk.",
			noQueriesException.getMessage()
		);
		snmpV3Cli.setGet(SNMP_OID);
		assertDoesNotThrow(() -> snmpV3Cli.validate());
		snmpV3Cli.setGetNext(SNMP_OID);
		ParameterException manyQueriesException = assertThrows(ParameterException.class, () -> snmpV3Cli.validate());
		assertEquals(
			"Only one SNMP V3 query can be specified at a time: --snmpv3-get, --snmpv3-getnext, --snmpv3-walk.",
			manyQueriesException.getMessage()
		);
	}
}
