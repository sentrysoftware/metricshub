package com.sentrysoftware.matrix.connector.model.monitor.job.source.type.reference;

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
public class ReferenceSource extends Source {

	private static final long serialVersionUID = -4192645639386266586L;

	private String reference;

	@Builder
	public ReferenceSource(List<Compute> computes, boolean forceSerialization, String reference, int index, String key,
			ExecuteForEachEntry executeForEachEntry) {

		super(computes, forceSerialization, index, key, executeForEachEntry);
		this.reference = reference;
	}

	@Override
	public SourceTable accept(final ISourceVisitor sourceVisitor) {
		return sourceVisitor.visit(this);
	}

	public ReferenceSource copy() {
		return ReferenceSource.builder()
				.index(index)
				.key(key)
				.forceSerialization(forceSerialization)
				.computes(getComputes() != null ? new ArrayList<>(getComputes()) : null)
				.executeForEachEntry(executeForEachEntry != null ? executeForEachEntry.copy() : null)
				.reference(reference)
				.build();
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		reference = updater.apply(reference);
	}

	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(HardwareConstants.NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- reference=", reference);

		return stringJoiner.toString(); 
	}

}
