package com.sentrysoftware.matrix.connector.model.monitor.task.source;

import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.NEW_LINE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.WHITE_SPACE_TAB;
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
public class OsCommandSource extends Source {

	private static final long serialVersionUID = 1L;

	private String commandLine;
	private Long timeout;
	private boolean executeLocally;
	private String exclude;
	private String keep;
	private Integer removeHeader;
	private Integer removeFooter;
	private String separators = WHITE_SPACE_TAB;
	private List<String> selectColumns = new ArrayList<>();

	@Builder
	public OsCommandSource( // NOSONAR on constructor
		String type, 
		List<Compute> computes,
		boolean forceSerialization,
		String commandLine,
		Long timeout,
		boolean executeLocally,
		String exclude,
		String keep,
		Integer removeHeader, Integer removeFooter,
		String separators,
		List<String> selectColumns,
		String key,
		ExecuteForEachEntryOf executeForEachEntryOf
	) {

		super(type, computes, forceSerialization, key, executeForEachEntryOf);

		this.commandLine = commandLine;
		this.timeout = timeout;
		this.executeLocally = executeLocally;
		this.exclude = exclude;
		this.keep = keep;
		this.removeHeader = removeHeader;
		this.removeFooter = removeFooter;
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
				.removeFooter(removeFooter)
				.removeHeader(removeHeader)
				.selectColumns(selectColumns != null ? new ArrayList<>(selectColumns) : null)
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
		addNonNull(stringJoiner, "- removeHeader=", removeHeader);
		addNonNull(stringJoiner, "- removeFooter=", removeFooter);
		addNonNull(stringJoiner, "- separators=", separators);
		addNonNull(stringJoiner, "- selectColumns=", selectColumns);

		return stringJoiner.toString();

	}

}
