package com.sentrysoftware.hardware.agent.configuration;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("ssl")
public class ServerConfig {

	@Value("${http.port}")
	private int httpPort;

	@Value("${server.port}")
	private int serverPort;

	@Value("${server.ssl.redirect-http}")
	private boolean redirectHttp;

	@Bean
	public ServletWebServerFactory servletContainer() {

		TomcatServletWebServerFactory tomcat = redirectHttp
		? new TomcatServletWebServerFactory() {
			@Override
			protected void postProcessContext(Context context) {
				SecurityConstraint securityConstraint = new SecurityConstraint();
				securityConstraint.setUserConstraint("CONFIDENTIAL");
				SecurityCollection collection = new SecurityCollection();
				collection.addPattern("/*");
				securityConstraint.addCollection(collection);
				context.addConstraint(securityConstraint);
			}
		}
		: new TomcatServletWebServerFactory();

		tomcat.addAdditionalTomcatConnectors(getHttpConnector());

		return tomcat;
	}

	private Connector getHttpConnector() {

		Connector connector = new Connector(TomcatServletWebServerFactory.DEFAULT_PROTOCOL);
		connector.setScheme("http");
		connector.setPort(httpPort);
		connector.setSecure(false);

		if (redirectHttp) {
			connector.setRedirectPort(serverPort);
		}

		return connector;
	}
}