package com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source;

import static com.fasterxml.jackson.annotation.Nulls.FAIL;
import static com.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.NEW_LINE;
import static com.sentrysoftware.metricshub.engine.common.helpers.StringHelper.addNonNull;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sentrysoftware.metricshub.engine.connector.deserializer.custom.NonBlankDeserializer;
import com.sentrysoftware.metricshub.engine.connector.deserializer.custom.PositiveIntegerDeserializer;
import com.sentrysoftware.metricshub.engine.connector.model.common.ExecuteForEachEntryOf;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.Compute;
import com.sentrysoftware.metricshub.engine.strategy.source.ISourceProcessor;
import com.sentrysoftware.metricshub.engine.strategy.source.SourceTable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.UnaryOperator;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class TableJoinSource extends Source {

	private static final long serialVersionUID = 1L;

	@JsonSetter(nulls = FAIL)
	@JsonDeserialize(using = NonBlankDeserializer.class)
	private String leftTable;

	@JsonSetter(nulls = FAIL)
	@JsonDeserialize(using = NonBlankDeserializer.class)
	private String rightTable;

	@JsonSetter(nulls = FAIL)
	@JsonDeserialize(using = PositiveIntegerDeserializer.class)
	private Integer leftKeyColumn;

	@JsonSetter(nulls = FAIL)
	@JsonDeserialize(using = PositiveIntegerDeserializer.class)
	private Integer rightKeyColumn;

	private String defaultRightLine;
	private String keyType;

	@Builder
	@JsonCreator
	public TableJoinSource(
		@JsonProperty("type") String type,
		@JsonProperty("computes") List<Compute> computes,
		@JsonProperty("forceSerialization") boolean forceSerialization,
		@JsonProperty(value = "leftTable", required = true) String leftTable,
		@JsonProperty(value = "rightTable", required = true) String rightTable,
		@JsonProperty(value = "leftKeyColumn", required = true) Integer leftKeyColumn,
		@JsonProperty(value = "rightKeyColumn", required = true) Integer rightKeyColumn,
		@JsonProperty("defaultRightLine") String defaultRightLine,
		@JsonProperty("keyType") String keyType,
		@JsonProperty("key") String key,
		@JsonProperty("executeForEachEntryOf") ExecuteForEachEntryOf executeForEachEntryOf
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
		return TableJoinSource
			.builder()
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
		defaultRightLine = updater.apply(defaultRightLine);
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

	@Override
	public SourceTable accept(final ISourceProcessor sourceProcessor) {
		return sourceProcessor.process(this);
	}
}
