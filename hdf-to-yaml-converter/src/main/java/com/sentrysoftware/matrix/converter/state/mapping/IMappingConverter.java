package com.sentrysoftware.matrix.converter.state.mapping;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;

public interface IMappingConverter {


	/**
	 * Convert the HDF collect property into a connector YAML collect property
	 * 
	 * @param key   HDF parameter key to be converted.
	 * @param value The value to be set in a new {@link TextNode}.<br>
	 *              Depending on the conversion specifications this value may change.
	 * @param node  The node on which we want to set the key-value pair
	 */
	void convertCollectProperty(final String key, final String value, final JsonNode node);

	/**
	 * Post conversion of the HDF discovery properties into YAML properties <br>
	 * This method should be called at the end of the conversion because
	 * discovery attributes are dependent
	 * 
	 * @param mapping The mapping object node defining the <em>attributes</em> section
	 */
	void postConvertDiscoveryProperties(final JsonNode mapping);
}
