package com.sentrysoftware.hardware.agent.configuration;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * Register a CORS Configuration in order to allow origin, methods and headers.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class BackendCorsFilter extends CorsFilter {

	public BackendCorsFilter() {

		super(configurationSource());
	}

	/**
	 * Create an {@link UrlBasedCorsConfigurationSource} configuration to be passed to the super {@link CorsFilter} constructor
	 * 
	 * @return {@link UrlBasedCorsConfigurationSource} allowing origin, headers and methods
	 */
	private static UrlBasedCorsConfigurationSource configurationSource() {

		final CorsConfiguration config = new CorsConfiguration();

		// origins
		config.addAllowedOrigin("*");

		// withCredentials: true, we require exact origin match
		config.setAllowCredentials(false);

		// headers
		config.addAllowedHeader("*");

		// methods
		config.addAllowedMethod("*");

		final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", config);

		return source;
	}
}