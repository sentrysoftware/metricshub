package com.sentrysoftware.matrix.connector.model.monitor.task.source;

import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.NEW_LINE;
import static com.sentrysoftware.matrix.common.helpers.StringHelper.addNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.UnaryOperator;

import com.sentrysoftware.matrix.connector.model.common.ExecuteForEachEntry;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Compute;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class UcsSource extends Source {

	private static final long serialVersionUID = 1L;

	private List<String> queries = new ArrayList<>();
	private String exclude;
	private String keep;
	private List<String> selectColumns = new ArrayList<>();

	@Builder
	public UcsSource( // NOSONAR on constructor
		String type,
		List<Compute> computes,
		boolean forceSerialization,
		List<String> queries,
		String exclude,
		String keep,
		List<String> selectColumns,
		String key,
		ExecuteForEachEntry executeForEachEntry
	) {

		super(type, computes, forceSerialization, key, executeForEachEntry);
		this.queries = queries;
		this.exclude = exclude;
		this.keep = keep;
		this.selectColumns = selectColumns;
	}

	@Override
	public UcsSource copy() {
		return UcsSource.builder()
				.type(type)
				.key(key)
				.forceSerialization(forceSerialization)
				.computes(getComputes() != null ? new ArrayList<>(getComputes()) : null)
				.executeForEachEntry(executeForEachEntry != null ? executeForEachEntry.copy() : null)
				.queries(queries != null ? new ArrayList<>(queries) : null)
				.exclude(exclude)
				.keep(keep)
				.selectColumns(selectColumns != null ? new ArrayList<>(selectColumns) : null)
				.build();
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		exclude = updater.apply(exclude);
		keep = updater.apply(keep);
	}

	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- queries=", queries);
		addNonNull(stringJoiner, "- exclude=", exclude);
		addNonNull(stringJoiner, "- keep=", keep);
		addNonNull(stringJoiner, "- selectColumns=", selectColumns);

		return stringJoiner.toString();
	}

}
