package org.sentrysoftware.metricshub.engine.connector.model.identity;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ConnectorIdentity implements Serializable {

	private static final long serialVersionUID = 1L;

	private String compiledFilename;
	private String displayName;
	private String platforms;
	private String reliesOn;
	private String version;
	private String projectVersion;
	private String information;

	private Detection detection;
}
