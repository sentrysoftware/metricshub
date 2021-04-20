package com.sentrysoftware.matrix.connector.model.monitor.job;

import java.util.List;
import java.util.Map;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Discovery extends MonitorJob {

	private static final long serialVersionUID = -6681722012874951001L;

	private Source instanceTable;

	@Builder
	public Discovery(List<Source> sources, Source instanceTable, Map<String, String> parameters) {

		super(sources, parameters);
		this.instanceTable = instanceTable;
	}

}
