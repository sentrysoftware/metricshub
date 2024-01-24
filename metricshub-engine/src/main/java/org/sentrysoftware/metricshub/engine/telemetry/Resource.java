package org.sentrysoftware.metricshub.engine.telemetry;

import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a resource associated with a monitor.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Resource {

	private String type;

	@Default
	private Map<String, String> attributes = new HashMap<>();
}
