package com.sentrysoftware.metricshub.engine.connector.model.common;

import java.io.Serializable;
import java.util.function.UnaryOperator;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmbeddedFile implements Serializable {

	private static final long serialVersionUID = -197665338834839387L;

	private String content;
	private String type;
	private String reference;

	/**
	 *
	 * @return EmbeddedFile instance
	 */
	public EmbeddedFile copy() {
		return EmbeddedFile.builder().content(content).type(type).reference(reference).build();
	}

	/**
	 *
	 * @return String that contains the embedded file reference
	 */
	public String description() {
		return reference;
	}

	/**
	 *
	 * @param updater
	 */
	public void update(UnaryOperator<String> updater) {
		content = updater.apply(content);
		type = updater.apply(type);
	}
}
