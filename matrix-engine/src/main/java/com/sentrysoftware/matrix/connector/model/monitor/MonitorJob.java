package com.sentrysoftware.matrix.connector.model.monitor;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION, defaultImpl = StandardMonitorJob.class)
@JsonSubTypes(@JsonSubTypes.Type(value = SimpleMonitorJob.class))
public interface MonitorJob extends Serializable {}
