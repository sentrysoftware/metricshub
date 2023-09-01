package com.sentrysoftware.matrix.strategy.source;

import static com.sentrysoftware.matrix.constants.Constants.ECS1_01;
import static com.sentrysoftware.matrix.constants.Constants.PASSWORD;
import static com.sentrysoftware.matrix.constants.Constants.URL;
import static com.sentrysoftware.matrix.constants.Constants.USERNAME;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;

import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sentrysoftware.matrix.configuration.HostConfiguration;
import com.sentrysoftware.matrix.configuration.HttpConfiguration;
import com.sentrysoftware.matrix.connector.model.common.DeviceKind;
import com.sentrysoftware.matrix.connector.model.common.HttpMethod;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.HttpSource;
import com.sentrysoftware.matrix.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;

@ExtendWith(MockitoExtension.class)
class SourceProcessorTest {

	@Mock
	private MatsyaClientsExecutor matsyaClientsExecutorMock;

	@Test
	void testProcessHttpSourceOK() {
		final HttpConfiguration httpConfiguration = HttpConfiguration.builder()
			.username(USERNAME)
			.password(PASSWORD.toCharArray())
			.port(161)
			.timeout(120L)
			.build();
		final HostConfiguration hostConfiguration = HostConfiguration.builder()
			.hostname(ECS1_01)
			.hostId(ECS1_01)
			.hostType(DeviceKind.LINUX)
			.configurations(Collections.singletonMap(HttpConfiguration.class, httpConfiguration))
			.build();

		final TelemetryManager telemetryManager = TelemetryManager.builder().hostConfiguration(hostConfiguration).build();
		final SourceProcessor sourceProcessor = SourceProcessor.builder()
			.telemetryManager(telemetryManager)
			.matsyaClientsExecutor(matsyaClientsExecutorMock)
			.build();

		doReturn(ECS1_01).when(matsyaClientsExecutorMock).executeHttp(any(), eq(true));
		final SourceTable actual = sourceProcessor.process(HttpSource.builder()
			.url(URL)
			.method(HttpMethod.GET)
			.build());

		final SourceTable expected = SourceTable.builder().rawData(ECS1_01).build();
		assertEquals(expected, actual);
	}

	@Test
	void testProcessHttpSourceNoHttpConfiguration() {
		final HostConfiguration hostConfiguration = HostConfiguration.builder()
			.hostname(ECS1_01)
			.hostId(ECS1_01)
			.hostType(DeviceKind.LINUX)
			.build();

		final TelemetryManager telemetryManager = TelemetryManager.builder().hostConfiguration(hostConfiguration).build();
		final SourceProcessor sourceProcessor = SourceProcessor.builder().telemetryManager(telemetryManager).build();

		assertEquals(SourceTable.empty(), sourceProcessor.process(HttpSource.builder()
			.url("my/url")
			.method(HttpMethod.GET)
			.build()));
	}
}
