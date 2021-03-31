package com.sentrysoftware.matrix.connector.model.monitor.job;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class MonitorJob implements Serializable {

	private static final long serialVersionUID = 2236765379409185806L;

	private List<Source> sources = new ArrayList<>();

	private Map<String, String> parameters = new HashMap<>();
}
