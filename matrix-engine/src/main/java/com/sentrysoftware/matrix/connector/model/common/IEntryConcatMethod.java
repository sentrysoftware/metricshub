package com.sentrysoftware.matrix.connector.model.common;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;


@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION, defaultImpl = EntryConcatMethod.class) 
@JsonSubTypes(@JsonSubTypes.Type(value = CustomConcatMethod.class))
public interface IEntryConcatMethod extends Serializable {

	IEntryConcatMethod copy();

	String getDescription();
}
