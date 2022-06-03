package com.sentrysoftware.hardware.agent.dto.metric;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NonNull;

@Data
@EqualsAndHashCode(callSuper = true)
public class StaticIdentifyingAttribute extends AbstractIdentifyingAttribute {

	@Builder
	public StaticIdentifyingAttribute(@NonNull String key, @NonNull String value) {
		super(key, value);
	}
}
