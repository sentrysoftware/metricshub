package com.sentrysoftware.matrix.it;

import com.sentrysoftware.matrix.engine.EngineConfiguration;
import com.sentrysoftware.matrix.engine.protocol.IProtocolConfiguration;
import com.sentrysoftware.matrix.engine.protocol.WBEMProtocol;
import com.sentrysoftware.matrix.engine.protocol.WBEMProtocol.WBEMProtocols;
import com.sentrysoftware.matrix.engine.strategy.collect.CollectOperation;
import com.sentrysoftware.matrix.engine.strategy.detection.DetectionOperation;
import com.sentrysoftware.matrix.engine.strategy.discovery.DiscoveryOperation;
import com.sentrysoftware.matrix.engine.target.HardwareTarget;
import com.sentrysoftware.matrix.engine.target.TargetType;
import com.sentrysoftware.matrix.it.job.ITJob;
import com.sentrysoftware.matrix.it.job.WbemITJob;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoring;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;

class EMCDiskArrayIT {

	private static final String INPUT = Paths.get("src", "it", "resources", "wbem", "emcDiskArray", "input").toAbsolutePath().toString();

	private static final Set<String> CONNECTORS = Set.of("EMCDiskArray");

	private static final Map<Class<? extends IProtocolConfiguration>, IProtocolConfiguration> PROTOCOL_CONFIGURATIONS = Map.of(
			WBEMProtocol.class,
			WBEMProtocol.builder()
				.protocol(WBEMProtocols.HTTPS)
				.port(5900)
				.namespace("root/emc")
				.username("username")
				.password("password".toCharArray())
				.timeout(120L)
				.build());

	private static EngineConfiguration engineConfiguration;

	@BeforeAll
	static void setUp() {
		engineConfiguration = EngineConfiguration.builder()
				.target(HardwareTarget.builder().hostname("0.0.0.0").id("localhost").type(TargetType.STORAGE).build())
				.selectedConnectors(CONNECTORS)
				.protocolConfigurations(PROTOCOL_CONFIGURATIONS).build();
	}

	@Test
	void test() throws Exception {

		final ITJob itJob = new WbemITJob();
		final IHostMonitoring hostMonitoring = new HostMonitoring();

		itJob
			.withServerRecordData(INPUT)
			.prepareEngine(engineConfiguration, hostMonitoring)
			.executeStrategy(new DetectionOperation())
			.executeStrategy(new DiscoveryOperation())
			.executeStrategy(new CollectOperation())
			.verifyExpected("wbem/emcDiskArray/expected/expected.json");
	}
}
