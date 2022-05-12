package com.sentrysoftware.matrix.connector.model.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum OSType {

	VMS("OpenVMS"),
	OSF1("Tru64"),
	HP("HP-UX"),
	RS6000("AIX"),
	LINUX("Linux"),
	OOB("Management"),
	NT("Windows"),
	NETWORK("Network"),
	STORAGE("Storage"),
	SOLARIS("Solaris"),
	SUNOS("Solaris");

	@Getter
	private String displayName;

}
