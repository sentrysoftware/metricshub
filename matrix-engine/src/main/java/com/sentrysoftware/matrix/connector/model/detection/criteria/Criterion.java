package com.sentrysoftware.matrix.connector.model.detection.criteria;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

import static com.sentrysoftware.matrix.utils.Assert.isTrue;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class Criterion implements Serializable {

	private static final long serialVersionUID = -3677479724786317941L;

	private boolean forceSerialization;

	private Integer index;

	protected Criterion(boolean forceSerialization) {

		this.forceSerialization = forceSerialization;
		this.index = null;
	}

	public void setIndex(int index) {

		isTrue(index > 0, "Invalid index: " + index);
		this.index = index;
	}
}
