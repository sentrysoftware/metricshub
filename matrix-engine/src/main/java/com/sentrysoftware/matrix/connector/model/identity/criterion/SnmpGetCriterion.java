package com.sentrysoftware.matrix.connector.model.identity.criterion;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sentrysoftware.matrix.strategy.detection.CriterionTestResult;
import com.sentrysoftware.matrix.strategy.detection.ICriterionProcessor;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class SnmpGetCriterion extends SnmpCriterion {

	private static final long serialVersionUID = 1L;

	@Builder
	@JsonCreator
	public SnmpGetCriterion(
		@JsonProperty("type") String type, 
		@JsonProperty("forceSerialization") boolean forceSerialization, 
		@JsonProperty(value = "oid", required = true) @NonNull String oid, 
		@JsonProperty("expectedResult") String expectedResult) {

		super(type, forceSerialization, oid, expectedResult);
	}

	@Override
	public CriterionTestResult accept(ICriterionProcessor criterionProcessor) {
		return criterionProcessor.process(this);
	}
}
