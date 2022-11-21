package com.sentrysoftware.hardware.agent.service.opentelemetry.signal;

import java.util.List;

import com.sentrysoftware.hardware.agent.dto.MultiHostsConfigurationDto;
import com.sentrysoftware.hardware.agent.mapping.opentelemetry.dto.MetricInfo;
import com.sentrysoftware.matrix.model.monitor.Monitor;
import com.sentrysoftware.matrix.model.monitoring.IHostMonitoring;

import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OtelMetadataToMetricObserver extends AbstractOtelMetricObserver {

	private IHostMonitoring hostMonitoring;

	@Builder
	public OtelMetadataToMetricObserver(Monitor monitor, SdkMeterProvider sdkMeterProvider,
			MultiHostsConfigurationDto multiHostsConfigurationDto, List<MetricInfo> metricInfoList,
			String matrixMetadata, IHostMonitoring hostMonitoring) {
		super(monitor, sdkMeterProvider, multiHostsConfigurationDto, metricInfoList, matrixMetadata);
		this.hostMonitoring = hostMonitoring;
	}

	/**
	 * Observe the metadata value
	 * 
	 * @param metricInfo The metric information (name, unit, description, conversion factor...)
	 * @param monitor    The monitor we wish to observe its parameter
	 * @param recorder   An instance observing measurements with double values
	 */
	@Override
	void observe(final MetricInfo metricInfo, final Monitor monitor, final ObservableDoubleMeasurement recorder) {

		// We are observing! is the metadata available?
		if (checkMetadata(monitor, matrixDataKey, hostMonitoring)) {

			// Record the value
			recorder.record(
					convertValue(monitor.getMetadata(matrixDataKey), metricInfo.getFactor()),
					createAttributes(metricInfo, monitor)
			);
		}

	}

	/**
	 * Check if the metadata is collected and available as a number in the given monitor
	 *
	 * @param monitor        The monitor we wish to check its metadata
	 * @param metadata       The name of the metadata
	 * @param hostMonitoring The wrapper of all the monitors
	 * @return <code>true</code> if the metadata is collected otherwise <code>false</code>
	 */
	static boolean checkMetadata(final Monitor monitor, final String metadata, final IHostMonitoring hostMonitoring) {
		if (monitor == null || monitor.getMetadata() == null || hostMonitoring == null) {
			return false;
		}

		// Don't report the device metadata with failed connector
		if (!hostMonitoring.isConnectorStatusOk(monitor)) {
			return false;
		}

		return canParseDoubleValue(monitor.getMetadata(metadata));
	}
}
