package org.sentrysoftware.metricshub.engine.connector.model.monitor;

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
import static org.sentrysoftware.metricshub.engine.common.helpers.MetricsHubConstants.DEFAULT_KEYS;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import java.util.LinkedHashSet;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.sentrysoftware.metricshub.engine.connector.model.monitor.task.Simple;

/**
 * Represents a monitor job associated with a simple monitoring task.
 *
 * <p>
 * This class implements the {@link MonitorJob} interface and is used to encapsulate the configuration and details of a monitoring job that involves a
 * simple monitoring task.
 * </p>
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SimpleMonitorJob implements MonitorJob {

	private static final long serialVersionUID = 1L;

	/**
	 * The monitor job keys needed to build the monitor id
	 */
	@Default
	@JsonProperty("keys")
	@JsonSetter(nulls = SKIP)
	private LinkedHashSet<String> keys = new LinkedHashSet<>(DEFAULT_KEYS);

	/**
	 * The simple task associated with this monitor job.
	 */
	private Simple simple;
}
