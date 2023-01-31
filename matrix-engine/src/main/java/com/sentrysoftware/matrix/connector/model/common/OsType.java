package com.sentrysoftware.matrix.connector.model.common;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum OsType {

	VMS("OpenVMS"),
	TRU64("Tru64"),
	HPUX("HP-UX"),
	AIX("AIX"),
	LINUX("Linux"),
	OOB("Management"),
	WINDOWS("Windows"),
	NETWORK("Network"),
	STORAGE("Storage"),
	SOLARIS("Solaris");

	@Getter
	private String displayName;

	public static final Set<OsType> OS_TYPES = Set.of(OsType.values());

	/**
	 * Map each HostType with a regular expression that detects it
	 */
	private static final Map<OsType, Pattern> DETECTORS = Map.of(
			LINUX, Pattern.compile("^linux$"),
			WINDOWS, Pattern.compile("^(microsoft\s*)?windows$|^win$|^nt$"),
			OOB, Pattern.compile("^management\s*card$|^out-of-band$|^out\s*of\s*band$|^oob$"),
			NETWORK, Pattern.compile("^network$|^switch$"),
			STORAGE, Pattern.compile("^storage$|^san$|^library$|^array$"),
			VMS, Pattern.compile("^vms$|^(hp\s*)?open\s*vms$"),
			TRU64, Pattern.compile("^tru64$|^osf1$|^hp\s*tru64\s*unix$"),
			HPUX, Pattern.compile("^hp-ux$|^hpux$|^hp$"),
			AIX, Pattern.compile("^ibm(\s*|-)aix$|^aix$|^rs6000$"),
			SOLARIS, Pattern.compile("^solaris$|^sunos$")
	);

	/**
	 * Detect {@link OsType} using the value defined in the connector code
	 * 
	 * @param value
	 * @return {@link OsType} instance
	 */
	public static OsType detect(final String value) {
		// Null returns null
		if (value == null) {
			return null;
		}

		// Check all regex in DETECTORS to see which one matches
		final String lCaseValue = value.trim().toLowerCase();
		for (Map.Entry<OsType, Pattern> detector : DETECTORS.entrySet()) {
			if (detector.getValue().matcher(lCaseValue).find()) {
				return detector.getKey();
			}
		}

		// No match => Exception
		throw new IllegalArgumentException("'" + value + "' is not a supported OsType. Accepted values are: [ linux, windows, oob, network, storage, vms, tru64, hpux, aix, solaris ].");
	}
}
