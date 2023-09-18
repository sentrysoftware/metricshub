package com.sentrysoftware.matrix.connector.model.common;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;

@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION, defaultImpl = EntryConcatMethod.class)
@JsonSubTypes(@JsonSubTypes.Type(value = CustomConcatMethod.class))
public interface IEntryConcatMethod extends Serializable {
	IEntryConcatMethod copy();

	String getDescription();
}
