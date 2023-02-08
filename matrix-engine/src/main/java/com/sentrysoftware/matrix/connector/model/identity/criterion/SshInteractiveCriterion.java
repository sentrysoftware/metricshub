package com.sentrysoftware.matrix.connector.model.identity.criterion;

import static com.fasterxml.jackson.annotation.Nulls.FAIL;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sentrysoftware.matrix.connector.deserializer.custom.PortDeserializer;
import com.sentrysoftware.matrix.connector.model.common.sshstep.Step;

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
public class SshInteractiveCriterion extends Criterion {

	private static final long serialVersionUID = 1L;

	@JsonDeserialize(using = PortDeserializer.class)
	private Integer port;
	private String expectedResult;

	@NonNull
	@JsonSetter(nulls = FAIL)
	private List<Step> steps = new ArrayList<>();

	@Builder
	@JsonCreator
	public SshInteractiveCriterion(
			@JsonProperty("type") String type,
			@JsonProperty("forceSerialization") boolean forceSerialization,
			@JsonProperty("port") Integer port,
			@JsonProperty("expectedResult") String expectedResult,
			@JsonProperty(value = "steps", required = true) @NonNull List<Step> steps) {

		super(type, forceSerialization);
		this.port = port;
		this.expectedResult = expectedResult;
		this.steps = steps;
	}
}
