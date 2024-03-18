package org.sentrysoftware.metricshub.engine.extension;

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

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;

/**
 * Manages and aggregates various types of extensions used within MetricsHub.
 * This class acts as a central point for accessing and managing protocol
 * extensions, strategy provider extensions, and connector store provider
 * extensions.
 * <p>
 * The {@link ExtensionManager} is designed to be flexible and extensible,
 * supporting various types of extensions that can be added or removed as
 * needed. It uses the Builder pattern to simplify instantiation and setup.
 * </p>
 */
@Data
@AllArgsConstructor
@Builder(setterPrefix = "with")
public class ExtensionManager {

	@Default
	private List<IProtocolExtension> protocolExtensions = new ArrayList<>();

	@Default
	private List<IStrategyProviderExtension> strategyProviderExtensions = new ArrayList<>();

	@Default
	private List<IConnectorStoreProviderExtension> connectorStoreProviderExtensions = new ArrayList<>();

	/**
	 * Create a new empty instance of the Extension Manager.
	 * @return a new instance of {@link ExtensionManager}.
	 */
	public static ExtensionManager empty() {
		return ExtensionManager.builder().build();
	}
}
