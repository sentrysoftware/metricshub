package com.sentrysoftware.hardware.cli.component.cli.protocols;

import lombok.Data;
import picocli.CommandLine.Option;

@Data
public class SNMPCredentials {
	@Option(names = "--snmp-version", defaultValue = "V1", description = "SNMP version : v1, v2c, v3- No Authentication, v3-MD5, v3-SHA.")
	SNMPVersion snmpVersion;
	@Option(names = "--snmp-port", defaultValue = "161", description = "SNMP Port, default : 161.")
	int port;
	@Option(names = "--snmp-community", defaultValue = "public", description = "SNMP Community, default : public.")
	String community;
	@Option(names = "--snmp-timeout", defaultValue = "120", description = "SNMP Timeout, unit: seconds, default: 120.")
	int timeout;
}




