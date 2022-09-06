package com.sentrysoftware.hardware.agent.service.opentelemetry.process;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.awaitility.Awaitility;
import org.awaitility.Durations;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import com.sentrysoftware.hardware.agent.process.config.ProcessConfig;
import com.sentrysoftware.hardware.agent.process.config.ProcessOutput;
import com.sentrysoftware.hardware.agent.process.io.CustomInputStream;
import com.sentrysoftware.hardware.agent.process.io.GobblerStreamProcessor;
import com.sentrysoftware.hardware.agent.process.runtime.ProcessControl;

class OtelCollectorProcessTest {

	@Test
	void test() throws IOException {

		final ProcessBuilder pb = Mockito.mock(ProcessBuilder.class);
		final Process process = Mockito.mock(Process.class);

		try (MockedStatic<ProcessControl> processControl = mockStatic(ProcessControl.class)) {
			processControl.when(() -> ProcessControl
				.newProcessBuilder(
					anyList(),
					anyMap(),
					any(File.class),
					anyBoolean()
				)
			).thenReturn(pb);

			processControl.when(() -> ProcessControl.start(any(ProcessBuilder.class))).thenCallRealMethod();

			doReturn(process).when(pb).start();
			doReturn(new CustomInputStream("OpenTelemetry Collector started.")).when(process).getInputStream();
			doReturn(new CustomInputStream("Error.")).when(process).getErrorStream();

			final GobblerStreamProcessor outputProcessor = new GobblerStreamProcessor();
			final GobblerStreamProcessor errorProcessor = new GobblerStreamProcessor();
			final OtelCollectorProcess otelProcess = new OtelCollectorProcess(
				ProcessConfig
					.builder()
					.commandLine(List.of("otelcol-contrib", "--config", "/opt/hws-otel-collector/config/otel-config.yaml"))
					.output(ProcessOutput
						.builder()
						.outputProcessor(outputProcessor)
						.errorProcessor(errorProcessor)
						.build()
					)
					.workingDir(new File("."))
					.build()
			);

			otelProcess.start();

			Awaitility
				.await()
				.atMost(Durations.FIVE_SECONDS)
				.untilAsserted(() -> {
					assertEquals("OpenTelemetry Collector started.", outputProcessor.getBlocks());
					assertEquals("Error.", errorProcessor.getBlocks());
				});

			otelProcess.stop();

			assertTrue(otelProcess.isStopped());

		}
	}

	@Test
	void testWithoutOutputProcessor() throws IOException {

		final ProcessBuilder pb = Mockito.mock(ProcessBuilder.class);
		final Process process = Mockito.mock(Process.class);

		try (MockedStatic<ProcessControl> processControl = mockStatic(ProcessControl.class)) {
			processControl.when(() -> ProcessControl
				.newProcessBuilder(
					anyList(),
					anyMap(),
					any(File.class),
					anyBoolean()
				)
			).thenReturn(pb);

			processControl.when(() -> ProcessControl.start(any(ProcessBuilder.class))).thenCallRealMethod();

			doReturn(process).when(pb).start();
			doReturn(new CustomInputStream("OpenTelemetry Collector started.")).when(process).getInputStream();
			doReturn(new CustomInputStream("Error.")).when(process).getErrorStream();

			final OtelCollectorProcess otelProcess = new OtelCollectorProcess(
				ProcessConfig
					.builder()
					.commandLine(List.of("otelcol-contrib", "--config", "/opt/hws-otel-collector/config/otel-config.yaml"))
					.output(null) // No output
					.workingDir(new File("."))
					.build()
			);

			assertDoesNotThrow(() -> otelProcess.start());

			otelProcess.stop();

			assertTrue(otelProcess.isStopped());

		}
	}

	@Test
	void testWithoutErrorOutputProcessor() throws IOException {
		final ProcessBuilder pb = Mockito.mock(ProcessBuilder.class);
		final Process process = Mockito.mock(Process.class);

		try (MockedStatic<ProcessControl> processControl = mockStatic(ProcessControl.class)) {
			processControl.when(() -> ProcessControl
				.newProcessBuilder(
					anyList(),
					anyMap(),
					any(File.class),
					anyBoolean()
				)
			).thenReturn(pb);

			processControl.when(() -> ProcessControl.start(any(ProcessBuilder.class))).thenCallRealMethod();

			doReturn(process).when(pb).start();
			doReturn(new CustomInputStream("OpenTelemetry Collector started.")).when(process).getInputStream();
			doReturn(new CustomInputStream("Error.")).when(process).getErrorStream();

			final GobblerStreamProcessor outputProcessor = new GobblerStreamProcessor();
			final OtelCollectorProcess otelProcess = new OtelCollectorProcess(
				ProcessConfig
					.builder()
					.commandLine(List.of("otelcol-contrib", "--config", "/opt/hws-otel-collector/config/otel-config.yaml"))
					.output(ProcessOutput
						.builder()
						.outputProcessor(outputProcessor)
						.build()
					)
					.workingDir(new File("."))
					.build()
			);

			otelProcess.start();

			Awaitility
				.await()
				.atMost(Durations.FIVE_SECONDS)
				.untilAsserted(() -> {
					assertEquals("OpenTelemetry Collector started.", outputProcessor.getBlocks());
				});

			otelProcess.stop();

			assertTrue(otelProcess.isStopped());

		}
	}
}
