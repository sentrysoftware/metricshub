package com.sentrysoftware.matrix.connector.model.monitor.job.source.compute;

import static com.sentrysoftware.matrix.common.helpers.StringHelper.addNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.connector.model.common.EmbeddedFile;
import com.sentrysoftware.matrix.engine.strategy.source.compute.IComputeVisitor;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Awk extends Compute {

	private static final long serialVersionUID = -60202574277309621L;

	private EmbeddedFile awkScript;
	private String excludeRegExp;
	private String keepOnlyRegExp;
	private String separators;
	private List<String> selectColumns = new ArrayList<>();

	@Builder
	public Awk(Integer index, EmbeddedFile awkScript, String excludeRegExp, String keepOnlyRegExp, String separators,
			List<String> selectColumns) {
		super(index);
		this.awkScript = awkScript;
		this.excludeRegExp = excludeRegExp;
		this.keepOnlyRegExp = keepOnlyRegExp;
		this.separators = separators;
		this.selectColumns = selectColumns == null ? new ArrayList<>() : selectColumns;
	}

	@Override
	public void accept(final IComputeVisitor computeVisitor) {
		computeVisitor.visit(this);
	}

	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(HardwareConstants.NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- awkScript=", awkScript != null ? awkScript.description() : null);
		addNonNull(stringJoiner, "- excludeRegExp=", excludeRegExp);
		addNonNull(stringJoiner, "- keepOnlyRegExp=", keepOnlyRegExp);
		addNonNull(stringJoiner, "- separators=", separators);
		addNonNull(stringJoiner, "- selectColumns=", selectColumns);

		return stringJoiner.toString();
	}

	@Override
	public Awk copy() {
		return Awk
			.builder()
			.index(index)
			.awkScript(awkScript != null ? awkScript.copy() : null)
			.excludeRegExp(excludeRegExp)
			.keepOnlyRegExp(keepOnlyRegExp)
			.separators(separators)
			.selectColumns(selectColumns != null ? new ArrayList<>(selectColumns) : null)
			.build();
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		if (awkScript != null) {
			awkScript.update(updater);
		}
		excludeRegExp = updater.apply(excludeRegExp);
		keepOnlyRegExp = updater.apply(keepOnlyRegExp);
		separators = updater.apply(separators);
		if (selectColumns != null && !selectColumns.isEmpty()) {
			selectColumns = selectColumns
				.stream()
				.map(updater::apply)
				.collect(Collectors.toCollection(ArrayList::new));
		}
	}
}
