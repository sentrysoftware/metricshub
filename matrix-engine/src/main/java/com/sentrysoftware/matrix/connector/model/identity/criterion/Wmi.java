package com.sentrysoftware.matrix.connector.model.identity.criterion;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Wmi extends WqlCriterion {

	private static final long serialVersionUID = 1L;

	@JsonCreator
	@Builder
	public Wmi(
		@JsonProperty("type") String type,
		@JsonProperty("forceSerialization") boolean forceSerialization,
		@JsonProperty(value = "query", required =  true) @NonNull String query,
		@JsonProperty(value = "namespace") String namespace,
		@JsonProperty(value = "expectedResult") String expectedResult,
		@JsonProperty(value = "errorMessage") String errorMessage
	) {

		super(type, forceSerialization, query, namespace, expectedResult, errorMessage);
	}


	@Override
	public Wmi copy() {
		return Wmi
				.builder()
				.query(getQuery())
				.namespace(getNamespace())
				.expectedResult(getExpectedResult())
				.errorMessage(getErrorMessage())
				.forceSerialization(isForceSerialization())
				.build();
	}

	@Override
	public String toString() {
		return super.toString();
	}
}
