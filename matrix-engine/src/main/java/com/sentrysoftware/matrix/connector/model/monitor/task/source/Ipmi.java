package com.sentrysoftware.matrix.connector.model.monitor.task.source;

import java.util.ArrayList;
import java.util.List;
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
public class Ipmi extends Source {

	private static final long serialVersionUID = 1L;

	@Builder
	public Ipmi(
		String type, 
		List<Compute> computes,
		boolean forceSerialization,
		String key,
		ExecuteForEachEntry executeForEachEntry
	) {

		super(type, computes, forceSerialization, key, executeForEachEntry);
	}

	public Ipmi copy() {
		return Ipmi.builder()
				.type(type)
				.key(key)
				.forceSerialization(forceSerialization)
				.computes(getComputes() != null ? new ArrayList<>(getComputes()) : null)
				.executeForEachEntry(executeForEachEntry != null ? executeForEachEntry.copy() : null)
				.build();
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		// For now, there is nothing to update
	}

	@Override
	public String toString() {
		return super.toString();
	}

}
