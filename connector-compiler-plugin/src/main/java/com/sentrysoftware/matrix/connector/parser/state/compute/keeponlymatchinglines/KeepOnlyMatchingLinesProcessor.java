package com.sentrysoftware.matrix.connector.parser.state.compute.keeponlymatchinglines;

import com.sentrysoftware.matrix.connector.model.Connector;
import com.sentrysoftware.matrix.connector.model.detection.criteria.Criterion;
import com.sentrysoftware.matrix.connector.model.monitor.HardwareMonitor;
import com.sentrysoftware.matrix.connector.model.monitor.job.MonitorJob;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.Source;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.Compute;
import com.sentrysoftware.matrix.connector.model.monitor.job.source.compute.KeepOnlyMatchingLines;
import com.sentrysoftware.matrix.connector.parser.ConnectorParserConstants;
import com.sentrysoftware.matrix.connector.parser.state.IConnectorStateParser;

import java.util.List;
import java.util.regex.Matcher;

import static org.springframework.util.Assert.notNull;

public abstract class KeepOnlyMatchingLinesProcessor implements IConnectorStateParser {

    protected static final String KEEP_ONLY_MATCHING_LINES_TYPE_VALUE = "KeepOnlyMatchingLines";

    protected abstract Matcher getMatcher(String key);

    @Override
    public boolean detect(final String key, final String value, final Connector connector) {

        return value != null
                && key != null
                && getMatcher(key).matches()
                && isKeepOnlyMatchingLinesContext(key, value, connector);
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
    private boolean isKeepOnlyMatchingLinesContext(String key, String value, Connector connector) {

        if (this instanceof TypeProcessor) {

            return KEEP_ONLY_MATCHING_LINES_TYPE_VALUE.equalsIgnoreCase(
                    value.replaceAll(ConnectorParserConstants.DOUBLE_QUOTES_REGEX_REPLACEMENT, "$1")
            );
        }

        return getKeepOnlyMatchingLines(key, connector) != null;
    }

    @Override
    public void parse(final String key, final String value, final Connector connector) {

        notNull(key, "key cannot be null.");
        notNull(value, "value cannot be null.");
        notNull(connector, "Connector cannot be null.");
    }

    private KeepOnlyMatchingLines getKeepOnlyMatchingLines(final String key, final Connector connector) {

        Matcher matcher = getMatcher(key);

        if (!matcher.matches()) {
            return null;
        }

        String monitorName = getMonitorName(matcher);
        HardwareMonitor hardwareMonitor = getHardwareMonitor(connector, monitorName);

        return getKeepOnlyMatchingLines(
                hardwareMonitor,
                getMonitorJobName(matcher),
                getSourceIndex(matcher),
                getComputeIndex(matcher)
        );
    }

    private KeepOnlyMatchingLines getKeepOnlyMatchingLines(
            HardwareMonitor hardwareMonitor,
            String monitorJobName,
            int sourceIndex,
            int computeIndex
    ) {

        Source source = getSource(hardwareMonitor, monitorJobName, sourceIndex);
        if (source == null) {
            return null;
        }

        return getKeepOnlyMatchingLines(source.getComputes(), computeIndex);
    }

    private KeepOnlyMatchingLines getKeepOnlyMatchingLines(List<Compute> computes, int computeIndex) {

        if (computes == null) {
            return null;
        }

        return (KeepOnlyMatchingLines) computes
                .stream()
                .filter(
                        compute -> compute instanceof KeepOnlyMatchingLines
                                && compute.getIndex() == computeIndex
                )
                .findFirst()
                .orElse(null);
    }

    protected KeepOnlyMatchingLines getKeepOnlyMatchingLines(Matcher matcher, Connector connector) {

        Source source = getSource(matcher, connector);
        if (source == null) {
            return null;
        }

        return getKeepOnlyMatchingLines(source.getComputes(), getComputeIndex(matcher));
    }

    private Source getSource(HardwareMonitor hardwareMonitor, String monitorJobName, int sourceIndex) {

        if (hardwareMonitor == null) {
            return null;
        }

        MonitorJob monitorJob = ConnectorParserConstants.DISCOVERY.equalsIgnoreCase(monitorJobName)
                ? hardwareMonitor.getDiscovery()
                : hardwareMonitor.getCollect();

        if (monitorJob == null) {
            return null;
        }

        List<Source> sources = monitorJob.getSources();
        if (sources == null) {
            return null;
        }

        return sources
                .stream()
                .filter(source -> source.getIndex() == sourceIndex)
                .findFirst()
                .orElse(null);
    }

    protected Source getSource(Matcher matcher, Connector connector) {

        String monitorName = getMonitorName(matcher);
        HardwareMonitor hardwareMonitor = getHardwareMonitor(connector, monitorName);

        return getSource(
                hardwareMonitor,
                getMonitorJobName(matcher),
                getSourceIndex(matcher)
        );
    }

    private HardwareMonitor getHardwareMonitor(Connector connector, String monitorName) {

        return connector
                .getHardwareMonitors()
                .stream()
                .filter(
                        hardwareMonitor -> hardwareMonitor
                                .getType()
                                .getName()
                                .equalsIgnoreCase(monitorName)
                )
                .findFirst()
                .orElse(null);
    }

    private String getMonitorName(Matcher matcher) {

        return matcher.group(1);
    }

    private String getMonitorJobName(Matcher matcher) {

        return matcher.group(2);
    }

    private Integer getSourceIndex(Matcher matcher) {

        return Integer.parseInt(matcher.group(3));
    }

    protected Integer getComputeIndex(Matcher matcher) {

        return Integer.parseInt(matcher.group(4));
    }
}
