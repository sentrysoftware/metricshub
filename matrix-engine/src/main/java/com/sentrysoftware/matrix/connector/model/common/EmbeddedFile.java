package com.sentrysoftware.matrix.connector.model.common;

import java.io.Serializable;

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

	public EmbeddedFile copy() {
		return EmbeddedFile.builder().content(content).type(type).build();
	}
}
