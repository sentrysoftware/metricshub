package org.sentrysoftware.metricshub.engine.configuration;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.sentrysoftware.metricshub.engine.common.exception.InvalidConfigurationException;

@Data
@AllArgsConstructor
@Builder
public class TestConfiguration implements IConfiguration {

	@Override
	public void validateConfiguration(String resourceKey) throws InvalidConfigurationException {}
}
