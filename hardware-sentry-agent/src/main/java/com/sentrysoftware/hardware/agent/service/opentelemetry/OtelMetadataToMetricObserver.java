package com.sentrysoftware.hardware.agent.service.opentelemetry;

import static com.sentrysoftware.hardware.agent.service.opentelemetry.MetricsMapping.DEFAULT_ATTRIBUTE_NAMES;

import com.sentrysoftware.hardware.agent.dto.MetricInfo;
import com.sentrysoftware.hardware.agent.dto.MultiHostsConfigurationDTO;
import com.sentrysoftware.matrix.model.monitor.Monitor;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OtelMetadataToMetricObserver extends AbstractOtelMetricObserver {

	@Builder
	public OtelMetadataToMetricObserver(Monitor monitor, SdkMeterProvider sdkMeterProvider,
			MultiHostsConfigurationDTO multiHostsConfigurationDTO, MetricInfo metricInfo, String matrixMetadata) {
		super(monitor, sdkMeterProvider, multiHostsConfigurationDTO, metricInfo, matrixMetadata);
	}

	@Override
	void observe(final Monitor monitor, final ObservableDoubleMeasurement recorder) {

		// We are observing! is the metadata available?
		if (checkMetadata(monitor, matrixDataKey)) {

			final Attributes attributes = createAttributes(monitor, getAttributeKeys(DEFAULT_ATTRIBUTE_NAMES));

			// Record the value
			recorder.observe(
					convertValue(monitor.getMetadata(matrixDataKey), metricInfo.getFactor()),
					attributes
			);
		}

	}

	/**
	 * Check if the metadata is collected and available as a number in the given monitor
	 *
	 * @param monitor       The monitor we wish to check its metadata
	 * @param metadata      The name of the metadata
	 * @return <code>true</code> if the metadata is collected otherwise <code>false</code>
	 */
	static boolean checkMetadata(final Monitor monitor, final String metadata) {
		return monitor != null
				&& monitor.getMetadata() != null
				&& canParseDoubleValue(monitor.getMetadata(metadata));
	}
}