package org.sentrysoftware.metricshub.engine.connector.model.common;

import static com.fasterxml.jackson.annotation.Nulls.FAIL;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

/**
 * Represents a custom concatenation method for combining multiple entries.
 */
@Data
@NoArgsConstructor
public class CustomConcatMethod implements IEntryConcatMethod {

	private static final long serialVersionUID = 1L;

	/**
	 * The concatenation start string.
	 */
	@JsonSetter(nulls = FAIL)
	@NonNull
	private String concatStart;

	/**
	 * The concatenation end string.
	 */
	@JsonSetter(nulls = FAIL)
	@NonNull
	private String concatEnd;

	/**
	 * Constructor to create a CustomConcatMethod instance.
	 *
	 * @param concatStart The concatenation start string.
	 * @param concatEnd   The concatenation end string.
	 */
	@Builder
	@JsonCreator
	public CustomConcatMethod(
		@JsonProperty(value = "concatStart", required = true) @NonNull String concatStart,
		@JsonProperty(value = "concatEnd", required = true) @NonNull String concatEnd
	) {
		this.concatStart = concatStart;
		this.concatEnd = concatEnd;
	}

	@Override
	public CustomConcatMethod copy() {
		return CustomConcatMethod.builder().concatStart(concatStart).concatEnd(concatEnd).build();
	}

	@Override
	public String getDescription() {
		return String.format("custom[concatStart=%s, concatEnd=%s]", concatStart, concatEnd);
	}
}
