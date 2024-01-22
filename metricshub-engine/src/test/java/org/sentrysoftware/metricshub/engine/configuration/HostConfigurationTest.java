package org.sentrysoftware.metricshub.engine.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.sentrysoftware.metricshub.engine.connector.model.common.DeviceKind;
import org.sentrysoftware.metricshub.engine.constants.Constants;

/**
 * Test of {@link HostConfiguration}
 */
class HostConfigurationTest {

	@Test
	void testToString() {
		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostId(Constants.LOCALHOST)
			.strategyTimeout(Constants.STRATEGY_TIMEOUT)
			.hostType(DeviceKind.LINUX)
			.hostname(Constants.LOCALHOST)
			.sequential(false)
			.retryDelay(Constants.RETRY_DELAY)
			.build();
		Assertions.assertEquals(Constants.HOST_CONFIGURATION_TO_STRING, hostConfiguration.toString());
	}
}
