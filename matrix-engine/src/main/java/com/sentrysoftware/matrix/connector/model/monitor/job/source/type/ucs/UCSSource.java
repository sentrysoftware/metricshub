package com.sentrysoftware.matrix.connector.model.monitor.job.source.type.ucs;

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
public class UCSSource extends Source {

	private static final long serialVersionUID = 2010036387689462346L;

	private List<String> queries = new ArrayList<>();
	private String excludeRegExp;
	private String keepOnlyRegExp;
	private List<String> selectColumns = new ArrayList<>();

	@Builder
	public UCSSource(List<Compute> computes, boolean forceSerialization, List<String> queries,
			String excludeRegExp, String keepOnlyRegExp,
			List<String> selectColumns, int index, String key,
			ExecuteForEachEntry executeForEachEntry) {

		super(computes, forceSerialization, index, key, executeForEachEntry);
		this.queries = queries;
		this.excludeRegExp = excludeRegExp;
		this.keepOnlyRegExp = keepOnlyRegExp;
		this.selectColumns = selectColumns;
	}

	@Override
	public UCSSource copy() {
		return UCSSource.builder()
				.index(index)
				.key(key)
				.forceSerialization(forceSerialization)
				.computes(getComputes() != null ? new ArrayList<>(getComputes()) : null)
				.executeForEachEntry(executeForEachEntry != null ? executeForEachEntry.copy() : null)
				.queries(queries != null ? new ArrayList<>(queries) : null)
				.excludeRegExp(excludeRegExp)
				.keepOnlyRegExp(keepOnlyRegExp)
				.selectColumns(selectColumns != null ? new ArrayList<>(selectColumns) : null)
				.build();
	}

	@Override
	public SourceTable accept(final ISourceVisitor sourceVisitor) {
		return sourceVisitor.visit(this);
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		excludeRegExp = updater.apply(excludeRegExp);
		keepOnlyRegExp = updater.apply(keepOnlyRegExp);
	}

	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(HardwareConstants.NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- queries=", queries);
		addNonNull(stringJoiner, "- excludeRegExp=", excludeRegExp);
		addNonNull(stringJoiner, "- keepOnlyRegExp=", keepOnlyRegExp);
		addNonNull(stringJoiner, "- selectColumns=", selectColumns);

		return stringJoiner.toString();
	}

}
