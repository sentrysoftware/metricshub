package com.sentrysoftware.matrix.connector.model.monitor.task.source;

import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.NEW_LINE;
import static com.sentrysoftware.matrix.common.helpers.StringHelper.addNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.UnaryOperator;

import com.sentrysoftware.matrix.connector.model.common.ExecuteForEachEntryOf;
import com.sentrysoftware.matrix.connector.model.common.sshstep.Step;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Compute;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SshInteractiveSource extends Source {

	private static final long serialVersionUID = 1L;

	private Integer port;
	private String exclude;
	private String keep;
	private Integer removeHeader;
	private Integer removeFooter;
	private String separators;
	private List<String> selectColumns = new ArrayList<>();
	private List<Step> steps = new ArrayList<>();

	@Builder
	public SshInteractiveSource( // NOSONAR on constructor
		String type,
		List<Compute> computes,
		boolean forceSerialization,
		Integer port,
		String exclude,
		String keep,
		Integer removeHeader,
		Integer removeFooter,
		String separators,
		List<String> selectColumns,
		List<Step> steps,
		String key,
		ExecuteForEachEntryOf executeForEachEntryOf) {

		super(type, computes, forceSerialization, key, executeForEachEntryOf);
		this.port = port;
		this.exclude = exclude;
		this.keep = keep;
		this.removeHeader = removeHeader;
		this.removeFooter = removeFooter;
		this.separators = separators;
		this.selectColumns = selectColumns;
		this.steps = steps;
	}


	public SshInteractiveSource copy() {
		return SshInteractiveSource.builder()
				.type(type)
				.key(key)
				.forceSerialization(forceSerialization)
				.computes(getComputes() != null ? new ArrayList<>(getComputes()) : null)
				.executeForEachEntryOf(executeForEachEntryOf != null ? executeForEachEntryOf.copy() : null)
				.port(port)
				.exclude(exclude)
				.keep(keep)
				.removeHeader(removeHeader)
				.removeFooter(removeFooter)
				.separators(separators)
				.selectColumns(selectColumns != null ? new ArrayList<>(selectColumns) : null)
				.steps(steps != null ? steps.stream().map(Step::copy).toList() : null)
 				.build();
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		exclude = updater.apply(exclude);
		keep = updater.apply(keep);
		separators = updater.apply(separators);
		if (steps != null) {
			steps.forEach(step -> step.update(updater));
		}
	}

	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- port=", port);
		addNonNull(stringJoiner, "- exclude=", exclude);
		addNonNull(stringJoiner, "- keep=", keep);
		addNonNull(stringJoiner, "- removeHeader=", removeHeader);
		addNonNull(stringJoiner, "- removeFooter=", removeFooter);
		addNonNull(stringJoiner, "- separators=", separators);
		addNonNull(stringJoiner, "- selectColumns=", selectColumns);

		return stringJoiner.toString();

	}

}
