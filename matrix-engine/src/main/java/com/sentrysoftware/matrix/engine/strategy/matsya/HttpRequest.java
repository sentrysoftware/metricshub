package com.sentrysoftware.matrix.engine.strategy.matsya;

import com.sentrysoftware.matrix.connector.model.common.http.ResultContent;
import com.sentrysoftware.matrix.connector.model.common.http.body.Body;
import com.sentrysoftware.matrix.connector.model.common.http.header.Header;
import com.sentrysoftware.matrix.engine.protocol.HttpProtocol;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Builder.Default;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HttpRequest {
	@NonNull
	private HttpProtocol httpProtocol;

	@NonNull
	private String hostname;

	private String method;

	private String url;

	private Header header;

	private Body body;

	@Default
	@NonNull
	private ResultContent resultContent = ResultContent.BODY;

	private String authenticationToken;
}
