package com.sentrysoftware.matrix.connector.model.identity.criterion;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sentrysoftware.matrix.connector.deserializer.custom.NonBlankDeserializer;

import static com.fasterxml.jackson.annotation.Nulls.FAIL;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.ToString;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public abstract class Snmp extends Criterion {

	private static final long serialVersionUID = 1L;

	@NonNull
	@JsonSetter(nulls = FAIL)
	@JsonDeserialize(using = NonBlankDeserializer.class)
	private String oid;
	private String expectedResult;

	protected Snmp(
			@JsonProperty("type") String type, 
			@JsonProperty("forceSerialization") boolean forceSerialization, 
			@JsonProperty(value = "oid", required = true) @NonNull String oid, 
			@JsonProperty("expectedResult") String expectedResult) {

		super(type, forceSerialization);
		this.oid = oid;
		this.expectedResult = expectedResult;
	}
}
