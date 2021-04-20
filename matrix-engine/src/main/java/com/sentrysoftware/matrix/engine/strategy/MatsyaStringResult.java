package com.sentrysoftware.matrix.engine.strategy;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MatsyaStringResult implements IMatsyaQueryResult {

	String data;
}
