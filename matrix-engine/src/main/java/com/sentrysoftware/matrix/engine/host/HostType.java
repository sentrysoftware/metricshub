package com.sentrysoftware.matrix.engine.host;

import java.util.Map;
import java.util.regex.Pattern;

import com.sentrysoftware.matrix.connector.model.common.OsType;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum HostType {

	HP_OPEN_VMS(OsType.VMS, "OpenVMS"),
	HP_TRU64_UNIX(OsType.OSF1, "Tru64"),
	HP_UX(OsType.HP, "HP-UX"),
	IBM_AIX(OsType.RS6000, "AIX"),
	LINUX(OsType.LINUX, "Linux"),
	MGMT_CARD_BLADE_ESXI(OsType.OOB, "Management"),
	MS_WINDOWS(OsType.NT, "Windows"),
	NETWORK_SWITCH(OsType.NETWORK, "Network"),
	STORAGE(OsType.STORAGE, "Storage"),
	SUN_SOLARIS(OsType.SOLARIS, "Solaris");

	/**
	 * Map each HostType with a regular expression that detects it
	 */
	private static final Map<HostType, Pattern> DETECTORS = Map.of(
			LINUX, Pattern.compile("^lin|^lnx$"),
			MS_WINDOWS, Pattern.compile("^win|^ms.*win|^microsoft.*w"),
			MGMT_CARD_BLADE_ESXI, Pattern.compile("^oob$|^out|^vmware|^mgmt|^management|^esx|^blade"),
			NETWORK_SWITCH, Pattern.compile("^net|^switch"),
			STORAGE, Pattern.compile("^sto|^san"),
			HP_OPEN_VMS, Pattern.compile("vms"),
			HP_TRU64_UNIX, Pattern.compile("tru64|osf"),
			HP_UX, Pattern.compile("hp.*ux"),
			IBM_AIX, Pattern.compile("aix|rs6000"),
			SUN_SOLARIS, Pattern.compile("^sun|^ora|sol")
	);
	@Getter
	private OsType osType;

	@Getter
	private String displayName;

	/**
	 * Interpret the specified string as a HostType (in a flexible way).
	 * <p>
	 * @param value String to be interpreted
	 * @return a HostType value (or null if null)
	 * @throws IllegalArgumentException when specified value is not supported
	 */
	public static HostType interpretValueOf(String value) {

		// Null returns null
		if (value == null) {
			return null;
		}

		// Check all regex in DETECTORS to see which one matches
		String lCaseValue = value.trim().toLowerCase();
		for (Map.Entry<HostType, Pattern> detector : DETECTORS.entrySet()) {
			if (detector.getValue().matcher(lCaseValue).find()) {
				return detector.getKey();
			}
		}

		// No match => Exception
		throw new IllegalArgumentException("'" + value + "' is not a supported host type");
	}

}
