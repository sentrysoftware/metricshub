package org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source;

import static com.fasterxml.jackson.annotation.Nulls.FAIL;
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.NEW_LINE;
import static org.sentrysoftware.metricshub.engine.common.helpers.StringHelper.addNonNull;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.List;
import java.util.StringJoiner;
import java.util.function.UnaryOperator;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.sentrysoftware.metricshub.engine.connector.deserializer.custom.NonBlankDeserializer;
import org.sentrysoftware.metricshub.engine.connector.model.common.ExecuteForEachEntryOf;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.compute.Compute;

/**
 * Represents a base class for SNMP-based source tasks.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class SnmpSource extends Source {

	private static final long serialVersionUID = 1L;

	/**
	 * The SNMP OID (Object Identifier) associated with the source.
	 */
	@NonNull
	@JsonSetter(nulls = FAIL)
	@JsonDeserialize(using = NonBlankDeserializer.class)
	protected String oid;

	/**
	 * Constructs a new {@code SnmpSource} instance with the provided attributes.
	 *
	 * @param type                  the type of the source
	 * @param computes              the list of compute operations to be applied
	 * @param forceSerialization    flag indicating whether serialization should be forced
	 * @param oid                   the SNMP OID to retrieve data
	 * @param key                   the key associated with the source
	 * @param executeForEachEntryOf the execute-for-each-entry-of information
	 */
	protected SnmpSource(
		String type,
		List<Compute> computes,
		boolean forceSerialization,
		@NonNull String oid,
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
