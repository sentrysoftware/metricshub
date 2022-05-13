package com.sentrysoftware.matrix.engine;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.ipmi.Ipmi;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.oscommand.OsCommandSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.sshinteractive.SshInteractiveSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.wbem.WbemSource;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.type.wmi.WmiSource;
import com.sentrysoftware.matrix.engine.protocol.SshProtocol;
import com.sentrysoftware.matrix.engine.protocol.WbemProtocol;
import com.sentrysoftware.matrix.engine.protocol.WmiProtocol;
import com.sentrysoftware.matrix.engine.target.HardwareTarget;
import com.sentrysoftware.matrix.engine.target.TargetType;

class EngineConfigurationTest {

	@Test
	void testDetermineAcceptedSources() {
		EngineConfiguration engineConfiguration = EngineConfiguration.builder().build();
		{
			engineConfiguration.setProtocolConfigurations(Map.of(WbemProtocol.class, WbemProtocol.builder().build()));
			engineConfiguration.setTarget(HardwareTarget.builder().type(TargetType.MS_WINDOWS).build());

			final Set<Class<? extends Source>> actual = engineConfiguration.determineAcceptedSources(false);
			final Set<Class<? extends Source>> expected = Collections.singleton(WbemSource.class);
			assertEquals(expected, actual);
		}

		{
			engineConfiguration.setProtocolConfigurations(Map.of(WmiProtocol.class, WmiProtocol.builder().build()));
			engineConfiguration.setTarget(HardwareTarget.builder().type(TargetType.MS_WINDOWS).build());

			final Set<Class<? extends Source>> actual = engineConfiguration.determineAcceptedSources(false);
			final Set<Class<? extends Source>> expected = Set.of(Ipmi.class, WmiSource.class);
			assertEquals(expected, actual);
		}

		{
			engineConfiguration.setProtocolConfigurations(Map.of(WmiProtocol.class, WmiProtocol.builder().build()));
			engineConfiguration.setTarget(HardwareTarget.builder().type(TargetType.LINUX).build());

			final Set<Class<? extends Source>> actual = engineConfiguration.determineAcceptedSources(false);
			final Set<Class<? extends Source>> expected = Collections.emptySet();
			assertEquals(expected, actual);
		}

		{
			engineConfiguration.setProtocolConfigurations(Map.of(SshProtocol.class, SshProtocol.builder().build()));
			engineConfiguration.setTarget(HardwareTarget.builder().type(TargetType.LINUX).build());

			final Set<Class<? extends Source>> actual = engineConfiguration.determineAcceptedSources(false);
			final Set<Class<? extends Source>> expected = Set.of(OsCommandSource.class, Ipmi.class,
					SshInteractiveSource.class);
			assertEquals(expected, actual);
		}

		{
			engineConfiguration.setProtocolConfigurations(Map.of(SshProtocol.class, SshProtocol.builder().build()));
			engineConfiguration.setTarget(HardwareTarget.builder().type(TargetType.SUN_SOLARIS).build());

			final Set<Class<? extends Source>> actual = engineConfiguration.determineAcceptedSources(false);
			final Set<Class<? extends Source>> expected = Set.of(OsCommandSource.class, Ipmi.class,
					SshInteractiveSource.class);
			assertEquals(expected, actual);
		}

		{
			engineConfiguration.setProtocolConfigurations(Collections.emptyMap());
			engineConfiguration.setTarget(HardwareTarget.builder().type(TargetType.MS_WINDOWS).build());

			final Set<Class<? extends Source>> actual = engineConfiguration.determineAcceptedSources(true);
			final Set<Class<? extends Source>> expected = Collections.singleton(OsCommandSource.class);
			assertEquals(expected, actual);
		}

		{
			engineConfiguration.setProtocolConfigurations(Collections.emptyMap());
			engineConfiguration.setTarget(HardwareTarget.builder().type(TargetType.LINUX).build());

			final Set<Class<? extends Source>> actual = engineConfiguration.determineAcceptedSources(true);
			final Set<Class<? extends Source>> expected = Set.of(OsCommandSource.class, Ipmi.class);
			assertEquals(expected, actual);
		}

		{
			engineConfiguration.setProtocolConfigurations(Collections.emptyMap());
			engineConfiguration.setTarget(HardwareTarget.builder().type(TargetType.SUN_SOLARIS).build());

			final Set<Class<? extends Source>> actual = engineConfiguration.determineAcceptedSources(true);
			final Set<Class<? extends Source>> expected = Set.of(OsCommandSource.class, Ipmi.class);
			assertEquals(expected, actual);
		}

		{
			engineConfiguration.setProtocolConfigurations(Map.of(SshProtocol.class, SshProtocol.builder().build()));
			engineConfiguration.setTarget(HardwareTarget.builder().type(TargetType.SUN_SOLARIS).build());

			final Set<Class<? extends Source>> actual = engineConfiguration.determineAcceptedSources(true);
			final Set<Class<? extends Source>> expected = Set.of(OsCommandSource.class, Ipmi.class,
					SshInteractiveSource.class);
			assertEquals(expected, actual);
		}
	}

}
