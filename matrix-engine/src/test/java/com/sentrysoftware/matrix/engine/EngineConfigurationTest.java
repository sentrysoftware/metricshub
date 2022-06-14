package com.sentrysoftware.matrix.engine;

import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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
import com.sentrysoftware.matrix.engine.protocol.WinRmProtocol;
import com.sentrysoftware.matrix.engine.protocol.WmiProtocol;

import com.sentrysoftware.matrix.engine.host.HardwareHost;
import com.sentrysoftware.matrix.engine.host.HostType;

class EngineConfigurationTest {

	@Test
	void testDetermineAcceptedSources() {
		EngineConfiguration engineConfiguration = EngineConfiguration.builder().build();
		{
			engineConfiguration.setProtocolConfigurations(Map.of(WbemProtocol.class, WbemProtocol.builder().build()));
			engineConfiguration.setHost(HardwareHost.builder().type(HostType.MS_WINDOWS).build());

			final Set<Class<? extends Source>> actual = engineConfiguration.determineAcceptedSources(false);
			final Set<Class<? extends Source>> expected = Collections.singleton(WbemSource.class);
			assertEquals(expected, actual);
		}

		{
			engineConfiguration.setProtocolConfigurations(Map.of(WmiProtocol.class, WmiProtocol.builder().build()));
			engineConfiguration.setHost(HardwareHost.builder().type(HostType.MS_WINDOWS).build());

			final Set<Class<? extends Source>> actual = engineConfiguration.determineAcceptedSources(false);
			final Set<Class<? extends Source>> expected = Set.of(Ipmi.class, WmiSource.class);
			assertEquals(expected, actual);
		}

		{
			engineConfiguration.setProtocolConfigurations(Map.of(WmiProtocol.class, WmiProtocol.builder().build()));
			engineConfiguration.setHost(HardwareHost.builder().type(HostType.LINUX).build());

			final Set<Class<? extends Source>> actual = engineConfiguration.determineAcceptedSources(false);
			final Set<Class<? extends Source>> expected = Collections.emptySet();
			assertEquals(expected, actual);
		}

		{
			engineConfiguration.setProtocolConfigurations(Map.of(SshProtocol.class, SshProtocol.builder().build()));
			engineConfiguration.setHost(HardwareHost.builder().type(HostType.LINUX).build());

			final Set<Class<? extends Source>> actual = engineConfiguration.determineAcceptedSources(false);
			final Set<Class<? extends Source>> expected = Set.of(OsCommandSource.class, Ipmi.class,
					SshInteractiveSource.class);
			assertEquals(expected, actual);
		}

		{
			engineConfiguration.setProtocolConfigurations(Map.of(SshProtocol.class, SshProtocol.builder().build()));
			engineConfiguration.setHost(HardwareHost.builder().type(HostType.SUN_SOLARIS).build());

			final Set<Class<? extends Source>> actual = engineConfiguration.determineAcceptedSources(false);
			final Set<Class<? extends Source>> expected = Set.of(OsCommandSource.class, Ipmi.class,
					SshInteractiveSource.class);
			assertEquals(expected, actual);
		}

		{
			engineConfiguration.setProtocolConfigurations(Collections.emptyMap());
			engineConfiguration.setHost(HardwareHost.builder().type(HostType.MS_WINDOWS).build());

			final Set<Class<? extends Source>> actual = engineConfiguration.determineAcceptedSources(true);
			final Set<Class<? extends Source>> expected = Collections.singleton(OsCommandSource.class);
			assertEquals(expected, actual);
		}

		{
			engineConfiguration.setProtocolConfigurations(Collections.emptyMap());
			engineConfiguration.setHost(HardwareHost.builder().type(HostType.LINUX).build());

			final Set<Class<? extends Source>> actual = engineConfiguration.determineAcceptedSources(true);
			final Set<Class<? extends Source>> expected = Set.of(OsCommandSource.class, Ipmi.class);
			assertEquals(expected, actual);
		}

		{
			engineConfiguration.setProtocolConfigurations(Collections.emptyMap());
			engineConfiguration.setHost(HardwareHost.builder().type(HostType.SUN_SOLARIS).build());

			final Set<Class<? extends Source>> actual = engineConfiguration.determineAcceptedSources(true);
			final Set<Class<? extends Source>> expected = Set.of(OsCommandSource.class, Ipmi.class);
			assertEquals(expected, actual);
		}

		{
			engineConfiguration.setProtocolConfigurations(Map.of(SshProtocol.class, SshProtocol.builder().build()));
			engineConfiguration.setHost(HardwareHost.builder().type(HostType.SUN_SOLARIS).build());

			final Set<Class<? extends Source>> actual = engineConfiguration.determineAcceptedSources(true);
			final Set<Class<? extends Source>> expected = Set.of(OsCommandSource.class, Ipmi.class,
					SshInteractiveSource.class);
			assertEquals(expected, actual);
		}
	}

	@Test
	void testGetWinProtocol() {
		assertNull(new EngineConfiguration().getWinProtocol());
		assertNotNull(
			EngineConfiguration
				.builder()
				.protocolConfigurations(Map.of(WinRmProtocol.class, new WinRmProtocol()))
				.build()
				.getWinProtocol()
		);
		assertNotNull(
			EngineConfiguration
				.builder()
				.protocolConfigurations(Map.of(WmiProtocol.class, new WmiProtocol()))
				.build()
				.getWinProtocol()
		);
		assertEquals(
			WinRmProtocol.class,
			EngineConfiguration
				.builder()
				.protocolConfigurations(
					Map.of(
						WmiProtocol.class, new WmiProtocol(),
						WinRmProtocol.class, new WinRmProtocol()
					)
				)
				.build()
				.getWinProtocol()
				.getClass()
		);
	}
}
