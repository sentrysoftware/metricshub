package org.sentrysoftware.metricshub.engine.connector.model.monitor.task;

import static com.fasterxml.jackson.annotation.Nulls.SKIP;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sentrysoftware.metricshub.engine.connector.deserializer.custom.CaseInsensitiveTreeMapDeserializer;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.mapping.MappingResource;

/**
 * A class representing the mapping configuration for a monitor task. It includes mappings for source attributes,
 * metrics, conditional collections, and legacy text parameters. The mappings are case-insensitive.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Mapping implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * The source associated with the mapping.
	 */
	private String source;

	/**
	 * A case-insensitive map of attributes associated with the source.
	 */
	@Default
	@JsonDeserialize(using = CaseInsensitiveTreeMapDeserializer.class)
	@JsonSetter(nulls = SKIP)
	private Map<String, String> attributes = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

	/**
	 * A case-insensitive map of metrics associated with the source.
	 */
	@Default
	@JsonDeserialize(using = CaseInsensitiveTreeMapDeserializer.class)
	@JsonSetter(nulls = SKIP)
	private Map<String, String> metrics = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

	/**
	 * A case-insensitive map of conditional collections associated with the source.
	 */
	@Default
	@JsonDeserialize(using = CaseInsensitiveTreeMapDeserializer.class)
	@JsonSetter(nulls = SKIP)
	private Map<String, String> conditionalCollection = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

	/**
	 * A case-insensitive map of legacy text parameters associated with the source.
	 */
	@Default
	@JsonDeserialize(using = CaseInsensitiveTreeMapDeserializer.class)
	@JsonSetter(nulls = SKIP)
	private Map<String, String> legacyTextParameters = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

	/**
	 * The resource associated with the mapping.
	 */
	@JsonSetter(nulls = SKIP)
	private MappingResource resource;
}
