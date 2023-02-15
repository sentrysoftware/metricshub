package com.sentrysoftware.matrix.connector.deserializer.custom;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLParser;
import com.sentrysoftware.matrix.common.helpers.JsonHelper;
import com.sentrysoftware.matrix.connector.model.common.CustomConcatMethod;
import com.sentrysoftware.matrix.connector.model.common.EntryConcatMethod;
import com.sentrysoftware.matrix.connector.model.common.ExecuteForEachEntryOf;

@ExtendWith(MockitoExtension.class)
class EntryConcatMethodDeserializerTest {

	private static final String SOURCE_REF = "$pre.source1";
	private static final String EXECUTE_FOR_EACH_ENTRY_OF_YAML = """
			source: $pre.source1
			concatMethod: ReplaceMe
			""";

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper(new YAMLFactory());

	private static final String REPLACE_ME = "ReplaceMe";

	private static final EntryConcatMethodDeserializer DESERIALIZER = new EntryConcatMethodDeserializer();

	@Mock
	private YAMLParser yamlParser;

	@Test
	void testNull() throws IOException {

		{
			assertEquals(null, DESERIALIZER.deserialize(null, null));
		}

		{
			doReturn(false).when(yamlParser).isExpectedStartObjectToken();
			doReturn(null).when(yamlParser).getValueAsString();
			assertNull(DESERIALIZER.deserialize(yamlParser, null));
		}
		{
			doReturn(true).when(yamlParser).isExpectedStartObjectToken();
			doReturn(null).when(yamlParser).readValueAs(CustomConcatMethod.class);
			assertNull(DESERIALIZER.deserialize(yamlParser, null));
		}
	}

	@Test
	void testCustomConcatMethodValue() throws IOException {
		final String yaml = """
				
				  concatStart: "<tag>"
				  concatEnd: "</tag>"
				""";

		final ExecuteForEachEntryOf executeForEachEntry = JsonHelper.deserialize(
			OBJECT_MAPPER,
			new ByteArrayInputStream(EXECUTE_FOR_EACH_ENTRY_OF_YAML.replace(REPLACE_ME, yaml).getBytes()),
			ExecuteForEachEntryOf.class
		);
		assertEquals(
			ExecuteForEachEntryOf
				.builder()
				.source(SOURCE_REF)
				.concatMethod(new CustomConcatMethod("<tag>", "</tag>"))
				.build(),
			executeForEachEntry
		);
	}

	@Test
	void testConcatMethodValueNull() throws IOException {

		final ExecuteForEachEntryOf executeForEachEntry = JsonHelper.deserialize(
			OBJECT_MAPPER,
			new ByteArrayInputStream(EXECUTE_FOR_EACH_ENTRY_OF_YAML.replace(REPLACE_ME, "").getBytes()),
			ExecuteForEachEntryOf.class
		);
		assertEquals(
			ExecuteForEachEntryOf
				.builder()
				.source(SOURCE_REF)
				.concatMethod(EntryConcatMethod.LIST)
				.build(),
			executeForEachEntry
		);
	}

	@Test
	void testEntryConcatMethodJsonArray() throws IOException {
		final ExecuteForEachEntryOf entry = JsonHelper.deserialize(
			OBJECT_MAPPER,
			new ByteArrayInputStream(EXECUTE_FOR_EACH_ENTRY_OF_YAML.replace(REPLACE_ME, "JSONArray").getBytes()),
			ExecuteForEachEntryOf.class
		);
		assertEquals(
			ExecuteForEachEntryOf
				.builder()
				.source(SOURCE_REF)
				.concatMethod(EntryConcatMethod.JSON_ARRAY)
				.build(),
			entry
		);
	}

	@Test
	void testEntryConcatMethodList() throws IOException {
		final ExecuteForEachEntryOf executeForEachEntry = JsonHelper.deserialize(
			OBJECT_MAPPER,
			new ByteArrayInputStream(EXECUTE_FOR_EACH_ENTRY_OF_YAML.replace(REPLACE_ME, "list").getBytes()),
			ExecuteForEachEntryOf.class
		);
		assertEquals(
			ExecuteForEachEntryOf
				.builder()
				.source(SOURCE_REF)
				.concatMethod(EntryConcatMethod.LIST)
				.build(),
			executeForEachEntry
		);
			
	}

	@Test
	void testEntryConcatMethodJsonArrayExtended() throws IOException {
		final ExecuteForEachEntryOf executeForEachEntry = JsonHelper.deserialize(
			OBJECT_MAPPER,
			new ByteArrayInputStream(EXECUTE_FOR_EACH_ENTRY_OF_YAML.replace(REPLACE_ME, "JSONArrayExtended").getBytes()),
			ExecuteForEachEntryOf.class
		);
		assertEquals(
			ExecuteForEachEntryOf
				.builder()
				.source(SOURCE_REF)
				.concatMethod(EntryConcatMethod.JSON_ARRAY_EXTENDED)
				.build(),
			executeForEachEntry
		);
	}

	@Test
	void testEntryConcatMethodUnknown() throws IOException {
		final Executable executable = () -> JsonHelper.deserialize(
			OBJECT_MAPPER,
			new ByteArrayInputStream(EXECUTE_FOR_EACH_ENTRY_OF_YAML.replace(REPLACE_ME, "Unknown").getBytes()),
			ExecuteForEachEntryOf.class
		);
		assertThrows(
			InvalidFormatException.class,
			executable
		);
	}

}
