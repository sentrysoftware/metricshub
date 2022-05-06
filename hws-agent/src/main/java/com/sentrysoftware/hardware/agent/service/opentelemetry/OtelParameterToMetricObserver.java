package com.sentrysoftware.hardware.agent.service.opentelemetry;

import com.sentrysoftware.hardware.agent.dto.MetricInfo;
import com.sentrysoftware.hardware.agent.dto.MultiHostsConfigurationDto;
import com.sentrysoftware.matrix.common.meta.parameter.MetaParameter;
import com.sentrysoftware.matrix.model.monitor.Monitor;

import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Data
@EqualsAndHashCode(callSuper =  true)
@ToString(callSuper = true)
public class OtelParameterToMetricObserver extends AbstractOtelMetricObserver {

	@Builder
	public OtelParameterToMetricObserver(Monitor monitor, SdkMeterProvider sdkMeterProvider,
			MultiHostsConfigurationDto multiHostsConfigurationDto, MetricInfo metricInfo, String matrixParameterName) {

		super(monitor, sdkMeterProvider, multiHostsConfigurationDto, metricInfo, matrixParameterName);

	}

	/**
	 * Observe the parameter value
	 * 
	 * @param monitor  The monitor we wish to observe its parameter
	 * @param recorder An instance observing measurements with double values
	 */
	@Override
	void observe(final Monitor monitor, final ObservableDoubleMeasurement recorder) {

		// The parameter is not available, we can just stop our callback
		if (!isParameterAvailable(monitor, matrixDataKey)) {
			return;
		}

		// Record the value
		recorder.record(
				getParameterValue(monitor, matrixDataKey).doubleValue() * metricInfo.getFactor(),
				// Create the metric attributes
				createAttributes(monitor)
		);
	}

	/**
	 * Check if the parameter defined in the passed {@link MetaParameter} is collected on the given monitor instance
	 *
	 * @param monitor       The monitor we wish to check the parameter
	 * @param parameterName The name of the parameter to check
	 * @return <code>true</code> if the metricInfo is collected otherwise <code>false</code>
	 */
	static boolean isParameterAvailable(final Monitor monitor, final String parameterName) {
		return checkParameter(monitor, parameterName)
				&& getParameterValue(monitor, parameterName) != null;
	}

	/**
	 * Check if the parameter exists in the given monitor
	 *
	 * @param monitor       The monitor we wish to check its parameter
	 * @param parameterName The name of the parameter e.g. energyUsage, voltage, temperature.
	 * @return <code>true</code> if the metricInfo is collected otherwise <code>false</code>
	 */
	static boolean checkParameter(final Monitor monitor, final String parameterName) {
		return monitor != null
				&& monitor.getParameters() != null
				&& monitor.getParameters().get(parameterName) != null;
	}

	/**
	 * Get the parameter number value from the monitor instance
	 *
	 * @param monitor       The monitor from which we extract the parameter value
	 * @param parameterName The parameter name we want to extract from the given monitor instance
	 * @return {@link Number} value
	 */
	static Number getParameterValue(final Monitor monitor, final String parameterName) {

		return monitor.getParameters().get(parameterName).numberValue();
	}

}
