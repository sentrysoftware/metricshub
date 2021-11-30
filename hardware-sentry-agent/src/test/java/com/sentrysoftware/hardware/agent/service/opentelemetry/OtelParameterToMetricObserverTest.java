package com.sentrysoftware.hardware.agent.service.opentelemetry;

import static com.sentrysoftware.hardware.agent.service.opentelemetry.MetricsMapping.ID;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENERGY_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.ENERGY_USAGE_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.FQDN;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.STATUS_PARAMETER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.TEST_REPORT_PARAMETER;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.hardware.agent.dto.MultiHostsConfigurationDTO;
import com.sentrysoftware.matrix.common.meta.monitor.Enclosure;
import com.sentrysoftware.matrix.common.meta.monitor.MetaConnector;
import com.sentrysoftware.matrix.common.meta.parameter.state.Status;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.parameter.DiscreteParam;
import com.sentrysoftware.matrix.model.parameter.IParameter;
import com.sentrysoftware.matrix.model.parameter.NumberParam;
import com.sentrysoftware.matrix.model.parameter.TextParam;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.testing.InMemoryMetricReader;
import io.opentelemetry.sdk.resources.Resource;

class OtelParameterToMetricObserverTest {

	private static final String LABEL_VALUE = "monitor";
	private static final String PARENT_ID_VALUE = "parent_id";
	private static final String ID_VALUE = ID;

	@Test
	void testInit() {
		testObservability(DiscreteParam
					.builder()
					.state(Status.OK)
					.collectTime(new Date().getTime())
					.name(STATUS_PARAMETER)
					.build(), 
				"hw.enclosure.status",
				true
		);

		testObservability(NumberParam
				.builder()
				.name(ENERGY_PARAMETER)
				.collectTime(new Date().getTime())
				.value(50000.0)
				.build(), 
			"hw.enclosure.energy_joules_total",
			false
		);
	}

	/**
	 * Test the observability via OpenTelemetry on the given {@link IParameter}
	 * 
	 * @param parameter          The Parameter we wish to test
	 * @param expectedMetricName The expected OpenTelemetry metric name
	 * @param gauge              whether the metric is measured as gauge or counter
	 *                           (sum)
	 */
	private static void testObservability(final IParameter parameter, String expectedMetricName, boolean gauge) {

		final Monitor target = Monitor.builder().id(ID).name("host").build();
		target.addMetadata(FQDN, "host.my.domain.net");
		final Resource resource = OtelHelper.createHostResource(target, "host");

		final InMemoryMetricReader inMemoryReader = InMemoryMetricReader.create();
		final SdkMeterProvider meterProvider = SdkMeterProvider.builder()
				.setResource(resource)
				.registerMetricReader(inMemoryReader)
				.buildAndRegisterGlobal();

		final MultiHostsConfigurationDTO multiHostsConfigurationDTO= MultiHostsConfigurationDTO
				.builder()
				.extraLabels(Map.of("site", "Datacenter 1"))
				.build();
		
		final Monitor enclosure = Monitor
				.builder()
				.id("id_enclosure")
				.name("enclosure 1")
				.parentId("host")
				.monitorType(MonitorType.ENCLOSURE)
				.build();
		enclosure.addMetadata(FQDN, "host.my.domain.net");

		OtelParameterToMetricObserver
			.builder()
			.monitor(enclosure)
			.sdkMeterProvider(meterProvider)
			.multiHostsConfigurationDTO(multiHostsConfigurationDTO)
			.metricInfo(MetricsMapping.getMetricInfo(MonitorType.ENCLOSURE, parameter.getName()).get())
			.matrixParameterName(parameter.getName())
			.build()
			.init();

		// This will trigger the observe callback
		Collection<MetricData> metrics = inMemoryReader.collectAllMetrics();

		// The parameter is not collected yet
		assertTrue(metrics.isEmpty());

		enclosure.collectParameter(parameter);

		// Trigger the observe callback
		metrics = inMemoryReader.collectAllMetrics();

		// We should observe a new value
		assertEquals(1, metrics.size());
		final MetricData metricData = metrics.stream().findFirst().orElse(null);

		assertNotNull(metricData);
		assertNotNull(metricData.getUnit());
		assertNotNull(metricData.getDescription());
		assertEquals(expectedMetricName, metricData.getName());

		final  Collection<DoublePointData> points;
		if (gauge) {
			points = metricData.getDoubleGaugeData().getPoints();
		} else {
			points = metricData.getDoubleSumData().getPoints();
		}

		final DoublePointData dataPoint = points.stream().findFirst().orElse(null);
		assertEquals(parameter.numberValue().doubleValue(), dataPoint.getValue());

		final Attributes expected = Attributes.builder()
				.put("id", "id_enclosure")
				.put("label", "enclosure 1")
				.put("fqdn", "host.my.domain.net")
				.put("parent", "host")
				.put("site", "Datacenter 1")
				.build();

		assertEquals(expected, dataPoint.getAttributes());
	}

	@Test
	void testIsParameterAvailable() {

		{
			final DiscreteParam statusParam = DiscreteParam.builder().name(STATUS_PARAMETER).state(Status.OK).build();
			final Monitor monitor = Monitor.builder()
						.id(ID_VALUE)
						.parentId(PARENT_ID_VALUE)
						.name(LABEL_VALUE)
						.parameters(Map.of(STATUS_PARAMETER, statusParam))
						.build();
			assertTrue(OtelParameterToMetricObserver.isParameterAvailable(monitor, Enclosure.STATUS.getName()));
		}

		{

			final DiscreteParam statusParamNotAvailable = DiscreteParam
					.builder()
					.name(STATUS_PARAMETER)
					.state(Status.OK)
					.build();

			statusParamNotAvailable.setState(null);

			final Monitor monitor = Monitor.builder()
						.id(ID_VALUE)
						.parentId(PARENT_ID_VALUE)
						.name(LABEL_VALUE)
						.parameters(Map.of(STATUS_PARAMETER, statusParamNotAvailable))
						.build();
			assertFalse(OtelParameterToMetricObserver.isParameterAvailable(monitor, Enclosure.STATUS.getName()));

		}

		{
			final TextParam textParam = TextParam.builder().name(TEST_REPORT_PARAMETER).value("text").build();
			final Monitor monitor = Monitor.builder()
						.id(ID_VALUE)
						.parentId(PARENT_ID_VALUE)
						.name(LABEL_VALUE)
						.parameters(Map.of(TEST_REPORT_PARAMETER, textParam))
						.build();
			assertFalse(OtelParameterToMetricObserver.isParameterAvailable(monitor, MetaConnector.TEST_REPORT.getName()));

		}

	}

	@Test
	void testCheckParameter() {
		{
			final Monitor monitor = Monitor.builder()
					.id(ID_VALUE)
					.parentId(PARENT_ID_VALUE)
					.name(LABEL_VALUE)
					.parameters(Map.of(ENERGY_USAGE_PARAMETER,
							NumberParam.builder().name(ENERGY_USAGE_PARAMETER)
							.value(3000D).build()))
					.build();
			assertTrue(OtelParameterToMetricObserver.checkParameter(monitor, ENERGY_USAGE_PARAMETER));
		}

		{
			final Monitor monitor = Monitor.builder()
					.id(ID_VALUE)
					.parentId(PARENT_ID_VALUE)
					.name(LABEL_VALUE)
					.parameters(Collections.emptyMap())
					.build();
			assertFalse(OtelParameterToMetricObserver.checkParameter(monitor, ENERGY_USAGE_PARAMETER));
		}

		{
			final Monitor monitor = Monitor.builder()
					.id(ID_VALUE)
					.parentId(PARENT_ID_VALUE)
					.name(LABEL_VALUE)
					.parameters(null)
					.build();
			assertFalse(OtelParameterToMetricObserver.checkParameter(monitor, ENERGY_USAGE_PARAMETER));
		}

		{
			assertFalse(OtelParameterToMetricObserver.checkParameter(null, ENERGY_USAGE_PARAMETER));
		}
	}

	@Test
	void testConvertValue() {
		assertEquals(18, OtelParameterToMetricObserver.convertValue("1.8", 10));
		assertThrows(IllegalArgumentException.class, () -> OtelParameterToMetricObserver.convertValue(null, 10));
	}
}
