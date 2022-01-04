// Copyright The OpenTelemetry Authors
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//       http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package hwsagentextension

import (
	"bufio"
	"context"
	"io"
	"os"
	"os/exec"
	"syscall"
	"time"

	"go.opentelemetry.io/collector/component"
	"go.uber.org/zap"
)

type processManager struct {
	cancel         context.CancelFunc
	conf           *Config
	logger         *zap.Logger
	shutdownSignal chan struct{}
	delegate       pmDelegate
}

type pmDelegate interface {
	delegatedExecPath() string
}

func (pm *processManager) delegatedExecPath() string {
	return getExecutablePath()
}

func newProcessManager(conf *Config, logger *zap.Logger) *processManager {
	return &processManager{
		conf:           conf,
		logger:         logger,
		shutdownSignal: make(chan struct{}),
		delegate:       &processManager{},
	}
}

type procState string

const (
	starting     procState = "Starting"
	running      procState = "Running"
	shuttingDown procState = "ShuttingDown"
	stopped      procState = "Stopped"
	restarting   procState = "Restarting"
	errored      procState = "Errored"
)

func (pm *processManager) Start(ctx context.Context, _ component.Host) error {
	childCtx, cancel := context.WithCancel(ctx)
	pm.cancel = cancel

	go func() {

		var restartDelay time.Duration
		if pm.conf.RestartDelay == nil {
			restartDelay = DefaultRestartDelay
		} else {
			restartDelay = *pm.conf.RestartDelay
		}

		var retries int
		if pm.conf.Retries == nil {
			retries = DefaultRetries
		} else {
			retries = *pm.conf.Retries
		}

		var args []string = pm.conf.ExtraArgs
		if pm.conf.Grpc != nil {
			args = append(args, "--grpc="+*pm.conf.Grpc)
		}

		run(childCtx, pm.delegate.delegatedExecPath(), args, restartDelay, retries, pm.logger)
		close(pm.shutdownSignal)
	}()
	return nil
}

// Shutdown is invoked during service shutdown.
func (pm *processManager) Shutdown(context.Context) error {
	pm.cancel()
	t := time.NewTimer(5 * time.Second)

	// Wait for either the process to terminate or the timeout
	// period, whichever comes first.
	select {
	case <-pm.shutdownSignal:
	case <-t.C:
	}

	return nil
}

func run(ctx context.Context,
	execPath string, args []string,
	restartDelay time.Duration,
	retries int, logger *zap.Logger) {

	state := starting

	var cmd *exec.Cmd
	var err error
	var stdin io.WriteCloser
	var stdout io.ReadCloser
	// procWait is guaranteed to be sent exactly one message per successful process start
	procWait := make(chan error)

	// A state machine makes the management easier to understand and account
	// for all of the edge cases when managing the hardware sentry agent
	for {
		logger.Debug("hws_agent changed state", zap.String("state", string(state)))

		switch state {
		case errored:
			logger.Error("hws_agent died", zap.Error(err))
			// Should we retry?
			if retries == 0 {
				state = stopped
				continue
			}

			retries--
			state = restarting

		case starting:
			cmd, stdin, stdout = createCommand(execPath, args, logger)

			logger.Debug("Starting hws_agent", zap.String("command", cmd.String()))

			err = cmd.Start()
			if err != nil {
				state = errored
				continue
			}

			go signalWhenProcessDone(cmd, procWait)

			state = running

		case running:
			go collectOutput(stdout, logger)

			select {
			case err = <-procWait:
				if ctx.Err() == nil {
					// We aren't supposed to shutdown yet so this is an error
					// state.
					state = errored
					continue
				}
				state = stopped
			case <-ctx.Done():
				state = shuttingDown
			}

		case shuttingDown:
			_ = cmd.Process.Signal(syscall.SIGTERM)
			<-procWait
			stdout.Close()
			state = stopped

		case restarting:
			_ = stdout.Close()
			_ = stdin.Close()

			// Sleep for the configured RestartDelay so we don't have a hot loop on repeated failures.
			time.Sleep(restartDelay)
			state = starting

		case stopped:
			return
		}
	}
}

func signalWhenProcessDone(cmd *exec.Cmd, procWait chan<- error) {
	err := cmd.Wait()
	procWait <- err
}

func createCommand(execPath string, args []string, logger *zap.Logger) (*exec.Cmd, io.WriteCloser, io.ReadCloser) {
	cmd := execCommand(execPath, args)

	inReader, inWriter, err := os.Pipe()
	if err != nil {
		panic("Input pipe could not be created for hws_agent")
	}

	cmd.Stdin = inReader

	outReader, outWriter, err := os.Pipe()
	// If this errors things are really wrong with the system
	if err != nil {
		panic("Output pipe could not be created for hws_agent")
	}
	cmd.Stdout = outWriter
	cmd.Stderr = outWriter

	cmd.Env = os.Environ()

	applyOSSpecificCmdModifications(cmd)

	return cmd, inWriter, outReader
}

// Collect the output of the hardware sentry agent
func collectOutput(stdout io.Reader, logger *zap.Logger) {
	scanner := bufio.NewScanner(stdout)

	for scanner.Scan() {
		logger.Debug(scanner.Text())
	}
	// Returns when stdout is closed when the process ends
}
