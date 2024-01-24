package org.sentrysoftware.metricshub.engine.common.helpers;

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

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum KnownMonitorType {
	CONNECTOR("connector"),
	HOST("host"),
	BATTERY("battery"),
	BLADE("blade"),
	CPU("cpu"),
	DISK_CONTROLLER("disk_controller"),
	ENCLOSURE("enclosure"),
	FAN("fan"),
	GPU("gpu"),
	LED("led"),
	LOGICAL_DISK("logical_disk"),
	LUN("lun"),
	MEMORY("memory"),
	NETWORK("network"),
	OTHER_DEVICE("other_device"),
	PHYSICAL_DISK("physical_disk"),
	POWER_SUPPLY("power_supply"),
	ROBOTICS("robotics"),
	TAPE_DRIVE("tape_drive"),
	TEMPERATURE("temperature"),
	VM("vm"),
	VOLTAGE("voltage");

	private String key;
}
