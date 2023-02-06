package com.sentrysoftware.matrix.connector.model.common.sshstep;

import static com.sentrysoftware.matrix.common.helpers.MatrixConstants.NEW_LINE;
import static com.sentrysoftware.matrix.common.helpers.StringHelper.addNonNull;

import java.io.Serializable;
import java.util.StringJoiner;
import java.util.function.UnaryOperator;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type", visible = true)
@JsonSubTypes(
	{
		@JsonSubTypes.Type(value = GetAvailable.class, name = "getAvailable"),
		@JsonSubTypes.Type(value = GetUntilPrompt.class, name = "getUntilPrompt"),
		@JsonSubTypes.Type(value = SendPassword.class, name = "sendPassword"),
		@JsonSubTypes.Type(value = SendText.class, name = "sendText"),
		@JsonSubTypes.Type(value = SendUsername.class, name = "sendUsername"),
		@JsonSubTypes.Type(value = Sleep.class, name = "sleep"),
		@JsonSubTypes.Type(value = WaitFor.class, name = "waitFor"),
		@JsonSubTypes.Type(value = WaitForPrompt.class, name = "waitForPrompt")
	}
)
@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class Step implements Serializable {

	private static final long serialVersionUID = 1L;

	protected String type;

	protected Boolean capture;

	/** If true this step is not processed. (Mainly when parameter TelnetOnly is set) */
	protected boolean ignored;

	public abstract Step copy();

	@Override
	public String toString() {
		final StringJoiner stringJoiner = new StringJoiner(NEW_LINE);

		stringJoiner.add(new StringBuilder("- type=").append(this.getClass().getSimpleName()));

		addNonNull(stringJoiner, "- capture=", capture);
		addNonNull(stringJoiner, "- ignored=", ignored);

		return stringJoiner.toString();
	}

	public abstract void update(UnaryOperator<String> updater);
}
