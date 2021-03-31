package com.sentrysoftware.matrix.connector.model.detection.criteria.kmversion;

import com.sentrysoftware.matrix.connector.model.detection.criteria.Criteria;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class KMVersion extends Criteria {

	private static final long serialVersionUID = -5818089198751116637L;

	private String version;

	@Builder
	public KMVersion(boolean forceSerialization, String version) {
		super(forceSerialization);
		this.version = version;
	}
}
