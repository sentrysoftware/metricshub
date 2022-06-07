package com.sentrysoftware.hardware.agent.dto.metric;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public abstract class AbstractIdentifyingAttribute {

	private String key;
	private String value;
}
