package com.sentrysoftware.matrix.connector.parser.state.detection.os;

import static com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants.COMMA;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import com.sentrysoftware.matrix.connector.model.common.OSType;
import com.sentrysoftware.matrix.connector.model.detection.criteria.os.OS;
import com.sentrysoftware.matrix.connector.parser.state.AbstractStateParser;

public abstract class OsProcessor extends AbstractStateParser {

	protected static final String OS_TYPE_VALUE = "OS";

	@Override
	public Class<OS> getType() {
		return OS.class;
	}

	@Override
	public String getTypeValue() {
		return OS_TYPE_VALUE;
	}

	/**
	 * Parse the given value to extract a set of {@link OSType} instances
	 * 
	 * @param value the value to parse
	 * @return Set of {@link OSType} instances
	 */
	protected Set<OSType> getOsTypes(final String value) {
		Set<OSType> osTypes = new HashSet<>();
		try {
			Arrays.stream(value.split(COMMA))
			.forEach(os -> osTypes.add(OSType.valueOf(os.trim().toUpperCase())));

			return osTypes;
		} catch (Exception e) {
			throw new IllegalStateException(this.getClass().getSimpleName() + " parse: invalid OS type in " + value + ": " + e.getMessage());
		}
	}
}
