package com.sentrysoftware.matrix.connector.model.detection.criteria.service;

import com.sentrysoftware.matrix.connector.model.detection.criteria.Criteria;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Service extends Criteria {

	private static final long serialVersionUID = -6173571823803344096L;

	private String serviceName;

	@Builder
	public Service(boolean forceSerialization, String serviceName) {

		super(forceSerialization);
		this.serviceName = serviceName;
	}

	
}
