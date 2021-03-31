package com.sentrysoftware.matrix.connector.model.common.http.header;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StringHeader implements Header {

	private static final long serialVersionUID = 7838818669996389750L;

	private String header;

}
