package org.sentrysoftware.metricshub.engine.connector.model.identity;

/*-
 * ╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲
 * MetricsHub Engine
 * ჻჻჻჻჻჻
 * Copyright 2023 - 2024 Sentry Software
 * ჻჻჻჻჻჻
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * ╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱╲╱
 */

import static com.fasterxml.jackson.annotation.Nulls.FAIL;
import static com.fasterxml.jackson.annotation.Nulls.SKIP;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
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
import org.sentrysoftware.metricshub.engine.connector.deserializer.custom.ConnectionTypeSetDeserializer;
import org.sentrysoftware.metricshub.engine.connector.deserializer.custom.DeviceKindSetDeserializer;
import org.sentrysoftware.metricshub.engine.connector.deserializer.custom.NonBlankDeserializer;
import org.sentrysoftware.metricshub.engine.connector.deserializer.custom.SupersedesDeserializer;
import org.sentrysoftware.metricshub.engine.connector.model.common.DeviceKind;
import org.sentrysoftware.metricshub.engine.connector.model.identity.criterion.Criterion;

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

	@JsonSetter(nulls = SKIP)
	private Set<String> tags = new HashSet<>();

	@Builder
	@JsonCreator
	public Detection(
		@JsonProperty("connectionTypes") Set<ConnectionType> connectionTypes,
		@JsonProperty("disableAutoDetection") boolean disableAutoDetection,
		@JsonProperty("onLastResort") String onLastResort,
		@JsonProperty(value = "appliesTo", required = true) @NonNull Set<DeviceKind> appliesTo,
		@JsonProperty("supersedes") Set<String> supersedes,
		@JsonProperty("criteria") List<Criterion> criteria,
		@JsonProperty("tags") Set<String> tags
	) {
		this.connectionTypes =
			connectionTypes == null ? new HashSet<>(Collections.singleton(ConnectionType.LOCAL)) : connectionTypes;
		this.disableAutoDetection = disableAutoDetection;
		this.onLastResort = onLastResort;
		this.appliesTo = appliesTo;
		this.supersedes = supersedes == null ? new HashSet<>() : supersedes;
		this.criteria = criteria == null ? new ArrayList<>() : criteria;
		this.tags = tags == null ? new HashSet<>() : tags;
	}
}
