package org.sentrysoftware.metricshub.cli.snmp;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.sentrysoftware.metricshub.cli.service.protocol.SnmpConfigCli;

import com.fasterxml.jackson.databind.JsonNode;
import picocli.CommandLine;
import picocli.CommandLine.ParameterException;

class SnmpCliTest {

	SnmpCli snmpCli;
	CommandLine commandLine;

	static String SNMP_OID = "1.3.6.1.4.1.674.10892.5.5.1.20.130.4";
	static String SNMP_VERSION = "v2c";
	static String SNMP_COMMUNITY = "public";

	void initCli() {
		snmpCli = new SnmpCli();
		commandLine = new CommandLine(snmpCli);
	}

	void initSnmpGet() {
		initCli();

		commandLine.execute(
			"hostname",
			"--snmp-get",
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

	void initSnmpGetNext() {
		initCli();

		commandLine.execute(
			"hostname",
			"--snmp-getnext",
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

	void initSnmpWalk() {
		initCli();

		commandLine.execute(
			"hostname",
			"--snmp-walk",
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
		initSnmpGet();
		assertEquals(SNMP_OID, snmpCli.get);
		initSnmpGetNext();
		assertEquals(SNMP_OID, snmpCli.getNext);
		initSnmpWalk();
		assertEquals(SNMP_OID, snmpCli.walk);
	}

	@Test
	void testGetQuery() {
		initSnmpGet();
		JsonNode snmpQuery = snmpCli.getQuery();
		assertEquals("get", snmpQuery.get("action").asText());
		assertEquals(SNMP_OID, snmpQuery.get("oid").asText());

		initSnmpGetNext();
		snmpQuery = snmpCli.getQuery();
		assertEquals("getNext", snmpQuery.get("action").asText());
		assertEquals(SNMP_OID, snmpQuery.get("oid").asText());

		initSnmpWalk();
		snmpQuery = snmpCli.getQuery();
		assertEquals("walk", snmpQuery.get("action").asText());
		assertEquals(SNMP_OID, snmpQuery.get("oid").asText());
	}

	@Test
	void testValidate() {
		initCli();
		ParameterException snmpConfigException = assertThrows(ParameterException.class, () -> snmpCli.validate());
		assertEquals("SNMP protocol must be configured: --snmp.", snmpConfigException.getMessage());
		snmpCli.setSnmpConfigCli(new SnmpConfigCli());
		ParameterException noQueriesException = assertThrows(ParameterException.class, () -> snmpCli.validate());
		assertEquals("At least one SNMP query must be specified: --snmp-get, --snmp-getnext, --snmp-walk.", noQueriesException.getMessage());
		snmpCli.setGet(SNMP_OID);
		assertDoesNotThrow(() -> snmpCli.validate());
		snmpCli.setGetNext(SNMP_OID);
		ParameterException manyQueriesException = assertThrows(ParameterException.class, () -> snmpCli.validate());
		assertEquals("Only one SNMP query can be specified at a time: --snmp-get, --snmp-getnext, --snmp-walk.", manyQueriesException.getMessage());
	}
}
