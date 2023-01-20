package com.sentrysoftware.matrix.connector.model.identity.criterion;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class ProductRequirements extends Criterion {

	private static final long serialVersionUID = 1L;

	private String engineVersion;
	private String kmVersion;

	@Builder
	public ProductRequirements(
		String type,
		boolean forceSerialization,
		String engineVersion,
		String kmVersion
	) {
		super(type, forceSerialization);
		this.engineVersion = engineVersion;
		this.kmVersion = kmVersion;
	}

}
