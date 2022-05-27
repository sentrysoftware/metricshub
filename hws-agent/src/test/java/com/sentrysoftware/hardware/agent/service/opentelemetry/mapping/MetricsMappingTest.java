package com.sentrysoftware.hardware.agent.service.opentelemetry.mapping;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.EXPECTED_PATH_COUNT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.MAXIMUM_SPEED;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.POWER_SUPPLY_POWER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import com.sentrysoftware.hardware.agent.dto.metric.MetricInfo;
import com.sentrysoftware.hardware.agent.service.ServiceHelper;
import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;

class MetricsMappingTest {

	@Test
	@Disabled
	void testGetAttributesMap() {
		MonitorType.MONITOR_TYPES.forEach(monitorType -> assertNotNull(MetricsMapping.getAttributesMap(monitorType)));
		// Null attributes that are already defined as metrics.
		assertNull(MetricsMapping.getAttributesMap(MonitorType.PHYSICAL_DISK).get(SIZE));
		assertNull(MetricsMapping.getAttributesMap(MonitorType.LOGICAL_DISK).get(SIZE));
		assertNull(MetricsMapping.getAttributesMap(MonitorType.MEMORY).get(SIZE));
		assertNull(MetricsMapping.getAttributesMap(MonitorType.PHYSICAL_DISK).get(SIZE));
		assertNull(MetricsMapping.getAttributesMap(MonitorType.GPU).get(SIZE));
		assertNull(MetricsMapping.getAttributesMap(MonitorType.CPU)
				.get(ServiceHelper.camelCaseToSnakeCase(MAXIMUM_SPEED)));
		assertNull(MetricsMapping.getAttributesMap(MonitorType.POWER_SUPPLY)
				.get(ServiceHelper.camelCaseToSnakeCase(POWER_SUPPLY_POWER)));
		assertNull(MetricsMapping.getAttributesMap(MonitorType.LUN)
				.get(ServiceHelper.camelCaseToSnakeCase(EXPECTED_PATH_COUNT)));

		assertEquals(HardwareConstants.HOSTNAME, MetricsMapping.getAttributesMap(MonitorType.VM)
				.get(MetricsMapping.VM_HOST_NAME));
	}

	@Test
	void testGetMetricInfo() {

		Set<String> metricIds = new HashSet<>();
		for (MonitorType monitorType : MonitorType.MONITOR_TYPES) {

			final Map<String, List<MetricInfo>> matrixParamToMetricInfo = MetricsMapping.getMatrixParamToMetricMap()
					.get(monitorType);
			if (matrixParamToMetricInfo != null) {
				for (Entry<String, List<MetricInfo>> entry : matrixParamToMetricInfo.entrySet())  {
					for (MetricInfo metricInfo : entry.getValue()) {
						assertNotNull(metricInfo);
						String metricId;
						if (metricInfo.getIdentifyingAttribute() != null) {
							metricId = String.format("%s.%s.%s", metricInfo.getName(), metricInfo.getIdentifyingAttribute().getKey(), metricInfo.getIdentifyingAttribute().getValue());
						} else {
							metricId = metricInfo.getName();
						}

						// TODO: resolve conflict with 2 statuses for controller in HW Sentry KM
						if(metricId.equals("hw.disk_controller.status.state.ok")) {
							continue;
						}
						if(metricId.equals("hw.disk_controller.status.state.degraded")) {
							continue;
						}
						if(metricId.equals("hw.disk_controller.status.state.failed")) {
							continue;
						}

						// Check if the metric is not defined multiple times
						assertFalse(
							metricIds.contains(metricId),
							String.format(
								"This metric (%s) is badly mapped for monitor type %s. Metric id: %s."
										+ "\nIf no identifying attributes are defined for this metric, the metric name must be unique."
										+ "\nIf this metric is mapped with identifying attributes, the identifying attributes must be unique.",
								metricInfo.getName(),
								monitorType,
								metricId
							)
						);

						metricIds.add(metricId);
					}
				}
			}
		}

		final Optional<List<MetricInfo>> batteryChargeInfo = MetricsMapping.getMetricInfoList(MonitorType.BATTERY, "charge");

		assertTrue(batteryChargeInfo.isPresent());
		assertTrue(MetricsMapping.getMetricInfoList(MonitorType.BATTERY, "undefinedMatrixParamName").isEmpty());
		assertTrue(MetricsMapping.getMetricInfoList(MonitorType.BATTERY, "").isEmpty());

	}

	@Test
	void testGetMetadataAsMetricInfo() {

		assertTrue(MetricsMapping.getMetadataAsMetricInfoList(MonitorType.PHYSICAL_DISK, SIZE).isPresent());
		assertEquals(Optional.empty(), MetricsMapping.getMetadataAsMetricInfoList(MonitorType.MEMORY, "undefinedMatrixMetadataName"));
		assertEquals(Optional.empty(), MetricsMapping.getMetadataAsMetricInfoList(MonitorType.MEMORY, ""));
		assertEquals(Optional.empty(), MetricsMapping.getMetadataAsMetricInfoList(MonitorType.MEMORY, null));
		assertEquals(Optional.empty(), MetricsMapping.getMetadataAsMetricInfoList(MonitorType.ENCLOSURE, ""));

	}

}
