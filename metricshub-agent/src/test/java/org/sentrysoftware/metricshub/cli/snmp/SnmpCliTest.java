package org.sentrysoftware.metricshub.cli.snmp;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import picocli.CommandLine;
import picocli.CommandLine.ParameterException;

class SnmpCliTest {

	SnmpCli snmpCli;
	CommandLine commandLine;

	static String SNMP_OID = "1.3.6.1.4.1.64.58.5.5.1.20.10.4";
	static String SNMP_VERSION = "v2c";
	static String SNMP_COMMUNITY = "public";

	void initCli() {
		snmpCli = new SnmpCli();
		commandLine = new CommandLine(snmpCli);
	}

	void execute(String snmpMethod) {
		commandLine.execute(
			"hostname",
			snmpMethod,
			SNMP_OID,
			"--snmp",
			SNMP_VERSION,
			"--community",
			SNMP_COMMUNITY,
			"--retry",
			"1000",
			"--retry",
			"6000"
		);
	}

	@Test
	void testExecute() {
		initCli();
		execute("--snmp-get");
		assertEquals(SNMP_OID, snmpCli.get);
		execute("--snmp-getnext");
		assertEquals(SNMP_OID, snmpCli.getNext);
		execute("--snmp-walk");
		assertEquals(SNMP_OID, snmpCli.walk);
	}

	@Test
	void testGetQuery() {
		initCli();
		execute("--snmp-get");
		JsonNode snmpQuery = snmpCli.getQuery();
		assertEquals("get", snmpQuery.get("action").asText());
		assertEquals(SNMP_OID, snmpQuery.get("oid").asText());

		execute("--snmp-getnext");
		snmpQuery = snmpCli.getQuery();
		assertEquals("getNext", snmpQuery.get("action").asText());
		assertEquals(SNMP_OID, snmpQuery.get("oid").asText());

		execute("--snmp-walk");
		snmpQuery = snmpCli.getQuery();
		assertEquals("walk", snmpQuery.get("action").asText());
		assertEquals(SNMP_OID, snmpQuery.get("oid").asText());
	}

	@Test
	void testValidate() {
		initCli();
		ParameterException noQueriesException = assertThrows(ParameterException.class, () -> snmpCli.validate());
		assertEquals(
			"At least one SNMP query must be specified: --snmp-get, --snmp-getnext, --snmp-walk.",
			noQueriesException.getMessage()
		);
		snmpCli.setGet(SNMP_OID);
		assertDoesNotThrow(() -> snmpCli.validate());
		snmpCli.setGetNext(SNMP_OID);
		ParameterException manyQueriesException = assertThrows(ParameterException.class, () -> snmpCli.validate());
		assertEquals(
			"Only one SNMP query can be specified at a time: --snmp-get, --snmp-getnext, --snmp-walk.",
			manyQueriesException.getMessage()
		);
	}
}
