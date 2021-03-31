package com.sentrysoftware.matrix.connector.model.monitor.job.source;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Compute;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class Source implements Serializable {

	private static final long serialVersionUID = 4765209445308968001L;

	private List<Compute> computes = new ArrayList<>();

	private boolean forceSerialization;
}
