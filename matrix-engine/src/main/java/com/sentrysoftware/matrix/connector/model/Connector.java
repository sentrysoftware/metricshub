package com.sentrysoftware.matrix.connector.model;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.sentrysoftware.matrix.connector.model.common.TranslationTable;
import com.sentrysoftware.matrix.connector.model.identity.ConnectorIdentity;
import com.sentrysoftware.matrix.connector.model.metric.MonitorDefinition;
import com.sentrysoftware.matrix.connector.model.monitor.MonitorJob;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.Source;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Connector implements Serializable {

	private static final long serialVersionUID = 1L;

	@JsonProperty("connector")
	private ConnectorIdentity connectorIdentity;

	@JsonProperty("extends")
	@Default
	private Set<String> extendsConnectors = new HashSet<>();

	@Default
	private Map<String, MonitorDefinition> metricsMapping = new HashMap<>();

	@Default
	private Map<String, String> constants = new HashMap<>();

	@Default
	private Set<String> sudoCommands = new HashSet<>();

	@Default 
	private Map<String, Source> pre = new HashMap<>();

	@Default
	private Map<String, MonitorJob> monitors = new LinkedHashMap<>();

	@Default
	private Map<String, String> embedded = new HashMap<>();

	@Default
	private Map<String, TranslationTable> translations = new HashMap<>();

	@Default
	private Set<Class <? extends Source>> sourceTypes = new HashSet<>();

}
