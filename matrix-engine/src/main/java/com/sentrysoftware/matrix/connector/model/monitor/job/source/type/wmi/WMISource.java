package com.sentrysoftware.matrix.connector.model.monitor.job.source.type.wmi;

import java.util.List;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Compute;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class WMISource extends Source {

	private static final long serialVersionUID = 218584585059836958L;

	private String wbemQuery;
	private String wbemNameSpace;

	@Builder
	public WMISource(List<Compute> computes, boolean forceSerialization, String wbemQuery,
			String wbemNameSpace, int index, String key) {

		super(computes, forceSerialization, index, key);
		this.wbemQuery = wbemQuery;
		this.wbemNameSpace = wbemNameSpace;
	}

}
