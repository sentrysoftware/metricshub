package com.sentrysoftware.matrix.connector.model.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.function.UnaryOperator;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmbeddedFile implements Serializable {

	private static final long serialVersionUID = -197665338834839387L;

	private String content;
	private String type;
	private Integer index;

	/**
	 *
	 * @return EmbeddedFile instance
	 */
	public EmbeddedFile copy() {
		return EmbeddedFile
				.builder()
				.content(content)
				.type(type)
				.index(index)
				.build();
	}

	/**
	 *
	 * @return String that contains the embedded file description
	 */
	public String description() {
		return String.format("EmbeddedFile(%d)", index);
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
