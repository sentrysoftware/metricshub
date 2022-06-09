package main

import (
	"context"
	"fmt"

	"go.uber.org/zap"
	"go.uber.org/zap/zapcore"

	"go.opentelemetry.io/collector/confmap"
	"go.opentelemetry.io/collector/confmap/converter/expandconverter"
	"go.opentelemetry.io/collector/confmap/converter/overwritepropertiesconverter"
	"go.opentelemetry.io/collector/confmap/provider/envprovider"
	"go.opentelemetry.io/collector/confmap/provider/fileprovider"
	"go.opentelemetry.io/collector/confmap/provider/yamlprovider"
	"go.opentelemetry.io/collector/service"

	"gopkg.in/natefinch/lumberjack.v2"
)

// Create a new collector with zap log core as logging option
func newCollectorWithLogCore(set service.CollectorSettings) (*service.Collector, error) {
	if set.ConfigProvider == nil {
		var err error
		cfgSet := service.ConfigProviderSettings{
			Locations:     getConfigFlag(),
			MapProviders:  makeMapProvidersMap(fileprovider.New(), envprovider.New(), yamlprovider.New()),
			MapConverters: []confmap.Converter{expandconverter.New()},
		}
		// Append the "overwrite properties converter" as the first converter.
		cfgSet.MapConverters = append(
			[]confmap.Converter{overwritepropertiesconverter.New(getSetFlag())},
			cfgSet.MapConverters...)
		set.ConfigProvider, err = service.NewConfigProvider(cfgSet)
		if err != nil {
			return nil, err
		}
	}

	cfg, err := set.ConfigProvider.Get(context.Background(), set.Factories)
	if err != nil {
		return nil, fmt.Errorf("failed to get config: %w", err)
	}

	set.LoggingOptions = append(
		set.LoggingOptions,
		zap.WrapCore(withLogCore(cfg.Service.Telemetry.Logs.Level)),
	)
	return service.New(set)
}

// Create the zap core callback using lumberjack for log rotation
func withLogCore(logLevel zapcore.Level) func(zapcore.Core) zapcore.Core {
	return func(core zapcore.Core) zapcore.Core {
		pe := zap.NewProductionEncoderConfig()
		pe.EncodeTime = zapcore.ISO8601TimeEncoder

		logsDir, err := getLogsDir()
		if err != nil {
			panic(err)
		}

		w := zapcore.AddSync(&lumberjack.Logger{
			Filename:   logsDir + "/otel.log", // The file to write logs to
			MaxSize:    100,                   // The maximum size in megabytes of the log file before it gets rotated
			MaxBackups: 3,                     // The maximum number of old log files to retain
			MaxAge:     30,                    // The maximum number of days to retain old log files based on the timestamp encoded in their filename
			LocalTime:  true,                  // Use the computer's local time instead of UTC time
		})
		return zapcore.NewCore(zapcore.NewConsoleEncoder(pe), w, logLevel)
	}
}

// Make a new map of the given providers indexed by the provider's Scheme
func makeMapProvidersMap(providers ...confmap.Provider) map[string]confmap.Provider {
	ret := make(map[string]confmap.Provider, len(providers))
	for _, provider := range providers {
		ret[provider.Scheme()] = provider
	}
	return ret
}
