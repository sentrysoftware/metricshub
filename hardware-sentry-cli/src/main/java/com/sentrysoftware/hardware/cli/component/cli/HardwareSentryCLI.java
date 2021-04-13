package com.sentrysoftware.hardware.cli.component.cli;

import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.sentrysoftware.hardware.cli.service.EngineService;

import lombok.Data;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Component
@Command(name = "hardware-sentry-cli", mixinStandardHelpOptions = true)
public class HardwareSentryCLI implements Callable<Boolean> {
	
	@Autowired
	private EngineService engineService;

    @Option(names = "--hostname", required = true, description = "Update me")
    private String hostname;

    @ArgGroup(validate = false)
    private SNMPCredentials snmpCredentials;

    @Override
    public Boolean call() {
        System.out.printf("monitor-hardware was called with -h=%s%n", hostname);
        System.out.println(snmpCredentials);
        System.out.println(engineService.call(hostname));
        return true;
    }

    @Data
    static class SNMPCredentials {
    	@Option(names = "--snmp-version", defaultValue = "V1", description = "Update me") SNMPVersion snmpVersion;
        @Option(names = "--snmp-port", description = "Update me") int port = 161;
        @Option(names = "--snmp-community", description = "Update me") String community;
        @Option(names = "--snmp-timeout", description = "Update me") int timeout;
    }

    enum SNMPVersion {
    	V1, V2C, V3, V3_MD5, V3_SHA, V3_NO_AUTH
    }
}