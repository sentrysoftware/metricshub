package com.sentrysoftware.matrix.connector.model.common.http.header;

import com.sentrysoftware.matrix.connector.model.common.EmbeddedFile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EmbeddedFileHeader implements Header {

	private static final long serialVersionUID = 7171137961999511622L;

	private EmbeddedFile header;
}
