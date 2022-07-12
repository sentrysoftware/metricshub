package com.sentrysoftware.hardware.agent.mapping.opentelemetry.dto;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class DynamicIdentifyingAttribute extends AbstractIdentifyingAttribute {

	@Builder
	public DynamicIdentifyingAttribute(@NonNull String key, @NonNull String value) {
		super(key, value);
	}
}