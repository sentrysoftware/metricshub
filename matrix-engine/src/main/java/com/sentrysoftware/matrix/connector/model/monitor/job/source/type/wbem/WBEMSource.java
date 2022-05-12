package com.sentrysoftware.matrix.connector.model.monitor.job.source.type.wbem;

import static com.sentrysoftware.matrix.common.helpers.StringHelper.addNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.UnaryOperator;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.connector.model.common.ExecuteForEachEntry;
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
public class WBEMSource extends Source {

	private static final long serialVersionUID = -1068957824633332862L;

	private String wbemQuery;
	private String wbemNamespace;

	@Builder
	public WBEMSource(List<Compute> computes, boolean forceSerialization, String wbemQuery,
			String wbemNamespace, int index, String key, ExecuteForEachEntry executeForEachEntry) {

		super(computes, forceSerialization, index, key, executeForEachEntry);
		this.wbemQuery = wbemQuery;
		this.wbemNamespace = wbemNamespace;
	}

	@Override
	public SourceTable accept(final ISourceVisitor sourceVisitor) {
		return sourceVisitor.visit(this);
	}

	@Override
	public WBEMSource copy() {
		return WBEMSource.builder()
				.index(index)
				.key(key)
				.forceSerialization(forceSerialization)
				.computes(getComputes() != null ? new ArrayList<>(getComputes()) : null)
				.executeForEachEntry(executeForEachEntry != null ? executeForEachEntry.copy() : null)
				.wbemQuery(wbemQuery)
				.wbemNamespace(wbemNamespace)
				.build();
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		wbemQuery = updater.apply(wbemQuery);
		wbemNamespace = updater.apply(wbemNamespace);
	}

	@Override
	public String toString() {

		final StringJoiner stringJoiner = new StringJoiner(HardwareConstants.NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- wbemQuery=", wbemQuery);
		addNonNull(stringJoiner, "- wbemNamespace=", wbemNamespace);

		return stringJoiner.toString();
	}

}
