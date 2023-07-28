package com.sentrysoftware.matrix.engine.configuration;

import com.sentrysoftware.matrix.connector.model.common.DeviceKind;
import org.junit.Test;

import static com.sentrysoftware.matrix.constants.Constants.HOST_CONFIGURATION_TO_STRING;
import static com.sentrysoftware.matrix.constants.Constants.LOCALHOST;
import static com.sentrysoftware.matrix.constants.Constants.RETRY_DELAY;
import static com.sentrysoftware.matrix.constants.Constants.STRATEGY_TIMEOUT;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test of {@link HostConfiguration}
 */
class HostConfigurationTest {
	@Test
	public void testToString() {
		final HostConfiguration hostConfiguration = HostConfiguration.builder()
			.strategyTimeout(STRATEGY_TIMEOUT)
			.hostType(DeviceKind.LINUX)
			.hostname(LOCALHOST)
			.sequential(false)
			.retryDelay(RETRY_DELAY)
			.build();
		assertEquals(HOST_CONFIGURATION_TO_STRING, hostConfiguration.toString());
	}
}
