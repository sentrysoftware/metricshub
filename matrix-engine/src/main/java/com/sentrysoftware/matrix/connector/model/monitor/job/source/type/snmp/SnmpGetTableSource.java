package com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp;

import static com.sentrysoftware.matrix.common.helpers.StringHelper.addNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.connector.model.common.ExecuteForEachEntry;
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
public class SnmpGetTableSource extends SnmpSource {

	private static final long serialVersionUID = -8516718446207060520L;

	private List<String> snmpTableSelectColumns = new ArrayList<>();

	@Builder
	public SnmpGetTableSource(List<Compute> computes, boolean forceSerialization, String oid,
			List<String> snmpTableSelectColumns, int index, String key,
			ExecuteForEachEntry executeForEachEntry) {

		super(computes, forceSerialization, oid, index, key, executeForEachEntry);
		this.snmpTableSelectColumns = snmpTableSelectColumns;
	}

	@Override
	public SourceTable accept(final ISourceVisitor sourceVisitor) {
		return sourceVisitor.visit(this);
	}

	/**
	 * Copy the current instance
	 * 
	 * @return new {@link SnmpGetTableSource} instance
	 */
	public SnmpGetTableSource copy() {
		return SnmpGetTableSource.builder()
				.index(index)
				.key(key)
				.forceSerialization(forceSerialization)
				.computes(getComputes() != null ? new ArrayList<>(getComputes()) : null)
				.executeForEachEntry(executeForEachEntry != null ? executeForEachEntry.copy() : null)
				.oid(oid)
				.snmpTableSelectColumns(
						snmpTableSelectColumns != null ? new ArrayList<>(snmpTableSelectColumns) : null)
				.build();
	}

	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(HardwareConstants.NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- snmpTableSelectColumns=", snmpTableSelectColumns);

		return stringJoiner.toString();
	}

}
