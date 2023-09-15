package com.sentrysoftware.matrix.connector.model.common;

import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
public enum DeviceKind {
	VMS("HP Open VMS"),
	TRU64("HP Tru64"),
	HPUX("HP-UX"),
	AIX("IBM AIX"),
	LINUX("Linux"),
	OOB("Management"),
	WINDOWS("Microsoft Windows"),
	NETWORK("Network"),
	STORAGE("Storage"),
	SOLARIS("Oracle Solaris");

	@Getter
	private String displayName;

	public static final Set<DeviceKind> DEVICE_KINDS = Set.of(DeviceKind.values());

	/**
	 * Map each OsType with a regular expression that detects it
	 */
	private static final Map<DeviceKind, Pattern> DETECTORS = Map.of(
		LINUX,
		Pattern.compile("^linux$"),
		WINDOWS,
		Pattern.compile("^(microsoft\\s*)?windows$|^win$|^nt$"),
		OOB,
		Pattern.compile("^management\\s*card$|^out-of-band$|^out\\s*of\\s*band$|^oob$"),
		NETWORK,
		Pattern.compile("^network$|^switch$"),
		STORAGE,
		Pattern.compile("^storage$|^san$|^library$|^array$"),
		VMS,
		Pattern.compile("^vms$|^(hp\\s*)?open\\s*vms$"),
		TRU64,
		Pattern.compile("^tru64$|^osf1$|^hp\\s*tru64\\s*unix$"),
		HPUX,
		Pattern.compile("^hp-ux$|^hpux$|^hp$"),
		AIX,
		Pattern.compile("^ibm(\\s*|-)aix$|^aix$|^rs6000$"),
		SOLARIS,
		Pattern.compile("^((sun|oracle)\\s*)?solaris$|^sunos$")
	);

	/**
	 * Detect {@link DeviceKind} using the value defined in the connector code
	 *
	 * @param value
	 * @return {@link DeviceKind} instance
	 */
	public static DeviceKind detect(final String value) {
		// Null returns null
		if (value == null) {
			return null;
		}

		// Check all regex in DETECTORS to see which one matches
		final String lCaseValue = value.trim().toLowerCase();
		for (Map.Entry<DeviceKind, Pattern> detector : DETECTORS.entrySet()) {
			if (detector.getValue().matcher(lCaseValue).find()) {
				return detector.getKey();
			}
		}

		// No match => Exception
		throw new IllegalArgumentException("'" + value + "' is not a supported device kind.");
	}
}
