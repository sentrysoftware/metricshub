package com.sentrysoftware.matrix.connector.deserializer.custom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.CopySource;
import com.sentrysoftware.matrix.connector.model.monitor.task.source.Source;

@ExtendWith(MockitoExtension.class)
class PreDeserializerTest {

	private static final PreDeserializer PRE_DESERIALIZER = new PreDeserializer();

	@Mock
	private YAMLParser yamlParser;

	@Test
	void testNull() throws IOException {

		{
			assertEquals(new LinkedHashMap<>(), PRE_DESERIALIZER.deserialize(null, null));
		}

		{
			assertEquals(new LinkedHashMap<>(), PRE_DESERIALIZER.deserialize(yamlParser, null));
		}
		{
			doReturn(null).when(yamlParser).readValueAs(any(TypeReference.class));
			assertEquals(new LinkedHashMap<>(), PRE_DESERIALIZER.deserialize(yamlParser, null));
		}
		{
			doReturn(new LinkedHashMap<>()).when(yamlParser).readValueAs(any(TypeReference.class));
			assertEquals(new LinkedHashMap<>(), PRE_DESERIALIZER.deserialize(yamlParser, null));
		}
	}

	@Test
	void testBadValue() throws IOException {
		{
			Map<String, Source> pre = new LinkedHashMap<>();
			pre.put(null, new CopySource());

			doReturn(pre).when(yamlParser).readValueAs(any(TypeReference.class));
			assertThrows(IOException.class, () -> PRE_DESERIALIZER.deserialize(yamlParser, null));
		}

		{
			Map<String, Source> pre = new LinkedHashMap<>();
			pre.put(" ", new CopySource());

			doReturn(pre).when(yamlParser).readValueAs(any(TypeReference.class));
			assertThrows(IOException.class, () -> PRE_DESERIALIZER.deserialize(yamlParser, null));
		}
	}
}
