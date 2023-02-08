package com.sentrysoftware.matrix.connector.model.common;

import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.NEW_LINE;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.EMPTY;
import static com.sentrysoftware.matrix.common.helpers.StringHelper.addNonNull;

import java.io.Serializable;
import java.util.StringJoiner;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExecuteForEachEntryOf  implements Serializable {

	private static final long serialVersionUID = 1L;

	private String source;
	private IEntryConcatMethod concatMethod;

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
