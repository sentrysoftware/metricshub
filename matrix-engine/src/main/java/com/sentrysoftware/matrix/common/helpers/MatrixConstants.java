package com.sentrysoftware.matrix.common.helpers;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MatrixConstants {

	public static final String N_A = "N/A";
	public static final String SEMICOLON = ";";
	public static final String TABLE_SEP = SEMICOLON;
	public static final String EMPTY = "";
	public static final String WHITE_SPACE = " ";
	public static final String NEW_LINE = "\n";
	public static final String TAB = "\t";
	public static final String WHITE_SPACE_TAB = WHITE_SPACE + TAB;
	public static final String YAML_CONNECTOR_KEY = "connector";
	public static final String YAML_DISPLAY_NAME_KEY = "displayName";
	public static final long DEFAULT_JOB_TIMEOUT = 5 * 60L;
	public static final String METRICS_KEY = "mtx_sentry.connector.status";
	public static final String STATE_SET_METRIC_OK = "ok";
	public static final String STATE_SET_METRIC_FAILED = "failed";
	public static final String MONITOR_ATTRIBUTE_ID = "id";
	public static final String MONITOR_ATTRIBUTE_NAME = "name";
	public static final String MONITOR_ATTRIBUTE_CONNECTOR_ID = "connector_id";
	public static final String MONITOR_ATTRIBUTE_APPLIES_TO_OS = "applies_to_os";
	public static final String MONITOR_ATTRIBUTE_DETECTION = "detection";

}
