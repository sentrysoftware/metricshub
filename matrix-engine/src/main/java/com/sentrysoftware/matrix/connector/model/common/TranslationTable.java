package com.sentrysoftware.matrix.connector.model.common;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TranslationTable implements Serializable {

	private static final long serialVersionUID = 1722484394465097003L;

	private String name;
	@Default
	private Map<String, String> tanslations = new HashMap<>();
}
