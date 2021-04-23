package com.sentrysoftware.matrix.engine.strategy.matsya;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MatsyaStringResult implements IMatsyaQueryResult {

	private String data;
}
