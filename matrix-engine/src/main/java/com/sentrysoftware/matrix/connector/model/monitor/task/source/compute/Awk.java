package com.sentrysoftware.matrix.connector.model.monitor.task.source.compute;

import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.NEW_LINE;

import static com.sentrysoftware.matrix.common.helpers.StringHelper.addNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Awk extends Compute {

	private static final long serialVersionUID = 1L;

	private String script;
	private String exclude;
	private String keep;
	private String separators;
	private List<String> selectColumns = new ArrayList<>();

	@Builder
	public Awk(String type, String script, String exclude, String keep, String separators,
			List<String> selectColumns) {
		super(type);
		this.script = script;
		this.exclude = exclude;
		this.keep = keep;
		this.separators = separators;
		this.selectColumns = selectColumns == null ? new ArrayList<>() : selectColumns;
	}

	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- script=", script);
		addNonNull(stringJoiner, "- exclude=", exclude);
		addNonNull(stringJoiner, "- keep=", keep);
		addNonNull(stringJoiner, "- separators=", separators);
		addNonNull(stringJoiner, "- selectColumns=", selectColumns);

		return stringJoiner.toString();
	}

	@Override
	public Awk copy() {
		return Awk
			.builder()
			.type(type)
			.script(script)
			.exclude(exclude)
			.keep(keep)
			.separators(separators)
			.selectColumns(selectColumns != null ? new ArrayList<>(selectColumns) : null)
			.build();
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		script = updater.apply(script);
		exclude = updater.apply(exclude);
		keep = updater.apply(keep);
		separators = updater.apply(separators);
		if (selectColumns != null && !selectColumns.isEmpty()) {
			selectColumns = selectColumns
				.stream()
				.map(updater::apply)
				.collect(Collectors.toCollection(ArrayList::new));
		}
	}
}
