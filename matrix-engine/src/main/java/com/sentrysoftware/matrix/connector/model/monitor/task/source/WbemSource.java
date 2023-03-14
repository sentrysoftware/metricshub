package com.sentrysoftware.matrix.connector.model.monitor.task.source;

import static com.fasterxml.jackson.annotation.Nulls.FAIL;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.NEW_LINE;
import static com.sentrysoftware.matrix.common.helpers.StringHelper.addNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.UnaryOperator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sentrysoftware.matrix.connector.deserializer.custom.NonBlankDeserializer;
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
public class WbemSource extends Source {

	private static final long serialVersionUID = 1L;

	@NonNull
	@JsonDeserialize(using = NonBlankDeserializer.class)
	@JsonSetter(nulls = FAIL)
	private String query;

	@JsonDeserialize(using = NonBlankDeserializer.class)
	private String namespace;

	@Builder
	@JsonCreator
	public WbemSource(
			@JsonProperty("type") String type, 
			@JsonProperty("computes") List<Compute> computes,
			@JsonProperty("forceSerialization") boolean forceSerialization,
			@JsonProperty(value = "query", required = true) @NonNull String query,
			@JsonProperty("namespace") String namespace,
			@JsonProperty("key") String key,
			@JsonProperty("executeForEachEntryOf") ExecuteForEachEntryOf executeForEachEntryOf
	) {

		super(type, computes, forceSerialization, key, executeForEachEntryOf);
		this.query = query;
		this.namespace = namespace;
	}

	@Override
	public WbemSource copy() {
		return WbemSource.builder()
				.type(type)
				.key(key)
				.forceSerialization(forceSerialization)
				.computes(getComputes() != null ? new ArrayList<>(getComputes()) : null)
				.executeForEachEntryOf(executeForEachEntryOf != null ? executeForEachEntryOf.copy() : null)
				.query(query)
				.namespace(namespace)
				.build();
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		query = updater.apply(query);
		namespace = updater.apply(namespace);
	}

	@Override
	public String toString() {

		final StringJoiner stringJoiner = new StringJoiner(NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- query=", query);
		addNonNull(stringJoiner, "- namespace=", namespace);

		return stringJoiner.toString();
	}

}
