package com.sentrysoftware.metricshub.engine.connector.model.identity;

import static com.fasterxml.jackson.annotation.Nulls.FAIL;
import static com.fasterxml.jackson.annotation.Nulls.SKIP;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.sentrysoftware.metricshub.engine.connector.deserializer.custom.ConnectionTypeSetDeserializer;
import com.sentrysoftware.metricshub.engine.connector.deserializer.custom.DeviceKindSetDeserializer;
import com.sentrysoftware.metricshub.engine.connector.deserializer.custom.NonBlankDeserializer;
import com.sentrysoftware.metricshub.engine.connector.deserializer.custom.SupersedesDeserializer;
import com.sentrysoftware.metricshub.engine.connector.model.common.DeviceKind;
import com.sentrysoftware.metricshub.engine.connector.model.identity.criterion.Criterion;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

@Data
@NoArgsConstructor
public class Detection implements Serializable {

	private static final long serialVersionUID = 1L;

	@JsonDeserialize(using = ConnectionTypeSetDeserializer.class)
	@JsonSetter(nulls = SKIP)
	private Set<ConnectionType> connectionTypes = new HashSet<>();

	private boolean disableAutoDetection;

	@JsonDeserialize(using = NonBlankDeserializer.class)
	private String onLastResort;

	@JsonDeserialize(using = DeviceKindSetDeserializer.class)
	@JsonSetter(nulls = FAIL)
	@NonNull
	private Set<DeviceKind> appliesTo;

	@JsonDeserialize(using = SupersedesDeserializer.class)
	@JsonSetter(nulls = SKIP)
	private Set<String> supersedes = new HashSet<>();

	private List<Criterion> criteria = new ArrayList<>();

	@Builder
	@JsonCreator
	public Detection(
		@JsonProperty("connectionTypes") Set<ConnectionType> connectionTypes,
		@JsonProperty("disableAutoDetection") boolean disableAutoDetection,
		@JsonProperty("onLastResort") String onLastResort,
		@JsonProperty(value = "appliesTo", required = true) @NonNull Set<DeviceKind> appliesTo,
		@JsonProperty("supersedes") Set<String> supersedes,
		@JsonProperty("criteria") List<Criterion> criteria
	) {
		this.connectionTypes =
			connectionTypes == null ? new HashSet<>(Collections.singleton(ConnectionType.LOCAL)) : connectionTypes;
		this.disableAutoDetection = disableAutoDetection;
		this.onLastResort = onLastResort;
		this.appliesTo = appliesTo;
		this.supersedes = supersedes == null ? new HashSet<>() : supersedes;
		this.criteria = criteria == null ? new ArrayList<>() : criteria;
	}
}
