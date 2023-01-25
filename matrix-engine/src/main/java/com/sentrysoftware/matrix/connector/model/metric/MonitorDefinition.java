package com.sentrysoftware.matrix.connector.model.metric;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MonitorDefinition implements Serializable {

	private static final long serialVersionUID = 1L;

	@Default
	private Set<String> attributes = new HashSet<>();

	@Default
	private Map<String, MetricDefinition> metrics = new HashMap<>();
}
