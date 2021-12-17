package com.sentrysoftware.hardware.agent.service.opentelemetry;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.EMPTY;

import java.util.Map;
import java.util.Optional;

import org.springframework.util.Assert;

import com.sentrysoftware.hardware.agent.dto.MetricInfo;
import com.sentrysoftware.hardware.agent.dto.MetricInfo.MetricType;
import com.sentrysoftware.hardware.agent.dto.MultiHostsConfigurationDTO;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
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

		if (type.equals(MetricType.COUNTER)) {
			// Sum (Counter)
			meter
				.counterBuilder(metricInfo.getName() + "_total")
				.setDescription(metricInfo.getDescription())
				.setUnit(metricInfo.getUnit())
				.ofDoubles()
				.buildWithCallback(recorder -> observe(monitor, recorder));

		} else {
			// Gauge
			meter
				.gaugeBuilder(metricInfo.getName())
				.setDescription(metricInfo.getDescription())
				.setUnit(metricInfo.getUnit())
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
	 * Create OpenTelemetry {@link Attributes} using known attributes which could be overridden by the user
	 * 
	 * @param monitor           The monitor from which we want to extract the
	 *                          metadata

	 * @return OpenTelemetry {@link Attributes} instance
	 */
	Attributes createAttributes(final Monitor monitor) {

		// This is defined by the internal metrics mapping
		final Map<String, String> initialAttributesMap = MetricsMapping.getAttributesMap(monitor.getMonitorType());

		checkAttributesMap(monitor.getMonitorType(), initialAttributesMap);

		final AttributesBuilder attributesBuilder = Attributes.builder();

		//  > Attribute value from the predefined function 
		//    > or from overridden extra labels
		//      > or from metadata value
		//        > or empty
		getAttributeKeys(initialAttributesMap.keySet())
				.forEach(attributeKey ->  {
					final String attributeValue = ATTRIBUTE_FUNCTIONS
							.getOrDefault(
									attributeKey, 
									mo -> multiHostsConfigurationDTO.getExtraLabels()
									.getOrDefault(
											attributeKey,
											convertMetadataInfoValue(mo, initialAttributesMap.get(attributeKey))
									)
							)
							.apply(monitor);
					attributesBuilder.put(attributeKey, attributeValue);
				});

		return attributesBuilder.build();

	}

	/**
	 * Convert the metadata value if needed otherwise get the value as it is
	 * 
	 * @param monitor             The monitor from which we extract the metadata value
	 * @param matrixMetadataName  The metadata identifier
	 * @return String value
	 */
	String convertMetadataInfoValue(final Monitor monitor, final String matrixMetadataName) {
		if (matrixMetadataName == null || matrixMetadataName.isBlank() || monitor == null || monitor.getMetadata() == null) {
			return EMPTY;
		}

		// Check if its value needs to be converted
		String metricValue = getValueOrElse(monitor.getMetadata(matrixMetadataName), EMPTY);

		// Check if there is a metadata MetricInfo in order to get the factor
		final Optional<MetricInfo> maybeMetric = MetricsMapping
				.getMetadataAsMetricInfo(monitor.getMonitorType(), matrixMetadataName);

		if (!maybeMetric.isPresent()) {
			return metricValue;
		}

		// Check the metadata is a number value
		if (canParseDoubleValue(metricValue)) {
			// Ok, now we can get the metricInfo related to the given metadata
			final MetricInfo metric = maybeMetric.get();

			return convertValue(metricValue, metric.getFactor()).toString();
		}

		// This is an unexpected metadata value (expected as number value)
		// Let's use empty instead of a bad non-number value in number attribute.
		return EMPTY;

	}

	/**
	 * Check the attributes map
	 * 
	 * @param monitorType    The monitor type for which we want to check the attributes map
	 * @param attributesMap  Map of attribute key to matrix metadata name
	 */
	static void checkAttributesMap(final MonitorType monitorType, final Map<String, String> attributesMap) {
		Assert.state(attributesMap != null && !attributesMap.isEmpty(),
				() -> "The attributes map is not defined for the monitor type: " + monitorType.getDisplayName());
	}
}
