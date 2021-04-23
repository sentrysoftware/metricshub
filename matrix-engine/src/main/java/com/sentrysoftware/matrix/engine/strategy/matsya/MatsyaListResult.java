package com.sentrysoftware.matrix.engine.strategy.matsya;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MatsyaListResult implements IMatsyaQueryResult {

	private List<List<String>> data;

}
