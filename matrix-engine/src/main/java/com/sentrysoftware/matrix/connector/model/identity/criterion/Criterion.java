package com.sentrysoftware.matrix.connector.model.identity.criterion;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public abstract class Criterion implements Serializable {

	private static final long serialVersionUID = 1L;

	protected String type;
	protected boolean forceSerialization;

}
