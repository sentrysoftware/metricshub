package com.sentrysoftware.matrix.connector.model.monitor.task;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Mapping implements Serializable {

	private static final long serialVersionUID = 1L;

	private String source;

	@Default
	private Map<String, String> attributes = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

	@Default
	private Map<String, String> metrics = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

	@Default
	private Map<String, String> conditionalCollection = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

}
