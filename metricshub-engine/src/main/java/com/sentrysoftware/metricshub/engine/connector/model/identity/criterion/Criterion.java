package com.sentrysoftware.metricshub.engine.connector.model.identity.criterion;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.sentrysoftware.metricshub.engine.strategy.detection.CriterionTestResult;
import com.sentrysoftware.metricshub.engine.strategy.detection.ICriterionProcessor;
import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", visible = true)
@JsonSubTypes(
	{
		@JsonSubTypes.Type(value = IpmiCriterion.class, name = "ipmi"),
		@JsonSubTypes.Type(value = HttpCriterion.class, name = "http"),
		@JsonSubTypes.Type(value = DeviceTypeCriterion.class, name = "deviceType"),
		@JsonSubTypes.Type(value = ProcessCriterion.class, name = "process"),
		@JsonSubTypes.Type(value = ProductRequirementsCriterion.class, name = "productRequirements"),
		@JsonSubTypes.Type(value = SnmpGetCriterion.class, name = "snmpGet"),
		@JsonSubTypes.Type(value = SnmpGetNextCriterion.class, name = "snmpGetNext"),
		@JsonSubTypes.Type(value = WmiCriterion.class, name = "wmi"),
		@JsonSubTypes.Type(value = WbemCriterion.class, name = "wbem"),
		@JsonSubTypes.Type(value = ServiceCriterion.class, name = "service"),
		@JsonSubTypes.Type(value = OsCommandCriterion.class, name = "osCommand")
	}
)
@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class Criterion implements Serializable {

	private static final long serialVersionUID = 1L;

	protected String type;
	protected boolean forceSerialization;

	public abstract CriterionTestResult accept(ICriterionProcessor criterionProcessor);
}
