package com.sentrysoftware.hardware.agent.service.opentelemetry;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.EMPTY;

import java.util.List;
import java.util.stream.Stream;

import com.sentrysoftware.hardware.agent.dto.MetricInfo;
import com.sentrysoftware.hardware.agent.dto.MultiHostsConfigurationDTO;
import com.sentrysoftware.hardware.agent.dto.MetricInfo.MetricType;
import com.sentrysoftware.matrix.model.monitor.Monitor;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public abstract class AbstractOtelMetricObserver extends AbstractOtelObserver {

	protected final MetricInfo metricInfo;
	protected final String matrixDataKey;

	protected AbstractOtelMetricObserver(Monitor monitor, SdkMeterProvider sdkMeterProvider,
			MultiHostsConfigurationDTO multiHostsConfigurationDTO, MetricInfo metricInfo, String matrixDataKey) {
		super(monitor, sdkMeterProvider, multiHostsConfigurationDTO);
		this.metricInfo = metricInfo;
		this.matrixDataKey = matrixDataKey;
	}

	/**
	 * Initialize a {@link Meter} instance in order to produce metrics, then builds
	 * an asynchronous instrument with a callback.
	 * The callback will be called when the Meter is being observed.
	 */
	@Override
	public void init() {

		final MetricType type = metricInfo.getType();

		// Gets or creates a named meter instance using the unique id of
		// the monitor as instrumentation library
		final Meter meter = getMeter();

		if (type.equals(MetricType.GAUGE)) {
			// Gauge
			meter
				.gaugeBuilder(metricInfo.getName())
				.setDescription(metricInfo.getDescription())
				.setUnit(metricInfo.getUnit())
				.ofLongs()
				.ofDoubles()
				.buildWithCallback(recorder -> observe(monitor, recorder));
		} else {
			// Sum (Counter)
			meter
				.counterBuilder(metricInfo.getName() + "_total")
				.setDescription(metricInfo.getDescription())
				.setUnit(metricInfo.getUnit())
				.ofDoubles()
				.buildWithCallback(recorder -> observe(monitor, recorder));
		}

	}

	/**
	 * Observe the metricInfo value
	 * 
	 * @param monitor    The monitor we wish to observe its parameter or metadata
	 * @param observable An instance observing measurements with double values
	 */
	abstract void observe(Monitor monitor, ObservableDoubleMeasurement recorder);

	/**
	 * Create OpenTelemetry {@link Attributes}. Attributes: <em>$fqdn</em>,
	 * <em>$monitorId</em>, <em>$monitorName</em>, <em>$monitorParentId</em> then
	 * the extra labels
	 *
	 * @param monitor           The monitor we wish to extract its id, parentId and
	 *                          name
	 * @param attributeKeys     The stream of attribute names
	 * @return {@link List} of {@link String} values
	 */
	protected Attributes createAttributes(final Monitor monitor, final Stream<String> attributeKeys) {

		final AttributesBuilder attributesBuilder = Attributes.builder();

		attributeKeys.forEach(attributeKey -> {
			String value = ATTRIBUTE_FUNCTIONS
					.getOrDefault(attributeKey, mo -> multiHostsConfigurationDTO
							.getExtraLabels()
							.getOrDefault(attributeKey, EMPTY))
					.apply(monitor);
			attributesBuilder.put(attributeKey, value);
		});
	
		return attributesBuilder.build();
	}
}
