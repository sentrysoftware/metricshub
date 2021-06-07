package com.sentrysoftware.matrix.connector.parser.state.detection.snmp;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.detection.Detection;
import com.sentrysoftware.matrix.connector.model.detection.criteria.snmp.SNMP;
import com.sentrysoftware.matrix.connector.model.detection.criteria.snmp.SNMPGet;
import com.sentrysoftware.matrix.connector.model.detection.criteria.snmp.SNMPGetNext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.springframework.util.Assert.isTrue;

public class OidProcessor extends SnmpProcessor {

	private static final String SNMP_GET_OID_KEY = ".snmpget";

	private static final Pattern OID_KEY_PATTERN = Pattern.compile(
		"^\\s*detection\\.criteria\\(([1-9]\\d*)\\)\\.snmpget(next)?\\s*$",
		Pattern.CASE_INSENSITIVE);

	@Override
	protected Matcher getMatcher(String key) {
		return OID_KEY_PATTERN.matcher(key);
	}

	@Override
	public void parse(final String key, final String value, final Connector connector) {

		super.parse(key, value, connector);

		Matcher matcher = getMatcher(key);
		isTrue(matcher.matches(), () -> "Invalid key: " + key);

		SNMP criterion = key.trim().endsWith(SNMP_GET_OID_KEY)
			? new SNMPGet()
			: new SNMPGetNext();

		criterion.setIndex(getCriterionIndex(matcher));
		criterion.setOid(value.trim());

		Detection detection = connector.getDetection();
		if (detection == null) {

			detection = new Detection();
			connector.setDetection(detection);
		}

		detection.getCriteria().add(criterion);
	}
}
