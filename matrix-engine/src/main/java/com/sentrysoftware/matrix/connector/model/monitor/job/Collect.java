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
public class Collect extends MonitorJob {

	private static final long serialVersionUID = 2147452391194801554L;

	private CollectType type;
	private Source valueTable;

	@Builder
	public Collect(List<Source> sources, Map<String, String> parameters, CollectType type, Source valueTable) {

		super(sources, parameters);
		this.type = type;
		this.valueTable = valueTable;
	}

}
