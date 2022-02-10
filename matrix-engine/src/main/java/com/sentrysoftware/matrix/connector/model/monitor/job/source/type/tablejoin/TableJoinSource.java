package com.sentrysoftware.matrix.connector.model.monitor.job.source.type.tablejoin;

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
public class TableJoinSource extends Source {

	private static final long serialVersionUID = -7311795243465084164L;

	private String leftTable;
	private String rightTable;
	private Integer leftKeyColumn;
	private Integer rightKeyColumn;
	private List<String> defaultRightLine;
	private String keyType;

	@Builder
	public TableJoinSource(List<Compute> computes, boolean forceSerialization, String leftTable,
			String rightTable, Integer leftKeyColumn, Integer rightKeyColumn, List<String> defaultRightLine,
			String keyType, int index, String key,
			ExecuteForEachEntry executeForEachEntry) {

		super(computes, forceSerialization, index, key, executeForEachEntry);
		this.leftTable = leftTable;
		this.rightTable = rightTable;
		this.leftKeyColumn = leftKeyColumn;
		this.rightKeyColumn = rightKeyColumn;
		this.defaultRightLine = defaultRightLine;
		this.keyType = keyType;
	}

	@Override
	public SourceTable accept(final ISourceVisitor sourceVisitor) {
		return sourceVisitor.visit(this);
	}

	@Override
	public TableJoinSource copy() {
		return TableJoinSource.builder()
				.index(index)
				.key(key)
				.forceSerialization(forceSerialization)
				.computes(getComputes() != null ? new ArrayList<>(getComputes()) : null)
				.executeForEachEntry(executeForEachEntry != null ? executeForEachEntry.copy() : null)
				.leftTable(leftTable)
				.rightTable(rightTable)
				.leftKeyColumn(leftKeyColumn)
				.rightKeyColumn(rightKeyColumn)
				.defaultRightLine(defaultRightLine != null ? new ArrayList<>(defaultRightLine) : null)
				.keyType(keyType)
				.build();
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		leftTable = updater.apply(leftTable);
		rightTable = updater.apply(rightTable);
		keyType = updater.apply(keyType);
	}

	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(HardwareConstants.NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- leftTable=", leftTable);
		addNonNull(stringJoiner, "- rightTable=", rightTable);
		addNonNull(stringJoiner, "- leftKeyColumn=", leftKeyColumn);
		addNonNull(stringJoiner, "- rightKeyColumn=", rightKeyColumn);
		addNonNull(stringJoiner, "- defaultRightLine=", defaultRightLine);
		addNonNull(stringJoiner, "- keyType=", keyType);

		return stringJoiner.toString();
	}

}
