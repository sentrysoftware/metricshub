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
public class TableUnionSource extends Source {

	private static final long serialVersionUID = 1L;

	private List<String> tables = new ArrayList<>();

	@Builder
	public TableUnionSource(
		String type,
		List<Compute> computes,
		boolean forceSerialization,
		List<String> tables,
		String key,
		ExecuteForEachEntry executeForEachEntry) {

		super(type, computes, forceSerialization, key, executeForEachEntry);
		this.tables = tables;
	}

	@Override
	public TableUnionSource copy() {
		return TableUnionSource.builder()
			.type(type)
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
		final StringJoiner stringJoiner = new StringJoiner(NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- tables=", tables);

		return stringJoiner.toString();
	}

}
