package com.sentrysoftware.matrix.connector.model.common;

import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.NEW_LINE;
import static com.fasterxml.jackson.annotation.Nulls.FAIL;
import static com.fasterxml.jackson.annotation.Nulls.SKIP;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.EMPTY;
import static com.sentrysoftware.matrix.common.helpers.StringHelper.addNonNull;

import java.io.Serializable;
import java.util.StringJoiner;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sentrysoftware.matrix.connector.deserializer.custom.EntryConcatMethodDeserializer;
import com.sentrysoftware.matrix.connector.deserializer.custom.NonBlankDeserializer;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
public class ExecuteForEachEntryOf  implements Serializable {

	private static final long serialVersionUID = 1L;

	@NonNull
	@JsonSetter(nulls = FAIL)
	@JsonDeserialize(using = NonBlankDeserializer.class)
	private String source;

	@JsonDeserialize(using = EntryConcatMethodDeserializer.class)
	@JsonSetter(nulls = SKIP)
	private IEntryConcatMethod concatMethod = EntryConcatMethod.LIST;

	@Builder
	@JsonCreator
	public ExecuteForEachEntryOf(
		@JsonProperty(value = "source", required = true) @NonNull String source,
		@JsonProperty("concatMethod") IEntryConcatMethod concatMethod
	) {

		this.source = source;
		this.concatMethod = concatMethod == null ? EntryConcatMethod.LIST : concatMethod;
	}

	public ExecuteForEachEntryOf copy() {
		return ExecuteForEachEntryOf
			.builder()
			.source(source)
			.concatMethod(concatMethod.copy())
			.build();
	}

	@Override
	public String toString() {

		final StringJoiner stringJoiner = new StringJoiner(NEW_LINE);

		addNonNull(stringJoiner, "- executeForEachEntryOf=", source);
		addNonNull(stringJoiner, "- concatMethod=", concatMethod != null ? concatMethod.getDescription() : EMPTY);

		return stringJoiner.toString();
	}
}
