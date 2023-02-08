package com.sentrysoftware.matrix.connector.model.monitor.task.source;

import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.NEW_LINE;
import static com.sentrysoftware.matrix.common.helpers.StringHelper.addNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import com.sentrysoftware.matrix.connector.model.common.ExecuteForEachEntryOf;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Compute;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SnmpTableSource extends SnmpSource {

	private static final long serialVersionUID = 1L;

	private List<String> selectColumns = new ArrayList<>();

	@Builder
	public SnmpTableSource(
		String type, 
		List<Compute> computes,
		boolean forceSerialization,
		String oid,
		List<String> selectColumns,
		String key,
		ExecuteForEachEntryOf executeForEachEntryOf
	) {

		super(type, computes, forceSerialization, oid, key, executeForEachEntryOf);
		this.selectColumns = selectColumns;
	}


	/**
	 * Copy the current instance
	 * 
	 * @return new {@link SnmpTableSource} instance
	 */
	public SnmpTableSource copy() {
		return SnmpTableSource.builder()
				.type(type)
				.key(key)
				.forceSerialization(forceSerialization)
				.computes(getComputes() != null ? new ArrayList<>(getComputes()) : null)
				.executeForEachEntryOf(executeForEachEntryOf != null ? executeForEachEntryOf.copy() : null)
				.oid(oid)
				.selectColumns(
						selectColumns != null ? new ArrayList<>(selectColumns) : null)
				.build();
	}

	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- selectColumns=", selectColumns);

		return stringJoiner.toString();
	}

}
