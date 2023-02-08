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
import lombok.ToString;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class Process extends Criterion {

	private static final long serialVersionUID = 1L;

	@NonNull
	@JsonDeserialize(using = NonBlankDeserializer.class)
	@JsonSetter(nulls = FAIL)
	private String commandLine;

	@Builder
	@JsonCreator
	public Process(
			@JsonProperty("type") String type,
			@JsonProperty("forceSerialization") boolean forceSerialization,
			@JsonProperty(value = "commandLine", required = true) @NonNull String commandLine) {

		super(type, forceSerialization);
		this.commandLine = commandLine;
	}
}
