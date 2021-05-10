package com.sentrysoftware.matrix.connector.model.monitor.job;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public abstract class MonitorJob implements Serializable {

	private static final long serialVersionUID = 2236765379409185806L;

	private List<Source> sources;

	private Map<String, String> parameters;

	protected MonitorJob(List<Source> sources, Map<String, String> parameters)  {
		this.sources = sources == null ? new ArrayList<>() : sources;
		this.parameters = parameters == null ?  new TreeMap<>(String.CASE_INSENSITIVE_ORDER) : parameters;
	}
}
