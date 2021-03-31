package com.sentrysoftware.matrix.connector.model.monitor.job.source.compute;

import java.util.ArrayList;
import java.util.List;

import com.sentrysoftware.matrix.connector.model.common.TranslationTable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder.Default;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PerBitTranslation implements Compute {

	private static final long serialVersionUID = -7456243256660647476L;

	private Integer column;
	@Default
	private List<Integer> bitList = new ArrayList<>();
	private TranslationTable bitTranslationTable;
	
}
