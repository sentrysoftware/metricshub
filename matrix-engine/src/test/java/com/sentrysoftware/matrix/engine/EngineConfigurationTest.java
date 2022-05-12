package com.sentrysoftware.matrix.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.ipmi.IPMI;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.oscommand.OSCommandSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.sshinteractive.SshInteractiveSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.wbem.WBEMSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.wmi.WMISource;
import com.sentrysoftware.matrix.engine.protocol.SSHProtocol;
import com.sentrysoftware.matrix.engine.protocol.WBEMProtocol;
import com.sentrysoftware.matrix.engine.protocol.WMIProtocol;
import com.sentrysoftware.matrix.engine.target.HardwareTarget;
import com.sentrysoftware.matrix.engine.target.TargetType;

class EngineConfigurationTest {

	@Test
	void testDetermineAcceptedSources() {
		EngineConfiguration engineConfiguration = EngineConfiguration.builder().build();
		{
			engineConfiguration.setProtocolConfigurations(Map.of(WBEMProtocol.class, WBEMProtocol.builder().build()));
			engineConfiguration.setTarget(HardwareTarget.builder().type(TargetType.MS_WINDOWS).build());

			final Set<Class<? extends Source>> actual = engineConfiguration.determineAcceptedSources(false);
			final Set<Class<? extends Source>> expected = Collections.singleton(WBEMSource.class);
			assertEquals(expected, actual);
		}

		{
			engineConfiguration.setProtocolConfigurations(Map.of(WMIProtocol.class, WMIProtocol.builder().build()));
			engineConfiguration.setTarget(HardwareTarget.builder().type(TargetType.MS_WINDOWS).build());

			final Set<Class<? extends Source>> actual = engineConfiguration.determineAcceptedSources(false);
			final Set<Class<? extends Source>> expected = Set.of(IPMI.class, WMISource.class);
			assertEquals(expected, actual);
		}

		{
			engineConfiguration.setProtocolConfigurations(Map.of(WMIProtocol.class, WMIProtocol.builder().build()));
			engineConfiguration.setTarget(HardwareTarget.builder().type(TargetType.LINUX).build());

			final Set<Class<? extends Source>> actual = engineConfiguration.determineAcceptedSources(false);
			final Set<Class<? extends Source>> expected = Collections.emptySet();
			assertEquals(expected, actual);
		}

		{
			engineConfiguration.setProtocolConfigurations(Map.of(SSHProtocol.class, SSHProtocol.builder().build()));
			engineConfiguration.setTarget(HardwareTarget.builder().type(TargetType.LINUX).build());

			final Set<Class<? extends Source>> actual = engineConfiguration.determineAcceptedSources(false);
			final Set<Class<? extends Source>> expected = Set.of(OSCommandSource.class, IPMI.class,
					SshInteractiveSource.class);
			assertEquals(expected, actual);
		}

		{
			engineConfiguration.setProtocolConfigurations(Map.of(SSHProtocol.class, SSHProtocol.builder().build()));
			engineConfiguration.setTarget(HardwareTarget.builder().type(TargetType.SUN_SOLARIS).build());

			final Set<Class<? extends Source>> actual = engineConfiguration.determineAcceptedSources(false);
			final Set<Class<? extends Source>> expected = Set.of(OSCommandSource.class, IPMI.class,
					SshInteractiveSource.class);
			assertEquals(expected, actual);
		}

		{
			engineConfiguration.setProtocolConfigurations(Collections.emptyMap());
			engineConfiguration.setTarget(HardwareTarget.builder().type(TargetType.MS_WINDOWS).build());

			final Set<Class<? extends Source>> actual = engineConfiguration.determineAcceptedSources(true);
			final Set<Class<? extends Source>> expected = Collections.singleton(OSCommandSource.class);
			assertEquals(expected, actual);
		}

		{
			engineConfiguration.setProtocolConfigurations(Collections.emptyMap());
			engineConfiguration.setTarget(HardwareTarget.builder().type(TargetType.LINUX).build());

			final Set<Class<? extends Source>> actual = engineConfiguration.determineAcceptedSources(true);
			final Set<Class<? extends Source>> expected = Set.of(OSCommandSource.class, IPMI.class);
			assertEquals(expected, actual);
		}

		{
			engineConfiguration.setProtocolConfigurations(Collections.emptyMap());
			engineConfiguration.setTarget(HardwareTarget.builder().type(TargetType.SUN_SOLARIS).build());

			final Set<Class<? extends Source>> actual = engineConfiguration.determineAcceptedSources(true);
			final Set<Class<? extends Source>> expected = Set.of(OSCommandSource.class, IPMI.class);
			assertEquals(expected, actual);
		}

		{
			engineConfiguration.setProtocolConfigurations(Map.of(SSHProtocol.class, SSHProtocol.builder().build()));
			engineConfiguration.setTarget(HardwareTarget.builder().type(TargetType.SUN_SOLARIS).build());

			final Set<Class<? extends Source>> actual = engineConfiguration.determineAcceptedSources(true);
			final Set<Class<? extends Source>> expected = Set.of(OSCommandSource.class, IPMI.class,
					SshInteractiveSource.class);
			assertEquals(expected, actual);
		}
	}

}
