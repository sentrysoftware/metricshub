package com.sentrysoftware.matrix.connector.model.monitor.job.source.type.tableunion;

import static com.sentrysoftware.matrix.common.helpers.StringHelper.addNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.UnaryOperator;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.connector.model.common.ExecuteForEachEntry;
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
public class TableUnionSource extends Source {

	private static final long serialVersionUID = -348604258888047116L;

	private List<String> tables = new ArrayList<>();

	@Builder
	public TableUnionSource(List<Compute> computes, boolean forceSerialization,
			List<String> tables, int index, String key,
			ExecuteForEachEntry executeForEachEntry) {

		super(computes, forceSerialization, index, key, executeForEachEntry);
		this.tables = tables;
	}

	@Override
	public SourceTable accept(final ISourceVisitor sourceVisitor) {
		return sourceVisitor.visit(this);
	}

	@Override
	public TableUnionSource copy() {
		return TableUnionSource.builder()
				.index(index)
				.key(key)
				.forceSerialization(forceSerialization)
				.computes(getComputes() != null ? new ArrayList<>(getComputes()) : null)
				.executeForEachEntry(executeForEachEntry != null ? executeForEachEntry.copy() : null)
				.tables(tables != null ? new ArrayList<>(tables) : null)
				.build();
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		// For now, there is nothing to update
	}

	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(HardwareConstants.NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- tables=", tables);

		return stringJoiner.toString();
	}

}
