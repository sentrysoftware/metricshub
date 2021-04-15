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

    protected int criterionIndexFromHdfsFile;
    protected int criterionIndexInDetection;
    protected Criterion knownCriterion;

    protected static final String INDEX_REGEX = "\\((\\d+)\\)";
    protected static final Pattern CRITERION_INDEX_PATTERN = Pattern.compile(INDEX_REGEX);

    protected abstract String getKeyRegex();

    @Override
    public boolean detect(final String key, final String value, final Connector connector) {

        return value != null
                && key != null
                && key.matches(getKeyRegex())
                && isSnmpContext(key, connector);
    }

    /**
     * @param key       The current line's key.
     * @param connector The {@link Connector} whose detection criteria we wish to check.
     *
     * @return          Whether the given {@link Connector}
     *                  has a detection SNMP {@link Criterion} with the same index as the key, or not.
     *                  Always returns <i>true</i> for OID keys
     *                  (e.g. <i>Detection.Criteria(1).SnmpGetNext</i> or <i>Detection.Criteria(1).SnmpGet</i>)
     */
    private boolean isSnmpContext(String key, Connector connector) {

        if (this instanceof OidProcessor) {
            return true;
        }

        setKnownCriterion(key, connector);

        return knownCriterion instanceof SNMP;
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

        // For OID keys, let us try to add directly a new SNMP criterion
        if (this instanceof OidProcessor) {

            setCriterionIndexFromHdfsFile(key);
            isTrue(criterionIndexFromHdfsFile != 0, key + "'s index is invalid.");

            // By default, we add an SNMPGetNext type criterion.
            // If necessary, the OidProcessor instance will change it to an SNMPGet type.
            knownCriterion = new SNMPGetNext();
            knownCriterion.setIndex(criterionIndexFromHdfsFile);

            List<Criterion> criteria = getDetection(connector).getCriteria();

            criteria.add(knownCriterion);
            criterionIndexInDetection = criteria.size() - 1;
        }
    }

    /**
     * @param connector The {@link Connector} whose {@link Detection} we wish to get.
     *
     * @return          The given {@link Connector}'s {@link Detection}.
     *                  If the {@link Connector} does not have any {@link Detection}, creates one.
     */
    private Detection getDetection(Connector connector) {

        if (connector.getDetection() == null) {
            connector.setDetection(new Detection());
        }

        return connector.getDetection();
    }

    /**
     * Tries to find in the given {@link Connector}
     * the {@link SNMP} type {@link Criterion} having the same index as the given key.
     *
     * @param key       The current line's key.
     * @param connector The {@link Connector} whose matching {@link Criterion} we wish to set.
     */
    private void setKnownCriterion(String key, Connector connector) {

        notNull(connector, "Connector cannot be null.");

        setCriterionIndexFromHdfsFile(key);
        isTrue(criterionIndexFromHdfsFile != 0, key + "'s index is invalid.");

        knownCriterion = null;
        criterionIndexInDetection = -1;

        List<Criterion> criteria = getDetection(connector).getCriteria();
        for (int i = 0; i < criteria.size(); i++) {

            if (criteria.get(i).getIndex() == criterionIndexFromHdfsFile) {

                knownCriterion = criteria.get(i);
                isSnmp();
                criterionIndexInDetection = i;

                break;
            }
        }
    }

    /**
     * Extracts the given key's index.
     *
     * @param key   The key whose index we wish to extract.
     */
    @SuppressWarnings("all")
    private void setCriterionIndexFromHdfsFile(String key) {

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
        criterionIndexFromHdfsFile = Integer.parseInt(matcher.group(1));
    }

    /**
     * Checks the current known {@link Criterion} is an {@link SNMP} type {@link Criterion}.
     */
    protected void isSnmp() {

        isTrue(
                knownCriterion instanceof SNMP,
                "Detection.Criteria("
                        + criterionIndexFromHdfsFile
                        + ") was expected to be an SNMP, but is a(n) "
                        + knownCriterion.getClass()
                        + "."
        );
    }
}
