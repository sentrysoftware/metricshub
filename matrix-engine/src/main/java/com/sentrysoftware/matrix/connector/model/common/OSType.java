package com.sentrysoftware.matrix.connector.model.common;

public enum OSType {

	NT("NT"),
	LINUX("LINUX"),
	OOB("OOB"),
	SOLARIS("SOLARIS"),
	OSF1("OSF1"),
	VMS("VMS"),
	HP("HP"),
	RS6000("RS6000"),
	STORAGE("STORAGE"),
	NETWORK("NETWORK");

	public final String osType;

	OSType(String posType) {
		osType = posType;
	}

	public String getOSType() {
		return osType;
	}
}
