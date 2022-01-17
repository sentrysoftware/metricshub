//go:build windows
// +build windows

package main

import (
	"context"
	"fmt"
	"os"
	"time"

	"go.uber.org/zap"
	"go.uber.org/zap/zapcore"
	"golang.org/x/sys/windows/svc"
	"golang.org/x/sys/windows/svc/eventlog"

	"go.opentelemetry.io/collector/service"
)

type WindowsService struct {
	settings service.CollectorSettings
	col      *service.Collector
}

func NewWindowsService(set service.CollectorSettings) *WindowsService {
	return &WindowsService{settings: set}
}

// Execute implements https://godoc.org/golang.org/x/sys/windows/svc#Handler
func (s *WindowsService) Execute(args []string, requests <-chan svc.ChangeRequest, changes chan<- svc.Status) (ssec bool, errno uint32) {
	// The first argument supplied to service.Execute is the service name. If this is
	// not provided for some reason, raise a relevant error to the system event log
	if len(args) == 0 {
		return false, 1213 // 1213: ERROR_INVALID_SERVICENAME
	}

	elog, err := openEventLog(args[0])
	if err != nil {
		return false, 1501 // 1501: ERROR_EVENTLOG_CANT_START
	}

	colErrorChannel := make(chan error, 1)

	changes <- svc.Status{State: svc.StartPending}
	if err = s.start(colErrorChannel); err != nil {
		elog.Error(3, fmt.Sprintf("failed to start service: %v", err))
		return false, 1064 // 1064: ERROR_EXCEPTION_IN_SERVICE
	}
	changes <- svc.Status{State: svc.Running, Accepts: svc.AcceptStop | svc.AcceptShutdown}

	for req := range requests {
		switch req.Cmd {
		case svc.Interrogate:
			changes <- req.CurrentStatus

		case svc.Stop, svc.Shutdown:
			changes <- svc.Status{State: svc.StopPending}
			if err = s.stop(colErrorChannel); err != nil {
				elog.Error(3, fmt.Sprintf("errors occurred while shutting down the service: %v", err))
			}
			changes <- svc.Status{State: svc.Stopped}
			return false, 0

		default:
			elog.Error(3, fmt.Sprintf("unexpected service control request #%d", req.Cmd))
			return false, 1052 // 1052: ERROR_INVALID_SERVICE_CONTROL
		}
	}

	return false, 0
}

func (s *WindowsService) start(colErrorChannel chan error) error {

	// Call flags which prepares a new flagSet including the default configuration
	flagSet, err := flags()
	if err != nil {
		return err
	}

	// Parse all the flags manually.
	if err := flagSet.Parse(os.Args[1:]); err != nil {
		return err
	}

	s.col, err = newColWithLogCore(s.settings)
	if err != nil {
		return err
	}

	// col.Run blocks until receiving a SIGTERM signal, so needs to be started
	// asynchronously, but it will exit early if an error occurs on startup
	go func() {
		colErrorChannel <- s.col.Run(context.Background())
	}()

	// wait until the collector server is in the Running state
	go func() {
		for {
			state := s.col.GetState()
			if state == service.Running {
				colErrorChannel <- nil
				break
			}
			time.Sleep(time.Millisecond * 200)
		}
	}()

	// wait until the collector server is in the Running state, or an error was returned
	return <-colErrorChannel
}

func (s *WindowsService) stop(colErrorChannel chan error) error {

	s.col.Shutdown()

	// return the response of col.Start
	return <-colErrorChannel
}

func openEventLog(serviceName string) (*eventlog.Log, error) {
	elog, err := eventlog.Open(serviceName)
	if err != nil {
		return nil, fmt.Errorf("service failed to open event log: %w", err)
	}

	return elog, nil
}

func newColWithLogCore(set service.CollectorSettings) (*service.Collector, error) {
	if set.ConfigProvider == nil {
		set.ConfigProvider = service.NewDefaultConfigProvider(getConfigFlag(), getSetFlag())
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

func withLogCore(logLevel zapcore.Level) func(zapcore.Core) zapcore.Core {
	return func(core zapcore.Core) zapcore.Core {
		pe := zap.NewProductionEncoderConfig()
		pe.LineEnding = "\r\n"
		pe.EncodeTime = zapcore.ISO8601TimeEncoder
		return zapcore.NewCore(zapcore.NewConsoleEncoder(pe), zapcore.AddSync(os.Stdout), logLevel)
	}
}
