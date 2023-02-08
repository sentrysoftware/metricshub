package com.sentrysoftware.matrix.connector.model.common.sshstep;

import static com.fasterxml.jackson.annotation.Nulls.FAIL;
import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.NEW_LINE;
import static com.sentrysoftware.matrix.common.helpers.StringHelper.addNonNull;

import java.util.StringJoiner;
import java.util.function.UnaryOperator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class SendText extends Step {

	private static final long serialVersionUID = 1L;

	@NonNull
	@JsonSetter(nulls = FAIL)
	private String text;

	@Builder
	@JsonCreator
	public SendText(
		@JsonProperty("type") String type,
		@JsonProperty("capture") Boolean capture,
		@JsonProperty("ignored") boolean ignored,
		@JsonProperty(value = "text", required = true) String text
	) {

		super(type, capture, ignored);
		this.text = text;
	}

	@Override
	public SendText copy() {

		return SendText.builder()
				.type(type)
				.capture(capture)
				.ignored(ignored)
				.text(text)
				.build();
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		text = updater.apply(text);
	}

	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(NEW_LINE);

		stringJoiner.add(super.toString());

		addNonNull(stringJoiner, "- text=", text);

		return stringJoiner.toString();
	}
}
