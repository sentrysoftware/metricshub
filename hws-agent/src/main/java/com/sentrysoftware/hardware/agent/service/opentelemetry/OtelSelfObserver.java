package com.sentrysoftware.hardware.agent.service.opentelemetry;

import static com.sentrysoftware.hardware.agent.service.opentelemetry.MetricsMapping.AGENT_METRIC_INFO;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.EMPTY;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.sentrysoftware.hardware.agent.dto.MultiHostsConfigurationDto;
import com.sentrysoftware.matrix.model.monitor.Monitor;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper =  true)
@ToString(callSuper = true)
public class OtelSelfObserver extends AbstractOtelObserver {

	private Map<String, String> agentInfo;

	@Builder
	public OtelSelfObserver(Monitor monitor, SdkMeterProvider sdkMeterProvider,
			MultiHostsConfigurationDto multiHostsConfigurationDto,
			Map<String, String> agentInfo) {
		super(monitor, sdkMeterProvider, multiHostsConfigurationDto);
		this.agentInfo = agentInfo;

	}

	/**
	 * Initialize a {@link Meter} instance in order to produce metrics, then builds
	 * an asynchronous instrument for the agent info and the extra-metrics using callback functions.
	 * The callback will be called when the Meter is being observed.
	 */
	public void init() {

		final Meter meter = sdkMeterProvider.get("com.sentrysoftware.hardware.agent");

		// Agent information
		meter
			.gaugeBuilder(AGENT_METRIC_INFO.getName())
			.setDescription(AGENT_METRIC_INFO.getDescription())
			.ofLongs()
			.buildWithCallback(this::observeCollectorInformation);

		// Extra metrics processing
		multiHostsConfigurationDto
			.getExtraMetrics()
			.entrySet()
			.stream()
			.filter(entry -> Objects.nonNull(entry.getValue()))
			.forEach(entry -> 
				meter
					.gaugeBuilder(entry.getKey())
					.setDescription(String.format("Reports metric %s", entry.getKey()))
					.buildWithCallback(recorder -> observeExtraMetric(recorder, entry.getValue()))
			);
	}

	/**
	 * Observe the collector information
	 * 
	 * @param recorder An instance observing measurements with long values. 
	 */
	void observeCollectorInformation(final ObservableLongMeasurement recorder) {
		// Get attributes with extra labels
		final Attributes attributes = createAttributes(agentInfo.keySet());

		// Record default value
		recorder.record(1, attributes);
	}

	/**
	 * Observe the extra metrics
	 * 
	 * @param recorder An instance observing measurements with double values.
	 * @param value    The value to record
	 */
	void observeExtraMetric(final ObservableDoubleMeasurement recorder, final double value) {
		// Get attributes with extra labels
		final Attributes attributes = createAttributes(Collections.emptySet());

		// Record the user value
		recorder.record(value, attributes);
	}

	/**
	 * Create OpenTelemetry attributes using the intialAttributes. Merge the extra
	 * labels if defined by the user.
	 * 
	 * @param attributeKeys set of attribute keys
	 * @return OpenTelemetry {@link Attributes} instance
	 */
	Attributes createAttributes(final Set<String> attributeKeys) {
		final AttributesBuilder builder = Attributes.builder();

		getAttributeKeys(attributeKeys)
			.forEach(attributeKey -> {
				final String value = agentInfo.getOrDefault(attributeKey,
						multiHostsConfigurationDto.getExtraLabels().getOrDefault(attributeKey, EMPTY));
				builder.put(attributeKey, value);
			});

		return builder.build();
	}
}
