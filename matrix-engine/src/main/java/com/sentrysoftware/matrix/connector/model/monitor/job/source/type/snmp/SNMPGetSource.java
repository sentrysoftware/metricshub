package com.sentrysoftware.matrix.connector.model.monitor.job.source.type.snmp;

import java.util.List;
import java.util.stream.Collectors;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Compute;
import com.sentrysoftware.matrix.engine.strategy.source.ISourceVisitor;
import com.sentrysoftware.matrix.engine.strategy.source.SourceTable;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SNMPGetSource extends SNMPSource {

	private static final long serialVersionUID = 9174253450745863940L;

	@Builder
	public SNMPGetSource(List<Compute> computes, boolean forceSerialization, String oid, int index, String key) {

		super(computes, forceSerialization, oid, index, key);
	}

	@Override
	public SourceTable accept(final ISourceVisitor sourceVisitor) {
		return sourceVisitor.visit(this);
	}

	/**
	 * Copy the current instance
	 * 
	 * @return new {@link SNMPGetSource} instance
	 */
	public SNMPGetSource copy() {
		return SNMPGetSource.builder()
				.oid(getOid())
				.index(getIndex())
				.key(getKey())
				.forceSerialization(isForceSerialization())
				.computes(
						getComputes() != null ? getComputes().stream()
								.collect(Collectors.toList()) : null)
				.build();
	}
}
