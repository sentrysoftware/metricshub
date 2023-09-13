package com.sentrysoftware.matrix.connector.model.monitor;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION, defaultImpl = StandardMonitorJob.class) 
@JsonSubTypes(@JsonSubTypes.Type(value = SimpleMonitorJob.class))
public interface MonitorJob extends Serializable {

}
