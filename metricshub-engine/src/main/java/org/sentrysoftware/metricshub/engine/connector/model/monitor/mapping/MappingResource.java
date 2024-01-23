package org.sentrysoftware.metricshub.engine.connector.model.monitor.mapping;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MappingResource implements Serializable {

	private static final long serialVersionUID = 1L;

	private String type;

	@Builder.Default
	private Map<String, String> attributes = new HashMap<>();

	/**
	 * Whether the type is defined or not
	 *
	 * @return boolean value
	 */
	public boolean hasType() {
		return type != null && !type.isBlank();
	}
}
