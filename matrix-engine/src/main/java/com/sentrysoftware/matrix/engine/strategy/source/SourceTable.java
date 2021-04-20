package com.sentrysoftware.matrix.engine.strategy.source;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SourceTable {
	private static final SourceTable EMPTY = SourceTable.builder().build();

	private String stringResult; // for the moment we keep the String possibility

	@Default
	private List<List<String>> table = new ArrayList<>();
	
	@Default
	private List<String> headers = new ArrayList<>();

	/**
	 * Return the List representation on a CSV file :
	 * a;b;c; 
	 * a1;b1;c1; 
	 * =>
	 * [[a,b,c],[a1,b1,c1]]
	 * 
	 * @param sourceCSV
	 * @return 
	 */
	public static List<List<String>> stringToSource(String sourceCSV) {
		List<List<String>> result = new ArrayList<>();
		if (null != sourceCSV) {
			for (String line : Arrays.asList(sourceCSV.split("\n"))) {
				result.add(Arrays.asList(line.split(";")));
			}
		}
		return result;
	}

	/**
	 * Transform a List<List<String>> into a String that represents a CSV file
	 * [[a,b,c],[a1,b1,c1]]
	 * =>
	 * a;b;c;
	 * a1;b1;c1; 
	 * @param data
	 * @return
	 */
	public static String sourceToString(List<List<String>> data) {
		StringBuilder csv = new StringBuilder();
		for(List<String> dataElt : data) {
			csv.append(String.join(";", dataElt)).append(";\n");
		}
		return csv.toString().trim();
	}

	public static SourceTable empty() {
		return EMPTY;
	}
}
