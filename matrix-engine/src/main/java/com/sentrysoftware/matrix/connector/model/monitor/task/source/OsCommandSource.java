package com.sentrysoftware.matrix.connector.model.monitor.task.source;

import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.NEW_LINE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.WHITE_SPACE_TAB;
import static com.sentrysoftware.matrix.common.helpers.StringHelper.addNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.UnaryOperator;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sentrysoftware.matrix.connector.deserializer.custom.NonBlankDeserializer;
import com.sentrysoftware.matrix.connector.deserializer.custom.TimeoutDeserializer;
import com.sentrysoftware.matrix.connector.model.common.ExecuteForEachEntryOf;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Compute;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OsCommandSource extends Source {

	private static final long serialVersionUID = 1L;

	@NonNull
	@JsonDeserialize(using = NonBlankDeserializer.class)
	private String commandLine;

	@JsonDeserialize(using = TimeoutDeserializer.class)
	private Long timeout;

	private boolean executeLocally;

	@JsonDeserialize(using = NonBlankDeserializer.class)
	private String exclude;

	@JsonDeserialize(using = NonBlankDeserializer.class)
	private String keep;

	private int beginAtLineNumber;
	private int endAtLineNumber;
	private String separators = WHITE_SPACE_TAB;
	private String selectColumns;

	@Builder
	public OsCommandSource( // NOSONAR on constructor
			@JsonProperty("type") String type,
			@JsonProperty("computes") List<Compute> computes,
			@JsonProperty("forceSerialization") boolean forceSerialization,
			@JsonProperty(value = "commandLine", required = true) @NonNull String commandLine,
			@JsonProperty("timeout") Long timeout,
			@JsonProperty("executeLocally") boolean executeLocally,
			@JsonProperty("exclude") String exclude,
			@JsonProperty("keep") String keep,
			@JsonProperty("beginAtLineNumber") int beginAtLineNumber,
			@JsonProperty("endAtLineNumber") int endAtLineNumber,
			@JsonProperty("separators") String separators,
			@JsonProperty("selectColumns") String selectColumns,
			@JsonProperty("key") String key,
			@JsonProperty("executeForEachEntryOf") ExecuteForEachEntryOf executeForEachEntryOf) {

		super(type, computes, forceSerialization, key, executeForEachEntryOf);

		this.commandLine = commandLine;
		this.timeout = timeout;
		this.executeLocally = executeLocally;
		this.exclude = exclude;
		this.keep = keep;
		this.beginAtLineNumber = beginAtLineNumber;
		this.endAtLineNumber = endAtLineNumber;
		this.separators = separators == null ? WHITE_SPACE_TAB : separators;
		this.selectColumns = selectColumns;
	}

	/**
	 * Copy the current instance
	 * 
	 * @return new {@link OsCommandSource} instance
	 */
	public OsCommandSource copy() {
		return OsCommandSource.builder()
				.type(type)
				.key(key)
				.forceSerialization(forceSerialization)
				.computes(getComputes() != null ? new ArrayList<>(getComputes()) : null)
				.executeForEachEntryOf(executeForEachEntryOf != null ? executeForEachEntryOf.copy() : null)
				.commandLine(commandLine)
				.executeLocally(executeLocally)
				.exclude(exclude)
				.keep(keep)
				.beginAtLineNumber(beginAtLineNumber)
				.endAtLineNumber(endAtLineNumber)
				.selectColumns(selectColumns)
				.separators(separators)
				.timeout(timeout)
				.build();
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		commandLine = updater.apply(commandLine);
		exclude = updater.apply(exclude);
		keep = updater.apply(keep);
		separators = updater.apply(separators);
		selectColumns = updater.apply(selectColumns);
	}

	@Override
	public String toString() {

		final StringJoiner stringJoiner = new StringJoiner(NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- commandLine=", commandLine);
		addNonNull(stringJoiner, "- timeout=", timeout);
		addNonNull(stringJoiner, "- executeLocally=", executeLocally);
		addNonNull(stringJoiner, "- exclude=", exclude);
		addNonNull(stringJoiner, "- keep=", keep);
		addNonNull(stringJoiner, "- beginAtLineNumber=", beginAtLineNumber);
		addNonNull(stringJoiner, "- endAtLineNumber=", endAtLineNumber);
		addNonNull(stringJoiner, "- separators=", separators);
		addNonNull(stringJoiner, "- selectColumns=", selectColumns);

		return stringJoiner.toString();

	}

}
