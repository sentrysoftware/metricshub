package com.sentrysoftware.hardware.agent.service.opentelemetry.signal;

import static com.sentrysoftware.hardware.agent.mapping.opentelemetry.MappingConstants.ID;
import static com.sentrysoftware.hardware.agent.mapping.opentelemetry.MappingConstants.NAME;
import static com.sentrysoftware.hardware.agent.mapping.opentelemetry.MappingConstants.PARENT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.EMPTY;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.sentrysoftware.hardware.agent.dto.MultiHostsConfigurationDto;
import com.sentrysoftware.hardware.agent.mapping.opentelemetry.dto.DynamicIdentifyingAttribute;
import com.sentrysoftware.hardware.agent.mapping.opentelemetry.dto.MetricInfo;
import com.sentrysoftware.matrix.common.helpers.NumberHelper;
import com.sentrysoftware.matrix.model.monitor.Monitor;

import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NonNull;

@Data
@AllArgsConstructor
public abstract class AbstractOtelObserver {

	protected final Monitor monitor;
	protected final SdkMeterProvider sdkMeterProvider;
	protected final MultiHostsConfigurationDto multiHostsConfigurationDto;

	protected static final Map<String, Function<Monitor, String>> ATTRIBUTE_FUNCTIONS = Map.of(
			ID, Monitor::getId,
			NAME, Monitor::getName,
			PARENT, mo -> getValueOrElse(mo.getParentId(), EMPTY)
	);

	/**
	 * Get the value or other if value is null
	 *
	 * @param <T>
	 * @param value
	 * @param other
	 * @return
	 */
	static <T> T getValueOrElse(final T value, final T other) {
		return value != null ? value : other;
	}

	/**
	 * Check if the given value can be parsed as double
	 *
	 * @param value The value we wish to check
	 * @return <code>true</code> if the value is not <code>null</code> and the parse succeeds otherwise <code>false</code>
	 */
	protected static boolean canParseDoubleValue(final String value) {
		return value != null && NumberHelper.parseDouble(value, null) != null;
	}

	/**
	 * Initialize a {@link Meter} instance in order to produce metrics, then builds
	 * an asynchronous instrument with a callback.
	 * The callback will be called when the Meter is being observed.
	 */
	abstract void init();

	/**
	 * Concatenate the given attributes with the extra labels and return the stream of
	 * sorted attribute keys
	 * 
	 * @param initialAttributeKeys
	 * @return Stream of String
	 */
	protected Stream<String> getAttributeKeys(final Collection<String> initialAttributeKeys) {
		return Stream
				.concat(initialAttributeKeys.stream(), multiHostsConfigurationDto.getExtraLabels().keySet().stream())
				.sorted();
	}

	/**
	 * Gets or creates a named meter instance.
	 * @param metricInfo Metric information used to create the unique meter
	 * 
	 * @return {@link Meter} instance defined by the metrics API
	 */
	protected Meter getMeter(final MetricInfo metricInfo) {

		return sdkMeterProvider.get(determineMeterId(metricInfo, monitor));
	}

	/**
	 * Determine the unique id to use when creating or getting a {@link Meter}
	 * 
	 * @param metricInfo Metric information used to get the
	 *                   {@link AbstractIdentifyingAttribute} list, the metric name
	 *                   and the additional id.
	 * @param monitor    Monitor defining the monitor id and used to get the
	 *                   {@link DynamicIdentifyingAttribute} instance.
	 * @return String value
	 */
	static String determineMeterId(final MetricInfo metricInfo, final Monitor monitor) {
		String meterId;
		final Optional<List<String[]>> maybeIdentifyingAttributes = OtelHelper.extractIdentifyingAttributes(metricInfo,
				monitor);
		final String format = "%s.%s";

		if (maybeIdentifyingAttributes.isPresent()) {
			final String identifyingAttributes = maybeIdentifyingAttributes
				.get()
				.stream()
				.map(keyValue -> String.format(format, keyValue[0], keyValue[1]))
				.collect(Collectors.joining("."));
			meterId = String.format("%s.%s.%s", monitor.getId(), metricInfo.getName(), identifyingAttributes);
		} else {
			meterId = String.format(format, monitor.getId(), metricInfo.getName());
		}

		final String additionalId = metricInfo.getAdditionalId();
		if (additionalId != null) {
			return String.format(format, meterId, additionalId);
		}

		return meterId;
	}

	/**
	 * Convert the given String value according to the given factor
	 *
	 * @param value  Cannot be null and must be a number
	 * @param factor The value is used to change the units of a measured value
	 * @return {@link Double} value
	 */
	protected static Double convertValue(@NonNull final String value, double factor) {

		return NumberHelper.parseDouble(value, null) * factor;
	}
}
