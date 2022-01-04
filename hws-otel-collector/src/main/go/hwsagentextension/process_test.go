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
	"context"
	"strings"
	"testing"
	"time"

	"github.com/shirou/gopsutil/v3/process"
	"github.com/stretchr/testify/require"
	"go.uber.org/zap"
	"go.uber.org/zap/zaptest/observer"
)

type testPmDelegate struct{}

func (tpmd *testPmDelegate) delegatedExecPath() string {
	return "go"
}

func setup(t *testing.T, conf *Config, args []string,
	workingDirectory string, restartDelay *time.Duration,
	retries *int) (*processManager, **process.Process, func() bool, *observer.ObservedLogs) {

	logCore, logObserver := observer.New(zap.DebugLevel)
	logger := zap.New(logCore)

	conf.ExtraArgs = args
	conf.RestartDelay = restartDelay
	conf.Retries = retries
	pm := newProcessManager(conf, logger)

	pm.delegate = &testPmDelegate{}

	var mockProc *process.Process
	findSubproc := func() bool {

		procs, _ := process.Processes()
		for _, proc := range procs {
			var cmdline, _ = proc.Cmdline()
			// args[1] defines the file path. ex: testdata/test_process.go
			if strings.Contains(cmdline, args[1]) {
				mockProc = proc
				return true
			}
		}
		return false
	}

	return pm, &mockProc, findSubproc, logObserver
}

func TestProcessManager(t *testing.T) {
	ctx, cancel := context.WithCancel(context.Background())
	defer cancel()

	args := []string{"run", "testdata/test_process.go", "100000"}
	pm, mockProc, findSubproc, _ := setup(t, &Config{}, args, "", nil, nil)

	pm.Start(ctx, nil)
	defer pm.Shutdown(ctx)

	require.Eventually(t, findSubproc, 12*time.Second, 100*time.Millisecond)
	require.NotNil(t, *mockProc)

	cmdline, err := (*mockProc).Cmdline()
	require.Nil(t, err)

	require.True(t, strings.Contains(cmdline, "testdata/test_process.go 100000"))

	oldProcPid := (*mockProc).Pid
	err = (*mockProc).Kill()
	require.NoError(t, err)

	// Should be restarted
	require.Eventually(t, findSubproc, DefaultRestartDelay+3*time.Second, 100*time.Millisecond)
	require.NotNil(t, *mockProc)

	require.NotEqual(t, (*mockProc).Pid, oldProcPid)

	err = (*mockProc).Kill()
	require.NoError(t, err)
}

func TestProcessManagerProcessErrored(t *testing.T) {
	ctx, cancel := context.WithCancel(context.Background())
	defer cancel()

	args := []string{"run", "testdata/test_unknown_file.go"}
	retries := 0
	pm, _, _, logObserver := setup(t, &Config{}, args, "", nil, &retries)

	pm.Start(ctx, nil)
	defer pm.Shutdown(ctx)

	time.Sleep(2 * time.Second)
	require.Len(t, logObserver.FilterMessage("hws_agent died").All(), 1)
}

func TestProcessManagerNoRetries(t *testing.T) {
	ctx, cancel := context.WithCancel(context.Background())
	defer cancel()

	args := []string{"run", "testdata/test_process.go"}
	retries := 0
	pm, mockProc, findSubproc, _ := setup(t, &Config{}, args, "", nil, &retries)

	pm.Start(ctx, nil)
	defer pm.Shutdown(ctx)

	require.Eventually(t, findSubproc, 12*time.Second, 100*time.Millisecond)
	require.NotNil(t, *mockProc)

	// Shouldn't be restarted
	notFound := func() bool {
		return !findSubproc()
	}
	require.Eventually(t, notFound, DefaultRestartDelay+3*time.Second, 100*time.Millisecond)

}
