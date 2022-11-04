package com.sentrysoftware.hardware.agent.deserialization;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.sentrysoftware.hardware.agent.dto.HostConfigurationDto;
import com.sentrysoftware.hardware.agent.dto.HostGroupConfigurationDto;
import com.sentrysoftware.hardware.agent.dto.MultiHostsConfigurationDto;

public class HostsDeserializer extends JsonDeserializer<Set<HostConfigurationDto>> {

	private static final String HOST_GROUP_FIELD = "hostGroup";
	private static final String HOST_FIELD = "host";

	@Override
	public Set<HostConfigurationDto> deserialize(JsonParser parser, DeserializationContext context)
			throws IOException {
		final Set<HostConfigurationDto> hosts = new HashSet<>();
		
		final ObjectCodec codec = parser.getCodec();
		
		final JsonNode hostsNode = codec.readTree(parser);
		
		if (hostsNode == null || hostsNode.isNull()) {
			return hosts;
		}
		
		if (!hostsNode.isArray()) {
			throw new IllegalStateException("The `hosts` attribute is not an array. The hosts cannot be parsed.");
		}

		MultiHostsConfigurationDto multiHostsConfigurationDto = (MultiHostsConfigurationDto) parser.getParsingContext().getCurrentValue();

		for (JsonNode configNode : hostsNode) {
			validateConfigNode(configNode);
			JsonNode possibleHost = configNode.get(HOST_FIELD);
			if (possibleHost != null) {
				hosts.add(codec.treeToValue(configNode, HostConfigurationDto.class));
			} else {
				final HostGroupConfigurationDto hostGroup = codec.treeToValue(configNode, HostGroupConfigurationDto.class);
				hosts.addAll(hostGroup.toHosts());
				multiHostsConfigurationDto.getHostGroups().add(hostGroup);
			}
		}
		
		return hosts;
	}
	
	/** Validate the given JsonNode doesn't contain both host and host group configurations.
	 * 
	 * @param configNode
	 * @throws IOException 
	 */
	private void validateConfigNode(JsonNode configNode) throws IOException {
		if (configNode.get(HOST_FIELD) == null && configNode.get(HOST_GROUP_FIELD) == null) {
			throw new IOException(String.format("Neither `host` or `hostGroup` is defined for the host configuration: %s", configNode.toString()));
		}
		
		if (configNode.get(HOST_FIELD) != null && configNode.get(HOST_GROUP_FIELD) != null) {
			throw new IOException(String.format("Host configuration cannot contain both `hosts` and `hostGroup` fields: %s", configNode.toString()));
		}
	}
}
