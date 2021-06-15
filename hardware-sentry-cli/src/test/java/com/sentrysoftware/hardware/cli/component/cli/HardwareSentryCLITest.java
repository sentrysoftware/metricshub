package com.sentrysoftware.hardware.cli.component.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol;
import com.sentrysoftware.matrix.engine.protocol.WBEMProtocol;
import com.sentrysoftware.matrix.engine.target.TargetType;

import picocli.CommandLine;
import picocli.CommandLine.MissingParameterException;

class HardwareSentryCLITest {

	@Test
	void argumentsTest() {
		String[] args_hdfs = { "-host", "hostaa", 
				"-dt", "HP_UX", 
				"--snmp-version", "V2C",
				"--snmp-port", "200", 
				"--snmp-community", "private", 
				"--snmp-timeout", "60", 
				"-hdfs", "hdfs1,hdfs2,hdfs3" };
		HardwareSentryCLI sentryCli = new HardwareSentryCLI();
		new CommandLine(sentryCli).parseArgs(args_hdfs);

		assertEquals("hostaa", sentryCli.getHostname()) ;
		assertEquals(TargetType.HP_UX, sentryCli.getDeviceType());
		assertEquals(SNMPProtocol.SNMPVersion.V2C, sentryCli.getSnmpCredentials().getSnmpVersion());
		assertEquals(200, sentryCli.getSnmpCredentials().getPort());
		assertEquals("private", sentryCli.getSnmpCredentials().getCommunity());
		assertEquals(60, sentryCli.getSnmpCredentials().getTimeout());
		assertEquals(sentryCli.getHdfs(), new HashSet<>(Arrays.asList("hdfs1", "hdfs2", "hdfs3")));
		assertNull(sentryCli.getHdfsExclusion());
		

		String[] args_exclud_hdfs = { "-host", "hosta", 
				"-dt", "HP_UX", 
				"--snmp-version", "V2C",
				"--snmp-port", "200", 
				"--snmp-community", "private", 
				"--snmp-timeout", "60", 
				"-hdfsExcluded", "hdfs1,hdfs2" };
		
		sentryCli = new HardwareSentryCLI();
		new CommandLine(sentryCli).parseArgs(args_exclud_hdfs);
		assertEquals("hosta", sentryCli.getHostname()) ;
		assertEquals(TargetType.HP_UX, sentryCli.getDeviceType());
		assertEquals(SNMPProtocol.SNMPVersion.V2C, sentryCli.getSnmpCredentials().getSnmpVersion());
		assertEquals(200, sentryCli.getSnmpCredentials().getPort());
		assertEquals("private", sentryCli.getSnmpCredentials().getCommunity());
		assertEquals(60, sentryCli.getSnmpCredentials().getTimeout());
		assertEquals(sentryCli.getHdfsExclusion(), new HashSet<>(Arrays.asList("hdfs1", "hdfs2")));
		assertNull(sentryCli.getHdfs());
		
		String[] args_default_snmp = { "-host", "hostaa", 
				"-dt", "HP_UX", 
				"--snmp-version", "V1",
				"-hdfs", "hdfs1,hdfs2" };
		
		sentryCli = new HardwareSentryCLI();
		new CommandLine(sentryCli).parseArgs(args_default_snmp);
		assertEquals("hostaa", sentryCli.getHostname()) ;
		assertEquals(TargetType.HP_UX, sentryCli.getDeviceType());
		assertEquals(SNMPProtocol.SNMPVersion.V1, sentryCli.getSnmpCredentials().getSnmpVersion());
		assertEquals(161, sentryCli.getSnmpCredentials().getPort());
		assertEquals("public", sentryCli.getSnmpCredentials().getCommunity());
		assertEquals(120, sentryCli.getSnmpCredentials().getTimeout());
		assertEquals(sentryCli.getHdfs(), new HashSet<>(Arrays.asList("hdfs1", "hdfs2")));
		assertNull(sentryCli.getHdfsExclusion());;
		
		String[] args_required_host = { 
				"-dt", "HP_UX", 
				"-hdfs", "hdfs1,hdfs2" };
		
		assertThrows(MissingParameterException.class, () -> new CommandLine(new HardwareSentryCLI()).parseArgs(args_required_host));
		
	}

	@Test
	void argumentsWBEMTest() {
		String[] args_hdfs = { "-host", "hostaa", 
				"-dt", "HP_UX", 
				"--wbem-protocol", "HTTP", 
				"--wbem-port", "5989",
				"--wbem-namespace", "root/emc", 
				"--wbem-timeout", "120", 
				"--wbem-username", "admin", 
				"--wbem-password", "#1Password", 
				"-hdfs", "hdfs1,hdfs2,hdfs3" };
		HardwareSentryCLI sentryCli = new HardwareSentryCLI();
		new CommandLine(sentryCli).parseArgs(args_hdfs);

		assertEquals("hostaa", sentryCli.getHostname());
		assertEquals(TargetType.HP_UX, sentryCli.getDeviceType());
		assertEquals(WBEMProtocol.WBEMProtocols.HTTP,
				WBEMProtocol.WBEMProtocols.getValue(sentryCli.getWbemCredentials().getProtocol()));
		assertEquals(5989, sentryCli.getWbemCredentials().getPort());
		assertEquals("root/emc", sentryCli.getWbemCredentials().getNamespace());
		assertEquals(120, sentryCli.getWbemCredentials().getTimeout());
		assertEquals("admin", sentryCli.getWbemCredentials().getUsername());
		assertEquals("#1Password", sentryCli.getWbemCredentials().getPassword());
		assertEquals(sentryCli.getHdfs(), new HashSet<>(Arrays.asList("hdfs1", "hdfs2", "hdfs3")));
		assertNull(sentryCli.getHdfsExclusion());

		String[] args_hdfs2 = { "-host", "dev-hv-01", 
				"-dt", "MS_WINDOWS", 
				"--wbem-protocol", "https", 
				"--wbem-port", "5989", 
				"--wbem-namespace", "root/emc", 
				"--wbem-timeout", "120", 
				"--wbem-username", "admin",
				"--wbem-password", "#1Password", 
				"-hdfs", "hdfs1,hdfs2,hdfs3" };

		new CommandLine(sentryCli).parseArgs(args_hdfs2);

		assertEquals("dev-hv-01", sentryCli.getHostname());
		assertEquals(TargetType.MS_WINDOWS, sentryCli.getDeviceType());
		assertEquals(WBEMProtocol.WBEMProtocols.HTTPS,
				WBEMProtocol.WBEMProtocols.getValue(sentryCli.getWbemCredentials().getProtocol()));
		assertEquals(5989, sentryCli.getWbemCredentials().getPort());
		assertEquals("root/emc", sentryCli.getWbemCredentials().getNamespace());
		assertEquals(120, sentryCli.getWbemCredentials().getTimeout());
		assertEquals("admin", sentryCli.getWbemCredentials().getUsername());
		assertEquals("#1Password", sentryCli.getWbemCredentials().getPassword());
		assertEquals(sentryCli.getHdfs(), new HashSet<>(Arrays.asList("hdfs1", "hdfs2", "hdfs3")));
		assertNull(sentryCli.getHdfsExclusion());

	}

}
