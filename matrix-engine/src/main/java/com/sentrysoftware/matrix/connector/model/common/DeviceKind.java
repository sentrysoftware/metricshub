package com.sentrysoftware.matrix.connector.model.common;

import java.util.AbstractMap.SimpleEntry;
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
	SOLARIS("Oracle Solaris"),
	OTHER("Other");

	@Getter
	private String displayName;

	public static final Set<DeviceKind> DEVICE_KINDS = Set.of(DeviceKind.values());

	/**
	 * Map each OsType with a regular expression that detects it
	 */
	private static final Map<DeviceKind, Pattern> DETECTORS = Map.ofEntries(
		new SimpleEntry<>(LINUX, Pattern.compile("^linux$")),
		new SimpleEntry<>(WINDOWS, Pattern.compile("^(microsoft\\s*)?windows$|^win$|^nt$")),
		new SimpleEntry<>(OOB, Pattern.compile("^management\\s*card$|^out-of-band$|^out\\s*of\\s*band$|^oob$")),
		new SimpleEntry<>(NETWORK, Pattern.compile("^network$|^switch$")),
		new SimpleEntry<>(STORAGE, Pattern.compile("^storage$|^san$|^library$|^array$")),
		new SimpleEntry<>(VMS, Pattern.compile("^vms$|^(hp\\s*)?open\\s*vms$")),
		new SimpleEntry<>(TRU64, Pattern.compile("^tru64$|^osf1$|^hp\\s*tru64\\s*unix$")),
		new SimpleEntry<>(HPUX, Pattern.compile("^hp-ux$|^hpux$|^hp$")),
		new SimpleEntry<>(AIX, Pattern.compile("^ibm(\\s*|-)aix$|^aix$|^rs6000$")),
		new SimpleEntry<>(SOLARIS, Pattern.compile("^((sun|oracle)\\s*)?solaris$|^sunos$")),
		new SimpleEntry<>(OTHER, Pattern.compile("^other$"))
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
