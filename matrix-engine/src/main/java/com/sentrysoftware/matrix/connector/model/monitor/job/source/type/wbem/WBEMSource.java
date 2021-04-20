package com.sentrysoftware.matrix.connector.model.monitor.job.source.type.wbem;

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
public class WBEMSource extends Source {

	private static final long serialVersionUID = -1068957824633332862L;

	private String wbemQuery;
	private String wbemNameSpace;

	@Builder
	public WBEMSource(List<Compute> computes, Source reference, boolean forceSerialization, String wbemQuery,
			String wbemNameSpace) {

		super(computes, forceSerialization);
		this.wbemQuery = wbemQuery;
		this.wbemNameSpace = wbemNameSpace;
	}

}
