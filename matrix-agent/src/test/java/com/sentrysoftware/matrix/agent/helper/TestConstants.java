package com.sentrysoftware.matrix.agent.helper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class TestConstants {

	public static final String GRAFANA_HEALTH_SOURCE_KEY = "grafanaHealth";
	public static final String GRAFANA_HEALTH_SOURCE_REF = "${source::monitors.grafana.simple.sources.grafanaHealth}";
	public static final String GRAFANA_MONITOR_JOB_KEY = "grafana";
	public static final String GRAFANA_DB_STATE_METRIC = "grafana.db.state";
	public static final String HTTP_SERVICE_URL = "https://hws-demo.sentrysoftware.com/api/health";
	public static final String HTTP_ACCEPT_HEADER = "Accept: application/json";
	public static final String SERVICE_VERSION = "service.version";
	public static final String ID_ATTRIBUTE_KEY = "id";
	public static final String HTTP_KEY_TYPE = "http";
	public static final String SENTRY_PARIS_SITE_VALUE = "Sentry-Paris";
	public static final String GRAFANA_SERVICE_RESOURCE_CONFIG_KEY = "grafana-service";
	public static final String SITE_ATTRIBUTE_KEY = "site";
	public static final String SERVICE_VERSION_ATTRIBUTE_KEY = "service.version";
}
