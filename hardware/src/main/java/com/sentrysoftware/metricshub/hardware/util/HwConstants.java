package com.sentrysoftware.metricshub.hardware.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HwConstants {

	// Hardware Metrics

	public static final String HW_ENERGY_DISK_CONTROLLER_METRIC = "hw.energy{hw.type=\"disk_controller\"}";
	public static final String HW_POWER_DISK_CONTROLLER_METRIC = "hw.power{hw.type=\"disk_controller\"}";

	public static final String HW_ENERGY_FAN_METRIC = "hw.energy{hw.type=\"fan\"}";
	public static final String HW_POWER_FAN_METRIC = "hw.power{hw.type=\"fan\"}";

	public static final String HW_ENERGY_ROBOTICS_METRIC = "hw.energy{hw.type=\"robotics\"}";
	public static final String HW_POWER_ROBOTICS_METRIC = "hw.power{hw.type=\"robotics\"}";

	public static final String HW_ENERGY_TAPE_DRIVE_METRIC = "hw.energy{hw.type=\"tape_drive\"}";
	public static final String HW_POWER_TAPE_DRIVE_METRIC = "hw.power{hw.type=\"tape_drive\"}";

	public static final String HW_ENERGY_MEMORY_METRIC = "hw.energy{hw.type=\"memory\"}";
	public static final String HW_POWER_MEMORY_METRIC = "hw.power{hw.type=\"memory\"}";

	public static final String HW_ENERGY_PHYSICAL_DISK_METRIC = "hw.energy{hw.type=\"physical_disk\"}";
	public static final String HW_POWER_PHYSICAL_DISK_METRIC = "hw.power{hw.type=\"physical_disk\"}";

	public static final String HW_ENERGY_NETWORK_METRIC = "hw.energy{hw.type=\"network\"}";
	public static final String HW_POWER_NETWORK_METRIC = "hw.power{hw.type=\"network\"}";

	public static final String HW_HOST_MEASURED_POWER = "hw.host.power{quality=\"measured\"}";
	public static final String HW_HOST_MEASURED_ENERGY = "hw.host.energy{quality=\"measured\"}";
	public static final String HW_HOST_ESTIMATED_POWER = "hw.host.power{quality=\"estimated\"}";
	public static final String HW_HOST_ESTIMATED_ENERGY = "hw.host.energy{quality=\"estimated\"}";

	public static final String HW_ENERGY_VM_METRIC = "hw.energy{hw.type=\"vm\"}";
	public static final String HW_POWER_VM_METRIC = "hw.power{hw.type=\"vm\"}";
	public static final String POWER_SOURCE_ID_ATTRIBUTE = "__power_source_id";
	public static final String HW_VM_POWER_SHARE_METRIC = "hw.vm.power_ratio.raw_power_share";
	public static final String HW_VM_POWER_STATE_METRIC = "hw.vm.power_state";

	public static final String HW_ENCLOSURE_POWER = "hw.enclosure.power";
	public static final String HW_ENCLOSURE_ENERGY = "hw.enclosure.energy";
	public static final String HW_ENERGY_CPU_METRIC = "hw.energy{hw.type=\"cpu\"}";
	public static final String HW_POWER_CPU_METRIC = "hw.power{hw.type=\"cpu\"}";

	public static final String HW_HOST_CPU_THERMAL_DISSIPATION_RATE = "__hw.host.cpu.thermal_dissipation_rate";
}
