package com.sentrysoftware.matrix.connector.model.identity.criterion;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Service extends Criterion {

	private static final long serialVersionUID = 1L;

	private String name;

	@Builder
	public Service(String type, boolean forceSerialization, String name) {

		super(type, forceSerialization);
		this.name = name;
	}

	@Override
	public String toString() {
		return "- Service: " + name;
	}

}
