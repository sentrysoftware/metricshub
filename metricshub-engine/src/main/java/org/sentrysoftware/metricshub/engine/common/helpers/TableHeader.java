package org.sentrysoftware.metricshub.engine.common.helpers;

import lombok.Getter;
import org.springframework.util.Assert;

/**
 * Represents the header of a table, including a title and the data type of the text it contains.
 */
@Getter
public class TableHeader {

	private final String title;
	private final TextDataType type;

	/**
	 * Constructs a new {@code TableHeader} with the specified title and text data type.
	 *
	 * @param title The title of the table header.
	 * @param type The data type of the text in the table header.
	 * @throws IllegalArgumentException if either {@code title} or {@code type} is {@code null}.
	 */
	public TableHeader(String title, TextDataType type) {
		Assert.notNull(title, "title cannot be null.");
		Assert.notNull(type, "type cannot be null.");

		this.title = title;
		this.type = type;
	}
}
