package com.sentrysoftware.matrix.sustainability;

import static com.sentrysoftware.matrix.common.Constants.NETWORK_ENERGY_METRIC;
import static com.sentrysoftware.matrix.common.Constants.NETWORK_LINK_SPEED_ATTRIBUTE;
import static com.sentrysoftware.matrix.common.Constants.NETWORK_LINK_STATUS_METRIC;
import static com.sentrysoftware.matrix.common.Constants.NETWORK_POWER_METRIC;
import static com.sentrysoftware.matrix.common.Constants.NETWORK_TRANSMITTED_BANDWIDTH_UTILIZATION_METRIC;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.sentrysoftware.matrix.configuration.HostConfiguration;
import com.sentrysoftware.matrix.telemetry.MetricFactory;
import com.sentrysoftware.matrix.telemetry.Monitor;
import com.sentrysoftware.matrix.telemetry.TelemetryManager;
import com.sentrysoftware.matrix.telemetry.metric.NumberMetric;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

class NetworkPowerAndEnergyEstimatorTest {

	@Test
	void testEstimatePower() {
		Monitor monitor = Monitor.builder().attributes(new HashMap<>(Map.of("name", "prefix_wan_suffix"))).build();
		TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.strategyTime(1696597422644L)
			.hostConfiguration(HostConfiguration.builder().hostname("localhost").build())
			.build();
		NetworkPowerAndEnergyEstimator networkPowerAndEnergyEstimator = new NetworkPowerAndEnergyEstimator(
			monitor,
			telemetryManager
		);

		// Virtual network card -> estimated power consumption is 0.0
		assertEquals(0.0, networkPowerAndEnergyEstimator.estimatePower());

		// Not virtual network card, linkStatus == 0.0 -> estimated power consumption is 1.0
		monitor.addAttribute("name", "real_network_card");
		monitor.addMetric(NETWORK_LINK_STATUS_METRIC, NumberMetric.builder().value(1.0).build());
		assertEquals(1.0, networkPowerAndEnergyEstimator.estimatePower());

		// linkStatus is up, bandwidthUtilization and linkSpeed are null
		// estimated power consumption is 10.0
		monitor.addMetric(NETWORK_LINK_STATUS_METRIC, NumberMetric.builder().value(0.0).build());
		assertEquals(10.0, networkPowerAndEnergyEstimator.estimatePower());

		// linkStatus is up, bandwidthUtilization is null and linkSpeed = 100.0
		// estimated power consumption is 7.5
		monitor.addMetric(NETWORK_LINK_SPEED_ATTRIBUTE, NumberMetric.builder().value(100.0).build());
		assertEquals(7.5, networkPowerAndEnergyEstimator.estimatePower());

		// linkStatus is up, bandwidthUtilization is null and linkSpeed = 5.0
		// estimated power consumption is 2.0
		monitor.addMetric(NETWORK_LINK_SPEED_ATTRIBUTE, NumberMetric.builder().value(5.0).build());
		assertEquals(2.0, networkPowerAndEnergyEstimator.estimatePower());

		// linkStatus is up, bandwidthUtilization = 0.5, linkSpeed = 100.0
		// estimated power consumption is 7.5
		monitor.addMetric(NETWORK_TRANSMITTED_BANDWIDTH_UTILIZATION_METRIC, NumberMetric.builder().value(0.5).build());
		monitor.addMetric(NETWORK_LINK_SPEED_ATTRIBUTE, NumberMetric.builder().value(100.0).build());
		assertEquals(7.5, networkPowerAndEnergyEstimator.estimatePower());

		// linkStatus is up, bandwidthUtilization = 0.5, linkSpeed = 5.0
		// estimated power consumption is 2.75
		monitor.addMetric(NETWORK_LINK_SPEED_ATTRIBUTE, NumberMetric.builder().value(5.0).build());
		assertEquals(3.75, networkPowerAndEnergyEstimator.estimatePower());
	}

	@Test
	void testEstimateEnergy() {
		Monitor monitor = Monitor
			.builder()
			.attributes(new HashMap<>(Map.of("name", "real_network_card", NETWORK_LINK_SPEED_ATTRIBUTE, "100.0")))
			.metrics(
				new HashMap<>(
					Map.of(
						NETWORK_LINK_STATUS_METRIC,
						NumberMetric.builder().value(1.0).build(),
						NETWORK_TRANSMITTED_BANDWIDTH_UTILIZATION_METRIC,
						NumberMetric.builder().value(0.1).build()
					)
				)
			)
			.build();
		TelemetryManager telemetryManager = TelemetryManager
			.builder()
			.strategyTime(1696597422644L)
			.hostConfiguration(HostConfiguration.builder().hostname("localhost").build())
			.build();
		NetworkPowerAndEnergyEstimator networkPowerAndEnergyEstimator = new NetworkPowerAndEnergyEstimator(
			monitor,
			telemetryManager
		);

		// Estimate energy consumption, no previous collect time
		assertNull(networkPowerAndEnergyEstimator.estimateEnergy());

		// Estimate power consumption
		Double estimatedPower = networkPowerAndEnergyEstimator.estimatePower();
		Double estimatedEnergy = networkPowerAndEnergyEstimator.estimateEnergy();
		assertNull(estimatedEnergy);
		// Create metricFactory and collect power
		final MetricFactory metricFactory = new MetricFactory(telemetryManager.getHostname());
		final NumberMetric collectedPowerMetric = metricFactory.collectNumberMetric(
			monitor,
			NETWORK_POWER_METRIC,
			estimatedPower,
			telemetryManager.getStrategyTime()
		);

		// Save the collected power metric
		collectedPowerMetric.save();
		telemetryManager.setStrategyTime(telemetryManager.getStrategyTime() + 2 * 60 * 1000);

		// Estimate power consumption again
		estimatedPower = networkPowerAndEnergyEstimator.estimatePower();

		// Collect the new power consumption metric
		metricFactory.collectNumberMetric(
			monitor,
			NETWORK_POWER_METRIC,
			estimatedPower,
			telemetryManager.getStrategyTime()
		);

		// Estimate the energy
		estimatedEnergy = networkPowerAndEnergyEstimator.estimateEnergy();
		assertEquals(120.0, estimatedEnergy);
		final NumberMetric collectedEnergyMetric = metricFactory.collectNumberMetric(
			monitor,
			NETWORK_ENERGY_METRIC,
			estimatedEnergy,
			telemetryManager.getStrategyTime()
		);
		collectedEnergyMetric.save();
		collectedPowerMetric.save();

		telemetryManager.setStrategyTime(telemetryManager.getStrategyTime() + 2 * 60 * 1000);

		// Estimate power consumption again
		estimatedPower = networkPowerAndEnergyEstimator.estimatePower();

		// Collect the new power consumption metric
		metricFactory.collectNumberMetric(
			monitor,
			NETWORK_POWER_METRIC,
			estimatedPower,
			telemetryManager.getStrategyTime()
		);

		// Estimate the energy
		assertEquals(240.0, networkPowerAndEnergyEstimator.estimateEnergy());
	}
}
