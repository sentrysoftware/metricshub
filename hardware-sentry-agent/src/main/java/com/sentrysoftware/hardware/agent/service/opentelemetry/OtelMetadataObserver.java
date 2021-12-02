package com.sentrysoftware.hardware.agent.service.opentelemetry;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.EMPTY;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.springframework.util.Assert;

import com.sentrysoftware.hardware.agent.dto.MetricInfo;
import com.sentrysoftware.hardware.agent.dto.MultiHostsConfigurationDTO;
import com.sentrysoftware.hardware.agent.service.ServiceHelper;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;
import com.sentrysoftware.matrix.model.monitor.Monitor;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OtelMetadataObserver extends AbstractOtelObserver {

	private MetricInfo metricInfo;

	@Builder
	public OtelMetadataObserver(Monitor monitor, SdkMeterProvider sdkMeterProvider,
			MultiHostsConfigurationDTO multiHostsConfigurationDTO, MetricInfo metricInfo) {
		super(monitor, sdkMeterProvider, multiHostsConfigurationDTO);

		this.metricInfo = metricInfo;
	}

	@Override
	public void init() {

		// Gets or creates a named and versioned meter instance using the unique id of
		// the monitor as instrumentation library
		final Meter meter = getMeter();

		meter
			.gaugeBuilder(metricInfo.getName())
			.setDescription(metricInfo.getDescription())
			.ofLongs()
			.buildWithCallback(recorder -> observe(monitor, recorder));
	}

	/**
	 * Observe the info gauge metricInfo (default value = 1) and update the metadata as
	 * attributes
	 * 
	 * @param monitor  The monitor instance from which we build the attributes
	 * @param recorder The instance observing measurements with long values
	 */
	void observe(final Monitor monitor, final ObservableLongMeasurement recorder) {

		final MonitorType monitorType = monitor.getMonitorType();

		Assert.notNull(monitorType, () -> "monitor type cannot be null for monitor: " + monitor.getId());

		// This is defined by the internal metrics mapping
		final Set<String> staticAttributes = MetricsMapping.getAttributes(monitorType);

		checkStaticAttributes(monitorType, staticAttributes);

		// Concatenate extra labels to static attributes
		final Stream<String> attributeKeys = getAttributeKeys(staticAttributes);

		// Observe the value
		recorder.observe(1, createAttributes(monitor, attributeKeys));
	}

	/**
	 * Check the static attributes
	 * 
	 * @param monitorType      The monitor type for which we want to check the
	 *                         static attributes
	 * @param staticAttributes Set of attribute keys
	 */
	static void checkStaticAttributes(final MonitorType monitorType, final Set<String> staticAttributes) {
		Assert.state(staticAttributes != null && !staticAttributes.isEmpty(),
				() -> "The attribute keys are not defined for the monitor type: " + monitorType.getDisplayName());
	}

	/**
	 * Create OpenTelemetry metadata {@link Attributes}.
	 * 
	 * @param monitor           The monitor from which we want to extract the
	 *                          metadata
	 * @param attributeKeys     Stream of attribute keys. Could be overridden by the user
	 *                          through the {@link MultiHostsConfigurationDTO}

	 * @return OpenTelemetry {@link Attributes} instance
	 */
	Attributes createAttributes(final Monitor monitor, final Stream<String> attributeKeys) {

		final AttributesBuilder attributesBuilder = Attributes.builder();

		//  > Attribute value from the predefined function 
		//    > or from overridden extra labels
		//      > or from metadata value
		//        > or empty
		attributeKeys
			.forEach(attributeKey ->  {
				final String attributeValue = ATTRIBUTE_FUNCTIONS
						.getOrDefault(attributeKey, mo -> multiHostsConfigurationDTO
								.getExtraLabels()
								.getOrDefault(attributeKey, convertMetadataInfoValue(mo, attributeKey)))
				.apply(monitor);
				attributesBuilder.put(attributeKey, attributeValue);
			});

		return attributesBuilder.build();

	}

	/**
	 * Convert the metadata value if needed otherwise get the value as it is
	 * 
	 * @param monitor  The monitor from which we extract the metadata value
	 * @param attributeKey The metricInfo attribute identifier
	 * @return String value
	 */
	String convertMetadataInfoValue(final Monitor monitor, final String attributeKey) {
		if (attributeKey == null || attributeKey.isEmpty() || monitor == null || monitor.getMetadata() == null) {
			return EMPTY;
		}

		// Get the metadata name
		final String matrixMetadataName = ServiceHelper.snakeCaseToCamelCase(attributeKey);

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

}
