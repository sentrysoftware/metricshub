package com.sentrysoftware.matrix.connector.model.monitor.job.source.type.reference;

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
public class StaticSource  extends Source {

	private static final long serialVersionUID = 3446426549562321969L;

	private String staticValue;

	@Builder
	public StaticSource(List<Compute> computes, boolean forceSerialization, String staticValue, int index, String key) {
		super(computes, forceSerialization, index, key);
		this.staticValue = staticValue;
	}

	@Override
	public SourceTable accept(final ISourceVisitor sourceVisitor) {
		return sourceVisitor.visit(this);
	}
}
