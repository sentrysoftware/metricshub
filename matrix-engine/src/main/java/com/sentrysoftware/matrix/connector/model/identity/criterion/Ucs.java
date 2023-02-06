package com.sentrysoftware.matrix.connector.model.identity.criterion;

import static com.fasterxml.jackson.annotation.Nulls.FAIL;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sentrysoftware.matrix.connector.deserializer.custom.NonBlankDeserializer;

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
public class Ucs extends Criterion {

	private static final long serialVersionUID = 1L;

	@NonNull
	@JsonSetter(nulls = FAIL)
	@JsonDeserialize(using = NonBlankDeserializer.class)
	private String query;
	private String errorMessage;
	private String expectedResult;

	@Builder
	public Ucs(
			@JsonProperty("type") String type,
			@JsonProperty("forceSerialization") boolean forceSerialization,
			@JsonProperty(value = "query", required = true) @NonNull String query,
			@JsonProperty("errorMessage") String errorMessage,
			@JsonProperty("expectedResult") String expectedResult) {

		super(type, forceSerialization);
		this.query = query;
		this.errorMessage = errorMessage;
		this.expectedResult = expectedResult;
	}
}
