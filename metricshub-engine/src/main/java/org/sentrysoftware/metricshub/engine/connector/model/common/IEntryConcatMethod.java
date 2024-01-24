package org.sentrysoftware.metricshub.engine.connector.model.common;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.io.Serializable;

/**
 * Represents an interface for entry concatenation methods used in connector models.
 * Implementing classes should provide methods to create a copy of the instance and retrieve a description.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.DEDUCTION, defaultImpl = EntryConcatMethod.class)
@JsonSubTypes(@JsonSubTypes.Type(value = CustomConcatMethod.class))
public interface IEntryConcatMethod extends Serializable {
	/**
	 * Creates a deep copy of the current {@link IEntryConcatMethod} instance.
	 *
	 * @return A new instance of {@link IEntryConcatMethod} representing a copy of the original instance.
	 */
	IEntryConcatMethod copy();

	/**
	 * Gets a human-readable description of the entry concatenation method.
	 *
	 * @return A string representing the description of the entry concatenation method.
	 */
	String getDescription();
}
