package com.sentrysoftware.matrix.connector.model.common;

import static com.sentrysoftware.matrix.common.helpers.StringHelper.addNonNull;

import java.io.Serializable;
import java.util.StringJoiner;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ExecuteForEachEntry  implements Serializable {

	private static final long serialVersionUID = -6115552625278347902L;

	private String executeForEachEntryOf;
	private EntryConcatMethod entryConcatMethod;
	private String entryConcatStart;
	private String entryConcatEnd;

	public ExecuteForEachEntry copy() {
		return ExecuteForEachEntry
				.builder()
				.executeForEachEntryOf(executeForEachEntryOf)
				.entryConcatMethod(entryConcatMethod)
				.entryConcatStart(entryConcatStart)
				.entryConcatEnd(entryConcatEnd)
				.build();
	}

	@Override
	public String toString() {

		final StringJoiner stringJoiner = new StringJoiner(HardwareConstants.NEW_LINE);

		addNonNull(stringJoiner, "- executeForEachEntryOf=", executeForEachEntryOf);
		addNonNull(stringJoiner, "- entryConcatMethod=", entryConcatMethod != null ? entryConcatMethod.getName() : null);
		addNonNull(stringJoiner, "- entryConcatStart=", entryConcatStart);
		addNonNull(stringJoiner, "- entryConcatEnd=", entryConcatEnd);

		return stringJoiner.toString();
	}
}
