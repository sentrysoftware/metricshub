package com.sentrysoftware.matrix.connector.model.monitor.task.source;

import static com.fasterxml.jackson.annotation.Nulls.FAIL;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.NEW_LINE;
import static com.sentrysoftware.matrix.common.helpers.StringHelper.addNonNull;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.function.UnaryOperator;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sentrysoftware.matrix.connector.deserializer.custom.NonBlankInLinkedHashSetDeserializer;
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
public class UcsSource extends Source {

	private static final long serialVersionUID = 1L;

	@JsonSetter(nulls = FAIL)
	@JsonDeserialize(using = NonBlankInLinkedHashSetDeserializer.class)
	@NonNull
	private Set<String> queries = new LinkedHashSet<>();
	private String exclude;
	private String keep;
	private String selectColumns;

	@Builder
	public UcsSource( // NOSONAR on constructor
		@JsonProperty("type") String type,
		@JsonProperty("computes") List<Compute> computes,
		@JsonProperty("forceSerialization") boolean forceSerialization,
		@JsonProperty(value = "queries", required = true) @NonNull Set<String> queries,
		@JsonProperty("exclude") String exclude,
		@JsonProperty("keep") String keep,
		@JsonProperty("selectColumns") String selectColumns,
		@JsonProperty("key") String key,
		@JsonProperty("executeForEachEntryOf") ExecuteForEachEntryOf executeForEachEntryOf) {
		super(type, computes, forceSerialization, key, executeForEachEntryOf);
		this.queries = queries;
		this.exclude = exclude;
		this.keep = keep;
		this.selectColumns = selectColumns;
	}

	@Override
	public UcsSource copy() {
		return UcsSource.builder()
			.type(type)
			.key(key)
			.forceSerialization(forceSerialization)
			.computes(getComputes() != null ? new ArrayList<>(getComputes()) : null)
			.executeForEachEntryOf(executeForEachEntryOf != null ? executeForEachEntryOf.copy() : null)
			.queries(queries != null ? new LinkedHashSet<>(queries) : null)
			.exclude(exclude)
			.keep(keep)
			.selectColumns(selectColumns)
			.build();
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		exclude = updater.apply(exclude);
		keep = updater.apply(keep);
		selectColumns = updater.apply(selectColumns);
	}

	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- queries=", queries);
		addNonNull(stringJoiner, "- exclude=", exclude);
		addNonNull(stringJoiner, "- keep=", keep);
		addNonNull(stringJoiner, "- selectColumns=", selectColumns);

		return stringJoiner.toString();
	}

}
