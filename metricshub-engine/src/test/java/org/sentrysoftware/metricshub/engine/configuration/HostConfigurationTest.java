package org.sentrysoftware.metricshub.engine.configuration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.sentrysoftware.metricshub.engine.constants.Constants.HOST_CONFIGURATION_TO_STRING;
import static org.sentrysoftware.metricshub.engine.constants.Constants.LOCALHOST;
import static org.sentrysoftware.metricshub.engine.constants.Constants.RETRY_DELAY;
import static org.sentrysoftware.metricshub.engine.constants.Constants.STRATEGY_TIMEOUT;

import org.junit.jupiter.api.Test;
import org.sentrysoftware.metricshub.engine.connector.model.common.DeviceKind;

/**
 * Test of {@link HostConfiguration}
 */
class HostConfigurationTest {

	@Test
	void testToString() {
		final HostConfiguration hostConfiguration = HostConfiguration
			.builder()
			.hostId(LOCALHOST)
			.strategyTimeout(STRATEGY_TIMEOUT)
			.hostType(DeviceKind.LINUX)
			.hostname(LOCALHOST)
			.sequential(false)
			.retryDelay(RETRY_DELAY)
			.build();
		assertEquals(HOST_CONFIGURATION_TO_STRING, hostConfiguration.toString());
	}
}
