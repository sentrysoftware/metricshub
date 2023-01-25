package com.sentrysoftware.matrix.connector.model.common;

import java.io.Serializable;

public interface IEntryConcatMethod extends Serializable {

	IEntryConcatMethod copy();

	String getDescription();
}
