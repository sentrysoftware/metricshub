package com.sentrysoftware.metricshub.engine.it.job.superconnector;

import java.io.IOException;
import java.nio.file.Path;

import com.sentrysoftware.metricshub.engine.it.job.AbstractITJob;
import com.sentrysoftware.metricshub.engine.it.job.ITJob;
import com.sentrysoftware.metricshub.engine.matsya.MatsyaClientsExecutor;
import com.sentrysoftware.metricshub.engine.telemetry.TelemetryManager;

public class SuperConnectorITJob extends AbstractITJob{


    public SuperConnectorITJob() {
        super(new MatsyaClientsExecutor(), new TelemetryManager());
    }

    @Override
    public ITJob withServerRecordData(String... recordDataPaths) throws Exception {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'withServerRecordData'");
    }

    @Override
    public void stopServer() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'stopServer'");
    }

    @Override
    public boolean isServerStarted() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'isServerStarted'");
    }

    @Override
    public ITJob saveHostMonitoringJson(Path path) throws IOException {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'saveHostMonitoringJson'");
    }
    
}
