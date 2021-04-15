package com.sentrysoftware.hardware.cli.component.cli;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.HashSet;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.hardware.cli.component.cli.protocols.SNMPVersion;
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
		assertEquals(SNMPVersion.V2C, sentryCli.getSnmpCredentials().getSnmpVersion());
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
		assertEquals(SNMPVersion.V2C, sentryCli.getSnmpCredentials().getSnmpVersion());
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
		assertEquals(SNMPVersion.V1, sentryCli.getSnmpCredentials().getSnmpVersion());
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
}
