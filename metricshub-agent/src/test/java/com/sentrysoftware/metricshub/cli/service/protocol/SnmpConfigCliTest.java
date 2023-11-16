package com.sentrysoftware.metricshub.cli.service.protocol;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.sentrysoftware.metricshub.engine.configuration.SnmpConfiguration;
import org.junit.jupiter.api.Test;

class SnmpConfigCliTest {

	@Test
	void testToProtocol() {
		final String password = "pwd";

		// Create a SnmpConfigCli instance and set its data
		final SnmpConfigCli snmpConfigCli = new SnmpConfigCli();
		snmpConfigCli.setSnmpVersion(SnmpConfiguration.SnmpVersion.V1);
		snmpConfigCli.setPort(80);
		snmpConfigCli.setCommunity("community");
		snmpConfigCli.setPrivacy(SnmpConfiguration.Privacy.AES);
		snmpConfigCli.setTimeout(120L);
		snmpConfigCli.setPrivacyPassword(password.toCharArray());

		// Create an SnmpConfiguration and call method toProtocol
		final SnmpConfiguration snmpConfiguration = (SnmpConfiguration) snmpConfigCli.toProtocol(
			"user1",
			password.toCharArray()
		);

		// Check the resulting snmp configuration
		assertNotNull(snmpConfiguration);
		assertEquals(SnmpConfiguration.SnmpVersion.V1, snmpConfiguration.getVersion());
		assertEquals("SNMP v1 (community)", snmpConfiguration.toString());
	}
}
