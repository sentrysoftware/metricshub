package com.sentrysoftware.matrix.connector.model.common;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.HashMap;
import java.util.Map;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReferenceTranslationTable implements ITranslationTable {

	private static final long serialVersionUID = 1L;

	@Default
	@JsonIgnore
	private Map<String, String> translations = new HashMap<>();

	private String name;

	@Override
	public ReferenceTranslationTable copy() {
		return ReferenceTranslationTable
			.builder()
			.translations(
				translations == null
					? null
					: translations
						.entrySet()
						.stream()
						.collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (k1, k2) -> k2, HashMap::new))
			)
			.build();
	}

	@Override
	public void update(UnaryOperator<String> updater) {
		if (translations != null) {
			translations.replaceAll((key, val) -> updater.apply(val));
		}
	}

	@JsonAnySetter
	public void setTranslation(String key, String value) {
		translations.put(key, value);
	}

	@JsonAnyGetter
	public Map<String, String> getTranslations() {
		return this.translations;
	}
}
