package com.sentrysoftware.matrix.engine.strategy.source;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;

public class SourceTableTest {

	@Test
	public static void stringToSourceTest() {
		String csvString = "a,b,c\nd,e,f\ng,h,i";
		List<List<String>> excpectedData = new ArrayList<>();
		excpectedData.add(Arrays.asList("a","b","c"));
		excpectedData.add(Arrays.asList("d","e","f"));
		excpectedData.add(Arrays.asList("g", "h", "i"));
		
		List<List<String>> fromStringSourceResult = SourceTable.stringToSource(csvString);
		assertTrue(excpectedData.size() == fromStringSourceResult.size() 
				&& excpectedData.containsAll(fromStringSourceResult) 
				&& excpectedData.containsAll(fromStringSourceResult));
	}
	
	@Test
	public static void sourceToStringTest() {
		String expected = "a,b,c\nd,e,f\ng,h,i";
		List<List<String>> source = new ArrayList<>();
		source.add(Arrays.asList("a","b","c"));
		source.add(Arrays.asList("d","e","f"));
		source.add(Arrays.asList("g", "h", "i"));
		
		String sourceToStringResult = SourceTable.sourceToString(source);
		assertEquals(expected, sourceToStringResult);
	}
}
