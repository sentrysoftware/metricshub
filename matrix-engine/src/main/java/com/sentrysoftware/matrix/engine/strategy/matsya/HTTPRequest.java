package com.sentrysoftware.matrix.engine.strategy.matsya;

import com.sentrysoftware.matrix.connector.model.common.http.ResultContent;
import com.sentrysoftware.matrix.connector.model.common.http.body.Body;
import com.sentrysoftware.matrix.connector.model.common.http.header.Header;
import com.sentrysoftware.matrix.engine.protocol.HTTPProtocol;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HTTPRequest {
	private HTTPProtocol httpProtocol;
	private String hostname;
	private String method;
	private String url;
	private Header header;
	private Body body;
	private ResultContent resultContent;
}
