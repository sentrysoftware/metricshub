package com.sentrysoftware.matrix.connector.model.monitor.job.source.type.ipmi;

import java.util.List;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Compute;
import com.sentrysoftware.matrix.engine.strategy.source.ISourceVisitor;
import com.sentrysoftware.matrix.engine.strategy.source.SourceTable;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class IPMI extends Source {

	private static final long serialVersionUID = 2314585274202787684L;

	@Builder
	public IPMI(List<Compute> computes, boolean forceSerialization, int index, String key) {

		super(computes, forceSerialization, index, key);
	}

	@Override
	public SourceTable accept(final ISourceVisitor sourceVisitor) {
		return sourceVisitor.visit(this);
	}

	@Override
	public String toString() {
		return super.toString();
	}

}
