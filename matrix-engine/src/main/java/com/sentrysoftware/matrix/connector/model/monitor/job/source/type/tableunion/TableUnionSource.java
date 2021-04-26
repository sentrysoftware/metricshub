package com.sentrysoftware.matrix.connector.model.monitor.job.source.type.tableunion;

import java.util.ArrayList;
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
public class TableUnionSource extends Source {

	private static final long serialVersionUID = -348604258888047116L;

	private List<String> tables = new ArrayList<>();

	@Builder
	public TableUnionSource(List<Compute> computes, boolean forceSerialization, List<String> tables, int index, String key) {

		super(computes, forceSerialization, index, key);
		this.tables = tables;
	}

	@Override
	public SourceTable accept(ISourceVisitor sourceVisitor) {
		return sourceVisitor.visit(this);
	}

}
