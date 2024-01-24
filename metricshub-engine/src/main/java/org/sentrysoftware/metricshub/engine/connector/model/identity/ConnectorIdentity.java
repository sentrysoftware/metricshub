package org.sentrysoftware.metricshub.engine.connector.model.identity;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents the identity information of a connector.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConnectorIdentity implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * The compiled filename of the connector.
	 */
	private String compiledFilename;

	/**
	 * The display name of the connector.
	 */
	private String displayName;

	/**
	 * The platforms on which the connector operates.
	 */
	private String platforms;

	/**
	 * The dependencies or components that the connector relies on.
	 */
	private String reliesOn;

	/**
	 * The version of the connector.
	 */
	private String version;

	/**
	 * The project version of the connector.
	 */
	private String projectVersion;

	/**
	 * Additional information about the connector.
	 */
	private String information;

	/**
	 * The detection information of the connector.
	 */
	private Detection detection;
}
