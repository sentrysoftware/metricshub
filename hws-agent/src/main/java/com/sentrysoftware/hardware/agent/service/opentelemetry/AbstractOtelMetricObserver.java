package com.sentrysoftware.hardware.agent.service.opentelemetry;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.EMPTY;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.util.Assert;

import com.sentrysoftware.hardware.agent.dto.MultiHostsConfigurationDto;
import com.sentrysoftware.hardware.agent.mapping.opentelemetry.MetricsMapping;
import com.sentrysoftware.hardware.agent.mapping.opentelemetry.dto.MetricInfo;
import com.sentrysoftware.hardware.agent.mapping.opentelemetry.dto.MetricInfo.MetricType;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.model.monitor.Monitor;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.metrics.DoubleGaugeBuilder;
import io.opentelemetry.api.metrics.LongCounterBuilder;
import io.opentelemetry.api.metrics.LongUpDownCounterBuilder;
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

	protected final List<MetricInfo> metricInfoList;
	protected final String matrixDataKey;

	protected AbstractOtelMetricObserver(Monitor monitor, SdkMeterProvider sdkMeterProvider,
			MultiHostsConfigurationDto multiHostsConfigurationDto, List<MetricInfo> metricInfoList, String matrixDataKey) {
		super(monitor, sdkMeterProvider, multiHostsConfigurationDto);
		this.metricInfoList = metricInfoList;
		this.matrixDataKey = matrixDataKey;
	}

	/**
	 * Initialize a {@link Meter} instance in order to produce metrics, then builds
	 * an asynchronous instrument with a callback.
	 * The callback will be called when the Meter is being observed.
	 */
	@Override
	public void init() {

		// Loop over each metric information and build the metric callback
		for (final MetricInfo metricInfo : metricInfoList) {

			final MetricType type = metricInfo.getType();

			// Gets or creates a named meter instance using the unique id of
			// the monitor and the metric identifier as instrument
			final Meter meter = getMeter(metricInfo);

			if (type.equals(MetricType.COUNTER)) {
				// Sum (Counter)
				final LongCounterBuilder builder = meter
					.counterBuilder(metricInfo.getName())
					.setDescription(metricInfo.getDescription());

				// Set the unit if it is available
				if (!metricInfo.getUnit().isBlank()) {
					builder.setUnit(metricInfo.getUnit());
				}

				builder
					.ofDoubles()
					.buildWithCallback(recorder -> observe(metricInfo, monitor, recorder));

			} else if (type.equals(MetricType.UP_DOWN_COUNTER)) {
				// UpDownCounter
				final LongUpDownCounterBuilder builder = meter
						.upDownCounterBuilder(metricInfo.getName())
						.setDescription(metricInfo.getDescription());

				// Set the unit if it is available
				if (!metricInfo.getUnit().isBlank()) {
					builder.setUnit(metricInfo.getUnit());
				}

				builder
					.ofDoubles()
					.buildWithCallback(recorder -> observe(metricInfo, monitor, recorder));
			} else {
				// Gauge
				final DoubleGaugeBuilder builder = meter
						.gaugeBuilder(metricInfo.getName())
						.setDescription(metricInfo.getDescription());

				// Set the unit if it is available
				if (!metricInfo.getUnit().isBlank()) {
					builder.setUnit(metricInfo.getUnit());
				}

				builder.buildWithCallback(recorder -> observe(metricInfo, monitor, recorder));
			}
		}

	}

	/**
	 * Observe the metricInfo value
	 * 
	 * @param metricInfo Metric information (name, unit, description, conversion factor...)
	 * @param monitor    The monitor we wish to observe its parameter or metadata
	 * @param recorder   An instance observing measurements with double values
	 */
	abstract void observe(MetricInfo metricInfo, Monitor monitor, ObservableDoubleMeasurement recorder);

	/**
	 * Create OpenTelemetry {@link Attributes} using known attributes which could be
	 * overridden by the user
	 * 
	 * @param metricInfo Metric information used to append the metric's identifying attributes
	 * @param monitor    The monitor from which we want to extract the metadata
	 * 
	 * @return OpenTelemetry {@link Attributes} instance
	 */
	Attributes createAttributes(final MetricInfo metricInfo, final Monitor monitor) {

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
									mo -> multiHostsConfigurationDto.getExtraLabels()
									.getOrDefault(
											attributeKey,
											convertMetadataInfoValue(mo, initialAttributesMap.get(attributeKey))
									)
							)
							.apply(monitor);
					attributesBuilder.put(attributeKey, attributeValue);
				});

		// Add the identifying attribute to the metric's attributes
		final Optional<List<String[]>> maybeIdentifyingAttributes =  OtelHelper.extractIdentifyingAttribute(metricInfo, monitor);
		if (maybeIdentifyingAttributes.isPresent()) {
			final List<String[]> identifyingAttributes = maybeIdentifyingAttributes.get();
			identifyingAttributes.forEach(identifyingAttribute ->
				attributesBuilder.put(identifyingAttribute[0], identifyingAttribute[1])
			);
		}

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
		final Optional<List<MetricInfo>> maybeMetrics = MetricsMapping
				.getMetadataAsMetricInfoList(monitor.getMonitorType(), matrixMetadataName);

		if (!maybeMetrics.isPresent()) {
			return metricValue;
		}

		// Check the metadata is a number value
		if (canParseDoubleValue(metricValue)) {
			// Ok, now we can get the metricInfo related to the given metadata
			final Optional<MetricInfo> maybeMetricInfo = maybeMetrics.get().stream().findFirst();

			// Convert the value
			if (maybeMetricInfo.isPresent()) {
				return convertValue(metricValue, maybeMetricInfo.get().getFactor()).toString();
			}

			return metricValue;
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
