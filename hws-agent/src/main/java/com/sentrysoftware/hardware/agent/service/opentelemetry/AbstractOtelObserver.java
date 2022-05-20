package com.sentrysoftware.hardware.agent.service.opentelemetry;

import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.ID;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.NAME;
import static com.sentrysoftware.hardware.agent.service.opentelemetry.mapping.MappingConstants.PARENT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.EMPTY;

import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import com.sentrysoftware.hardware.agent.dto.MultiHostsConfigurationDto;
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
	 * 
	 * @return {@link Meter} instance defined by the metrics API
	 */
	protected Meter getMeter() {
		return sdkMeterProvider.get(monitor.getId());
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
