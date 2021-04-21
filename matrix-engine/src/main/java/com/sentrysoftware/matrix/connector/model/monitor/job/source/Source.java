package com.sentrysoftware.matrix.connector.model.monitor.job.source;

import static org.springframework.util.Assert.isTrue;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Compute;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public abstract class Source implements Serializable {

	private static final long serialVersionUID = 4765209445308968001L;

	private List<Compute> computes;

	private boolean forceSerialization;

	private Integer index;

	protected Source(List<Compute> computes, boolean forceSerialization, Integer index) {
		this.computes = computes == null ? new ArrayList<>() : computes;
		this.forceSerialization = forceSerialization;
		this.index = index;
	}

	protected void setIndex(int index) {

		isTrue(index > 0, "Invalid index: " + index);
		this.index = index;
	}
}
