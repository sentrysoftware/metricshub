package com.sentrysoftware.matrix.connector.model.identity.criterion;

import com.sentrysoftware.matrix.connector.deserializer.custom.NonBlankDeserializer;
import static com.fasterxml.jackson.annotation.Nulls.FAIL;
import static com.fasterxml.jackson.annotation.Nulls.SKIP;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public abstract class WqlCriterion extends Criterion {

	private static final long serialVersionUID = 1L;

	@NonNull
	@JsonSetter(nulls = FAIL)
	@JsonDeserialize(using = NonBlankDeserializer.class)
	private String query;
	@JsonSetter(nulls = SKIP)
	@JsonDeserialize(using = NonBlankDeserializer.class)
	private String namespace = "root/cimv2";
	private String expectedResult;
	private String errorMessage;

	protected WqlCriterion(
		String type,
		boolean forceSerialization,
		@NonNull String query,
		String namespace,
		String expectedResult,
		String errorMessage
	) {

		super(type, forceSerialization);
		this.query = query;
		this.namespace = namespace == null ? "root/cimv2": namespace;
		this.expectedResult = expectedResult;
		this.errorMessage = errorMessage;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("- WQL Query: ")
				.append(query)
				.append("\n- Namespace: ")
				.append(namespace);
		if (expectedResult != null && !expectedResult.isBlank()) {
			sb.append("\n- Expected Result: ").append(expectedResult);
		}
		return sb.toString();
	}

	public abstract WqlCriterion copy();
}
