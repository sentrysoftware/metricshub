package com.sentrysoftware.matrix.connector.model.identity.criterion;

import static com.fasterxml.jackson.annotation.Nulls.FAIL;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sentrysoftware.matrix.connector.deserializer.custom.NonBlankDeserializer;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class ServiceCriterion extends Criterion {

	private static final long serialVersionUID = 1L;

	@NonNull
	@JsonDeserialize(using = NonBlankDeserializer.class)
	@JsonSetter(nulls = FAIL)
	private String name;

	@Builder
	@JsonCreator
	public ServiceCriterion(
			@JsonProperty("type") String type,
			@JsonProperty("forceSerialization") boolean forceSerialization,
			@JsonProperty(value = "name", required = true) @NonNull String name) {

		super(type, forceSerialization);
		this.name = name;
	}

	@Override
	public String toString() {
		return "- Service: " + name;
	}

}
