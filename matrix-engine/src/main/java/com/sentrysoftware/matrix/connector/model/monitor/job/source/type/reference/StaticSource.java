package com.sentrysoftware.matrix.connector.model.monitor.job.source.type.reference;

import static com.sentrysoftware.matrix.common.helpers.StringHelper.addNonNull;

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
public class StaticSource  extends Source {

	private static final long serialVersionUID = 3446426549562321969L;

	private String staticValue;

	@Builder
	public StaticSource(List<Compute> computes, boolean forceSerialization, String staticValue, int index, String key) {
		super(computes, forceSerialization, index, key);
		this.staticValue = staticValue;
	}

	@Override
	public SourceTable accept(final ISourceVisitor sourceVisitor) {
		return sourceVisitor.visit(this);
	}

	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(HardwareConstants.NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- staticValue=", staticValue);

		return stringJoiner.toString(); 
	}
}
