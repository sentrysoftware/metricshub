package com.sentrysoftware.matrix.connector.model.monitor.task.source;

import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.*;
import static com.sentrysoftware.matrix.common.helpers.StringHelper.addNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.UnaryOperator;

import com.sentrysoftware.matrix.connector.model.common.ExecuteForEachEntry;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Compute;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class StaticSource  extends Source {

	private static final long serialVersionUID = 1L;

	private String value;

	@Builder
	public StaticSource(
		String type,
		List<Compute> computes,
		boolean forceSerialization,
		String value,
		String key,
		ExecuteForEachEntry executeForEachEntry
	) {

		super("static", computes, forceSerialization, key, executeForEachEntry);
		this.value = value;
	}


	public StaticSource copy() {
		return StaticSource.builder()
				.type(type)
				.key(key)
				.forceSerialization(forceSerialization)
				.computes(getComputes() != null ? new ArrayList<>(getComputes()) : null)
				.executeForEachEntry(executeForEachEntry != null ? executeForEachEntry.copy() : null)
				.value(value)
				.build();
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		value = updater.apply(value);
	}

	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- value=", value);

		return stringJoiner.toString();
	}
}
