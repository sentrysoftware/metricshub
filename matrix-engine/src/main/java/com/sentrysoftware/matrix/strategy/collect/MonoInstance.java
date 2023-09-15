package com.sentrysoftware.matrix.strategy.collect;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true)
public class MonoInstance extends AbstractCollect {

	private String monitorId;

	@Override
	public void collect() {
		// TODO Auto-generated method stub

	}
}
