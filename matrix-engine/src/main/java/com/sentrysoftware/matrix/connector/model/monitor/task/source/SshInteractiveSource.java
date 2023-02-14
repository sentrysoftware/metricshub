package com.sentrysoftware.matrix.connector.model.monitor.task.source;

import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.NEW_LINE;
import static com.sentrysoftware.matrix.common.helpers.StringHelper.addNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.UnaryOperator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sentrysoftware.matrix.connector.model.common.ExecuteForEachEntryOf;
import com.sentrysoftware.matrix.connector.model.common.sshstep.Step;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Compute;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Singular;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SshInteractiveSource extends Source {

	private static final long serialVersionUID = 1L;

	private Integer port;
	private String exclude;
	private String keep;
	private Integer beginAtLineNumber;
	private Integer endAtLineNumber;
	private String separators;
	private String selectColumns;
	private List<Step> steps = new ArrayList<>();

	@Builder
	@JsonCreator
	public SshInteractiveSource( // NOSONAR on constructor
		@JsonProperty("type") String type, 
		@JsonProperty("computes") List<Compute> computes,
		@JsonProperty("forceSerialization") boolean forceSerialization,
		@JsonProperty("port") Integer port,
		@JsonProperty("exclude") String exclude,
		@JsonProperty("keep") String keep,
		@JsonProperty("beginAtLineNumber") Integer beginAtLineNumber,
		@JsonProperty("endAtLineNumber") Integer endAtLineNumber,
		@JsonProperty("separators") String separators,
		@JsonProperty("SelectColumns") String selectColumns,
		@JsonProperty(value = "steps", required = true) @Singular @NonNull List<Step> steps,
		@JsonProperty("key") String key,
		@JsonProperty("executeForEachEntryOf") ExecuteForEachEntryOf executeForEachEntryOf) {

		super(type, computes, forceSerialization, key, executeForEachEntryOf);
		this.port = port;
		this.exclude = exclude;
		this.keep = keep;
		this.beginAtLineNumber = beginAtLineNumber;
		this.endAtLineNumber = endAtLineNumber;
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
				.beginAtLineNumber(beginAtLineNumber)
				.endAtLineNumber(endAtLineNumber)
				.separators(separators)
				.selectColumns(selectColumns)
				.steps(steps != null ? steps.stream().map(Step::copy).toList() : null)
 				.build();
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		exclude = updater.apply(exclude);
		keep = updater.apply(keep);
		separators = updater.apply(separators);
		selectColumns = updater.apply(selectColumns);
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
		addNonNull(stringJoiner, "- beginAtLineNumber=", beginAtLineNumber);
		addNonNull(stringJoiner, "- endAtLineNumber=", endAtLineNumber);
		addNonNull(stringJoiner, "- separators=", separators);
		addNonNull(stringJoiner, "- selectColumns=", selectColumns);

		return stringJoiner.toString();

	}

}
