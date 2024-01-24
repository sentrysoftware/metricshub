package org.sentrysoftware.metricshub.engine.connector.model.identity.criterion;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;
import org.sentrysoftware.metricshub.engine.strategy.detection.CriterionTestResult;
import org.sentrysoftware.metricshub.engine.strategy.detection.ICriterionProcessor;

/**
 * Represents an SNMP GET criterion used for detection.
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SnmpGetCriterion extends SnmpCriterion {

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs an SNMP GET criterion with the specified type, forceSerialization flag, OID, and expected result.
	 *
	 * @param type               The type of the SNMP GET criterion.
	 * @param forceSerialization A flag indicating whether serialization should be forced.
	 * @param oid                The SNMP OID (Object Identifier) for the GET operation.
	 * @param expectedResult     The expected result for the GET operation, or null if no specific result is expected.
	 */
	@Builder
	@JsonCreator
	public SnmpGetCriterion(
		@JsonProperty("type") String type,
		@JsonProperty("forceSerialization") boolean forceSerialization,
		@JsonProperty(value = "oid", required = true) @NonNull String oid,
		@JsonProperty("expectedResult") String expectedResult
	) {
		super(type, forceSerialization, oid, expectedResult);
	}

	@Override
	public CriterionTestResult accept(ICriterionProcessor criterionProcessor) {
		return criterionProcessor.process(this);
	}
}
