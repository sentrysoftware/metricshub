package org.sentrysoftware.metricshub.engine.common.helpers;

import lombok.Getter;
import org.springframework.util.Assert;

@Getter
public class TableHeader {

	private final String title;
	private final TextDataType type;

	public TableHeader(String title, TextDataType type) {
		Assert.notNull(title, "title cannot be null.");
		Assert.notNull(type, "type cannot be null.");

		this.title = title;
		this.type = type;
	}
}
