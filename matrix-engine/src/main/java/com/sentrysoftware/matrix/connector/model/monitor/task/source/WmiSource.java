package com.sentrysoftware.matrix.connector.model.monitor.task.source;

import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.NEW_LINE;
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
public class WmiSource extends Source {

	private static final long serialVersionUID = 1L;

	private String query;
	private String namespace;

	@Builder
	public WmiSource(
		String type, 
		List<Compute> computes,
		boolean forceSerialization,
		String query,
		String namespace,
		String key,
		ExecuteForEachEntry executeForEachEntry
	) {

		super(type, computes, forceSerialization, key, executeForEachEntry);
		this.query = query;
		this.namespace = namespace;
	}

	@Override
	public WmiSource copy() {
		return WmiSource.builder()
				.type(type)
				.key(key)
				.forceSerialization(forceSerialization)
				.computes(getComputes() != null ? new ArrayList<>(getComputes()) : null)
				.executeForEachEntry(executeForEachEntry != null ? executeForEachEntry.copy() : null)
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
