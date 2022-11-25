package com.sentrysoftware.hardware.agent.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

import org.apache.logging.log4j.Level;
import org.junit.jupiter.api.MethodOrderer.OrderAnnotation;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import com.sentrysoftware.hardware.agent.configuration.ConfigHelper;
import com.sentrysoftware.hardware.agent.dto.HardwareHostDto;
import com.sentrysoftware.hardware.agent.dto.HostConfigurationDto;
import com.sentrysoftware.hardware.agent.dto.MultiHostsConfigurationDto;
import com.sentrysoftware.hardware.agent.dto.protocol.SnmpProtocolDto;
import com.sentrysoftware.hardware.agent.service.task.StrategyTask;
import com.sentrysoftware.matrix.engine.host.HostType;
import com.sentrysoftware.matrix.model.monitoring.HostMonitoring;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@TestMethodOrder(OrderAnnotation.class)
class TaskSchedulingServiceRemovalTest {

	/**
	 * The reset all unit test requires a separate spring context as the code modifies the content of the internal 
	 * otelSdkConfiguration bean when the SDK configuration is updated and this causes an unexpected behavior on other unit tests.
	 */

	@Autowired
	private File configFile;

	@Mock
	private ThreadPoolTaskScheduler hostTaskScheduler;

	@Mock
	private MultiHostsConfigurationDto multiHostsConfigurationDto;

	@Mock
	private Map<String, IHostMonitoring> hostMonitoringMap;

	@Mock
	private Map<String, ScheduledFuture<?>> hostSchedules;

	@InjectMocks
	@Autowired
	public TaskSchedulingService taskSechedulingService;

	@Test
	@Order(1)
	void testUpdateConfigurationRemoveObsoleteSchedules() {

		// Current /data/hws-config.yaml has 3 hosts
		// Let's say we have 4 hosts from the previous configuration but the current contains only 3 hosts
		// Let's check that 1 host is unscheduled and the existing hosts are never re-scheduled
		final MultiHostsConfigurationDto previous = ConfigHelper.readConfigurationSafe(configFile);
		previous.getHosts().add(HostConfigurationDto.builder()
				.collectPeriod(MultiHostsConfigurationDto.DEFAULT_COLLECT_PERIOD)
				.discoveryCycle(MultiHostsConfigurationDto.DEFAULT_DISCOVERY_CYCLE)
				.host(HardwareHostDto
						.builder()
						.hostname("host1")
						.id("host1")
						.type(HostType.LINUX)
						.build())
				.snmp(SnmpProtocolDto.builder().community("public1".toCharArray()).build())
				.build());

		doReturn(previous.getHosts()).when(multiHostsConfigurationDto).getHosts();
		doReturn(previous.getOtelCollector()).when(multiHostsConfigurationDto).getOtelCollector();
		final ScheduledFuture<?> mock = Mockito.mock(ScheduledFuture.class);
		doReturn(mock).when(hostSchedules).get(any());
		doReturn(true).when(mock).cancel(true);

		taskSechedulingService.updateConfiguration(configFile);

		verify(mock, times(1)).cancel(true);
		verify(hostTaskScheduler, never()).schedule(any(StrategyTask.class), any(Trigger.class));

	}

	@Test
	@Order(2)
	void testUpdateConfigurationRestartAll() {

		// Current /data/hws-config.yaml has 3 hosts
		// Let's say we have updated the SDK configuration
		final MultiHostsConfigurationDto previous = ConfigHelper.readConfigurationSafe(configFile);
		previous.getExporter().getOtlp().getHeaders().put("accept", "*/*".toCharArray());

		try (MockedStatic<ConfigHelper> configHelper = mockStatic(ConfigHelper.class)) {

			configHelper.when(() -> ConfigHelper.readConfigurationSafe(any())).thenReturn(previous);
			configHelper.when(() -> ConfigHelper.decrypt(any())).thenAnswer(invocation -> invocation.getArgument(0));
			configHelper.when(() -> ConfigHelper.fillHostMonitoringMap(any(), any(), any())).thenCallRealMethod();
			configHelper.when(() -> ConfigHelper.getSubPath(any())).thenAnswer(invocation -> Paths.get(invocation.getArgument(0).toString()));
			configHelper.when(() -> ConfigHelper.getLoggerLevel(anyString())).thenReturn(Level.OFF);
	
			doReturn(
				previous.getHosts(),
				previous.getHosts().stream().collect(Collectors.toSet()),
				previous.getHosts().stream().collect(Collectors.toSet())
			).when(multiHostsConfigurationDto).getHosts();

			doReturn(previous.getOtelCollector()).when(multiHostsConfigurationDto).getOtelCollector();

			doReturn(new HostMonitoring()).when(hostMonitoringMap).get(any());

			final ScheduledFuture<?> mock = Mockito.mock(ScheduledFuture.class);
			doReturn(mock).when(hostSchedules).get(any());
			doReturn(true).when(mock).cancel(true);
			taskSechedulingService.updateConfiguration(configFile);

			verify(mock, times(3)).cancel(true);
			verify(hostTaskScheduler, times(3)).schedule(any(StrategyTask.class), any(Trigger.class));

		}

	}
}
