package com.sentrysoftware.matrix.common;

public class Constants {

	public static final String FAN_SPEED_METRIC = "hw.fan.speed";
	public static final String FAN_SPEED_RATIO_METRIC = "hw.fan.speed_ratio";
	public static final String FAN_POWER_METRIC = "hw.power{hw.type=\"fan\"}";
	public static final String FAN_ENERGY_METRIC = "hw.energy{hw.type=\"fan\"}";
	public static final String ROBOTICS_MOVE_COUNT_METRIC = "hw.robotics.moves";
	public static final String ROBOTICS_POWER_METRIC = "hw.power{hw.type=\"robotics\"}";
	public static final String ROBOTICS_ENERGY_METRIC = "hw.energy{hw.type=\"robotics\"}";
	public static final String TAPE_DRIVE_MOUNT_COUNT_METRIC = "hw.tape_drive.operations{type=\"mount\"}";
	public static final String TAPE_DRIVE_UNMOUNT_COUNT_METRIC = "hw.tape_drive.operations{type=\"unmount\"}";
	public static final String TAPE_DRIVE_POWER_METRIC = "hw.power{hw.type=\"tape_drive\"}";
	public static final String TAPE_DRIVE_ENERGY_METRIC = "hw.energy{hw.type=\"tape_drive\"}";
	public static final String DISK_CONTROLLER_POWER_METRIC = "hw.power{hw.type=\"disk_controller\"}";
	public static final String DISK_CONTROLLER_ENERGY_METRIC = "hw.energy{hw.type=\"disk_controller\"}";
	public static final String MEMORY_ENERGY_METRIC = "hw.energy{hw.type=\"memory\"}";
	public static final String MEMORY_POWER_METRIC = "hw.power{hw.type=\"memory\"}";

	public static final String LOCALHOST = "localhost";
	public static final String HW_HOST_AVERAGE_CPU_TEMPERATURE = "__hw.host.average_cpu_temperature";
	public static final String HW_HOST_AMBIENT_TEMPERATURE = "__hw.host.ambient_temperature";
	public static final String TEMPERATURE_METRIC = "hw.temperature";
}
