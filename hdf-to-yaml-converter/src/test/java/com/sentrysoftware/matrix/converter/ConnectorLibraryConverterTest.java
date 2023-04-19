package com.sentrysoftware.matrix.converter;

import static org.junit.jupiter.api.Assertions.*;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

import javax.json.JsonException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.sentrysoftware.matrix.common.helpers.JsonHelper;

class ConnectorLibraryConverterTest {

	private static final String HDF_DIRECTORY = "src/test/resources/hdf";
	private static final String YAML_DIRECTORY = "src/test/resources/yaml";

	@TempDir
	private Path tempDir;

	@Test
	void testProcess() throws IOException {
		final ConnectorLibraryConverter processor = new ConnectorLibraryConverter(Path.of(HDF_DIRECTORY), tempDir);
		processor.process();
		final File file = tempDir.resolve("DellOpenManage.yaml").toFile();
		assertTrue(file.exists());
	}

	@Test
	void testProcessNode() throws IOException { 
		final ObjectMapper mapper = JsonHelper.buildYamlMapper();
		
		final ConnectorLibraryConverter processor = new ConnectorLibraryConverter(Path.of(HDF_DIRECTORY), tempDir);
		processor.process();

		final Path expectedPath = Path.of(YAML_DIRECTORY, "DellOpenManage.yaml");

		final File inputFile = tempDir.resolve("DellOpenManage.yaml").toFile();
		final File expectedFile = expectedPath.toFile();
		
		assertTrue(inputFile.exists());
		assertTrue(expectedFile.exists());

		final JsonNode expectedNode = mapper.readTree(expectedFile);
		JsonNode inputNode = mapper.readTree(inputFile);

		removeJsonField((ObjectNode) inputNode);

		// Comparing objects (no comments involved)
		assertEquals(expectedNode.toPrettyString(), inputNode.toPrettyString());
	}

	@Test
	void testProcessComments() throws Exception {
		final ConnectorLibraryConverter processor = new ConnectorLibraryConverter(Path.of(HDF_DIRECTORY), tempDir);
		processor.process();

		final Path expectedPath = Path.of(YAML_DIRECTORY, "DellOpenManage.yaml");

		final File inputFile = tempDir.resolve("DellOpenManage.yaml").toFile();
		final File expectedFile = expectedPath.toFile();
		
		assertTrue(inputFile.exists());
		assertTrue(expectedFile.exists());

		final List<String> inputLines = Files.readAllLines(Path.of(inputFile.getAbsolutePath()));
		final List<String> expectedLines = Files.readAllLines(expectedPath);

		// compares line by line. includes comments
		assertEquals(expectedLines, inputLines);
	}

	private static void removeJsonField(ObjectNode obj) throws JsonException{
		obj.remove("_comment");
	
		Iterator<String> it = obj.fieldNames();
		while(it.hasNext()){
			String key = it.next();
			Object childObj = obj.get(key);
			if(childObj instanceof ArrayNode){
				ArrayNode arrayChildObjs =((ArrayNode)childObj);
				int size = arrayChildObjs.size();
				for(int i=0;i<size;i++){
					if (arrayChildObjs.get(i) instanceof ObjectNode){
						removeJsonField((ObjectNode)arrayChildObjs.get(i));
					}
				}
			}
			if(childObj instanceof ObjectNode){
				removeJsonField((ObjectNode)childObj);
			}
		}
	}
}
