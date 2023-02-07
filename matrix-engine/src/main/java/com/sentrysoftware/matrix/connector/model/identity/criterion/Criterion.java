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
		@JsonSubTypes.Type(value = Ipmi.class, name = "ipmi"),
		@JsonSubTypes.Type(value = Http.class, name = "http"),
		@JsonSubTypes.Type(value = DeviceType.class, name = "deviceType"),
		@JsonSubTypes.Type(value = Process.class, name = "process"),
		@JsonSubTypes.Type(value = ProductRequirements.class, name = "productRequirements"),
		@JsonSubTypes.Type(value = Wmi.class, name = "wmi"),
		@JsonSubTypes.Type(value = Wbem.class, name = "wbem")
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
