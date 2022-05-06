package com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp;

import static com.sentrysoftware.matrix.common.helpers.StringHelper.addNonNull;

import java.util.List;
import java.util.StringJoiner;
import java.util.function.UnaryOperator;

import com.sentrysoftware.matrix.common.helpers.HardwareConstants;
import com.sentrysoftware.matrix.connector.model.common.ExecuteForEachEntry;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Compute;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class SnmpSource extends Source {

	private static final long serialVersionUID = 4987906836798379805L;

	protected String oid;

	protected SnmpSource(List<Compute> computes, boolean forceSerialization, String oid, int index, String key,
			ExecuteForEachEntry executeForEachEntry) {

		super(computes, forceSerialization, index, key, executeForEachEntry);
		this.oid = oid;
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		oid = updater.apply(oid);
	}

	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(HardwareConstants.NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- oid=", oid);

		return stringJoiner.toString();
	}
}
