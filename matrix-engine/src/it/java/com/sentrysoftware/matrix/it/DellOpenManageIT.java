package com.sentrysoftware.matrix.it;

import com.sentrysoftware.matrix.engine.EngineConfiguration;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol;
import com.sentrysoftware.matrix.engine.protocol.SNMPProtocol.SNMPVersion;
import com.sentrysoftware.matrix.engine.strategy.collect.CollectOperation;
import com.sentrysoftware.matrix.engine.strategy.detection.DetectionOperation;
import com.sentrysoftware.matrix.engine.strategy.discovery.DiscoveryOperation;
import com.sentrysoftware.matrix.engine.target.HardwareTarget;
import com.sentrysoftware.matrix.engine.target.TargetType;
import com.sentrysoftware.matrix.it.job.ITJob;
import com.sentrysoftware.matrix.it.job.SnmpITJob;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoring;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

class DellOpenManageIT {

	private static EngineConfiguration engineConfiguration;

	@BeforeAll
	static void setUp() {
		final SNMPProtocol protocol = SNMPProtocol.builder().community("public").version(SNMPVersion.V1)
				.timeout(120L).build();

		engineConfiguration = EngineConfiguration.builder()
				.target(HardwareTarget.builder().hostname("localhost").id("localhost").type(TargetType.LINUX).build())
				.selectedConnectors(Set.of("MS_HW_DellOpenManage.connector"))
				.protocolConfigurations(Map.of(SNMPProtocol.class, protocol)).build();
	}

	@Test
	void test() throws Exception {

		final ITJob itJob = new SnmpITJob();
		final IHostMonitoring hostMonitoring = new HostMonitoring();

		itJob
			.withServerRecordData("snmp/DellOpenManage/input/input.snmp")
			.prepareEngine(engineConfiguration, hostMonitoring)
			.executeStrategy(new DetectionOperation())
			.executeStrategy(new DiscoveryOperation())
			.executeStrategy(new CollectOperation())
			.verifyExpected("snmp/DellOpenManage/expected/expected.json");
	}
}
