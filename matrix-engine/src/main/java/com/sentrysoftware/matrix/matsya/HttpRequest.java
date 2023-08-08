package com.sentrysoftware.matrix.matsya;

import com.sentrysoftware.matrix.configuration.HttpConfiguration;
import com.sentrysoftware.matrix.connector.model.common.ResultContent;
import com.sentrysoftware.matrix.connector.model.common.http.body.Body;
import com.sentrysoftware.matrix.connector.model.common.http.header.Header;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HttpRequest {
	@NonNull
	private HttpConfiguration httpConfiguration;

	@NonNull
	private String hostname;

	private String method;

	private String url;

	private Header header;

	private Body body;

	@Builder.Default
	@NonNull
	private ResultContent resultContent = ResultContent.BODY;

	private String authenticationToken;
}
