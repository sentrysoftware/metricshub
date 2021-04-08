package com.sentrysoftware.matrix.connector.parser.state.detection.snmp;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.detection.Detection;
import com.sentrysoftware.matrix.connector.model.detection.criteria.Criterion;
import com.sentrysoftware.matrix.connector.model.detection.criteria.snmp.SNMP;
import com.sentrysoftware.matrix.connector.model.detection.criteria.snmp.SNMPGetNext;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.sentrysoftware.matrix.utils.Assert.isTrue;
import static com.sentrysoftware.matrix.utils.Assert.notNull;

public abstract class SnmpProcessor implements IConnectorStateParser {

    protected int criterionIndex;

    protected static final String INDEX_REGEX = "\\((\\d+)\\)";
    protected static final Pattern CRITERION_INDEX_PATTERN = Pattern.compile(INDEX_REGEX);

    protected abstract String getKeyRegex();

    @Override
    public boolean detect(final String key, final String value, final Connector connector) {

        return value != null
                && key != null
                && key.matches(getKeyRegex());
    }

    @Override
    public void parse(final String key, final String value, final Connector connector) {

        notNull(key, "key cannot be null.");
        isTrue(
                key.matches(getKeyRegex()),
                "The key (" + key + ") does not match the following regex: " + getKeyRegex()
        );
        notNull(value, "value cannot be null.");
        notNull(connector, "Connector cannot be null.");
        notNull(getKeyRegex(), "getKeyRegex() should never return null.");

        setCriterionIndex(key);
        isTrue(criterionIndex > 0, key + " is an invalid key.");

        List<Criterion> criteria = getDetection(connector).getCriteria();

        // Checking if a new criterion declaration has been encountered
        if (criteria.size() < criterionIndex) {

            // Let us create a SNMPGetNext criterion by default.
            // OidProcessor will change it to SNMPGet if necessary.
            criteria.add(new SNMPGetNext());
        }
    }

    private Detection getDetection(Connector connector) {

        notNull(connector, "Connector cannot be null.");

        Detection detection = connector.getDetection();
        if (detection == null) {

            detection = new Detection();
            connector.setDetection(detection);
        }

        return detection;
    }

    @SuppressWarnings("all")
    private void setCriterionIndex(String key) {

        notNull(key, "key cannot be null.");
        notNull(getKeyRegex(), "getKeyRegex() should never return null.");
        isTrue(
                key.matches(getKeyRegex()),
                "The key (" + key + ") does not match the following regex: " + getKeyRegex()
        );
        isTrue(
                getKeyRegex().contains(INDEX_REGEX),
                "getKeyRegex() [" + getKeyRegex() + "] does not contain " + INDEX_REGEX + "."
        );

        Matcher matcher = CRITERION_INDEX_PATTERN.matcher(key);

        // Will always return true,
        // because key.matches(getKeyRegex()) && getKeyRegex().contains(INDEX_REGEX)
        matcher.find();

        // group(1) exists, because of "(\\d+)" in "\\((\\d+)\\)".
        // group(1) will extract <integer> in "detection.criteria(<integer>)"
        criterionIndex = Integer.parseInt(matcher.group(1));
    }

    protected void isSnmp(Criterion criterion) {

        isTrue(
                criterion instanceof SNMP,
                "Detection.Criteria("
                        + criterionIndex
                        + ") was expected to be an SMP, but is a(n) "
                        + criterion.getClass()
                        + "."
        );
    }
}
