package com.sentrysoftware.matrix.connector.model.identity.criterion;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

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
		@JsonSubTypes.Type(value = UcsCriterion.class, name = "ucs"),
		@JsonSubTypes.Type(value = OsCommandCriterion.class, name = "osCommand"),
		@JsonSubTypes.Type(value = SshInteractiveCriterion.class, name = "sshInteractive")
	}
)
@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class Criterion implements Serializable {

	private static final long serialVersionUID = 1L;

	protected String type;
	protected boolean forceSerialization;

}
