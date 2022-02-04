package com.sentrysoftware.matrix.connector.model.monitor.job.source.type.sshinteractive;

import static com.sentrysoftware.matrix.common.helpers.StringHelper.addNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.connector.model.common.ExecuteForEachEntry;
import com.sentrysoftware.matrix.connector.model.common.sshinteractive.step.Step;
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
public class SshInteractiveSource extends Source {

	private static final long serialVersionUID = 7662516386312299806L;

	private Integer port;
	private String excludeRegExp;
	private String keepOnlyRegExp;
	private Integer removeHeader;
	private Integer removeFooter;
	private String separators;
	private List<String> selectColumns = new ArrayList<>();
	private List<Step> steps = new ArrayList<>();

	@Builder
	public SshInteractiveSource(List<Compute> computes, boolean forceSerialization, Integer port,
			String excludeRegExp, String keepOnlyRegExp, Integer removeHeader, Integer removeFooter,
			String separators, List<String> selectColumns, List<Step> steps, int index, String key,
			ExecuteForEachEntry executeForEachEntry) {

		super(computes, forceSerialization, index, key, executeForEachEntry);
		this.port = port;
		this.excludeRegExp = excludeRegExp;
		this.keepOnlyRegExp = keepOnlyRegExp;
		this.removeHeader = removeHeader;
		this.removeFooter = removeFooter;
		this.separators = separators;
		this.selectColumns = selectColumns;
		this.steps = steps;
	}

	@Override
	public SourceTable accept(final ISourceVisitor sourceVisitor) {
		return sourceVisitor.visit(this);
	}

	public SshInteractiveSource copy() {
		return SshInteractiveSource.builder()
				.index(index)
				.key(key)
				.forceSerialization(forceSerialization)
				.computes(getComputes() != null ? new ArrayList<>(getComputes()) : null)
				.executeForEachEntry(executeForEachEntry != null ? executeForEachEntry.copy() : null)
				.port(port)
				.excludeRegExp(excludeRegExp)
				.keepOnlyRegExp(keepOnlyRegExp)
				.removeHeader(removeHeader)
				.removeFooter(removeFooter)
				.separators(separators)
				.selectColumns(selectColumns != null ? new ArrayList<>(selectColumns) : null)
				.steps(steps != null ? steps.stream().map(Step::copy).collect(Collectors.toList()) : null)
 				.build();
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		excludeRegExp = updater.apply(excludeRegExp);
		keepOnlyRegExp = updater.apply(keepOnlyRegExp);
		separators = updater.apply(separators);
		if (steps != null) {
			steps.forEach(step -> step.update(updater));
		}
	}

	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(HardwareConstants.NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- port=", port);
		addNonNull(stringJoiner, "- excludeRegExp=", excludeRegExp);
		addNonNull(stringJoiner, "- keepOnlyRegExp=", keepOnlyRegExp);
		addNonNull(stringJoiner, "- removeHeader=", removeHeader);
		addNonNull(stringJoiner, "- removeFooter=", removeFooter);
		addNonNull(stringJoiner, "- separators=", separators);
		addNonNull(stringJoiner, "- selectColumns=", selectColumns);

		return stringJoiner.toString();

	}

}
