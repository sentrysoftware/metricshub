package com.sentrysoftware.matrix.connector.model.monitor.job.source.type.wmi;

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
public class WMISource extends Source {

	private static final long serialVersionUID = 218584585059836958L;

	private String wbemQuery;
	private String wbemNamespace;

	@Builder
	public WMISource(List<Compute> computes, boolean forceSerialization, String wbemQuery,
			String wbemNamespace, int index, String key) {

		super(computes, forceSerialization, index, key);
		this.wbemQuery = wbemQuery;
		this.wbemNamespace = wbemNamespace;
	}

	@Override
	public SourceTable accept(final ISourceVisitor sourceVisitor) {
		return sourceVisitor.visit(this);
	}

}
