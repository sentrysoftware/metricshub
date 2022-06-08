package com.sentrysoftware.hardware.agent.mapping.opentelemetry;

import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.EXPECTED_PATH_COUNT;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.MAXIMUM_SPEED;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.POWER_SUPPLY_POWER;
import static com.sentrysoftware.matrix.common.helpers.HardwareConstants.SIZE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;

import com.sentrysoftware.hardware.agent.mapping.MappingHelper;
import com.sentrysoftware.hardware.agent.mapping.opentelemetry.dto.MetricInfo;
import com.sentrysoftware.hardware.agent.mapping.opentelemetry.dto.MetricInfo.MetricType;
import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorType;

class MetricsMappingTest {

	@Test
	void testGetAttributesMap() {
		MonitorType.MONITOR_TYPES.forEach(monitorType -> assertNotNull(MetricsMapping.getAttributesMap(monitorType)));
		// Null attributes that are already defined as metrics.
		assertNull(MetricsMapping.getAttributesMap(MonitorType.PHYSICAL_DISK).get(SIZE));
		assertNull(MetricsMapping.getAttributesMap(MonitorType.LOGICAL_DISK).get(SIZE));
		assertNull(MetricsMapping.getAttributesMap(MonitorType.MEMORY).get(SIZE));
		assertNull(MetricsMapping.getAttributesMap(MonitorType.PHYSICAL_DISK).get(SIZE));
		assertNull(MetricsMapping.getAttributesMap(MonitorType.GPU).get(SIZE));
		assertNull(MetricsMapping.getAttributesMap(MonitorType.CPU)
				.get(MappingHelper.camelCaseToSnakeCase(MAXIMUM_SPEED)));
		assertNull(MetricsMapping.getAttributesMap(MonitorType.POWER_SUPPLY)
				.get(MappingHelper.camelCaseToSnakeCase(POWER_SUPPLY_POWER)));
		assertNull(MetricsMapping.getAttributesMap(MonitorType.LUN)
				.get(MappingHelper.camelCaseToSnakeCase(EXPECTED_PATH_COUNT)));

		assertEquals(HardwareConstants.HOSTNAME, MetricsMapping.getAttributesMap(MonitorType.VM)
				.get(VmMapping.VM_HOST_NAME_ATTRIBUTE_KEY));

		MonitorType
			.MONITOR_TYPES
			.stream()
			.filter(monitorType -> MonitorType.HOST != monitorType && MonitorType.CONNECTOR != monitorType)
			.forEach(monitorType -> 
				assertEquals(
					HardwareConstants.IDENTIFYING_INFORMATION,
					MetricsMapping.getAttributesMap(monitorType).get(MappingConstants.INFO_ATTRIBUTE_KEY),
					String.format(
						"The %s metadata name is not overridden for monitor type %s.",
						HardwareConstants.IDENTIFYING_INFORMATION,
						monitorType
					)
				)
			);

		assertEquals(HardwareConstants.VOLTAGE_TYPE, MetricsMapping.getAttributesMap(MonitorType.VOLTAGE)
				.get(MappingConstants.SENSOR_LOCATION_ATTRIBUTE_KEY));
		assertEquals(HardwareConstants.TEMPERATURE_TYPE, MetricsMapping.getAttributesMap(MonitorType.TEMPERATURE)
				.get(MappingConstants.SENSOR_LOCATION_ATTRIBUTE_KEY));
		assertEquals(HardwareConstants.FAN_TYPE, MetricsMapping.getAttributesMap(MonitorType.FAN)
				.get(MappingConstants.SENSOR_LOCATION_ATTRIBUTE_KEY));
		assertEquals(HardwareConstants.COMPILED_FILE_NAME, MetricsMapping.getAttributesMap(MonitorType.CONNECTOR)
				.get(ConnectorMapping.CONNECTOR_ID_ATTRIBUTE_KEY));

	}

	@Test
	void testGetMetricInfo() {

		final Map<String, Set<String>> metricDescriptions = new HashMap<>();
		for (MonitorType monitorType : MonitorType.MONITOR_TYPES) {

			assertMetrics(metricDescriptions, monitorType, MetricsMapping.getMatrixParamToMetricMap().get(monitorType));
			assertMetrics(metricDescriptions, monitorType, MetricsMapping.getMatrixMetadataToMetricMap().get(monitorType));
		}

		// Check metric descriptions
		for (Entry<String, Set<String>> metricEntry : metricDescriptions.entrySet()) {
			assertEquals(
				1,
				metricEntry.getValue().size(),
				String.format(
					"The metric %s is incorrectly mapped. This metric is mapped using %s descriptions: \n%s"
							+ ".\nOnly one description must be defined.",
					metricEntry.getKey(),
					metricEntry.getValue().size(),
					metricEntry.getValue().stream().collect(Collectors.joining("\n"))
				)
			);
		}

		final Optional<List<MetricInfo>> batteryChargeInfo = MetricsMapping.getMetricInfoList(MonitorType.BATTERY, "charge");

		assertTrue(batteryChargeInfo.isPresent());
		assertTrue(MetricsMapping.getMetricInfoList(MonitorType.BATTERY, "undefinedMatrixParamName").isEmpty());
		assertTrue(MetricsMapping.getMetricInfoList(MonitorType.BATTERY, "").isEmpty());

	}

	/**
	 * Check the given collection of matrixKeyToMetricInfo
	 * 
	 * @param metricDescriptions
	 * @param monitorType
	 * @param matrixKeyToMetricInfo
	 */
	void assertMetrics(Map<String, Set<String>> metricDescriptions, MonitorType monitorType, final Map<String, List<MetricInfo>> matrixKeyToMetricInfo) {
		final Set<String> metricIds = new HashSet<>();
		if (matrixKeyToMetricInfo != null) {
			for (Entry<String, List<MetricInfo>> entry : matrixKeyToMetricInfo.entrySet())  {
				for (MetricInfo metricInfo : entry.getValue()) {
					assertNotNull(metricInfo);
					String metricId;
					if (metricInfo.getIdentifyingAttribute() != null) {
						assertNotNull(metricInfo.getIdentifyingAttribute().getKey());
						assertNotNull(metricInfo.getIdentifyingAttribute().getValue());
						metricId = String.format("%s.%s.%s", metricInfo.getName(), metricInfo.getIdentifyingAttribute().getKey(), metricInfo.getIdentifyingAttribute().getValue());
					} else {
						metricId = metricInfo.getName();
					}

					// TODO: resolve conflict with 2 statuses for controller in HW Sentry KM
					if (metricInfo.getAdditionalId() != null) {
						metricId = String.format("%s.%s", metricId, metricInfo.getAdditionalId());
					}

					// Check if the metric is not defined multiple times
					assertFalse(
						metricIds.contains(metricId),
						String.format(
							"This metric (%s) is incorrectly mapped for monitor type %s. Metric id: %s."
									+ "\nIf no identifying attributes are defined for this metric, the metric name must be unique."
									+ "\nIf this metric is mapped with identifying attributes, the identifying attributes must be unique.",
							metricInfo.getName(),
							monitorType,
							metricId
						)
					);

					metricIds.add(metricId);

					metricDescriptions.computeIfAbsent(metricInfo.getName(), descriptions -> new HashSet<>());
					metricDescriptions.get(metricInfo.getName()).add(metricInfo.getDescription());

					// Assert the UpDownCounter metrics
					if (metricInfo.getName().endsWith("status")
							|| metricInfo.getPredicate() != null
							|| metricInfo.getName().equals(LogicalDiskMapping.USAGE_METRIC_NAME)) {
						assertEquals(
							MetricType.UP_DOWN_COUNTER,
							metricInfo.getType(),
							String.format("This metric (%s) must be an UpDownCounter.", metricInfo.getName())
						);
					}
				}
			}
		}
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
