package com.sentrysoftware.matrix.connector.model.common.http.body;

import com.sentrysoftware.matrix.connector.model.common.EmbeddedFile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmbeddedFileBody implements Body {

	private static final long serialVersionUID = -8300191804094179578L;

	private EmbeddedFile body;
}
