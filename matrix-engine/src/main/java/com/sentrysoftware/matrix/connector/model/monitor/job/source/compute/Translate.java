package com.sentrysoftware.matrix.connector.model.monitor.job.source.compute;

import com.sentrysoftware.matrix.connector.model.common.TranslationTable;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Translate implements Compute {

	private static final long serialVersionUID = -8078271095301494969L;

	private TranslationTable translationTable;
}
