package com.sentrysoftware.matrix.connector.model.monitor.task;

import static com.fasterxml.jackson.annotation.Nulls.SKIP;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sentrysoftware.matrix.connector.deserializer.custom.CaseInsensitiveTreeMapDeserializer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Mapping implements Serializable {

	private static final long serialVersionUID = 1L;

	private String source;

	private String deviceId;

	@Default
	@JsonDeserialize(using = CaseInsensitiveTreeMapDeserializer.class)
	@JsonSetter(nulls = SKIP)
	private Map<String, String> attributes = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

	@Default
	@JsonDeserialize(using = CaseInsensitiveTreeMapDeserializer.class)
	@JsonSetter(nulls = SKIP)
	private Map<String, String> metrics = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

	@Default
	@JsonDeserialize(using = CaseInsensitiveTreeMapDeserializer.class)
	@JsonSetter(nulls = SKIP)
	private Map<String, String> conditionalCollection = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

}