package org.sentrysoftware.metricshub.engine.connector.model.monitor.task;

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

import static com.fasterxml.jackson.annotation.Nulls.SKIP;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sentrysoftware.metricshub.engine.connector.deserializer.custom.NonBlankInLinkedHashSetDeserializer;
import org.sentrysoftware.metricshub.engine.connector.deserializer.custom.SourcesDeserializer;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.source.Source;

@Data
@NoArgsConstructor
public abstract class AbstractMonitorTask implements Serializable {

	private static final long serialVersionUID = 1L;

	@JsonSetter(nulls = SKIP)
	@JsonDeserialize(using = SourcesDeserializer.class)
	protected Map<String, Source> sources = new LinkedHashMap<>(); // NOSONAR LinkHashMap is Serializable

	protected Mapping mapping;

	@JsonDeserialize(using = NonBlankInLinkedHashSetDeserializer.class)
	protected Set<String> executionOrder = new LinkedHashSet<>(); // NOSONAR LinkedHashSet is Serializable

	protected List<Set<String>> sourceDep = new ArrayList<>(); // NOSONAR ArrayList is Serializable

	protected AbstractMonitorTask(
		final Map<String, Source> sources,
		final Mapping mapping,
		final Set<String> executionOrder
	) {
		this.sources = sources == null ? new LinkedHashMap<>() : sources;
		this.mapping = mapping;
		this.executionOrder = executionOrder != null ? executionOrder : new LinkedHashSet<>();
		this.sourceDep = new ArrayList<>();
	}
}
