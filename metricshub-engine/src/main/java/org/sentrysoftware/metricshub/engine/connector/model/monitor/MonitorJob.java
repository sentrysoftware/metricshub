package org.sentrysoftware.metricshub.engine.connector.model.monitor;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;

/**
 * Represents an interface for monitor jobs.
 *
 * <p>
 * This interface is designed to be implemented by various monitor job classes, providing a common base for different
 * types of monitor jobs.
 * </p>
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION, defaultImpl = StandardMonitorJob.class)
@JsonSubTypes(@JsonSubTypes.Type(value = SimpleMonitorJob.class))
public interface MonitorJob extends Serializable {}
