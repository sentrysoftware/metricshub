package com.sentrysoftware.hardware.agent.service.prometheus;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import com.sentrysoftware.hardware.agent.exception.BusinessException;
import com.sentrysoftware.matrix.common.helpers.ResourceHelper;

import io.prometheus.client.Collector;
import io.prometheus.client.CollectorRegistry;
import io.prometheus.client.GaugeMetricFamily;
import io.prometheus.client.exporter.common.TextFormat;

@ExtendWith(MockitoExtension.class)
@Deprecated(since = "1.1")
class PrometheusServiceTest {

	@Mock
	private HostMonitoringCollectorService hostMonitoringCollectorService;

	@InjectMocks
	private PrometheusService prometheusService = new PrometheusService();

	@Test
	void testCollectMetrics() throws BusinessException {


		doAnswer(invocation -> new TestCollector().register(invocation.getArgument(0)))
			.when(hostMonitoringCollectorService)
			.register(any(CollectorRegistry.class));

		assertEquals(ResourceHelper.getResourceAsString("/data/simple_gauge.txt", PrometheusServiceTest.class),
				prometheusService.collectMetrics());

		verify(hostMonitoringCollectorService).register(any(CollectorRegistry.class));
	}

	@Test
	void testCollectMetricsBusinessException() throws BusinessException {

		final PrintStream origin = System.out;
		final PrintStream printStream = new PrintStream(new CustomOutputStream());
		System.setOut(printStream);


		doAnswer(invocation -> new TestCollector().register(invocation.getArgument(0)))
			.when(hostMonitoringCollectorService)
			.register(any(CollectorRegistry.class));

		try (MockedStatic<TextFormat> textFormat = mockStatic(TextFormat.class)) {

			textFormat
				.when(() -> TextFormat.write004(any(Writer.class), any()))
				.thenThrow(new IOException("IO Exception thrown for Test"));

			assertThrows(BusinessException.class, () -> prometheusService.collectMetrics());

			verify(hostMonitoringCollectorService).register(any(CollectorRegistry.class));
		}

		printStream.close();
		System.setOut(origin);
	}

	class TestCollector extends Collector {
		@Override
		public List<MetricFamilySamples> collect() {

			List<MetricFamilySamples> mfs = new ArrayList<>();

			// With no labels.
			mfs.add(new GaugeMetricFamily("my_test_gauge", "help", 8));

			return mfs;
		}
	}

	/**
	 * This class is used to create a {@link PrintStream} object which is required
	 * to redirect the {@link System#out} outputs for some circumstances
	 *
	 */
	class CustomOutputStream extends OutputStream {

		StringBuilder stringBuilder;

		public CustomOutputStream() {

			stringBuilder = new StringBuilder();
		}

		@Override
		public final void write(int i) {

			char c = (char) i;

			if (c == '\r' || c == '\n') {

				if (stringBuilder.length() > 0) {
					stringBuilder = new StringBuilder();
				}
			} else {
				stringBuilder.append(c);
			}
		}
	}
}
