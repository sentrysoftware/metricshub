package com.sentrysoftware.matrix.connector.model.monitor.job.source.type.wbem;

import java.util.List;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Compute;
import com.sentrysoftware.matrix.engine.strategy.source.ISourceVisitor;
import com.sentrysoftware.matrix.engine.strategy.source.SourceTable;

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

	public static final String PROTOCOL = "WBEM";

	private String wbemQuery;
	private String wbemNamespace;

	@Builder
	public WBEMSource(List<Compute> computes, boolean forceSerialization, String wbemQuery,
			String wbemNamespace, int index, String key) {

		super(computes, forceSerialization, index, key);
		this.wbemQuery = wbemQuery;
		this.wbemNamespace = wbemNamespace;
	}

	@Override
	public SourceTable accept(final ISourceVisitor sourceVisitor) {
		return sourceVisitor.visit(this);
	}

	@Override
	public String getProtocol() {
		return PROTOCOL;
	}

}
