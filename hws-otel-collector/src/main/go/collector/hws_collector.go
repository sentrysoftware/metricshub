package main

import (
	"context"
	"fmt"

	"go.uber.org/zap"
	"go.uber.org/zap/zapcore"

	"go.opentelemetry.io/collector/service"

	"gopkg.in/natefinch/lumberjack.v2"
)

// Create a new collector with zap log core as logging option
func newCollectorWithLogCore(set service.CollectorSettings) (*service.Collector, error) {
	if set.ConfigProvider == nil {
		set.ConfigProvider = service.MustNewDefaultConfigProvider(getConfigFlag(), getSetFlag())
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
		pe.LineEnding = "\r\n"
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
