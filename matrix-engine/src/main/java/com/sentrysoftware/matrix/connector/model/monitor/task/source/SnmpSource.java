package com.sentrysoftware.matrix.connector.model.monitor.task.source;

import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.*;
import static com.sentrysoftware.matrix.common.helpers.StringHelper.addNonNull;

import java.util.List;
import java.util.StringJoiner;
import java.util.function.UnaryOperator;

import com.sentrysoftware.matrix.connector.model.common.ExecuteForEachEntryOf;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Compute;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class SnmpSource extends Source {

	private static final long serialVersionUID = 1L;

	protected String oid;

	protected SnmpSource(
		String type,
		List<Compute> computes, 
		boolean forceSerialization,
		String oid,
		String key,
		ExecuteForEachEntryOf executeForEachEntryOf
	) {

		super(type, computes, forceSerialization, key, executeForEachEntryOf);
		this.oid = oid;
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		oid = updater.apply(oid);
	}

	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- oid=", oid);

		return stringJoiner.toString();
	}
}
