package com.sentrysoftware.matrix.connector.parser.state.detection.os;

import static com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants.COMMA;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.sentrysoftware.matrix.connector.model.common.OsType;
import com.sentrysoftware.matrix.connector.model.detection.criteria.os.Os;
import com.sentrysoftware.matrix.connector.parser.state.AbstractStateParser;

public abstract class OsProcessor extends AbstractStateParser {

	protected static final String OS_TYPE_VALUE = "OS";

	@Override
	public Class<Os> getType() {
		return Os.class;
	}

	@Override
	public String getTypeValue() {
		return OS_TYPE_VALUE;
	}

	/**
	 * Parse the given value to extract a set of {@link OsType} instances
	 * 
	 * @param value the value to parse
	 * @return Set of {@link OsType} instances
	 */
	protected Set<OsType> getOsTypes(final String value) {
		Set<OsType> osTypes = new HashSet<>();
		try {
			Arrays.stream(value.split(COMMA))
			.forEach(os -> osTypes.add(OsType.valueOf(os.trim().toUpperCase())));

			return osTypes;
		} catch (Exception e) {
			throw new IllegalStateException(this.getClass().getSimpleName() + " parse: invalid OS type in " + value + ": " + e.getMessage());
		}
	}
}
