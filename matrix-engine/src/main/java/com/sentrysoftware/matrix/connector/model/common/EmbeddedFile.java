package com.sentrysoftware.matrix.connector.model.common;

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
	private Integer index;

	public EmbeddedFile copy() {
		return EmbeddedFile
				.builder()
				.content(content)
				.type(type)
				.index(index)
				.build();
	}

	public String description() {
		return String.format("EmbeddedFile(%d)", index);
	}

	public void update(UnaryOperator<String> updater) {
		content = updater.apply(content);
		type = updater.apply(type);
	}
}
