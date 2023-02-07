package com.sentrysoftware.matrix.connector.model.identity;

import static com.fasterxml.jackson.annotation.Nulls.FAIL;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonSetter;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

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

	@JsonSetter(nulls = FAIL)
	@NonNull
	private Detection detection;
}
