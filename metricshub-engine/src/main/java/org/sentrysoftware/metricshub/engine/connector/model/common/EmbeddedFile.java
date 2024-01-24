package org.sentrysoftware.metricshub.engine.connector.model.common;

import java.io.Serializable;
import java.util.function.UnaryOperator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents an embedded file within a connector.
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmbeddedFile implements Serializable {

	private static final long serialVersionUID = -197665338834839387L;

	/**
	 * The content of the embedded file.
	 */
	private String content;
	/**
	 * The type of the embedded file (e.g., MIME type).
	 */
	private String type;
	/**
	 * A reference to the embedded file, providing additional information or identification.
	 */
	private String reference;

	/**
	 * Creates a copy of the current embedded file.
	 *
	 * @return A new instance of {@link EmbeddedFile} with the same content, type, and reference.
	 */
	public EmbeddedFile copy() {
		return EmbeddedFile.builder().content(content).type(type).reference(reference).build();
	}

	/**
	 * Gets a string containing the embedded file reference.
	 *
	 * @return A string representing the reference to the embedded file.
	 */
	public String description() {
		return reference;
	}

	/**
	 * Updates the content and type of the embedded file using the provided updater.
	 *
	 * @param updater The unary operator to apply to the content and type.
	 */
	public void update(UnaryOperator<String> updater) {
		content = updater.apply(content);
		type = updater.apply(type);
	}
}
