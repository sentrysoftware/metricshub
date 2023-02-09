package com.sentrysoftware.matrix.connector.model.monitor.task.source;

import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.NEW_LINE;
import static com.sentrysoftware.matrix.common.helpers.StringHelper.addNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.UnaryOperator;

import com.sentrysoftware.matrix.connector.model.common.ExecuteForEachEntryOf;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Compute;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TableJoinSource extends Source {

	private static final long serialVersionUID = 1L;

	private String leftTable;
	private String rightTable;
	private Integer leftKeyColumn;
	private Integer rightKeyColumn;
	private String defaultRightLine;
	private String keyType;

	@Builder
	public TableJoinSource( // NOSONAR on constructor
		String type, 
		List<Compute> computes,
		boolean forceSerialization,
		String leftTable,
		String rightTable,
		Integer leftKeyColumn,
		Integer rightKeyColumn,
		String defaultRightLine,
		String keyType,
		String key,
		ExecuteForEachEntryOf executeForEachEntryOf
	) {

		super(type, computes, forceSerialization, key, executeForEachEntryOf);
		this.leftTable = leftTable;
		this.rightTable = rightTable;
		this.leftKeyColumn = leftKeyColumn;
		this.rightKeyColumn = rightKeyColumn;
		this.defaultRightLine = defaultRightLine;
		this.keyType = keyType;
	}

	@Override
	public TableJoinSource copy() {
		return TableJoinSource.builder()
				.type(type)
				.key(key)
				.forceSerialization(forceSerialization)
				.computes(getComputes() != null ? new ArrayList<>(getComputes()) : null)
				.executeForEachEntryOf(executeForEachEntryOf != null ? executeForEachEntryOf.copy() : null)
				.leftTable(leftTable)
				.rightTable(rightTable)
				.leftKeyColumn(leftKeyColumn)
				.rightKeyColumn(rightKeyColumn)
				.defaultRightLine(defaultRightLine)
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
		final StringJoiner stringJoiner = new StringJoiner(NEW_LINE);

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
