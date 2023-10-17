package com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sentrysoftware.metricshub.engine.connector.model.common.ExecuteForEachEntryOf;
import com.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.Compute;
import com.sentrysoftware.metricshub.engine.strategy.source.ISourceProcessor;
import com.sentrysoftware.metricshub.engine.strategy.source.SourceTable;
import java.util.ArrayList;
import java.util.List;
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
	@JsonCreator
	public SnmpGetSource(
		@JsonProperty("type") String type,
		@JsonProperty("computes") List<Compute> computes,
		@JsonProperty("forceSerialization") boolean forceSerialization,
		@JsonProperty(value = "oid", required = true) String oid,
		@JsonProperty("key") String key,
		@JsonProperty("executeForEachEntryOf") ExecuteForEachEntryOf executeForEachEntryOf
	) {
		super(type, computes, forceSerialization, oid, key, executeForEachEntryOf);
	}

	/**
	 * Copy the current instance
	 *
	 * @return new {@link SnmpGetSource} instance
	 */
	public SnmpGetSource copy() {
		return SnmpGetSource
			.builder()
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

	@Override
	public SourceTable accept(final ISourceProcessor sourceProcessor) {
		return sourceProcessor.process(this);
	}
}
