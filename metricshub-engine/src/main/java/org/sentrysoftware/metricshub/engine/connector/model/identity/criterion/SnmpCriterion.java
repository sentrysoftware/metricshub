package org.sentrysoftware.metricshub.engine.connector.model.identity.criterion;

import static com.fasterxml.jackson.annotation.Nulls.FAIL;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import org.sentrysoftware.metricshub.engine.connector.deserializer.custom.NonBlankDeserializer;

/**
 * Abstract class representing an SNMP criterion.
 * <p>
 * This class provides a base for SNMP criteria and includes common attributes such as OID (Object Identifier)
 * and an optional expected result. SNMP criteria are used to detect entities based on SNMP queries.
 * </p>
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public abstract class SnmpCriterion extends Criterion {

	private static final long serialVersionUID = 1L;

	/**
	 * The SNMP OID (Object Identifier) used for the criterion.
	 */
	@NonNull
	@JsonSetter(nulls = FAIL)
	@JsonDeserialize(using = NonBlankDeserializer.class)
	private String oid;

	/**
	 * The expected result for the SNMP criterion.
	 */
	private String expectedResult;

	/**
	 * Constructs an SNMP criterion with the specified type, forceSerialization flag, OID, and expected result.
	 *
	 * @param type               The type of the SNMP criterion.
	 * @param forceSerialization A flag indicating whether serialization should be forced.
	 * @param oid                The SNMP OID (Object Identifier) for the criterion.
	 * @param expectedResult     The expected result for the criterion, or null if no specific result is expected.
	 */
	protected SnmpCriterion(String type, boolean forceSerialization, @NonNull String oid, String expectedResult) {
		super(type, forceSerialization);
		this.oid = oid;
		this.expectedResult = expectedResult;
	}
}
