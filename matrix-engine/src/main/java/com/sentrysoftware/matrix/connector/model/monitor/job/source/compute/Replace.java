package com.sentrysoftware.matrix.connector.model.monitor.job.source.compute;

import static com.sentrysoftware.matrix.common.helpers.StringHelper.addNonNull;

import java.util.StringJoiner;
import java.util.function.UnaryOperator;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.engine.strategy.source.compute.IComputeVisitor;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Replace extends Compute {

	private static final long serialVersionUID = -1177932638215228955L;

	private Integer column;
	private String replace; // NOSONAR
	private String replaceBy;

	@Builder
	public Replace(Integer index, Integer column, String replace, String replaceBy) {
		super(index);
		this.column = column;
		this.replace = replace;
		this.replaceBy = replaceBy;
	}

	@Override
	public void accept(final IComputeVisitor computeVisitor) {
		computeVisitor.visit(this);
	}

	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(HardwareConstants.NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- column=", column);
		addNonNull(stringJoiner, "- replace=", replace);
		addNonNull(stringJoiner, "- replaceBy=", replaceBy);

		return stringJoiner.toString();
	}

	@Override
	public Replace copy() {
		return Replace
			.builder()
			.index(index)
			.column(column)
			.replace(replace)
			.replaceBy(replaceBy)
			.build();
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		replace = updater.apply(replace);
		replaceBy = updater.apply(replaceBy);
	}
}
