package com.sentrysoftware.matrix.connector.model.monitor.job.source.type.ucs;

import static com.sentrysoftware.matrix.common.helpers.StringHelper.addNonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
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
public class UCSSource extends Source {

	private static final long serialVersionUID = 2010036387689462346L;

	private List<String> queries = new ArrayList<>();
	private String excludeRegExp;
	private String keepOnlyRegExp;
	private List<String> selectColumns = new ArrayList<>();

	@Builder
	public UCSSource(List<Compute> computes, boolean forceSerialization, List<String> queries,
			String excludeRegExp, String keepOnlyRegExp, List<String> selectColumns, int index, String key) {

		super(computes, forceSerialization, index, key);
		this.queries = queries;
		this.excludeRegExp = excludeRegExp;
		this.keepOnlyRegExp = keepOnlyRegExp;
		this.selectColumns = selectColumns;
	}

	@Override
	public SourceTable accept(final ISourceVisitor sourceVisitor) {
		return sourceVisitor.visit(this);
	}

	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(HardwareConstants.NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- queries=", queries);
		addNonNull(stringJoiner, "- excludeRegExp=", excludeRegExp);
		addNonNull(stringJoiner, "- keepOnlyRegExp=", keepOnlyRegExp);
		addNonNull(stringJoiner, "- selectColumns=", selectColumns);

		return stringJoiner.toString();
	}

}
