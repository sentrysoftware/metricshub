package com.sentrysoftware.matrix.strategy.source;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SourceTable {
	private String rawData;
	private List<List<String>> table;
}
