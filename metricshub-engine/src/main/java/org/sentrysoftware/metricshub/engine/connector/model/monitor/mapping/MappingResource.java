package org.sentrysoftware.metricshub.engine.connector.model.monitor.mapping;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents a mapping resource used in monitor configuration.
 *
 * <p>
 * A {@code MappingResource} instance holds information about the type and attributes of a mapping resource.
 * </p>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MappingResource implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * The type of the mapping resource.
	 */
	private String type;

	/**
	 * The attributes associated with the mapping resource.
	 */
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
