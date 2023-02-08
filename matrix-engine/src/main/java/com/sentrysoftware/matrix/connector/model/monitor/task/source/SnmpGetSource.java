package com.sentrysoftware.matrix.connector.model.monitor.task.source;

import java.util.ArrayList;
import java.util.List;

import com.sentrysoftware.matrix.connector.model.common.ExecuteForEachEntryOf;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.compute.Compute;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SnmpGetSource extends SnmpSource {

	private static final long serialVersionUID = 1L;

	@Builder
	public SnmpGetSource(
		String type, 
		List<Compute> computes,
		boolean forceSerialization,
		String oid,
		String key,
		ExecuteForEachEntryOf executeForEachEntryOf
	) {

		super(type, computes, forceSerialization, oid, key, executeForEachEntryOf);
	}


	/**
	 * Copy the current instance
	 * 
	 * @return new {@link SnmpGetSource} instance
	 */
	public SnmpGetSource copy() {
		return SnmpGetSource.builder()
				.type(type)
				.key(key)
				.forceSerialization(forceSerialization)
				.computes(getComputes() != null ? new ArrayList<>(getComputes()) : null)
				.executeForEachEntryOf(executeForEachEntryOf != null ? executeForEachEntryOf.copy() : null)
				.oid(oid)
				.build();
	}

	@Override
	public String toString() {
		return super.toString();
	}
}
